package com.cupit.csv.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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
import com.cupit.model.SteraTerminal;
import com.cupit.repository.SteraTerminalRepository;

/**
 * {@link SteraTerminalFileImporter} のテスト。7列パース、取引コード必須チェック、
 * m_stera_terminalのNOT NULL制約に対応する必須項目チェック、列数不正行のスキップ、
 * 取引コード単位の洗い替え（重複許容・delete+saveAll）を検証する。
 */
@ExtendWith(MockitoExtension.class)
class SteraTerminalFileImporterTest {

    private static final int COLUMN_COUNT = 7;

    @Mock
    private SteraTerminalRepository steraTerminalRepository;

    private SteraTerminalFileImporter importer;

    @BeforeEach
    void setUp() {
        importer = new SteraTerminalFileImporter(steraTerminalRepository);
    }

    @Test
    void importsValidRow() throws Exception {
        MockMultipartFile file = csvFile(validRow("01-001"));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getSuccessCount()).isEqualTo(1);

        ArgumentCaptor<List<SteraTerminal>> captor = ArgumentCaptor.forClass(List.class);
        verify(steraTerminalRepository).saveAll(captor.capture());
        SteraTerminal saved = captor.getValue().get(0);
        assertThat(saved.getTradeCode()).isEqualTo("01-001");
        assertThat(saved.getTerminalId()).isEqualTo("TERM0000001");
        assertThat(saved.getUpdatedUserId()).isEqualTo("user001");
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
        fields[4] = ""; // 端末利用ステータス
        MockMultipartFile file = csvFile(String.join(",", fields));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors()).anyMatch(e -> e.getMessage().contains("端末利用ステータスは必須です"));
    }

    @Test
    void skipsRowWithFieldExceedingMaxLength() throws Exception {
        String[] fields = validFields("01-001");
        fields[1] = "1".repeat(14); // 端末識別番号（VARCHAR(13)）
        MockMultipartFile file = csvFile(String.join(",", fields));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors())
                .anyMatch(e -> e.getMessage().contains("端末識別番号は13文字以内で入力してください"));
    }

    @Test
    void allowsDuplicateTradeCodeInCsvAndReplacesByTradeCode() throws Exception {
        MockMultipartFile file = csvFile(validRow("01-001"), validRow("01-001"));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getSuccessCount()).isEqualTo(2);
        ArgumentCaptor<List<String>> deleteCaptor = ArgumentCaptor.forClass(List.class);
        verify(steraTerminalRepository).deleteByTradeCodeIn(deleteCaptor.capture());
        assertThat(deleteCaptor.getValue()).containsExactly("01-001");
    }

    @Test
    void skipsRowWithInvalidDateFormat() throws Exception {
        String[] fields = validFields("01-001");
        fields[5] = "2020-01-01"; // 端末利用開始日
        MockMultipartFile file = csvFile(String.join(",", fields));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors()).anyMatch(e -> e.getMessage().contains("日付変換エラー"));
    }

    @Test
    void parsesValidDateField() throws Exception {
        MockMultipartFile file = csvFile(validRow("01-001"));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.hasErrors()).isFalse();
        ArgumentCaptor<List<SteraTerminal>> captor = ArgumentCaptor.forClass(List.class);
        verify(steraTerminalRepository).saveAll(captor.capture());
        assertThat(captor.getValue().get(0).getTerminalStartDate())
                .isEqualTo(LocalDate.of(2020, 1, 1));
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
        StringBuilder sb = new StringBuilder("﻿").append(header()).append("\r\n");
        for (String line : dataLines) {
            sb.append(line).append("\r\n");
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
