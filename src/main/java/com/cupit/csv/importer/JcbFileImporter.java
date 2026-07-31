package com.cupit.csv.importer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.model.ImportBatch;
import com.cupit.model.JcbSalesDetail;
import com.cupit.model.PaygateStoreMapping;
import com.cupit.repository.JcbSalesDetailRepository;
import com.cupit.repository.PaygateMappingRepository;

/**
 * JCB売上明細CSVを解析してm_jcb_sales_detailに登録する。
 * 文字コード: UTF-8 BOM付きは自動検出、なければMS932。ヘッダー行: 1行目、データ: 2行目以降。
 * 列順序: 加盟店名称,加盟店番号,ご契約カード会社,お支払方法,
 *         お取扱カード名,支払区分,売上方法,集計日,売上件数,売上金額（円）
 * 1ファイルに複数の加盟店番号が混在する（花キューピット全店舗分を1ファイルに集計した
 * レポートが送られてくる）ため、取引コードは行ごとに加盟店番号でm_paygate_store_mapping
 * を引き直して解決する。マッピングが見つからない行・データ変換エラーが発生した行は
 * その行だけを登録せずスキップし、ファイルの最後まで処理を継続する
 * （データエラーによってファイル全体をロールバックすることはしない）。
 */
@Component
public class JcbFileImporter extends AbstractFileImporter {


    private final JcbSalesDetailRepository jcbSalesDetailRepository;
    private final PaygateMappingRepository paygateMappingRepository;

    public JcbFileImporter(
            JcbSalesDetailRepository jcbSalesDetailRepository,
            PaygateMappingRepository paygateMappingRepository) {
        this.jcbSalesDetailRepository = jcbSalesDetailRepository;
        this.paygateMappingRepository = paygateMappingRepository;
    }

    @Override
    public ImportResult importFile(MultipartFile file, ImportBatch batch) throws IOException {
        List<JcbSalesDetail> records = new ArrayList<>();
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
                if (fields.size() < 10) {
                    String merchantPart = fields.size() > 1
                            ? "加盟店番号「" + fields.get(1).trim() + "」: " : "";
                    errors.add(new CsvValidationError(rowNum, "",
                            merchantPart + "列数が不正です。期待: 10列、実際: "
                            + fields.size() + "列"));
                    continue;
                }

                String merchantNo = trim(fields.get(1));
                Optional<PaygateStoreMapping> mapping =
                        paygateMappingRepository.findFirstByJcbMerchantNo(merchantNo);
                if (mapping.isEmpty()) {
                    errors.add(new CsvValidationError(rowNum, "加盟店番号",
                            "加盟店番号「" + merchantNo
                                    + "」に対応する取引コードが取引コード紐付データに存在しません。"));
                    continue;
                }

                int errorCountBeforeRow = errors.size();
                JcbSalesDetail detail = new JcbSalesDetail();
                detail.setTradeCode(mapping.get().getTradeCode());
                detail.setBatchId(batch.getBatchId());
                detail.setStoreName(trim(fields.get(0)));
                detail.setStoreNumber(merchantNo);
                detail.setCardCompany(trim(fields.get(2)));
                detail.setPaymentMethod(trim(fields.get(3)));
                detail.setCardName(trim(fields.get(4)));
                detail.setPaymentType(trim(fields.get(5)));
                detail.setSalesMethod(trim(fields.get(6)));
                detail.setSalesDate(trim(fields.get(7)));
                detail.setSalesCount(
                        parseIntChecked(fields.get(8), rowNum, "売上件数", errors));
                detail.setSalesAmount(
                        parseIntChecked(fields.get(9), rowNum, "売上金額（円）", errors));
                if (errors.size() > errorCountBeforeRow) {
                    continue; // この行にデータ変換エラーがあるため登録しない
                }
                detail.setUpdateEmployee(batch.getUpdateEmployee());
                detail.setCreateDate(today);
                records.add(detail);
            }
        }

        int totalDataRows = rowNum - 1; // ヘッダー行を除いたデータ行数
        records.forEach(jcbSalesDetailRepository::save);
        return new ImportResult(records.size(), totalDataRows, errors);
    }

    @Override
    public void deleteBatchData(int batchId) {
        jcbSalesDetailRepository.deleteByBatchId(batchId);
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
            if (fields.size() < 2) {
                throw new IllegalArgumentException("JCBファイルの列数が不足しています。");
            }
            String merchantNo = trim(fields.get(1));
            if (merchantNo.isEmpty()) {
                throw new IllegalArgumentException(
                        "2行目: 加盟店番号が空のため識別キーを取得できませんでした。");
            }
            return merchantNo; // 加盟店番号
        }
    }

    @Override
    public List<String> extractAllLookupKeys(MultipartFile file) throws IOException {
        Set<String> keys = new LinkedHashSet<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), detectCharset(file)))) {
            reader.readLine(); // ヘッダー行スキップ
            String line;
            while ((line = reader.readLine()) != null) {
                line = stripCr(line);
                if (line.isBlank()) {
                    continue;
                }
                List<String> fields = parseLine(line);
                if (fields.size() < 2) {
                    continue;
                }
                String merchantNo = trim(fields.get(1));
                if (!merchantNo.isEmpty()) {
                    keys.add(merchantNo);
                }
            }
        }
        return new ArrayList<>(keys);
    }
}
