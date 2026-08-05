package com.cupit.csv.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
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
import com.cupit.model.SmccMerchantNo;
import com.cupit.repository.SmccMerchantNoRepository;

/**
 * {@link SmccMerchantNoFileImporter} のテスト。4列パース、取引コード必須チェック、
 * m_smcc_merchant_noのNOT NULL制約に対応する必須項目チェック、列数不正行のスキップ、
 * 取引コード単位の洗い替え（重複許容・delete+saveAll）を検証する。
 */
@ExtendWith(MockitoExtension.class)
class SmccMerchantNoFileImporterTest {

    private static final int COLUMN_COUNT = 4;

    @Mock
    private SmccMerchantNoRepository smccMerchantNoRepository;

    private SmccMerchantNoFileImporter importer;

    @BeforeEach
    void setUp() {
        importer = new SmccMerchantNoFileImporter(smccMerchantNoRepository);
    }

    @Test
    void importsValidRow() throws Exception {
        MockMultipartFile file = csvFile(validRow("01-001"));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getSuccessCount()).isEqualTo(1);

        ArgumentCaptor<List<SmccMerchantNo>> captor = ArgumentCaptor.forClass(List.class);
        verify(smccMerchantNoRepository).saveAll(captor.capture());
        SmccMerchantNo saved = captor.getValue().get(0);
        assertThat(saved.getTradeCode()).isEqualTo("01-001");
        assertThat(saved.getMerchantNo()).isEqualTo("12345678");
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
        fields[2] = ""; // 種別
        MockMultipartFile file = csvFile(String.join(",", fields));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors()).anyMatch(e -> e.getMessage().contains("種別は必須です"));
    }

    @Test
    void skipsRowWithFieldExceedingMaxLength() throws Exception {
        String[] fields = validFields("01-001");
        fields[1] = "1".repeat(11); // SMCC加盟店番号（VARCHAR(10)）
        MockMultipartFile file = csvFile(String.join(",", fields));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.getSuccessCount()).isZero();
        assertThat(result.getErrors())
                .anyMatch(e -> e.getMessage().contains("SMCC加盟店番号は10文字以内で入力してください"));
    }

    @Test
    void allowsDuplicateTradeCodeInCsvAndReplacesByTradeCode() throws Exception {
        MockMultipartFile file = csvFile(validRow("01-001"), validRow("01-001"));

        ImportResult result = importer.importFile(file, batch("user001"));

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getSuccessCount()).isEqualTo(2);
        ArgumentCaptor<List<String>> deleteCaptor = ArgumentCaptor.forClass(List.class);
        verify(smccMerchantNoRepository).deleteByTradeCodeIn(deleteCaptor.capture());
        assertThat(deleteCaptor.getValue()).containsExactly("01-001");
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
        f[1] = "12345678";
        f[2] = "クレジット";
        f[3] = "01-001000";
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
                "file", "smcc_merchant_no.csv", "text/csv",
                sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String header() {
        String[] cols = new String[COLUMN_COUNT];
        Arrays.fill(cols, "");
        cols[0] = "取引コード";
        return String.join(",", cols);
    }

}
