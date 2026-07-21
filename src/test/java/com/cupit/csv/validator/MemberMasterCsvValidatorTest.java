package com.cupit.csv.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.cupit.csv.CsvValidationResult;

/**
 * {@link MemberMasterCsvValidator} のテスト。255列固定チェック、必須項目チェック、
 * CSV内取引コード重複チェック、決済会社フォーマットチェック（{@link PaymentCompanyFormatChecker}）
 * との連携を検証する。
 */
class MemberMasterCsvValidatorTest {

    private static final int COLUMN_COUNT = 255;
    private static final int IDX_STORE_NAME = 13;
    private static final int IDX_STORE_NAME_KANA = 14;
    private static final int IDX_ADDR_ZIP = 20;
    private static final int IDX_ADDR_PREF = 21;
    private static final int IDX_ADDR_PREF_KANA = 22;
    private static final int IDX_ADDR_CITY = 23;
    private static final int IDX_ADDR_CITY_KANA = 24;
    private static final int IDX_ADDR_TOWN = 25;
    private static final int IDX_ADDR_TOWN_KANA = 26;
    private static final int IDX_ADDR_BLOCK = 27;
    private static final int IDX_ADDR_BLOCK_KANA = 28;
    private static final int IDX_ADDR_TEL = 31;
    private static final int IDX_CORP_ZIP = 62;
    private static final int IDX_CORP_PREF = 63;
    private static final int IDX_CORP_PREF_KANA = 64;
    private static final int IDX_CORP_CITY = 65;
    private static final int IDX_CORP_CITY_KANA = 66;
    private static final int IDX_CORP_TOWN = 67;
    private static final int IDX_CORP_TOWN_KANA = 68;
    private static final int IDX_CORP_BLOCK = 69;
    private static final int IDX_CORP_BLOCK_KANA = 70;
    private static final int IDX_HANDLING_ITEMS = 41;
    private static final int IDX_REP_LAST_NAME_KANA = 73;
    private static final int IDX_REP_FIRST_NAME_KANA = 74;
    private static final int IDX_REP_LAST_NAME = 75;
    private static final int IDX_REP_FIRST_NAME = 76;
    private static final int IDX_REP_BIRTH = 77;

    private final MemberMasterCsvValidator validator =
            new MemberMasterCsvValidator(new PaymentCompanyFormatChecker());

    @Test
    void rejectsNonCsvExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "member_master.txt", "text/plain", new byte[0]);

        CsvValidationResult result = validator.validate(file);

        assertThat(result.isFatal()).isTrue();
        assertThat(result.getErrors().get(0).getMessage()).contains("拡張子");
    }

    @Test
    void rejectsEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "member_master.csv", "text/csv", new byte[0]);

        CsvValidationResult result = validator.validate(file);

        assertThat(result.isFatal()).isTrue();
        assertThat(result.getErrors().get(0).getMessage()).contains("空です");
    }

    @Test
    void rejectsHeaderWithWrongColumnCount() throws Exception {
        MockMultipartFile file = csvFile("取引コード,店名");

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
        MockMultipartFile file = csvFile(header(), "01-001,店名だけ");

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

    @Test
    void detectsPaymentCompanyRequiredFieldMissing() throws Exception {
        MockMultipartFile file = csvFile(header(), rowMissingAddrZip("01-001"));

        CsvValidationResult result = validator.validate(file);

        assertThat(result.isFatal()).isFalse();
        assertThat(result.getErrors()).anyMatch(
                e -> e.getMessage().contains("所在地郵便番号") && e.getMessage().contains("必須"));
    }

    private MockMultipartFile csvFile(String... lines) {
        String content = "﻿" + String.join("\r\n", lines) + "\r\n";
        return new MockMultipartFile(
                "file", "member_master.csv", "text/csv",
                content.getBytes(StandardCharsets.UTF_8));
    }

    private String header() {
        String[] cols = new String[COLUMN_COUNT];
        Arrays.fill(cols, "");
        cols[0] = "取引コード";
        return String.join(",", cols);
    }

    /**
     * 決済会社フォーマットチェックの必須項目をあらかじめ満たしたデータ行を作る。
     * trade_code重複・列数不正など他の観点の検証がpayment company側のエラーで
     * 埋もれないようにするため。
     */
    private String row(String tradeCode) {
        String[] cols = baselineColumns();
        cols[0] = tradeCode;
        return String.join(",", cols);
    }

    private String rowMissingAddrZip(String tradeCode) {
        String[] cols = baselineColumns();
        cols[0] = tradeCode;
        cols[IDX_ADDR_ZIP] = "";
        return String.join(",", cols);
    }

    private String[] baselineColumns() {
        String[] cols = new String[COLUMN_COUNT];
        Arrays.fill(cols, "");
        cols[IDX_STORE_NAME] = "赤坂生花店";
        cols[IDX_STORE_NAME_KANA] = "アカサカセイカテン";
        cols[IDX_ADDR_ZIP] = "0700032";
        cols[IDX_ADDR_PREF] = "北海道";
        cols[IDX_ADDR_PREF_KANA] = "ホッカイドウ";
        cols[IDX_ADDR_CITY] = "旭川市";
        cols[IDX_ADDR_CITY_KANA] = "アサヒカワシ";
        cols[IDX_ADDR_TOWN] = "二条通";
        cols[IDX_ADDR_TOWN_KANA] = "ニジョウドオリ";
        cols[IDX_ADDR_BLOCK] = "6丁目右6号";
        cols[IDX_ADDR_BLOCK_KANA] = "ロクチョウメミギロクゴウ";
        cols[IDX_ADDR_TEL] = "0166-22-4276";
        cols[IDX_CORP_ZIP] = "0700032";
        cols[IDX_CORP_PREF] = "北海道";
        cols[IDX_CORP_PREF_KANA] = "ホッカイドウ";
        cols[IDX_CORP_CITY] = "旭川市";
        cols[IDX_CORP_CITY_KANA] = "アサヒカワシ";
        cols[IDX_CORP_TOWN] = "二条通";
        cols[IDX_CORP_TOWN_KANA] = "ニジョウドオリ";
        cols[IDX_CORP_BLOCK] = "6丁目右6号";
        cols[IDX_CORP_BLOCK_KANA] = "ロクチョウメミギロクゴウ";
        cols[IDX_REP_LAST_NAME] = "田中";
        cols[IDX_REP_FIRST_NAME] = "一郎";
        cols[IDX_REP_LAST_NAME_KANA] = "タナカ";
        cols[IDX_REP_FIRST_NAME_KANA] = "イチロウ";
        cols[IDX_REP_BIRTH] = "19000101";
        cols[IDX_HANDLING_ITEMS] = "慶弔葉鉢";
        return cols;
    }
}
