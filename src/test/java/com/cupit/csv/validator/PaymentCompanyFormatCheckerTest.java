package com.cupit.csv.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.cupit.csv.CsvValidationError;

/**
 * {@link PaymentCompanyFormatChecker} のテスト。決済会社（JCB・SMCC）の申込フォーマット
 * 要件のうち機械的に判定できるもの（必須・最大/固定文字数）の境界値を検証する。
 */
class PaymentCompanyFormatCheckerTest {

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
    private static final int IDX_ADDR_BUILDING = 29;
    private static final int IDX_ADDR_TEL = 31;
    private static final int IDX_CORP_NAME = 59;
    private static final int IDX_CORP_NAME_KANA = 61;
    private static final int IDX_HANDLING_ITEMS = 41;
    private static final int IDX_REP_LAST_NAME_KANA = 73;
    private static final int IDX_REP_FIRST_NAME_KANA = 74;
    private static final int IDX_REP_LAST_NAME = 75;
    private static final int IDX_REP_FIRST_NAME = 76;
    private static final int IDX_REP_BIRTH = 77;

    private final PaymentCompanyFormatChecker checker = new PaymentCompanyFormatChecker();

    @Test
    void baselineRowHasNoErrors() {
        List<CsvValidationError> errors = check(baseline());

        assertThat(errors).isEmpty();
    }

    @Test
    void requiresStoreName() {
        List<CsvValidationError> errors = check(withOverride(IDX_STORE_NAME, ""));

        assertThat(errors).anyMatch(
                e -> e.getColumnName().equals("店舗名") && e.getMessage().contains("必須"));
    }

    @Test
    void acceptsStoreNameAtMaxLength() {
        // 最も厳しいSMCC steracode仕様書の上限16桁ちょうど
        List<CsvValidationError> errors = check(withOverride(IDX_STORE_NAME, "一二三四五六七八九十一二三四五六"));

        assertThat(errors).isEmpty();
    }

    @Test
    void rejectsStoreNameOverMaxLength() {
        List<CsvValidationError> errors =
                check(withOverride(IDX_STORE_NAME, "一二三四五六七八九十一二三四五六七"));

        assertThat(errors).anyMatch(
                e -> e.getColumnName().equals("店舗名") && e.getMessage().contains("16桁"));
    }

    @Test
    void requiresAddrZipExactly7Digits() {
        List<CsvValidationError> errors = check(withOverride(IDX_ADDR_ZIP, "123456"));

        assertThat(errors).anyMatch(
                e -> e.getColumnName().equals("所在地郵便番号") && e.getMessage().contains("7桁固定"));
    }

    @Test
    void acceptsAddrZipWith7Digits() {
        List<CsvValidationError> errors = check(withOverride(IDX_ADDR_ZIP, "1234567"));

        assertThat(errors).noneMatch(e -> e.getColumnName().equals("所在地郵便番号"));
    }

    @Test
    void addrBuildingIsOptional() {
        List<CsvValidationError> errors = check(withOverride(IDX_ADDR_BUILDING, ""));

        assertThat(errors).noneMatch(e -> e.getColumnName().equals("所在地建物名・部屋番号"));
    }

    @Test
    void rejectsAddrBuildingOverMaxLengthWhenPresent() {
        List<CsvValidationError> errors = check(
                withOverride(IDX_ADDR_BUILDING, "一".repeat(31)));

        assertThat(errors).anyMatch(
                e -> e.getColumnName().equals("所在地建物名・部屋番号") && e.getMessage().contains("30桁"));
    }

    @Test
    void requiresRepBirthExactly8Digits() {
        List<CsvValidationError> errors = check(withOverride(IDX_REP_BIRTH, "1900101"));

        assertThat(errors).anyMatch(
                e -> e.getColumnName().equals("代表者生年月日") && e.getMessage().contains("8桁固定"));
    }

    @Test
    void corpNameAndKanaBothBlankIsAllowed() {
        List<CsvValidationError> errors = check(
                withOverrides(IDX_CORP_NAME, "", IDX_CORP_NAME_KANA, ""));

        assertThat(errors).noneMatch(
                e -> e.getColumnName().equals("法人名") || e.getColumnName().equals("法人名カナ"));
    }

    @Test
    void corpNameWithoutKanaIsRejected() {
        List<CsvValidationError> errors = check(
                withOverrides(IDX_CORP_NAME, "有限会社赤坂生花店", IDX_CORP_NAME_KANA, ""));

        assertThat(errors).anyMatch(
                e -> e.getColumnName().equals("法人名") && e.getMessage().contains("セット"));
    }

    @Test
    void corpNameWithKanaBothPresentIsAccepted() {
        List<CsvValidationError> errors = check(withOverrides(
                IDX_CORP_NAME, "有限会社赤坂生花店", IDX_CORP_NAME_KANA, "ユウゲンガイシャアカサカセイカテン"));

        assertThat(errors).noneMatch(
                e -> e.getColumnName().equals("法人名") || e.getColumnName().equals("法人名カナ"));
    }

    private List<CsvValidationError> check(List<String> fields) {
        List<CsvValidationError> errors = new ArrayList<>();
        checker.check(fields, 2, errors);
        return errors;
    }

    private List<String> withOverride(int index, String value) {
        List<String> cols = baseline();
        cols.set(index, value);
        return cols;
    }

    private List<String> withOverrides(int index1, String value1, int index2, String value2) {
        List<String> cols = baseline();
        cols.set(index1, value1);
        cols.set(index2, value2);
        return cols;
    }

    private List<String> baseline() {
        String[] blank = new String[COLUMN_COUNT];
        Arrays.fill(blank, "");
        List<String> cols = new ArrayList<>(Arrays.asList(blank));
        cols.set(IDX_STORE_NAME, "赤坂生花店");
        cols.set(IDX_STORE_NAME_KANA, "アカサカセイカテン");
        cols.set(IDX_ADDR_ZIP, "0700032");
        cols.set(IDX_ADDR_PREF, "北海道");
        cols.set(IDX_ADDR_PREF_KANA, "ホッカイドウ");
        cols.set(IDX_ADDR_CITY, "旭川市");
        cols.set(IDX_ADDR_CITY_KANA, "アサヒカワシ");
        cols.set(IDX_ADDR_TOWN, "二条通");
        cols.set(IDX_ADDR_TOWN_KANA, "ニジョウドオリ");
        cols.set(IDX_ADDR_BLOCK, "6丁目右6号");
        cols.set(IDX_ADDR_BLOCK_KANA, "ロクチョウメミギロクゴウ");
        cols.set(IDX_ADDR_TEL, "0166-22-4276");
        cols.set(IDX_REP_LAST_NAME, "田中");
        cols.set(IDX_REP_FIRST_NAME, "一郎");
        cols.set(IDX_REP_LAST_NAME_KANA, "タナカ");
        cols.set(IDX_REP_FIRST_NAME_KANA, "イチロウ");
        cols.set(IDX_REP_BIRTH, "19000101");
        cols.set(IDX_HANDLING_ITEMS, "慶弔葉鉢");
        return cols;
    }
}
