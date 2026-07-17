package com.cupit.csv.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.cupit.csv.CsvValidationResult;

/**
 * {@link ShopDataCsvValidator} のテスト。27列固定チェック、取引コード必須チェック、
 * 列数不正行の検出、取引コード重複の検出を検証する。
 * 取引コード以外の全項目は任意のため、それらのチェックは行わない。
 */
class ShopDataCsvValidatorTest {

    private static final int COLUMN_COUNT = 27;

    private final ShopDataCsvValidator validator = new ShopDataCsvValidator();

    @Test
    void rejectsNonCsvExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "shop_data.txt", "text/plain", new byte[0]);

        CsvValidationResult result = validator.validate(file);

        assertThat(result.isFatal()).isTrue();
        assertThat(result.getErrors().get(0).getMessage()).contains("拡張子");
    }

    @Test
    void rejectsEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "shop_data.csv", "text/csv", new byte[0]);

        CsvValidationResult result = validator.validate(file);

        assertThat(result.isFatal()).isTrue();
        assertThat(result.getErrors().get(0).getMessage()).contains("空です");
    }

    @Test
    void rejectsHeaderWithWrongColumnCount() throws Exception {
        MockMultipartFile file = csvFile("取引コード,項目1");

        CsvValidationResult result = validator.validate(file);

        assertThat(result.isFatal()).isTrue();
        assertThat(result.getErrors().get(0).getMessage()).contains("列数が不正");
    }

    @Test
    void acceptsValidFileWithNoErrors() throws Exception {
        MockMultipartFile file = csvFile(header(), row("01-001"), row("01-002"));

        CsvValidationResult result = validator.validate(file);

        assertThat(result.isFatal()).isFalse();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getTotalRowCount()).isEqualTo(2);
    }

    @Test
    void requiresTradeCode() throws Exception {
        MockMultipartFile file = csvFile(header(), row(""));

        CsvValidationResult result = validator.validate(file);

        assertThat(result.isFatal()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getMessage()).contains("取引コードは必須です");
    }

    @Test
    void reportsColumnCountErrorForDataRow() throws Exception {
        MockMultipartFile file = csvFile(header(), "01-001,項目1だけ");

        CsvValidationResult result = validator.validate(file);

        assertThat(result.isFatal()).isFalse();
        assertThat(result.getErrors().get(0).getMessage()).contains("列数が不正です");
    }

    @Test
    void detectsDuplicateTradeCodeWithinFile() throws Exception {
        MockMultipartFile file = csvFile(header(), row("01-001"), row("01-001"));

        CsvValidationResult result = validator.validate(file);

        assertThat(result.isFatal()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getMessage()).contains("重複しています");
    }

    private MockMultipartFile csvFile(String... lines) {
        String content = "\uFEFF" + String.join("\r\n", lines) + "\r\n";
        return new MockMultipartFile(
                "file", "shop_data.csv", "text/csv",
                content.getBytes(StandardCharsets.UTF_8));
    }

    private String header() {
        String[] cols = new String[COLUMN_COUNT];
        Arrays.fill(cols, "");
        cols[0] = "取引コード";
        return String.join(",", cols);
    }

    private String row(String tradeCode) {
        String[] cols = new String[COLUMN_COUNT];
        Arrays.fill(cols, "");
        cols[0] = tradeCode;
        return String.join(",", cols);
    }

}
