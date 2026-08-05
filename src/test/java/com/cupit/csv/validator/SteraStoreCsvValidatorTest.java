package com.cupit.csv.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.cupit.csv.CsvValidationResult;

/**
 * {@link SteraStoreCsvValidator} のテスト。30列固定チェック、取引コード必須チェック、
 * m_stera_storeのNOT NULL制約に対応する必須項目チェック、列数不正行の検出、
 * 取引コード重複の検出を検証する。
 */
class SteraStoreCsvValidatorTest {

    private static final int COLUMN_COUNT = 30;

    private static final Set<Integer> REQUIRED_INDEXES = Set.of(
            1, 2, 6, 8, 9, 10, 11, 12, 13, 14, 15, 18, 19, 20, 21, 22, 23, 24, 25, 27);

    private static final int[] MAX_LENGTHS = {
        10, 10, 8, 6, 13, 6, 9, 10, 50, 80,
        80, 7, 100, 150, 20, 100, 0, 0, 30, 4,
        20, 3, 4, 7, 80, 1, 0, 1, 0, 0,
    };

    private final SteraStoreCsvValidator validator = new SteraStoreCsvValidator();

    @Test
    void rejectsNonCsvExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "stera_store.txt", "text/plain", new byte[0]);

        CsvValidationResult result = validator.validate(file);

        assertThat(result.isFatal()).isTrue();
        assertThat(result.getErrors().get(0).getMessage()).contains("拡張子");
    }

    @Test
    void rejectsEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "stera_store.csv", "text/csv", new byte[0]);

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
        MockMultipartFile file = csvFile(header(), validRow("01-001"), validRow("01-002"));

        CsvValidationResult result = validator.validate(file);

        assertThat(result.isFatal()).isFalse();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getTotalRowCount()).isEqualTo(2);
    }

    @Test
    void requiresTradeCode() throws Exception {
        String[] fields = validFields("");
        MockMultipartFile file = csvFile(header(), String.join(",", fields));

        CsvValidationResult result = validator.validate(file);

        assertThat(result.isFatal()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.getMessage().contains("取引コードは必須です"));
    }

    @Test
    void reportsColumnCountErrorForDataRow() throws Exception {
        MockMultipartFile file = csvFile(header(), "01-001,項目1だけ");

        CsvValidationResult result = validator.validate(file);

        assertThat(result.isFatal()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.getMessage().contains("列数が不正です"));
    }

    @Test
    void detectsDuplicateTradeCodeInCsv() throws Exception {
        MockMultipartFile file = csvFile(header(), validRow("01-001"), validRow("01-001"));

        CsvValidationResult result = validator.validate(file);

        assertThat(result.getErrors()).anyMatch(e -> e.getMessage().contains("重複しています"));
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

    @Test
    void reportsErrorForEachFieldExceedingMaxLength() throws Exception {
        for (int index = 0; index < COLUMN_COUNT; index++) {
            int maxLength = MAX_LENGTHS[index];
            if (maxLength == 0) {
                continue;
            }
            String[] fields = validFields("01-001");
            fields[index] = "あ".repeat(maxLength + 1);
            MockMultipartFile file = csvFile(header(), String.join(",", fields));

            CsvValidationResult result = validator.validate(file);

            assertThat(result.getErrors())
                    .as("index %d should enforce max length %d", index, maxLength)
                    .anyMatch(e -> e.getMessage().contains("文字以内で入力してください"));
        }
    }

    private String[] validFields(String tradeCode) {
        String[] f = new String[COLUMN_COUNT];
        Arrays.fill(f, "");
        f[0] = tradeCode;
        f[1] = "Suica";
        f[2] = "12345678";
        f[6] = "01-001000";
        f[8] = "テスト店舗";
        f[9] = "テストテンポ";
        f[10] = "TEST STORE";
        f[11] = "1000001";
        f[12] = "東京都";
        f[13] = "トウキョウト";
        f[14] = "03-1234-5678";
        f[15] = "test@example.com";
        f[18] = "テスト銀行";
        f[19] = "0001";
        f[20] = "テスト支店";
        f[21] = "001";
        f[22] = "1:普通";
        f[23] = "1234567";
        f[24] = "テスト";
        f[25] = "済";
        f[27] = "未";
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
                "file", "stera_store.csv", "text/csv",
                sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String header() {
        String[] cols = new String[COLUMN_COUNT];
        Arrays.fill(cols, "");
        cols[0] = "取引コード";
        return String.join(",", cols);
    }

}
