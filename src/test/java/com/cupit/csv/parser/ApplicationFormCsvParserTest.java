package com.cupit.csv.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.cupit.model.ApplicationFormInput;

/**
 * {@link ApplicationFormCsvParser} のテスト。230列パース、取引コード必須チェック、
 * CSV内取引コード重複行のスキップ、列数不正行のスキップを検証する。
 * DBへの永続化は行わないため、解析結果は{@link ApplicationFormInput}のリストとして
 * 直接検証する。
 */
class ApplicationFormCsvParserTest {

    private static final int COLUMN_COUNT = 230;
    private static final int IDX_TRADE_CODE = 3;
    private static final int IDX_STORE_NAME = 6;

    private final ApplicationFormCsvParser parser = new ApplicationFormCsvParser();

    @Test
    void parsesRowWithTradeCodeAndStoreName() throws Exception {
        MockMultipartFile file = csvFile(rowWithField(IDX_STORE_NAME, "フラワーショップやざき"));

        ApplicationFormCsvParser.ParseResult result = parser.parse(file);

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getRecords()).hasSize(1);
        ApplicationFormInput input = result.getRecords().get(0);
        assertThat(input.getTradeCode()).isEqualTo("35-232");
        assertThat(input.getStoreName()).isEqualTo("フラワーショップやざき");
    }

    @Test
    void skipsRowMissingTradeCode() throws Exception {
        MockMultipartFile file = csvFile(row(""));

        ApplicationFormCsvParser.ParseResult result = parser.parse(file);

        assertThat(result.getRecords()).isEmpty();
        assertThat(result.getErrors()).anyMatch(e -> e.getMessage().contains("取引コードは必須です"));
    }

    @Test
    void skipsDuplicateTradeCodeInCsv() throws Exception {
        MockMultipartFile file = csvFile(row("35-232"), row("35-232"));

        ApplicationFormCsvParser.ParseResult result = parser.parse(file);

        // どちらの行が正しいか判断できないため、先着1件目も含めて両方スキップする
        assertThat(result.getRecords()).isEmpty();
        assertThat(result.getErrors()).hasSize(2);
        assertThat(result.getErrors()).allMatch(e -> e.getMessage().contains("重複しています"));
    }

    @Test
    void keepsOtherNonDuplicateRowsWhenAnotherTradeCodeIsDuplicated() throws Exception {
        MockMultipartFile file = csvFile(row("35-232"), row("35-232"), row("35-233"));

        ApplicationFormCsvParser.ParseResult result = parser.parse(file);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getTradeCode()).isEqualTo("35-233");
        assertThat(result.getErrors()).hasSize(2);
    }

    @Test
    void skipsRowWithWrongColumnCount() throws Exception {
        MockMultipartFile file = csvFile("a,b,c,35-232");

        ApplicationFormCsvParser.ParseResult result = parser.parse(file);

        assertThat(result.getRecords()).isEmpty();
        assertThat(result.getErrors()).anyMatch(e -> e.getMessage().contains("列数が不正です"));
    }

    @Test
    void skipsRowWithInvalidDateValue() throws Exception {
        MockMultipartFile file = csvFile(rowWithField(75, "not-a-date"));

        ApplicationFormCsvParser.ParseResult result = parser.parse(file);

        assertThat(result.getRecords()).isEmpty();
        assertThat(result.getErrors()).anyMatch(e -> e.getMessage().contains("日付変換エラー"));
    }

    @Test
    void skipsRowWithInvalidIntegerValue() throws Exception {
        MockMultipartFile file = csvFile(rowWithField(142, "abc"));

        ApplicationFormCsvParser.ParseResult result = parser.parse(file);

        assertThat(result.getRecords()).isEmpty();
        assertThat(result.getErrors()).anyMatch(e -> e.getMessage().contains("数値変換エラー"));
    }

    @Test
    void skipsRowWithInvalidDecimalValue() throws Exception {
        MockMultipartFile file = csvFile(rowWithField(110, "abc"));

        ApplicationFormCsvParser.ParseResult result = parser.parse(file);

        assertThat(result.getRecords()).isEmpty();
        assertThat(result.getErrors()).anyMatch(e -> e.getMessage().contains("小数変換エラー"));
    }

    @Test
    void skipsBlankLineWithoutCountingItAsError() throws Exception {
        StringBuilder sb = new StringBuilder("﻿").append(header()).append("\r\n");
        sb.append(row("35-232")).append("\r\n");
        sb.append("\r\n");
        sb.append(row("35-233")).append("\r\n");
        MockMultipartFile file = new MockMultipartFile(
                "file", "application_form_input.csv", "text/csv",
                sb.toString().getBytes(StandardCharsets.UTF_8));

        ApplicationFormCsvParser.ParseResult result = parser.parse(file);

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getRecords()).hasSize(2);
    }

    private MockMultipartFile csvFile(String... dataLines) {
        StringBuilder sb = new StringBuilder("﻿").append(header()).append("\r\n");
        for (String line : dataLines) {
            sb.append(line).append("\r\n");
        }
        return new MockMultipartFile(
                "file", "application_form_input.csv", "text/csv",
                sb.toString().getBytes(StandardCharsets.UTF_8));
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

    private String rowWithField(int index, String value) {
        String[] cols = new String[COLUMN_COUNT];
        Arrays.fill(cols, "");
        cols[IDX_TRADE_CODE] = "35-232";
        cols[index] = value;
        return String.join(",", cols);
    }

}
