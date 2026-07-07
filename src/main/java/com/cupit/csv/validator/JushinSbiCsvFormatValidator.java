package com.cupit.csv.validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.csv.CsvValidationResult;

/**
 * 住信SBI（VISA・MasterCard）精算データファイルのフォーマットを検証するクラス。
 * ファイル形式：DAT（CSV形式）、文字コード：UTF-8、囲み文字：ダブルクォート、ヘッダー行：なし。
 * レコード種別（第1フィールド）："1"=集計レコード（12列）、"2"=明細レコード（22列）
 */
public class JushinSbiCsvFormatValidator extends AbstractCsvFormatValidator {

    private static final int RECORD_TYPE_1_COLUMN_COUNT = 12;
    private static final int RECORD_TYPE_2_COLUMN_COUNT = 22;

    private static final int[] TYPE1_NUMERIC_INDICES = {8, 9, 10, 11};
    private static final int[] TYPE2_NUMERIC_INDICES = {
        10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21
    };

    @Override
    public CsvValidationResult validate(MultipartFile file) throws IOException {
        CsvValidationResult result = new CsvValidationResult();

        String ext = getExtension(file.getOriginalFilename());
        if (!"dat".equals(ext)) {
            result.addError(new CsvValidationError(
                    0, "", "ファイルの拡張子が不正です。期待: .dat、実際: ." + ext));
            result.markFatal();
            return result;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            int rowNumber = 0;
            String line;
            while ((line = reader.readLine()) != null && rowNumber <= getMaxRowsToValidate()) {
                rowNumber++;
                line = stripCarriageReturn(line);
                if (line.isBlank()) {
                    continue;
                }
                List<String> fields = parseLine(line);
                if (fields.isEmpty()) {
                    continue;
                }
                String recordType = fields.get(0).trim();
                if ("1".equals(recordType)) {
                    validateType1Record(result, rowNumber, fields);
                } else if ("2".equals(recordType)) {
                    validateType2Record(result, rowNumber, fields);
                } else {
                    result.addError(new CsvValidationError(
                            rowNumber, "レコード種別",
                            "レコード種別が不正です。\"1\"または\"2\"以外の値: 「" + recordType + "」"));
                }
            }
            result.setTotalRowCount(rowNumber);
        }
        return result;
    }

    private void validateType1Record(
            CsvValidationResult result, int rowNumber, List<String> fields) {
        if (fields.size() != RECORD_TYPE_1_COLUMN_COUNT) {
            addColumnCountError(result, rowNumber, RECORD_TYPE_1_COLUMN_COUNT, fields.size());
            return;
        }
        if (!isYyyymmdd(fields.get(1))) {
            addDateFormatError(result, rowNumber, "作成日", "YYYYMMDD", fields.get(1).trim());
        }
        if (!isYyyymmdd(fields.get(2))) {
            addDateFormatError(result, rowNumber, "売上計上日", "YYYYMMDD", fields.get(2).trim());
        }
        if (!isYyyymmdd(fields.get(7))) {
            addDateFormatError(result, rowNumber, "支払日", "YYYYMMDD", fields.get(7).trim());
        }
        for (int idx : TYPE1_NUMERIC_INDICES) {
            String value = fields.get(idx).trim();
            if (!isInteger(value)) {
                addNumericError(result, rowNumber, "列" + (idx + 1), value);
            }
        }
    }

    private void validateType2Record(
            CsvValidationResult result, int rowNumber, List<String> fields) {
        if (fields.size() != RECORD_TYPE_2_COLUMN_COUNT) {
            addColumnCountError(result, rowNumber, RECORD_TYPE_2_COLUMN_COUNT, fields.size());
            return;
        }
        if (!isYyyymmdd(fields.get(4))) {
            addDateFormatError(result, rowNumber, "売上日", "YYYYMMDD", fields.get(4).trim());
        }
        for (int idx : TYPE2_NUMERIC_INDICES) {
            String value = fields.get(idx).trim();
            if (!isInteger(value)) {
                addNumericError(result, rowNumber, "列" + (idx + 1), value);
            }
        }
    }

    private String stripCarriageReturn(String line) {
        if (line.endsWith("\r")) {
            return line.substring(0, line.length() - 1);
        }
        return line;
    }
}
