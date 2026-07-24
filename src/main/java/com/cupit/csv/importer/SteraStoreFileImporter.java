package com.cupit.csv.importer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.model.ImportBatch;
import com.cupit.model.SteraStore;
import com.cupit.repository.SteraStoreRepository;

/**
 * 店舗データ CSV を解析して m_stera_store に取引コード単位で洗い替えではなく
 * upsert（存在すればUPDATE、なければINSERT）で登録する。取引コードは
 * m_stera_storeでUNIQUE制約付きのため、既存の取引コードは record_no を引き継いで
 * 更新し、created_at は既存値を維持する（新規は現在時刻を設定する）。
 * 文字コード: UTF-8 BOM付きは自動検出、なければMS932。
 * ヘッダー行: 1行目は内容によらず常にヘッダー行として扱いスキップする（列名はチェックしない）。
 * 列数不足の行、取引コード未入力の行、必須項目未入力の行、日付変換に失敗した項目を含む行は
 * その行だけを登録せずスキップし、ファイルの最後まで処理を継続する（データエラーによって
 * ファイル全体をロールバックすることはしない）。CSV内で取引コードが重複する場合は
 * どちらの行が正しいか判断できないため、該当する取引コードの行を（先着1件目も含めて）
 * 全てスキップする。
 */
@Component
public class SteraStoreFileImporter extends AbstractFileImporter {

    private static final int EXPECTED_COLUMN_COUNT = 30;

    private static final String[] COLUMN_NAMES = {
        "取引コード", "交通系社局", "Edy ID", "dポイント加盟店コード", "dポイント店舗コード",
        "dポイント支部コード", "届出支店コード", "会員種別", "店舗名", "店舗名カナ",
        "店舗名（英字）", "店舗郵便番号", "店舗住所（漢字）", "店舗住所（カナ）", "店舗電話番号",
        "メールアドレス", "緯度", "経度", "金融機関名", "金融機関コード",
        "支店名", "支店コード", "口座種別", "口座番号", "口座名義（カナ）",
        "JCB利用状況", "JCB利用開始日", "dポイント利用状況", "dポイント利用開始日", "備考",
    };

    private static final Set<Integer> REQUIRED_INDEXES = Set.of(
            1, 2, 6, 8, 9, 10, 11, 12, 13, 14, 15, 18, 19, 20, 21, 22, 23, 24, 25, 27);

    private final SteraStoreRepository steraStoreRepository;

    public SteraStoreFileImporter(SteraStoreRepository steraStoreRepository) {
        this.steraStoreRepository = steraStoreRepository;
    }

    @Override
    public ImportResult importFile(MultipartFile file, ImportBatch batch) throws IOException {
        List<SteraStore> records = new ArrayList<>();
        List<CsvValidationError> errors = new ArrayList<>();
        int rowNum = 1;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), detectCharset(file)))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                return new ImportResult(0, 0, errors);
            }

            List<String> dataLines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                dataLines.add(stripCr(line));
            }

            Map<String, Integer> tradeCodeCounts = countTradeCodes(dataLines);

            for (String dataLine : dataLines) {
                rowNum++;
                if (dataLine.isBlank()) {
                    continue;
                }
                List<String> fields = parseLine(dataLine);
                if (fields.size() != EXPECTED_COLUMN_COUNT) {
                    errors.add(new CsvValidationError(rowNum, "取引コード",
                            "取引コード「" + fields.get(0).trim() + "」: 列数が不正です。期待: "
                            + EXPECTED_COLUMN_COUNT + "列、実際: " + fields.size() + "列"));
                    continue;
                }
                parseDataRow(fields, rowNum, records, errors, tradeCodeCounts);
            }
        }

        applyAuditColumnsAndUpsert(records, batch);
        int totalDataRows = rowNum - 1;
        return new ImportResult(records.size(), totalDataRows, errors);
    }

    /**
     * データ行を1回走査し、取引コードごとの出現回数を数える。列数が不正な行は
     * 取引コード自体を安全に取得できない可能性があるため集計対象から除く
     * （その行は別途「列数が不正です」エラーになる）。
     */
    private Map<String, Integer> countTradeCodes(List<String> dataLines) {
        Map<String, Integer> counts = new HashMap<>();
        for (String dataLine : dataLines) {
            if (dataLine.isBlank()) {
                continue;
            }
            List<String> fields = parseLine(dataLine);
            if (fields.size() != EXPECTED_COLUMN_COUNT) {
                continue;
            }
            String tradeCode = trim(fields.get(0));
            if (!tradeCode.isEmpty()) {
                counts.merge(tradeCode, 1, Integer::sum);
            }
        }
        return counts;
    }

    private void parseDataRow(
            List<String> fields, int rowNum, List<SteraStore> records,
            List<CsvValidationError> errors, Map<String, Integer> tradeCodeCounts) {
        String tradeCode = trim(fields.get(0));
        if (tradeCode.isEmpty()) {
            errors.add(new CsvValidationError(rowNum, "取引コード", "取引コードは必須です。"));
            return;
        }
        if (tradeCodeCounts.getOrDefault(tradeCode, 0) > 1) {
            errors.add(new CsvValidationError(rowNum, "取引コード",
                    "取引コード「" + tradeCode + "」がCSV内で重複しています。"));
            return;
        }

        int errorCountBeforeRow = errors.size();
        for (int index : REQUIRED_INDEXES) {
            if (trim(fields.get(index)).isEmpty()) {
                errors.add(new CsvValidationError(rowNum, COLUMN_NAMES[index],
                        "取引コード「" + tradeCode + "」: " + COLUMN_NAMES[index] + "は必須です。"));
            }
        }

        SteraStore record = new SteraStore();
        record.setTradeCode(tradeCode);
        record.setTransitCompany(trim(fields.get(1)));
        record.setEdyId(trim(fields.get(2)));
        record.setDPointMerchantCode(blankToNull(fields.get(3)));
        record.setDPointStoreCode(blankToNull(fields.get(4)));
        record.setDPointBranchCode(blankToNull(fields.get(5)));
        record.setBranchCode(trim(fields.get(6)));
        record.setMemberType(blankToNull(fields.get(7)));
        record.setStoreName(trim(fields.get(8)));
        record.setStoreNameKana(trim(fields.get(9)));
        record.setStoreNameEn(trim(fields.get(10)));
        record.setStoreZip(trim(fields.get(11)));
        record.setStoreAddress(trim(fields.get(12)));
        record.setStoreAddressKana(trim(fields.get(13)));
        record.setStoreTel(trim(fields.get(14)));
        record.setEmail(trim(fields.get(15)));
        record.setLatitude(parseBigDecimalChecked(fields.get(16), rowNum, "緯度", errors));
        record.setLongitude(parseBigDecimalChecked(fields.get(17), rowNum, "経度", errors));
        record.setBankName(trim(fields.get(18)));
        record.setBankCode(trim(fields.get(19)));
        record.setBranchName(trim(fields.get(20)));
        record.setBankBranchCode(trim(fields.get(21)));
        record.setAccountType(trim(fields.get(22)));
        record.setAccountNo(trim(fields.get(23)));
        record.setAccountHolderKana(trim(fields.get(24)));
        record.setJcbStatus(trim(fields.get(25)));
        record.setJcbStartDate(parseDateSlashChecked(fields.get(26), rowNum, "JCB利用開始日", errors));
        record.setDPointStatus(trim(fields.get(27)));
        record.setDPointStartDate(
                parseDateSlashChecked(fields.get(28), rowNum, "dポイント利用開始日", errors));
        record.setRemarks(blankToNull(fields.get(29)));
        if (errors.size() > errorCountBeforeRow) {
            return; // この行にデータエラーがあるため登録しない
        }
        records.add(record);
    }

    private String blankToNull(String s) {
        String trimmed = trim(s);
        return (trimmed == null || trimmed.isEmpty()) ? null : trimmed;
    }

    /**
     * 取引コードをキーに既存行を一括取得し、存在すれば record_no・created_at を
     * 引き継いでUPDATE、存在しなければ新規INSERTとしてsaveする。
     * updated_at・updated_user_id は常に今回の値で上書きする。
     */
    private void applyAuditColumnsAndUpsert(List<SteraStore> records, ImportBatch batch) {
        OffsetDateTime now = OffsetDateTime.now();
        List<String> tradeCodes = records.stream().map(SteraStore::getTradeCode).toList();
        Map<String, SteraStore> existingByTradeCode = new HashMap<>();
        for (SteraStore existing : steraStoreRepository.findByTradeCodeIn(tradeCodes)) {
            existingByTradeCode.put(existing.getTradeCode(), existing);
        }
        for (SteraStore record : records) {
            SteraStore existing = existingByTradeCode.get(record.getTradeCode());
            if (existing != null) {
                record.setRecordNo(existing.getRecordNo());
                record.setCreatedAt(existing.getCreatedAt());
            } else {
                record.setCreatedAt(now);
            }
            record.setUpdatedAt(now);
            record.setUpdatedUserId(batch.getUpdateEmployee());
        }
        steraStoreRepository.saveAll(records);
    }

}
