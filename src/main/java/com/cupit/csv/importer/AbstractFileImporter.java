package com.cupit.csv.importer;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;

/**
 * ファイルインポートの共通ユーティリティを提供する基底クラス。
 */
public abstract class AbstractFileImporter implements FileImporter {

    private static final DateTimeFormatter FMT_YYYYMMDD   = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter FMT_YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FMT_YYYY_SLASH_MM_SLASH_DD =
            DateTimeFormatter.ofPattern("yyyy/MM/dd");

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

    protected List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        int i = 0;
        while (i <= line.length()) {
            if (i < line.length() && line.charAt(i) == '"') {
                i++;
                StringBuilder sb = new StringBuilder();
                while (i < line.length()) {
                    if (line.charAt(i) == '"') {
                        if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                            sb.append('"');
                            i += 2;
                        } else {
                            i++;
                            break;
                        }
                    } else {
                        sb.append(line.charAt(i++));
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

    protected String stripCr(String line) {
        if (line != null && line.endsWith("\r")) {
            return line.substring(0, line.length() - 1);
        }
        return line;
    }

    // ──────────────────────────────────────────────────────
    // エラー収集なし（内部ユーティリティ用）
    // ──────────────────────────────────────────────────────

    protected LocalDate parseDate8(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(s.trim(), FMT_YYYYMMDD);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    protected LocalDate parseDateHyphen(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(s.trim(), FMT_YYYY_MM_DD);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    protected int parseIntOrZero(String s) {
        if (s == null || s.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    protected long parseLongOrZero(String s) {
        if (s == null || s.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    protected BigDecimal parseDecimalOrZero(String s) {
        if (s == null || s.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(s.trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    protected String trim(String s) {
        return s == null ? null : s.trim();
    }

    // ──────────────────────────────────────────────────────
    // エラー収集あり（データ検証用）
    // ──────────────────────────────────────────────────────

    protected int parseIntChecked(
            String s, int rowNum, String colName, List<CsvValidationError> errors) {
        if (s == null || s.isBlank()) {
            return 0;
        }
        String v = s.trim();
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            addError(errors, rowNum, colName, "数値変換エラー。値: 「" + v + "」");
            return 0;
        }
    }

    protected BigDecimal parseDecimalChecked(
            String s, int rowNum, String colName, List<CsvValidationError> errors) {
        if (s == null || s.isBlank()) {
            return BigDecimal.ZERO;
        }
        String v = s.trim();
        try {
            return new BigDecimal(v);
        } catch (NumberFormatException e) {
            addError(errors, rowNum, colName, "小数変換エラー。値: 「" + v + "」");
            return BigDecimal.ZERO;
        }
    }

    protected LocalDate parseDate8Checked(
            String s, int rowNum, String colName, List<CsvValidationError> errors) {
        if (s == null || s.isBlank()) {
            return null;
        }
        String v = s.trim();
        try {
            return LocalDate.parse(v, FMT_YYYYMMDD);
        } catch (DateTimeParseException e) {
            addError(errors, rowNum, colName,
                    "日付変換エラー（YYYYMMDD形式）。値: 「" + v + "」");
            return null;
        }
    }

    protected LocalDate parseDateHyphenChecked(
            String s, int rowNum, String colName, List<CsvValidationError> errors) {
        if (s == null || s.isBlank()) {
            return null;
        }
        String v = s.trim();
        try {
            return LocalDate.parse(v, FMT_YYYY_MM_DD);
        } catch (DateTimeParseException e) {
            addError(errors, rowNum, colName,
                    "日付変換エラー（YYYY-MM-DD形式）。値: 「" + v + "」");
            return null;
        }
    }

    // ──────────────────────────────────────────────────────
    // エラー収集あり（NULL許容列用、blankはnullを返す）
    // ──────────────────────────────────────────────────────

    protected Short parseShortChecked(
            String s, int rowNum, String colName, List<CsvValidationError> errors) {
        if (s == null || s.isBlank()) {
            return null;
        }
        String v = s.trim();
        try {
            return Short.parseShort(v);
        } catch (NumberFormatException e) {
            addError(errors, rowNum, colName, "数値変換エラー。値: 「" + v + "」");
            return null;
        }
    }

    protected Integer parseIntegerChecked(
            String s, int rowNum, String colName, List<CsvValidationError> errors) {
        if (s == null || s.isBlank()) {
            return null;
        }
        String v = s.trim();
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            addError(errors, rowNum, colName, "数値変換エラー。値: 「" + v + "」");
            return null;
        }
    }

    protected Long parseLongChecked(
            String s, int rowNum, String colName, List<CsvValidationError> errors) {
        if (s == null || s.isBlank()) {
            return null;
        }
        String v = s.trim();
        try {
            return Long.parseLong(v);
        } catch (NumberFormatException e) {
            addError(errors, rowNum, colName, "数値変換エラー。値: 「" + v + "」");
            return null;
        }
    }

    protected BigDecimal parseBigDecimalChecked(
            String s, int rowNum, String colName, List<CsvValidationError> errors) {
        if (s == null || s.isBlank()) {
            return null;
        }
        String v = s.trim();
        try {
            return new BigDecimal(v);
        } catch (NumberFormatException e) {
            addError(errors, rowNum, colName, "小数変換エラー。値: 「" + v + "」");
            return null;
        }
    }

    protected LocalDate parseDateSlashChecked(
            String s, int rowNum, String colName, List<CsvValidationError> errors) {
        if (s == null || s.isBlank()) {
            return null;
        }
        String v = s.trim();
        try {
            return LocalDate.parse(v, FMT_YYYY_SLASH_MM_SLASH_DD);
        } catch (DateTimeParseException e) {
            addError(errors, rowNum, colName,
                    "日付変換エラー（YYYY/MM/DD形式）。値: 「" + v + "」");
            return null;
        }
    }

    private void addError(List<CsvValidationError> errors, int rowNum, String colName, String msg) {
        errors.add(new CsvValidationError(rowNum, colName, msg));
    }
}
