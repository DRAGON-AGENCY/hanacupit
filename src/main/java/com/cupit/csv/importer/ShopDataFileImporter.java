package com.cupit.csv.importer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.model.ImportBatch;
import com.cupit.model.ShopData;
import com.cupit.repository.ShopDataRepository;

/**
 * 店舗データ CSV を解析して m_shop_data に登録・更新する。
 * trade_code が m_shop_data の主キーであるため、明示的な削除は行わず
 * shopDataRepository.saveAll() のみで upsert（存在すればUPDATE、なければINSERT）する。
 * 既存の取引コードは registered_date を維持し updated_date のみ今日にする（upsertで
 * 登録日を上書きしないため、保存前に一括で既存取引コードを取得する）。
 * 文字コード: UTF-8 BOM付きは自動検出、なければMS932。
 * ヘッダー行: 1行目は内容によらず常にヘッダー行として扱いスキップする（列名はチェックしない）。
 * 列数不足の行、取引コード未入力の行、CSV内で取引コードが重複する行、日付・数値変換に
 * 失敗した項目を含む行はその行だけを登録せずスキップし、ファイルの最後まで処理を継続する
 * （データエラーによってファイル全体をロールバックすることはしない）。取引コード以外の
 * 全項目は任意とする。
 */
@Component
public class ShopDataFileImporter extends AbstractFileImporter {

    private static final int EXPECTED_COLUMN_COUNT = 27;

    private final ShopDataRepository shopDataRepository;

    public ShopDataFileImporter(ShopDataRepository shopDataRepository) {
        this.shopDataRepository = shopDataRepository;
    }

    @Override
    public ImportResult importFile(MultipartFile file, ImportBatch batch) throws IOException {
        List<ShopData> records = new ArrayList<>();
        List<CsvValidationError> errors = new ArrayList<>();
        int rowNum = 1;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), detectCharset(file)))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                return new ImportResult(0, 0, errors);
            }

            Set<String> seenTradeCodes = new HashSet<>();
            String line;
            while ((line = reader.readLine()) != null) {
                rowNum++;
                line = stripCr(line);
                if (line.isBlank()) {
                    continue;
                }
                List<String> fields = parseLine(line);
                if (fields.size() != EXPECTED_COLUMN_COUNT) {
                    errors.add(new CsvValidationError(rowNum, "取引コード",
                            "取引コード「" + fields.get(0).trim() + "」: 列数が不正です。期待: "
                            + EXPECTED_COLUMN_COUNT + "列、実際: " + fields.size() + "列"));
                    continue;
                }
                parseDataRow(fields, rowNum, records, errors, seenTradeCodes);
            }
        }

        applyAuditColumns(records, batch);
        shopDataRepository.saveAll(records);
        int totalDataRows = rowNum - 1;
        return new ImportResult(records.size(), totalDataRows, errors);
    }

    private void parseDataRow(
            List<String> fields, int rowNum, List<ShopData> records,
            List<CsvValidationError> errors, Set<String> seenTradeCodes) {
        String tradeCode = trim(fields.get(0));
        if (tradeCode.isEmpty()) {
            errors.add(new CsvValidationError(rowNum, "取引コード", "取引コードは必須です。"));
            return;
        }
        if (!seenTradeCodes.add(tradeCode)) {
            errors.add(new CsvValidationError(rowNum, "取引コード",
                    "取引コード「" + tradeCode + "」がCSV内で重複しています。"));
            return;
        }

        int errorCountBeforeRow = errors.size();
        ShopData record = new ShopData();
        record.setTradeCode(tradeCode);
        record.setApplicationTypeFlag(trim(fields.get(1)));
        record.setStoreNameAlphabet(trim(fields.get(2)));
        record.setRepAddressKana(trim(fields.get(3)));
        record.setJcbMerchantNumber(trim(fields.get(4)));
        record.setCorporateNumber(trim(fields.get(5)));
        record.setDoorToDoorSalesFlag(trim(fields.get(6)));
        record.setTelemarketingSalesFlag(trim(fields.get(7)));
        record.setChainSalesFlag(trim(fields.get(8)));
        record.setBusinessOpportunitySalesFlag(trim(fields.get(9)));
        record.setContinuousServiceFlag(trim(fields.get(10)));
        record.setCardDataRetentionStatus(trim(fields.get(11)));
        record.setPciDssComplianceStatus(trim(fields.get(12)));
        record.setNonRetentionPlannedMonth(trim(fields.get(13)));
        record.setPciDssCompliancePlannedMonth(trim(fields.get(14)));
        record.setTerminalIcStatus(trim(fields.get(15)));
        record.setTerminalIcPlannedMonth(trim(fields.get(16)));
        record.setAcquirerUniqueKey(trim(fields.get(17)));
        record.setSteraTerminalId(trim(fields.get(18)));
        record.setLinkageDate(parseDateSlashChecked(fields.get(19), rowNum, "連携日", errors));
        record.setExistingContractFlag(trim(fields.get(20)));
        record.setClassification(trim(fields.get(21)));
        record.setContractSource(trim(fields.get(22)));
        record.setGiftContractFlag(trim(fields.get(23)));
        record.setEdyContractFlag(trim(fields.get(24)));
        record.setCancellationConfirmation(trim(fields.get(25)));
        record.setCancellationProcessStatus(trim(fields.get(26)));
        if (errors.size() > errorCountBeforeRow) {
            return; // この行にデータ変換エラーがあるため登録しない
        }
        records.add(record);
    }

    /**
     * 登録日・更新日・更新者を設定する。新規取引コードは登録日を今日にする一方、
     * 既存の取引コードは登録日を維持し更新日のみ今日にする（upsertで登録日を
     * 上書きしないため、保存前に一括で既存取引コードを取得する）。
     */
    private void applyAuditColumns(List<ShopData> records, ImportBatch batch) {
        LocalDate today = LocalDate.now();
        Set<String> tradeCodes = records.stream()
                .map(ShopData::getTradeCode)
                .collect(Collectors.toSet());
        Map<String, LocalDate> existingRegisteredDates = new HashMap<>();
        for (ShopData existing : shopDataRepository.findAllById(tradeCodes)) {
            existingRegisteredDates.put(existing.getTradeCode(), existing.getRegisteredDate());
        }
        for (ShopData record : records) {
            LocalDate existingRegisteredDate = existingRegisteredDates.get(record.getTradeCode());
            if (existingRegisteredDate != null) {
                record.setRegisteredDate(existingRegisteredDate);
                record.setUpdatedDate(today);
            } else {
                record.setRegisteredDate(today);
            }
            record.setUpdatedBy(batch.getUpdateEmployee());
        }
    }

}
