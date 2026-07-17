package com.cupit.csv.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.cupit.csv.validator.PaymentCompanyFormatChecker;
import com.cupit.model.ImportBatch;
import com.cupit.model.MemberInfo;
import com.cupit.repository.MemberInfoRepository;

/**
 * {@link MemberInfoFileImporter} のテスト。255列パース、必須項目チェック、
 * CSV内取引コード重複、数値・日付変換エラーによる部分登録、既存取引コードの
 * 登録日維持（upsert時に登録日を上書きしない）、決済会社フォーマットチェック
 * （{@link PaymentCompanyFormatChecker}）との連携を検証する。
 *
 * 決済会社フォーマットチェックにより店舗名・住所・代表者氏名等が必須になったため、
 * 個別の挙動（日付変換エラー等）のみを検証したいテストでは、それらの必須項目に
 * あらかじめ有効な値を入れた {@link #row} ベースラインを土台にして、検証対象の
 * 列だけを上書きする。
 */
@ExtendWith(MockitoExtension.class)
class MemberInfoFileImporterTest {

    private static final int COLUMN_COUNT = 255;
    private static final int IDX_TRADE_CODE = 0;
    private static final int IDX_MID_CODE = 5;
    private static final int IDX_JOIN_DATE = 7;
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
    private static final int IDX_HANDLING_ITEMS = 41;
    private static final int IDX_CAPITAL_YEN = 93;
    private static final int IDX_REP_LAST_NAME_KANA = 73;
    private static final int IDX_REP_FIRST_NAME_KANA = 74;
    private static final int IDX_REP_LAST_NAME = 75;
    private static final int IDX_REP_FIRST_NAME = 76;
    private static final int IDX_REP_BIRTH = 77;

    @Mock
    private MemberInfoRepository memberInfoRepository;

    private MemberInfoFileImporter importer;

    @BeforeEach
    void setUp() {
        importer = new MemberInfoFileImporter(
                memberInfoRepository, new PaymentCompanyFormatChecker());
        when(memberInfoRepository.findAllById(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());
    }

    @Test
    void importsRowSatisfyingPaymentCompanyRequiredFields() throws Exception {
        MockMultipartFile file = csvFile(row(field(IDX_TRADE_CODE, "01-001")));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getSuccessCount()).isEqualTo(1);

        ArgumentCaptor<List<MemberInfo>> captor = ArgumentCaptor.forClass(List.class);
        verify(memberInfoRepository).saveAll(captor.capture());
        MemberInfo saved = captor.getValue().get(0);
        assertThat(saved.getTradeCode()).isEqualTo("01-001");
        assertThat(saved.getStoreName()).isEqualTo("赤坂生花店");
        assertThat(saved.getUpdateEmployee()).isEqualTo("user001");
    }

    @Test
    void skipsRowMissingPaymentCompanyRequiredField() throws Exception {
        MockMultipartFile file = csvFile(row(
                field(IDX_TRADE_CODE, "01-001"), field(IDX_ADDR_ZIP, "")));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors()).anyMatch(
                e -> e.getMessage().contains("所在地郵便番号") && e.getMessage().contains("必須"));
    }

    @Test
    void importsRowWithMultipleColumnsPopulated() throws Exception {
        MockMultipartFile file = csvFile(row(
                field(IDX_TRADE_CODE, "01-001"),
                field(IDX_STORE_NAME, "赤坂生花店"),
                field(IDX_MID_CODE, "1"),
                field(IDX_JOIN_DATE, "1960/08/16"),
                field(IDX_CAPITAL_YEN, "3000000")));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.getSuccessCount()).isEqualTo(1);
        ArgumentCaptor<List<MemberInfo>> captor = ArgumentCaptor.forClass(List.class);
        verify(memberInfoRepository).saveAll(captor.capture());
        MemberInfo saved = captor.getValue().get(0);
        assertThat(saved.getStoreName()).isEqualTo("赤坂生花店");
        assertThat(saved.getMidCode()).isEqualTo((short) 1);
        assertThat(saved.getJoinDate()).isEqualTo(LocalDate.of(1960, 8, 16));
        assertThat(saved.getCapitalYen()).isEqualTo(3_000_000L);
    }

    @Test
    void skipsRowWithBlankTradeCode() throws Exception {
        MockMultipartFile file = csvFile(row(field(IDX_STORE_NAME, "赤坂生花店")));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getMessage()).contains("取引コードは必須です");
    }

    @Test
    void skipsRowWithWrongColumnCount() throws Exception {
        MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile(
                "file", "x.csv", "text/csv",
                ("﻿" + header() + "\r\n" + "01-001,店名だけ\r\n")
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors().get(0).getMessage()).contains("列数が不正です");
    }

    @Test
    void skipsSecondRowWhenTradeCodeDuplicatedWithinFile() throws Exception {
        MockMultipartFile file = csvFile(
                row(field(IDX_TRADE_CODE, "01-001"), field(IDX_STORE_NAME, "店舗A")),
                row(field(IDX_TRADE_CODE, "01-001"), field(IDX_STORE_NAME, "店舗B")));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.getSuccessCount()).isEqualTo(1);
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getMessage()).contains("重複しています");

        ArgumentCaptor<List<MemberInfo>> captor = ArgumentCaptor.forClass(List.class);
        verify(memberInfoRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getStoreName()).isEqualTo("店舗A");
    }

    @Test
    void skipsRowWithInvalidDateValue() throws Exception {
        MockMultipartFile file = csvFile(row(
                field(IDX_TRADE_CODE, "01-001"), field(IDX_JOIN_DATE, "not-a-date")));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors()).anyMatch(e -> e.getMessage().contains("日付変換エラー"));
    }

    @Test
    void skipsRowWithInvalidNumericValue() throws Exception {
        MockMultipartFile file = csvFile(row(
                field(IDX_TRADE_CODE, "01-001"), field(IDX_MID_CODE, "abc")));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors()).anyMatch(e -> e.getMessage().contains("数値変換エラー"));
    }

    @Test
    void setsCreateDateToTodayForNewTradeCode() throws Exception {
        MockMultipartFile file = csvFile(row(field(IDX_TRADE_CODE, "01-001")));

        importer.importFile(file, batch("user001"));

        ArgumentCaptor<List<MemberInfo>> captor = ArgumentCaptor.forClass(List.class);
        verify(memberInfoRepository).saveAll(captor.capture());
        MemberInfo saved = captor.getValue().get(0);
        assertThat(saved.getCreateDate()).isEqualTo(LocalDate.now());
        assertThat(saved.getUpdatedDate()).isNull();
    }

    @Test
    void preservesExistingCreateDateOnUpdate() throws Exception {
        LocalDate originalCreateDate = LocalDate.of(2020, 4, 1);
        MemberInfo existing = new MemberInfo();
        existing.setTradeCode("01-001");
        existing.setCreateDate(originalCreateDate);
        when(memberInfoRepository.findAllById(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(existing));

        MockMultipartFile file = csvFile(row(field(IDX_TRADE_CODE, "01-001")));

        importer.importFile(file, batch("user001"));

        ArgumentCaptor<List<MemberInfo>> captor = ArgumentCaptor.forClass(List.class);
        verify(memberInfoRepository).saveAll(captor.capture());
        MemberInfo saved = captor.getValue().get(0);
        assertThat(saved.getCreateDate()).isEqualTo(originalCreateDate);
        assertThat(saved.getUpdatedDate()).isEqualTo(LocalDate.now());
    }

    private ImportBatch batch(String employee) {
        ImportBatch batch = new ImportBatch();
        batch.setBatchId(1);
        batch.setUpdateEmployee(employee);
        return batch;
    }

    private MockMultipartFile csvFile(String... dataLines) {
        StringBuilder sb = new StringBuilder("﻿").append(header()).append("\r\n");
        for (String line : dataLines) {
            sb.append(line).append("\r\n");
        }
        return new MockMultipartFile("file", "member_master.csv", "text/csv",
                sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private String header() {
        String[] cols = new String[COLUMN_COUNT];
        for (int i = 0; i < COLUMN_COUNT; i++) {
            cols[i] = "col" + i;
        }
        return String.join(",", cols);
    }

    /**
     * 決済会社フォーマットチェックの必須項目をあらかじめ満たしたベースライン行を作り、
     * 引数で渡された列だけ上書きする。個別の検証対象以外の列で決済会社チェックの
     * エラーが混入しないようにするため。
     */
    private String row(FieldValue... overrides) {
        Map<Integer, String> values = new LinkedHashMap<>();
        values.put(IDX_STORE_NAME, "赤坂生花店");
        values.put(IDX_STORE_NAME_KANA, "アカサカセイカテン");
        values.put(IDX_ADDR_ZIP, "0700032");
        values.put(IDX_ADDR_PREF, "北海道");
        values.put(IDX_ADDR_PREF_KANA, "ホッカイドウ");
        values.put(IDX_ADDR_CITY, "旭川市");
        values.put(IDX_ADDR_CITY_KANA, "アサヒカワシ");
        values.put(IDX_ADDR_TOWN, "二条通");
        values.put(IDX_ADDR_TOWN_KANA, "ニジョウドオリ");
        values.put(IDX_ADDR_BLOCK, "6丁目右6号");
        values.put(IDX_ADDR_BLOCK_KANA, "ロクチョウメミギロクゴウ");
        values.put(IDX_ADDR_TEL, "0166-22-4276");
        values.put(IDX_REP_LAST_NAME, "田中");
        values.put(IDX_REP_FIRST_NAME, "一郎");
        values.put(IDX_REP_LAST_NAME_KANA, "タナカ");
        values.put(IDX_REP_FIRST_NAME_KANA, "イチロウ");
        values.put(IDX_REP_BIRTH, "19000101");
        values.put(IDX_HANDLING_ITEMS, "慶弔葉鉢");
        for (FieldValue fv : overrides) {
            values.put(fv.index, fv.value);
        }

        String[] cols = new String[COLUMN_COUNT];
        java.util.Arrays.fill(cols, "");
        values.forEach((index, value) -> cols[index] = value);
        return String.join(",", cols);
    }

    private static FieldValue field(int index, String value) {
        return new FieldValue(index, value);
    }

    private record FieldValue(int index, String value) {
    }
}
