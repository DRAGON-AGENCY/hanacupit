package com.cupit.csv.validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.csv.CsvValidationResult;

/**
 * JCB 売上明細レポートCSV のフォーマットを検証するクラス。
 * 文字コード：UTF-8 BOM付きは自動検出、なければMS932。区切り文字：カンマ、囲み文字：なし、ヘッダー行：あり（1行目）。
 * ヘッダー行の列名はチェックしない（列名の表記はファイルの作成元・作成時期によって
 * 変わり得るため、列数のみを検証対象とする）。EXPECTED_HEADERSはエラーメッセージの
 * 列名ラベルとしてのみ使用する。
 */
public class JcbCsvFormatValidator extends AbstractCsvFormatValidator {

    private static final int EXPECTED_COLUMN_COUNT = 10;

    private static final String[] EXPECTED_HEADERS = {
        "加盟店名称", "加盟店番号", "ご契約カード会社", "お支払方法", "お取扱カード名",
        "支払区分", "売上方法", "集計日", "売上件数", "売上金額（円）"
    };

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
            headerLine = stripCarriageReturn(headerLine);
            List<String> headers = parseLine(headerLine);

            if (headers.size() != EXPECTED_COLUMN_COUNT) {
                addColumnCountError(result, 1, EXPECTED_COLUMN_COUNT, headers.size());
                result.markFatal();
                result.setTotalRowCount(0);
                return result;
            }

            int rowNumber = 1;
            String line;
            while ((line = reader.readLine()) != null && rowNumber <= getMaxRowsToValidate()) {
                rowNumber++;
                line = stripCarriageReturn(line);
                if (line.isBlank()) {
                    continue;
                }
                List<String> fields = parseLine(line);
                if (fields.size() != EXPECTED_COLUMN_COUNT) {
                    addColumnCountError(result, rowNumber, EXPECTED_COLUMN_COUNT, fields.size());
                    continue;
                }
                String salesCount = fields.get(8).trim();
                if (!isInteger(salesCount)) {
                    addNumericError(result, rowNumber, EXPECTED_HEADERS[8], salesCount);
                }
                String salesAmount = fields.get(9).trim();
                if (!isInteger(salesAmount)) {
                    addNumericError(result, rowNumber, EXPECTED_HEADERS[9], salesAmount);
                }
            }
            result.setTotalRowCount(rowNumber - 1);
        }
        return result;
    }

    private String stripCarriageReturn(String line) {
        if (line.endsWith("\r")) {
            return line.substring(0, line.length() - 1);
        }
        return line;
    }
}
