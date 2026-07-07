package com.cupit.csv.importer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.model.ImportBatch;
import com.cupit.model.PaygateStoreMapping;
import com.cupit.repository.PaygateMappingRepository;

/**
 * PAYGATE 会員コード紐付 CSV を解析して m_paygate_store_mapping に取引コード単位で
 * 洗い替え登録する（取引コードが存在しなければ新規登録、存在すれば該当取引コードの
 * レコードのみを削除してから登録する）。CSVに含まれない取引コードの既存データは保持する。
 * 1取引コード（加盟店）に複数端末が存在する運用があるため、取引コード自体の重複は
 * 許容し、複数端末分の行がまとめて同一取引コードで登録される。
 * 文字コード: UTF-8 BOM付きは自動検出、なければMS932。
 * ヘッダー行: 1行目は内容によらず常にヘッダー行として扱いスキップする（列名はチェックしない）。
 * Excel が出力する科学的記数法（例: 5.01292E+12）は整数文字列に正規化する。
 * 列数不足の行や、精算データ取込み時の逆引きキーとなる4項目（端末識別番号・
 * 住信SBI加盟店番号・ネットスターズ店舗コード・楽天ペイ店舗コード）がCSV内で
 * 重複する行はその行だけを登録せずスキップし、ファイルの最後まで処理を継続する
 * （データエラーによってファイル全体をロールバックすることはしない）。
 */
@Component
public class PaygateMappingFileImporter extends AbstractFileImporter {

    private static final int EXPECTED_COLUMN_COUNT = 13;
    private static final DateTimeFormatter FMT_SLASH = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final PaygateMappingRepository paygateMappingRepository;

    public PaygateMappingFileImporter(PaygateMappingRepository paygateMappingRepository) {
        this.paygateMappingRepository = paygateMappingRepository;
    }

    @Override
    public ImportResult importFile(MultipartFile file, ImportBatch batch) throws IOException {
        List<PaygateStoreMapping> records = new ArrayList<>();
        List<CsvValidationError> errors = new ArrayList<>();
        LocalDate today = LocalDate.now();
        int rowNum = 1;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), detectCharset(file)))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                return new ImportResult(0, 0, errors);
            }

            Set<String> seenTerminalIds = new HashSet<>();
            Set<String> seenSbiMerchantIds = new HashSet<>();
            Set<String> seenNetstarStoreCodes = new HashSet<>();
            Set<String> seenRpayStoreCodes = new HashSet<>();
            String line;
            while ((line = reader.readLine()) != null) {
                rowNum++;
                line = stripCr(line);
                if (line.isBlank()) {
                    continue;
                }
                List<String> fields = parseLine(line);
                if (fields.size() != EXPECTED_COLUMN_COUNT) {
                    errors.add(new CsvValidationError(rowNum, "hana cupid管理番号",
                            "取引コード「" + fields.get(0).trim() + "」: 列数が不正です。期待: "
                            + EXPECTED_COLUMN_COUNT + "列、実際: " + fields.size() + "列"));
                    continue;
                }
                parseDataRow(fields, rowNum, batch, today, records, errors,
                        seenTerminalIds, seenSbiMerchantIds,
                        seenNetstarStoreCodes, seenRpayStoreCodes);
            }
        }

        Set<String> tradeCodes = records.stream()
                .map(PaygateStoreMapping::getTradeCode)
                .collect(Collectors.toSet());
        paygateMappingRepository.deleteByTradeCodeIn(tradeCodes);
        paygateMappingRepository.saveAll(records);
        int totalDataRows = rowNum - 1;
        return new ImportResult(records.size(), totalDataRows, errors);
    }

    /**
     * 1取引コード（加盟店）に複数端末が存在する運用のため、取引コード自体の重複は
     * 許容し「取引コード単位の洗い替え」で複数端末分がまとめて登録される。
     * 精算データ取込み時の逆引きキーとなる4項目（端末識別番号・住信SBI加盟店番号・
     * ネットスターズ店舗コード・楽天ペイ店舗コード）がCSV内で重複する行は、
     * どの端末の値か特定できなくなるためその行だけ登録せずスキップする。
     */
    private void parseDataRow(
            List<String> fields, int rowNum, ImportBatch batch, LocalDate today,
            List<PaygateStoreMapping> records, List<CsvValidationError> errors,
            Set<String> seenTerminalIds, Set<String> seenSbiMerchantIds,
            Set<String> seenNetstarStoreCodes, Set<String> seenRpayStoreCodes) {
        String tradeCode = trim(fields.get(0));
        if (tradeCode.isEmpty()) {
            errors.add(new CsvValidationError(rowNum, "hana cupid管理番号", "取引コードは必須です。"));
            return;
        }
        String terminalId = normalizeNumeric(fields.get(3));
        String sbiMerchantId = normalizeNumeric(fields.get(5));
        String netstarStoreCode = normalizeNumeric(fields.get(6));
        String rpayStoreCode = normalizeNumeric(fields.get(9));

        int errorCountBeforeRow = errors.size();
        checkDuplicate(errors, rowNum, "端末識別番号", terminalId, seenTerminalIds);
        checkDuplicate(errors, rowNum, "加盟店番号(住信SBI)", sbiMerchantId, seenSbiMerchantIds);
        checkDuplicate(errors, rowNum, "StarPay店舗コード(ﾈｯﾄｽﾀｰｽﾞ)",
                netstarStoreCode, seenNetstarStoreCodes);
        checkDuplicate(errors, rowNum, "GW店舗コード(Rpay)", rpayStoreCode, seenRpayStoreCodes);
        if (errors.size() > errorCountBeforeRow) {
            return; // この行に識別キーの重複エラーがあるため登録しない
        }

        PaygateStoreMapping mapping = new PaygateStoreMapping();
        mapping.setTradeCode(tradeCode);
        mapping.setStoreName(trim(fields.get(1)));
        mapping.setMemberType(trim(fields.get(2)));
        mapping.setTerminalId(terminalId);
        mapping.setReaderSerialNo(trim(fields.get(4)));
        mapping.setSbiMerchantId(sbiMerchantId);
        mapping.setNetstarStoreCode(netstarStoreCode);
        mapping.setJcbMerchantNo(normalizeNumeric(fields.get(7)));
        mapping.setDnpMgmtNo(trim(fields.get(8)));
        mapping.setRpayStoreCode(rpayStoreCode);
        mapping.setTerminalStatus(trim(fields.get(10)));
        mapping.setUsageIntention(trim(fields.get(11)));
        mapping.setUsageIntentionUpdated(parseSlashDate(fields.get(12)));
        mapping.setUpdateEmployee(batch.getUpdateEmployee());
        mapping.setCreateDate(today);
        records.add(mapping);
    }

    private void checkDuplicate(
            List<CsvValidationError> errors, int rowNum, String columnLabel,
            String value, Set<String> seenValues) {
        if (value == null || value.isEmpty()) {
            return;
        }
        if (!seenValues.add(value)) {
            errors.add(new CsvValidationError(rowNum, columnLabel,
                    columnLabel + "「" + value + "」がCSV内で重複しています。"));
        }
    }

    private LocalDate parseSlashDate(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(s.trim(), FMT_SLASH);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Excel が出力する科学的記数法（例: "5.01292E+12"）を整数文字列に正規化する。
     * 末尾の不要な小数部（"12345.0"）も除去する。
     */
    private String normalizeNumeric(String s) {
        if (s == null) {
            return null;
        }
        String v = s.trim();
        if (v.isEmpty()) {
            return null;
        }
        if (v.matches("(?i)\\d+\\.?\\d*[Ee][+\\-]?\\d+")) {
            try {
                return new BigDecimal(v).toBigInteger().toString();
            } catch (NumberFormatException e) {
                return v;
            }
        }
        if (v.matches("\\d+\\.0+")) {
            return v.replaceAll("\\.0+$", "");
        }
        return v;
    }

}
