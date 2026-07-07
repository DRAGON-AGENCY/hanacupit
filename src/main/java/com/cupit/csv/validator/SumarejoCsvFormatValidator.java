package com.cupit.csv.validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.csv.CsvValidationResult;

/**
 * スマレジ（端末月額）請求明細CSV のフォーマットを検証するクラス。
 * 文字コード：UTF-8 BOM付きは自動検出、なければMS932。区切り文字：カンマ、囲み文字：ダブルクォート、ヘッダー行：あり（1行目）
 * 注意：ファイル拡張子が「.csv.csv」と二重になる場合がある。
 * ヘッダー行の列名はチェックしない（列名の表記はファイルの作成元・作成時期によって
 * 変わり得るため、列数のみを検証対象とする）。EXPECTED_HEADERSはエラーメッセージの
 * 列名ラベルとしてのみ使用する。
 */
public class SumarejoCsvFormatValidator extends AbstractCsvFormatValidator {

    private static final int EXPECTED_COLUMN_COUNT = 24;

    private static final String[] EXPECTED_HEADERS = {
        "会社名", "請求対象月", "請求No", "発行日", "加盟店名", "端末識別番号", "単価",
        "数量（クレジット）", "数量（QRコード決済）", "数量（電子マネー・交通系）",
        "数量（電子マネー・ID）", "数量（電子マネー・WAON）", "数量（電子マネー・nanaco）",
        "数量（電子マネー・楽天Edy）", "数量（電子マネー・QUICPay）", "数量（SIM）",
        "ﾄﾗﾝｻﾞｸｼｮﾝ数（クレジット）", "ﾄﾗﾝｻﾞｸｼｮﾝ数（QRコード決済）",
        "ﾄﾗﾝｻﾞｸｼｮﾝ数（電子マネー）", "ﾄﾗﾝｻﾞｸｼｮﾝ数（合計）",
        "決済金額（クレジット）", "決済金額（QRコード決済）", "決済金額（電子マネー）", "決済金額（合計）"
    };

    private static final int[] NUMERIC_COLUMN_INDICES = {
        6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
        16, 17, 18, 19, 20, 21, 22, 23
    };

    @Override
    public CsvValidationResult validate(MultipartFile file) throws IOException {
        CsvValidationResult result = new CsvValidationResult();

        String ext = getExtension(file.getOriginalFilename());
        if (!"csv".equals(ext)) {
            result.addError(new CsvValidationError(
                    0, "", "ファイルの拡張子が不正です。期待: .csv（または .csv.csv）、実際: ." + ext));
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
                if (!isYyyyMmDd(fields.get(3))) {
                    addDateFormatError(result, rowNumber, EXPECTED_HEADERS[3], "YYYY-MM-DD", fields.get(3).trim());
                }
                for (int idx : NUMERIC_COLUMN_INDICES) {
                    String value = fields.get(idx).trim();
                    if (!isInteger(value)) {
                        addNumericError(result, rowNumber, EXPECTED_HEADERS[idx], value);
                    }
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
