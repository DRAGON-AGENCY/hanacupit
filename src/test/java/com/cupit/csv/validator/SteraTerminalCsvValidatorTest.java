package com.cupit.csv.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.cupit.csv.CsvValidationResult;

/**
 * {@link SteraTerminalCsvValidator} のテスト。7列固定チェック、取引コード必須チェック、
 * m_stera_terminalのNOT NULL制約に対応する必須項目チェック、列数不正行の検出、
 * 取引コード重複の許容（洗い替え運用のためエラーとしない）を検証する。
 */
class SteraTerminalCsvValidatorTest {

    private static final int COLUMN_COUNT = 7;
    private static final Set<Integer> REQUIRED_INDEXES = Set.of(1, 3, 4, 5);

    private final SteraTerminalCsvValidator validator = new SteraTerminalCsvValidator();

    @Test
    void rejectsNonCsvExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "stera_terminal.txt", "text/plain", new byte[0]);

        CsvValidationResult result = validator.validate(file);

        assertThat(result.isFatal()).isTrue();
        assertThat(result.getErrors().get(0).getMessage()).contains("拡張子");
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
        MockMultipartFile file = csvFile(header(), validRow("01-001"), validRow("01-002"));

        CsvValidationResult result = validator.validate(file);

        assertThat(result.isFatal()).isFalse();
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void allowsDuplicateTradeCodeInCsv() throws Exception {
        MockMultipartFile file = csvFile(header(), validRow("01-001"), validRow("01-001"));

        CsvValidationResult result = validator.validate(file);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void requiresTradeCode() throws Exception {
        String[] fields = validFields("");
        MockMultipartFile file = csvFile(header(), String.join(",", fields));

        CsvValidationResult result = validator.validate(file);

        assertThat(result.getErrors()).anyMatch(e -> e.getMessage().contains("取引コードは必須です"));
    }

    @Test
    void reportsErrorForEachBlankRequiredField() throws Exception {
        for (int index : REQUIRED_INDEXES) {
            String[] fields = validFields("01-001");
            fields[index] = "";
            MockMultipartFile file = csvFile(header(), String.join(",", fields));

            CsvValidationResult result = validator.validate(file);

            assertThat(result.getErrors())
                    .as("index %d should be required", index)
                    .anyMatch(e -> e.getMessage().contains("は必須です"));
        }
    }

    private String[] validFields(String tradeCode) {
        String[] f = new String[COLUMN_COUNT];
        Arrays.fill(f, "");
        f[0] = tradeCode;
        f[1] = "TERM0000001";
        f[3] = "01-001000";
        f[4] = "利用中";
        f[5] = "2020/01/01";
        return f;
    }

    private String validRow(String tradeCode) {
        return String.join(",", validFields(tradeCode));
    }

    private MockMultipartFile csvFile(String... dataLines) {
        StringBuilder sb = new StringBuilder("﻿").append(dataLines[0]).append("\r\n");
        for (int i = 1; i < dataLines.length; i++) {
            sb.append(dataLines[i]).append("\r\n");
        }
        return new MockMultipartFile(
                "file", "stera_terminal.csv", "text/csv",
                sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String header() {
        String[] cols = new String[COLUMN_COUNT];
        Arrays.fill(cols, "");
        cols[0] = "取引コード";
        return String.join(",", cols);
    }

}
