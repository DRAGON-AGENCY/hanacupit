package com.cupit.csv.validator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.csv.CsvValidationResult;

/**
 * CSVフォーマット検証の共通ロジック（CSVパース・エラー追加）を提供する抽象クラス。
 */
public abstract class AbstractCsvFormatValidator implements CsvFormatValidator {

    private static final int MAX_ROWS_TO_VALIDATE = 10_000;

    protected int getMaxRowsToValidate() {
        return MAX_ROWS_TO_VALIDATE;
    }

    /**
     * ファイルの先頭バイトでエンコーディングを判定する。
     * UTF-8 BOM（EF BB BF）→ UTF-8
     * UTF-16 BOM（FF FE / FE FF）→ IllegalArgumentException（非対応）
     * BOM なし → MS932（Shift-JIS）
     */
    protected Charset detectCharset(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream()) {
            byte[] bom = new byte[3];
            int read = is.read(bom);
            if (read >= 3
                    && (bom[0] & 0xFF) == 0xEF
                    && (bom[1] & 0xFF) == 0xBB
                    && (bom[2] & 0xFF) == 0xBF) {
                return StandardCharsets.UTF_8;
            }
            if (read >= 2
                    && (((bom[0] & 0xFF) == 0xFF && (bom[1] & 0xFF) == 0xFE)
                        || ((bom[0] & 0xFF) == 0xFE && (bom[1] & 0xFF) == 0xFF))) {
                throw new IllegalArgumentException(
                        "ファイルの文字コードがサポートされていません（UTF-16）。"
                        + "UTF-8（BOM付き）またはShift-JIS（MS932）で保存し直してください。");
            }
        }
        return Charset.forName("MS932");
    }

    /**
     * 1行のCSV文字列をフィールドのリストに分解する。
     * ダブルクォートで囲まれたフィールドと、クォート内のカンマ・改行を正しく処理する。
     */
    protected List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        int i = 0;
        while (i <= line.length()) {
            if (i < line.length() && line.charAt(i) == '"') {
                i++;
                StringBuilder sb = new StringBuilder();
                while (i < line.length()) {
                    char c = line.charAt(i);
                    if (c == '"') {
                        if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                            sb.append('"');
                            i += 2;
                        } else {
                            i++;
                            break;
                        }
                    } else {
                        sb.append(c);
                        i++;
                    }
                }
                fields.add(sb.toString());
                if (i < line.length() && line.charAt(i) == ',') {
                    i++;
                } else {
                    break;
                }
            } else {
                int start = i;
                while (i < line.length() && line.charAt(i) != ',') {
                    i++;
                }
                fields.add(line.substring(start, i));
                if (i < line.length()) {
                    i++;
                } else {
                    break;
                }
            }
        }
        return fields;
    }

    /**
     * 文字列が整数（正・ゼロ・負）として解釈できるか検査する。
     */
    protected boolean isInteger(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            Long.parseLong(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 文字列が YYYYMMDD 形式の日付として妥当かを検査する（数値8桁のみ）。
     */
    protected boolean isYyyymmdd(String value) {
        return value != null && value.trim().matches("\\d{8}");
    }

    /**
     * 文字列が YYYY-MM-DD 形式の日付として妥当かを検査する。
     */
    protected boolean isYyyyMmDd(String value) {
        return value != null && value.trim().matches("\\d{4}-\\d{2}-\\d{2}");
    }

    /**
     * ファイルの拡張子（ドット以降、小文字）を取得する。
     * 例：「sample.csv.csv」→「csv」
     */
    protected String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    protected void addColumnCountError(
            CsvValidationResult result, int rowNumber, int expected, int actual) {
        result.addError(new CsvValidationError(
                rowNumber,
                "",
                "列数が不正です。期待: " + expected + "列、実際: " + actual + "列"));
    }

    protected void addNumericError(
            CsvValidationResult result, int rowNumber, String columnName, String value) {
        result.addError(new CsvValidationError(
                rowNumber,
                columnName,
                "数値ではありません: 「" + value + "」"));
    }

    protected void addDateFormatError(
            CsvValidationResult result, int rowNumber, String columnName,
            String format, String value) {
        result.addError(new CsvValidationError(
                rowNumber,
                columnName,
                "日付形式が不正です（" + format + "）: 「" + value + "」"));
    }
}
