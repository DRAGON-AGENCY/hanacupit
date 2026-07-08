package com.cupit.csv.importer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.model.ImportBatch;
import com.cupit.model.SmccMerchantNo;
import com.cupit.model.SteraCreditSalesDetail;
import com.cupit.repository.SmccMerchantNoRepository;
import com.cupit.repository.SteraCreditSalesDetailRepository;

/**
 * steraクレジット売上件別明細CSVを解析してm_stera_credit_sales_detailに登録する。
 * 取引コードはm_smcc_merchant_no.merchant_no（ファイル列：利用加盟店番号）から解決する。
 */
@Component
public class SteraCreditFileImporter extends AbstractFileImporter {

    private final SteraCreditSalesDetailRepository steraCreditSalesDetailRepository;
    private final SmccMerchantNoRepository smccMerchantNoRepository;

    public SteraCreditFileImporter(
            SteraCreditSalesDetailRepository steraCreditSalesDetailRepository,
            SmccMerchantNoRepository smccMerchantNoRepository) {
        this.steraCreditSalesDetailRepository = steraCreditSalesDetailRepository;
        this.smccMerchantNoRepository = smccMerchantNoRepository;
    }

    @Override
    public ImportResult importFile(MultipartFile file, ImportBatch batch) throws IOException {
        List<SteraCreditSalesDetail> records = new ArrayList<>();
        List<CsvValidationError> errors = new ArrayList<>();
        LocalDate today = LocalDate.now();
        int rowNum = 1; // 1行目＝ヘッダー

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), detectCharset(file)))) {

            reader.readLine(); // ヘッダー行スキップ

            String line;
            while ((line = reader.readLine()) != null) {
                rowNum++;
                line = stripCr(line);
                if (line.isBlank()) {
                    continue;
                }
                List<String> fields = parseLine(line);
                if (fields.size() < 18) {
                    String merchantPart = !fields.isEmpty()
                            ? "利用加盟店番号「" + fields.get(0).trim() + "」: " : "";
                    errors.add(new CsvValidationError(rowNum, "",
                            merchantPart + "列数が不正です。期待: 18列、実際: "
                            + fields.size() + "列"));
                    continue;
                }

                String merchantId = trim(fields.get(0));
                Optional<SmccMerchantNo> mapping = resolveMerchant(
                        smccMerchantNoRepository.findByMerchantNo(merchantId), rowNum, merchantId, errors);
                if (mapping.isEmpty()) {
                    continue;
                }

                int errorCountBeforeRow = errors.size();
                SteraCreditSalesDetail detail = new SteraCreditSalesDetail();
                detail.setTradeCode(mapping.get().getTradeCode());
                detail.setBatchId(batch.getBatchId());
                detail.setMerchantId(merchantId);
                detail.setSentDate(trim(fields.get(1)));
                detail.setTransactionType(trim(fields.get(2)));
                detail.setTransactionType2(blankToNull(trim(fields.get(3))));
                detail.setCardNumberMasked(blankToNull(trim(fields.get(4))));
                detail.setTransactionDate(trim(fields.get(5)));
                detail.setAmountSign(trim(fields.get(6)));
                detail.setBillingAmount(
                        parseIntChecked(fields.get(7), rowNum, "請求金額", errors));
                detail.setOriginalAmount(
                        parseIntChecked(fields.get(8), rowNum, "利用元金額", errors));
                detail.setApprovalNumber(trim(fields.get(9)));
                detail.setTerminalId(trim(fields.get(10)));
                detail.setChangeDataFlag(blankToNull(trim(fields.get(11))));
                detail.setStoreName(trim(fields.get(12)));
                detail.setCardBrand(trim(fields.get(13)));
                detail.setTerminalSequenceNo(blankToNull(trim(fields.get(14))));
                detail.setSummaryCount(blankToNull(trim(fields.get(15))));
                detail.setReaderWriterId(blankToNull(trim(fields.get(16))));
                detail.setRepresentativeMerchantId(trim(fields.get(17)));
                if (errors.size() > errorCountBeforeRow) {
                    continue; // この行にデータ変換エラーがあるため登録しない
                }
                detail.setUpdateEmployee(batch.getUpdateEmployee());
                detail.setCreateDate(today);
                records.add(detail);
            }
        }

        int totalDataRows = rowNum - 1; // ヘッダー行を除いたデータ行数
        records.forEach(steraCreditSalesDetailRepository::save);
        return new ImportResult(records.size(), totalDataRows, errors);
    }

    @Override
    public void deleteBatchData(int batchId) {
        steraCreditSalesDetailRepository.deleteByBatchId(batchId);
    }

    @Override
    public String extractLookupKey(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), detectCharset(file)))) {
            reader.readLine(); // ヘッダー行スキップ
            String line = reader.readLine();
            if (line == null || line.isBlank()) {
                throw new IllegalArgumentException("データ行がありません。");
            }
            List<String> fields = parseLine(stripCr(line));
            if (fields.isEmpty()) {
                throw new IllegalArgumentException("steraクレジットファイルの列数が不足しています。");
            }
            String merchantId = trim(fields.get(0));
            if (merchantId.isEmpty()) {
                throw new IllegalArgumentException(
                        "2行目: 利用加盟店番号が空のため識別キーを取得できませんでした。");
            }
            return merchantId;
        }
    }

    private String blankToNull(String value) {
        return value.isEmpty() ? null : value;
    }

    /**
     * 検索結果を一意に絞り込む。m_smcc_merchant_noはmerchant_noに一意制約が無く、
     * かつm_stera_terminalと異なり有効期間・ステータス列も持たないため、複数件ヒットは
     * マスタデータの不整合として扱い、どちらを採用すべきか決め打ちせずエラーとして
     * その行をスキップする。
     */
    private Optional<SmccMerchantNo> resolveMerchant(
            List<SmccMerchantNo> candidates, int rowNum, String merchantId,
            List<CsvValidationError> errors) {
        if (candidates.isEmpty()) {
            errors.add(new CsvValidationError(rowNum, "利用加盟店番号",
                    "利用加盟店番号「" + merchantId
                            + "」に対応する取引コードがm_smcc_merchant_noに存在しません。"));
            return Optional.empty();
        }
        if (candidates.size() > 1) {
            errors.add(new CsvValidationError(rowNum, "利用加盟店番号",
                    "利用加盟店番号「" + merchantId
                            + "」に対応するデータがm_smcc_merchant_noに複数件存在するため"
                            + "取引コードを一意に決定できません。マスタデータをご確認ください。"));
            return Optional.empty();
        }
        return Optional.of(candidates.get(0));
    }
}
