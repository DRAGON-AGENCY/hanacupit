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
import com.cupit.model.TerminalData;
import com.cupit.repository.TerminalDataRepository;

/**
 * {@link TerminalDataFileImporter} のテスト。95列パース、取引コード必須チェック、
 * 列数不正行のスキップ、取引コード単位の洗い替え（重複許容・delete+saveAll）、
 * 日付・数値変換エラーによる部分登録を検証する。取引コード以外の全項目は任意とする。
 */
@ExtendWith(MockitoExtension.class)
class TerminalDataFileImporterTest {

    private static final int COLUMN_COUNT = 95;

    @Mock
    private TerminalDataRepository terminalDataRepository;

    private TerminalDataFileImporter importer;

    @BeforeEach
    void setUp() {
        importer = new TerminalDataFileImporter(terminalDataRepository);
    }

    @Test
    void importsRowWithTradeCodeOnly() throws Exception {
        MockMultipartFile file = csvFile(row("01-001"));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getSuccessCount()).isEqualTo(1);

        ArgumentCaptor<List<TerminalData>> captor = ArgumentCaptor.forClass(List.class);
        verify(terminalDataRepository).saveAll(captor.capture());
        TerminalData saved = captor.getValue().get(0);
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
        MockMultipartFile file = csvFile(rowWithField(1, "テスト値"));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.hasErrors()).isFalse();
        ArgumentCaptor<List<TerminalData>> captor = ArgumentCaptor.forClass(List.class);
        verify(terminalDataRepository).saveAll(captor.capture());
        assertThat(captor.getValue().get(0).getApplicationCategory()).isEqualTo("テスト値");
    }

    @Test
    void allowsDuplicateTradeCodeInCsvAndReplacesByTradeCode() throws Exception {
        MockMultipartFile file = csvFile(row("01-001"), row("01-001"));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getSuccessCount()).isEqualTo(2);
        ArgumentCaptor<java.util.Set<String>> deleteCaptor = ArgumentCaptor.forClass(java.util.Set.class);
        verify(terminalDataRepository).deleteByTradeCodeIn(deleteCaptor.capture());
        assertThat(deleteCaptor.getValue()).containsExactly("01-001");
    }

    @Test
    void skipsRowWithInvalidDateFormat() throws Exception {
        MockMultipartFile file = csvFile(rowWithField(3, "2020-01-01"));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors()).anyMatch(e -> e.getMessage().contains("日付変換エラー"));
    }

    @Test
    void parsesValidDateField() throws Exception {
        MockMultipartFile file = csvFile(rowWithField(3, "2020/01/01"));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.hasErrors()).isFalse();
        ArgumentCaptor<List<TerminalData>> captor = ArgumentCaptor.forClass(List.class);
        verify(terminalDataRepository).saveAll(captor.capture());
        assertThat(captor.getValue().get(0).getApplicationOrCancellationDate())
                .isEqualTo(java.time.LocalDate.of(2020, 1, 1));
    }

    @Test
    void skipsRowWithInvalidIntegerFormat() throws Exception {
        MockMultipartFile file = csvFile(rowWithField(30, "abc"));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors()).anyMatch(e -> e.getMessage().contains("数値変換エラー"));
    }

    @Test
    void skipsRowWithInvalidDecimalFormat() throws Exception {
        MockMultipartFile file = csvFile(rowWithField(63, "abc"));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors()).anyMatch(e -> e.getMessage().contains("小数変換エラー"));
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
                "file", "terminal_data.csv", "text/csv",
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
