package com.cupit.csv.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.cupit.model.ImportBatch;
import com.cupit.model.SteraStore;
import com.cupit.repository.SteraStoreRepository;

/**
 * {@link SteraStoreFileImporter} のテスト。30列パース、取引コード必須チェック、
 * m_stera_storeのNOT NULL制約に対応する必須項目チェック、列数不正行のスキップ、
 * CSV内取引コード重複行のスキップ、既存取引コードのrecord_no/created_at引き継ぎ
 * （upsert）を検証する。
 */
@ExtendWith(MockitoExtension.class)
class SteraStoreFileImporterTest {

    private static final int COLUMN_COUNT = 30;

    @Mock
    private SteraStoreRepository steraStoreRepository;

    private SteraStoreFileImporter importer;

    @BeforeEach
    void setUp() {
        importer = new SteraStoreFileImporter(steraStoreRepository);
        when(steraStoreRepository.findByTradeCodeIn(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());
    }

    @Test
    void importsValidRowAsNewRecord() throws Exception {
        MockMultipartFile file = csvFile(validRow("01-001"));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getSuccessCount()).isEqualTo(1);

        ArgumentCaptor<List<SteraStore>> captor = ArgumentCaptor.forClass(List.class);
        verify(steraStoreRepository).saveAll(captor.capture());
        SteraStore saved = captor.getValue().get(0);
        assertThat(saved.getTradeCode()).isEqualTo("01-001");
        assertThat(saved.getStoreName()).isEqualTo("テスト店舗");
        assertThat(saved.getUpdatedUserId()).isEqualTo("user001");
        assertThat(saved.getRecordNo()).isZero();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void skipsRowMissingTradeCode() throws Exception {
        String[] fields = validFields("");
        MockMultipartFile file = csvFile(String.join(",", fields));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors()).anyMatch(e -> e.getMessage().contains("取引コードは必須です"));
    }

    @Test
    void skipsRowWithWrongColumnCount() throws Exception {
        MockMultipartFile file = csvFile("01-001,項目1だけ");

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors()).anyMatch(e -> e.getMessage().contains("列数が不正です"));
    }

    @Test
    void skipsRowMissingRequiredField() throws Exception {
        String[] fields = validFields("01-001");
        fields[8] = ""; // 店舗名
        MockMultipartFile file = csvFile(String.join(",", fields));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors()).anyMatch(e -> e.getMessage().contains("店舗名は必須です"));
    }

    @Test
    void skipsRowWithFieldExceedingMaxLength() throws Exception {
        String[] fields = validFields("01-001");
        fields[8] = "あ".repeat(51); // 店舗名（VARCHAR(50)）
        MockMultipartFile file = csvFile(String.join(",", fields));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors())
                .anyMatch(e -> e.getMessage().contains("店舗名は50文字以内で入力してください"));
    }

    @Test
    void skipsDuplicateTradeCodeInCsv() throws Exception {
        MockMultipartFile file = csvFile(validRow("01-001"), validRow("01-001"));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors()).anyMatch(e -> e.getMessage().contains("重複しています"));
    }

    @Test
    void skipsRowWithInvalidDateFormat() throws Exception {
        String[] fields = validFields("01-001");
        fields[26] = "2020-01-01"; // JCB利用開始日
        MockMultipartFile file = csvFile(String.join(",", fields));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors()).anyMatch(e -> e.getMessage().contains("日付変換エラー"));
    }

    @Test
    void preservesRecordNoAndCreatedAtWhenTradeCodeAlreadyExists() throws Exception {
        SteraStore existing = new SteraStore();
        existing.setRecordNo(42L);
        existing.setTradeCode("01-001");
        OffsetDateTime originalCreatedAt = OffsetDateTime.now().minusDays(10);
        existing.setCreatedAt(originalCreatedAt);
        when(steraStoreRepository.findByTradeCodeIn(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(existing));

        MockMultipartFile file = csvFile(validRow("01-001"));
        importer.importFile(file, batch("user001"));

        ArgumentCaptor<List<SteraStore>> captor = ArgumentCaptor.forClass(List.class);
        verify(steraStoreRepository).saveAll(captor.capture());
        SteraStore saved = captor.getValue().get(0);
        assertThat(saved.getRecordNo()).isEqualTo(42L);
        assertThat(saved.getCreatedAt()).isEqualTo(originalCreatedAt);
    }

    @Test
    void parsesValidDateAndDecimalFields() throws Exception {
        String[] fields = validFields("01-001");
        fields[16] = "35.6620756"; // 緯度
        fields[26] = "2020/01/01"; // JCB利用開始日
        MockMultipartFile file = csvFile(String.join(",", fields));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.hasErrors()).isFalse();
        ArgumentCaptor<List<SteraStore>> captor = ArgumentCaptor.forClass(List.class);
        verify(steraStoreRepository).saveAll(captor.capture());
        SteraStore saved = captor.getValue().get(0);
        assertThat(saved.getJcbStartDate()).isEqualTo(LocalDate.of(2020, 1, 1));
        assertThat(saved.getLatitude()).isEqualByComparingTo("35.6620756");
    }

    private ImportBatch batch(String employee) {
        ImportBatch batch = new ImportBatch();
        batch.setBatchId(1);
        batch.setUpdateEmployee(employee);
        return batch;
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
        StringBuilder sb = new StringBuilder("﻿").append(header()).append("\r\n");
        for (String line : dataLines) {
            sb.append(line).append("\r\n");
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
