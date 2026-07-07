package com.cupit.csv.importer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.model.ImportBatch;
import com.cupit.model.PaygateStoreMapping;
import com.cupit.model.VisaMasterStoreHeader;
import com.cupit.model.VisaMasterTransaction;
import com.cupit.repository.PaygateMappingRepository;
import com.cupit.repository.VisaMasterStoreHeaderRepository;
import com.cupit.repository.VisaMasterTransactionRepository;

/**
 * 住信SBI（VISA・MasterCard）DATファイルを解析してDBに登録する。
 * 文字コード: UTF-8、ヘッダー行: なし。
 * 区分1（12列）→ m_visa_master_store_header
 * 区分2（22列）→ m_visa_master_transaction
 *
 * 全行を1パスで処理しエラーを収集する。ヘッダー（区分1）を保存して得たIDを
 * 後続の明細（区分2）に紐付けるため、1パス方式を採用。1ファイルに複数の加盟店ID
 * （区分1ブロック）が混在するため、取引コードは区分1ごとに加盟店IDで
 * m_paygate_store_mapping を引き直して解決し、後続の区分2にはその値を引き継ぐ。
 * マッピングが見つからない区分1・データ変換エラーが発生した区分1/区分2は、その行
 * だけを登録せずスキップし、ファイルの最後まで処理を継続する（データエラーに
 * よってファイル全体をロールバックすることはしない）。取引コードが未解決のまま
 * 区分2に到達した場合（先頭が区分1以外で始まる等）もその行をエラーとしてスキップする。
 * また、区分2自身が持つ加盟店ID（[2]列）が直前の区分1の加盟店IDと一致しない場合
 * （ファイルの並び順が壊れている等）も、誤った取引コードに紐付けないためエラーとして
 * スキップする（ファイルの並び順だけを信用せず、区分2側の加盟店IDでも突き合わせる）。
 *
 * 区分1列順序:
 *   [0]="1", [1]=作成日(YYYYMMDD), [2]=売上計上日(YYYYMMDD),
 *   [3]=親加盟店ID, [4]=親加盟店名, [5]=加盟店ID, [6]=加盟店名,
 *   [7]=支払日(YYYYMMDD), [8]=売上件数, [9]=売上金額, [10]=手数料額, [11]=振込金額
 *
 * 区分2列順序:
 *   [0]="2", [1]=親加盟店ID, [2]=加盟店ID, [3]=取引番号,
 *   [4]=売上日(YYYYMMDD), [5]=カード番号, [6]=ブランド区分, [7]=支払種別コード,
 *   [8]=支払方法, [9]=手数料率, [10-19]=各金額, [20-21]=未使用
 */
@Component
public class JushinSbiFileImporter extends AbstractFileImporter {

    private final VisaMasterStoreHeaderRepository headerRepository;
    private final VisaMasterTransactionRepository transactionRepository;
    private final PaygateMappingRepository paygateMappingRepository;

    public JushinSbiFileImporter(
            VisaMasterStoreHeaderRepository headerRepository,
            VisaMasterTransactionRepository transactionRepository,
            PaygateMappingRepository paygateMappingRepository) {
        this.headerRepository = headerRepository;
        this.transactionRepository = transactionRepository;
        this.paygateMappingRepository = paygateMappingRepository;
    }

    @Override
    public ImportResult importFile(MultipartFile file, ImportBatch batch) throws IOException {
        List<CsvValidationError> errors = new ArrayList<>();
        LocalDate today = LocalDate.now();
        int rowNum = 0;
        int count = 0;
        Integer currentHeaderId = null;
        String currentTradeCode = null;
        String currentMerchantId = null;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                rowNum++;
                line = stripCr(line);
                if (line.isBlank()) {
                    continue;
                }
                List<String> fields = parseLine(line);
                if (fields.isEmpty()) {
                    continue;
                }
                String recordType = trim(fields.get(0));
                if ("1".equals(recordType)) {
                    if (fields.size() < 12) {
                        String merchantPart = fields.size() > 5
                                ? "加盟店ID「" + fields.get(5).trim() + "」: " : "";
                        errors.add(new CsvValidationError(rowNum, "",
                                merchantPart + "区分1レコードの列数が不正です。期待: 12列、実際: "
                                + fields.size() + "列"));
                        continue;
                    }
                    String merchantId = trim(fields.get(5));
                    Optional<PaygateStoreMapping> mapping =
                            paygateMappingRepository.findFirstBySbiMerchantId(merchantId);
                    if (mapping.isEmpty()) {
                        errors.add(new CsvValidationError(rowNum, "加盟店ID",
                                "加盟店ID「" + merchantId
                                        + "」に対応する取引コードがm_paygate_store_mappingに存在しません。"));
                        currentHeaderId = null;
                        currentTradeCode = null;
                        currentMerchantId = null;
                        continue;
                    }
                    currentTradeCode = mapping.get().getTradeCode();
                    currentMerchantId = merchantId;
                    int errorCountBeforeRow = errors.size();
                    VisaMasterStoreHeader header =
                            buildHeader(fields, batch, currentTradeCode, today, rowNum, errors);
                    if (errors.size() > errorCountBeforeRow) {
                        currentHeaderId = null; // この区分1行にデータ変換エラーがあるため登録しない
                        continue;
                    }
                    currentHeaderId = headerRepository.save(header).getStoreHeaderId();
                    count++;
                } else if ("2".equals(recordType)) {
                    if (fields.size() < 20) {
                        String tradeCodePart = currentTradeCode != null
                                ? "取引コード「" + currentTradeCode + "」: " : "";
                        errors.add(new CsvValidationError(rowNum, "",
                                tradeCodePart + "区分2レコードの列数が不正です。期待: 20列、実際: "
                                + fields.size() + "列"));
                        continue;
                    }
                    if (currentTradeCode == null) {
                        errors.add(new CsvValidationError(rowNum, "",
                                "対応する区分1（店舗ヘッダー）の取引コードが未解決のため登録できません。"));
                        continue;
                    }
                    String ownMerchantId = trim(fields.get(2));
                    if (!ownMerchantId.equals(currentMerchantId)) {
                        errors.add(new CsvValidationError(rowNum, "加盟店ID",
                                "加盟店ID「" + ownMerchantId + "」: 対応する区分1（店舗ヘッダー、直前の加盟店ID「"
                                + currentMerchantId + "」）と一致しないため登録できません。"));
                        continue;
                    }
                    int errorCountBeforeRow = errors.size();
                    VisaMasterTransaction trn = buildTransaction(
                            fields, batch, currentHeaderId, currentTradeCode, today, rowNum, errors);
                    if (errors.size() > errorCountBeforeRow) {
                        continue; // この区分2行にデータ変換エラーがあるため登録しない
                    }
                    transactionRepository.save(trn);
                    count++;
                }
            }
        }

        return new ImportResult(count, count + errors.size(), errors);
    }

    private VisaMasterStoreHeader buildHeader(
            List<String> fields, ImportBatch batch, String tradeCode, LocalDate today,
            int rowNum, List<CsvValidationError> errors) {
        VisaMasterStoreHeader header = new VisaMasterStoreHeader();
        header.setTradeCode(tradeCode);
        header.setBatchId(batch.getBatchId());
        header.setFileCreatedDate(
                parseDate8Checked(fields.get(1), rowNum, "ファイル作成日", errors));
        header.setSalesSummaryDate(
                parseDate8Checked(fields.get(2), rowNum, "売上計上日", errors));
        header.setParentMerchantId(trim(fields.get(3)));
        header.setParentMerchantName(trim(fields.get(4)));
        header.setMerchantId(trim(fields.get(5)));
        header.setMerchantName(trim(fields.get(6)));
        header.setTransferDate(
                parseDate8Checked(fields.get(7), rowNum, "支払日", errors));
        header.setTotalSalesCount(
                parseIntChecked(fields.get(8), rowNum, "売上件数", errors));
        header.setTotalSalesAmount(
                parseIntChecked(fields.get(9), rowNum, "売上金額", errors));
        header.setTotalFeeAmount1(
                parseIntChecked(fields.get(10), rowNum, "手数料額", errors));
        header.setTotalPaymentAmount1(
                parseIntChecked(fields.get(11), rowNum, "振込金額", errors));
        header.setUpdateEmployee(batch.getUpdateEmployee());
        header.setCreateDate(today);
        return header;
    }

    @Override
    public String extractLookupKey(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int rowNum = 0;
            while ((line = reader.readLine()) != null) {
                rowNum++;
                line = stripCr(line);
                if (line.isBlank()) {
                    continue;
                }
                List<String> fields = parseLine(line);
                if (!fields.isEmpty() && "1".equals(trim(fields.get(0)))) {
                    if (fields.size() < 6) {
                        throw new IllegalArgumentException(
                                "住信SBI区分1レコードの列数が不足しています。");
                    }
                    String merchantId = trim(fields.get(5));
                    if (merchantId.isEmpty()) {
                        throw new IllegalArgumentException(
                                rowNum + "行目: 加盟店IDが空のため識別キーを取得できませんでした。");
                    }
                    return merchantId; // 加盟店ID
                }
            }
            throw new IllegalArgumentException("住信SBIファイルに区分1レコードがありません。");
        }
    }

    private VisaMasterTransaction buildTransaction(
            List<String> fields, ImportBatch batch, Integer headerId, String tradeCode,
            LocalDate today, int rowNum, List<CsvValidationError> errors) {
        VisaMasterTransaction trn = new VisaMasterTransaction();
        trn.setTradeCode(tradeCode);
        trn.setBatchId(batch.getBatchId());
        trn.setHeaderId(headerId);
        trn.setParentMerchantId(trim(fields.get(1)));
        trn.setMerchantId(trim(fields.get(2)));
        trn.setTransactionNo(trim(fields.get(3)));
        trn.setSalesDate(
                parseDate8Checked(fields.get(4), rowNum, "売上日", errors));
        trn.setCardNumberMasked(trim(fields.get(5)));
        trn.setBrandType(trim(fields.get(6)));
        trn.setPaymentTypeCode(trim(fields.get(7)));
        trn.setPaymentMethod(trim(fields.get(8)));
        trn.setFeeRate(
                parseDecimalChecked(fields.get(9), rowNum, "手数料率", errors));
        trn.setSalesAmount(
                parseIntChecked(fields.get(10), rowNum, "売上金額", errors));
        trn.setFeeAmount1(
                parseIntChecked(fields.get(11), rowNum, "手数料額1", errors));
        trn.setDeferredAmount(
                parseIntChecked(fields.get(12), rowNum, "後払金額", errors));
        trn.setDeferredFee(
                parseIntChecked(fields.get(13), rowNum, "後払手数料", errors));
        trn.setTransferDeferredAmount(
                parseIntChecked(fields.get(14), rowNum, "後払振替金額", errors));
        trn.setTransferDeferredFee(
                parseIntChecked(fields.get(15), rowNum, "後払振替手数料", errors));
        trn.setPayableSalesAmount(
                parseIntChecked(fields.get(16), rowNum, "支払可能売上金額", errors));
        trn.setPayableFeeAmount(
                parseIntChecked(fields.get(17), rowNum, "支払可能手数料額", errors));
        trn.setPaymentAmount1(
                parseIntChecked(fields.get(18), rowNum, "支払金額1", errors));
        trn.setDeferredBalance(
                parseIntChecked(fields.get(19), rowNum, "後払残高", errors));
        trn.setUpdateEmployee(batch.getUpdateEmployee());
        trn.setCreateDate(today);
        return trn;
    }
}
