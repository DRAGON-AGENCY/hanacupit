package com.cupit.csv.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.cupit.model.ImportBatch;
import com.cupit.model.MerchantNumberData;
import com.cupit.repository.MerchantNumberDataRepository;

/**
 * {@link MerchantNumberDataFileImporter} のテスト。26列パース、取引コード必須チェック、
 * 列数不正行のスキップ、取引コード単位の洗い替え（重複許容・delete+saveAll）、
 * 日付・数値変換エラーによる部分登録を検証する。取引コード以外の全項目は任意とする。
 */
@ExtendWith(MockitoExtension.class)
class MerchantNumberDataFileImporterTest {

    private static final int COLUMN_COUNT = 26;

    @Mock
    private MerchantNumberDataRepository merchantNumberDataRepository;

    private MerchantNumberDataFileImporter importer;

    @BeforeEach
    void setUp() {
        importer = new MerchantNumberDataFileImporter(merchantNumberDataRepository);
    }

    @Test
    void importsRowWithTradeCodeOnly() throws Exception {
        MockMultipartFile file = csvFile(row("01-001"));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getSuccessCount()).isEqualTo(1);

        ArgumentCaptor<List<MerchantNumberData>> captor = ArgumentCaptor.forClass(List.class);
        verify(merchantNumberDataRepository).saveAll(captor.capture());
        MerchantNumberData saved = captor.getValue().get(0);
        assertThat(saved.getTradeCode()).isEqualTo("01-001");
        assertThat(saved.getUpdatedBy()).isEqualTo("user001");
        assertThat(saved.getRegisteredDate()).isEqualTo(java.time.LocalDate.now());
    }

    @Test
    void skipsRowMissingTradeCode() throws Exception {
        MockMultipartFile file = csvFile(row(""));

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
    void importsStringFieldValue() throws Exception {
        MockMultipartFile file = csvFile(rowWithField(2, "テスト値"));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.hasErrors()).isFalse();
        ArgumentCaptor<List<MerchantNumberData>> captor = ArgumentCaptor.forClass(List.class);
        verify(merchantNumberDataRepository).saveAll(captor.capture());
        assertThat(captor.getValue().get(0).getLineType()).isEqualTo("テスト値");
    }

    @Test
    void allowsDuplicateTradeCodeInCsvAndReplacesByTradeCode() throws Exception {
        MockMultipartFile file = csvFile(row("01-001"), row("01-001"));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getSuccessCount()).isEqualTo(2);
        ArgumentCaptor<java.util.Set<String>> deleteCaptor = ArgumentCaptor.forClass(java.util.Set.class);
        verify(merchantNumberDataRepository).deleteByTradeCodeIn(deleteCaptor.capture());
        assertThat(deleteCaptor.getValue()).containsExactly("01-001");
    }

    @Test
    void skipsRowWithInvalidIntegerFormat() throws Exception {
        MockMultipartFile file = csvFile(rowWithField(1, "abc"));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors()).anyMatch(e -> e.getMessage().contains("数値変換エラー"));
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
        return new MockMultipartFile(
                "file", "merchant_number_data.csv", "text/csv",
                sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String header() {
        String[] cols = new String[COLUMN_COUNT];
        java.util.Arrays.fill(cols, "");
        cols[0] = "取引コード";
        return String.join(",", cols);
    }

    private String row(String tradeCode) {
        String[] cols = new String[COLUMN_COUNT];
        java.util.Arrays.fill(cols, "");
        cols[0] = tradeCode;
        return String.join(",", cols);
    }

    private String rowWithField(int index, String value) {
        String[] cols = new String[COLUMN_COUNT];
        java.util.Arrays.fill(cols, "");
        cols[0] = "01-001";
        cols[index] = value;
        return String.join(",", cols);
    }

}
