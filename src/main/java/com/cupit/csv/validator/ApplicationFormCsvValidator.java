package com.cupit.csv.validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.ApplicationFormColumn;
import com.cupit.csv.CsvValidationError;
import com.cupit.csv.CsvValidationResult;

/**
 * 各決済会社所定申込フォーム作成のINPUT CSV（{@link ApplicationFormColumn}で定義した列数）の
 * フォーマットを検証するクラス。列数固定、1行目は内容によらず常にヘッダー行として扱う
 * （列名の一致チェックは行わない）。取引コード（JFTD取引コード）は必須。1行＝1店舗の
 * 申込データのため、CSV内で取引コードが重複する場合はどちらの行が正しいか判断できないため、
 * 該当する取引コードの行を（先着1件目も含めて）全てエラーとする。
 * 取引コード以外の全項目は任意とする。
 */
@Component
public class ApplicationFormCsvValidator extends AbstractCsvFormatValidator {

    private static final int EXPECTED_COLUMN_COUNT = ApplicationFormColumn.values().length;
    private static final int IDX_TRADE_CODE = ApplicationFormColumn.TRADE_CODE.ordinal();

    @Override
    public CsvValidationResult validate(MultipartFile file) throws IOException {
        CsvValidationResult result = new CsvValidationResult();

        String ext = getExtension(file.getOriginalFilename());
        if (!"csv".equals(ext)) {
            result.addError(new CsvValidationError(
                    0, "", "ファイルの拡張子が不正です。期待: .csv、実際: ." + ext));
            result.markFatal();
            return result;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), detectCharset(file)))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                result.addError(new CsvValidationError(1, "", "ファイルが空です。"));
                result.markFatal();
                return result;
            }
            if (headerLine.startsWith("\uFEFF")) {
                headerLine = headerLine.substring(1);
            }
            headerLine = stripCr(headerLine);
            List<String> headerFields = parseLine(headerLine);

            if (headerFields.size() != EXPECTED_COLUMN_COUNT) {
                addColumnCountError(result, 1, EXPECTED_COLUMN_COUNT, headerFields.size());
                result.markFatal();
                result.setTotalRowCount(0);
                return result;
            }

            List<String> dataLines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null && dataLines.size() < getMaxRowsToValidate()) {
                dataLines.add(stripCr(line));
            }

            Map<String, Integer> tradeCodeCounts = countTradeCodes(dataLines);

            int rowNumber = 1;
            for (String dataLine : dataLines) {
                rowNumber++;
                if (dataLine.isBlank()) {
                    continue;
                }
                List<String> fields = parseLine(dataLine);
                if (fields.size() != EXPECTED_COLUMN_COUNT) {
                    result.addError(new CsvValidationError(rowNumber, "取引コード",
                            "取引コード「" + fields.get(IDX_TRADE_CODE).trim() + "」: 列数が不正です。"
                            + "期待: " + EXPECTED_COLUMN_COUNT + "列、実際: " + fields.size() + "列"));
                    continue;
                }
                validateDataRow(result, rowNumber, fields, tradeCodeCounts);
            }
            result.setTotalRowCount(rowNumber - 1);
        }
        return result;
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
            String tradeCode = fields.get(IDX_TRADE_CODE).trim();
            if (!tradeCode.isEmpty()) {
                counts.merge(tradeCode, 1, Integer::sum);
            }
        }
        return counts;
    }

    private void validateDataRow(
            CsvValidationResult result, int rowNumber, List<String> fields,
            Map<String, Integer> tradeCodeCounts) {
        String tradeCode = fields.get(IDX_TRADE_CODE).trim();
        if (tradeCode.isEmpty()) {
            result.addError(new CsvValidationError(rowNumber, "取引コード", "取引コードは必須です。"));
            return;
        }
        if (tradeCodeCounts.getOrDefault(tradeCode, 0) > 1) {
            result.addError(new CsvValidationError(rowNumber, "取引コード",
                    "取引コード「" + tradeCode + "」がCSV内で重複しています。"));
        }
    }

    private String stripCr(String line) {
        if (line != null && line.endsWith("\r")) {
            return line.substring(0, line.length() - 1);
        }
        return line;
    }

}
