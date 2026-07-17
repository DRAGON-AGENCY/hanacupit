package com.cupit.csv.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.cupit.csv.CsvValidationResult;

/**
 * {@link ApplicationFormCsvValidator} のテスト。230列固定チェック、取引コード
 * （4列目、JFTD取引コード）必須チェック、CSV内取引コード重複の検出を検証する。
 * 取引コード以外の全項目は任意とする。
 */
class ApplicationFormCsvValidatorTest {

    private static final int COLUMN_COUNT = 230;
    private static final int IDX_TRADE_CODE = 3;

    private final ApplicationFormCsvValidator validator = new ApplicationFormCsvValidator();

    @Test
    void rejectsNonCsvExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "application_form_input.txt", "text/plain", new byte[0]);

        CsvValidationResult result = validator.validate(file);

        assertThat(result.isFatal()).isTrue();
        assertThat(result.getErrors().get(0).getMessage()).contains("拡張子");
    }

    @Test
    void rejectsEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "application_form_input.csv", "text/csv", new byte[0]);

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
        MockMultipartFile file = csvFile(header(), row("35-232"), row("35-233"));

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
        MockMultipartFile file = csvFile(header(), "a,b,c,35-232");

        CsvValidationResult result = validator.validate(file);

        assertThat(result.isFatal()).isFalse();
        assertThat(result.getErrors().get(0).getMessage()).contains("列数が不正です");
    }

    @Test
    void detectsDuplicateTradeCodeWithinFile() throws Exception {
        MockMultipartFile file = csvFile(header(), row("35-232"), row("35-232"));

        CsvValidationResult result = validator.validate(file);

        assertThat(result.isFatal()).isFalse();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getMessage()).contains("重複しています");
    }

    private MockMultipartFile csvFile(String... lines) {
        String content = "﻿" + String.join("\r\n", lines) + "\r\n";
        return new MockMultipartFile(
                "file", "application_form_input.csv", "text/csv",
                content.getBytes(StandardCharsets.UTF_8));
    }

    private String header() {
        String[] cols = new String[COLUMN_COUNT];
        Arrays.fill(cols, "");
        cols[IDX_TRADE_CODE] = "JFTD取引コード";
        return String.join(",", cols);
    }

    private String row(String tradeCode) {
        String[] cols = new String[COLUMN_COUNT];
        Arrays.fill(cols, "");
        cols[IDX_TRADE_CODE] = tradeCode;
        return String.join(",", cols);
    }

}
