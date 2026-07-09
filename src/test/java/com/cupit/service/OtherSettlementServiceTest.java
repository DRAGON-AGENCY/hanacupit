package com.cupit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockMultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.csv.PaymentType;
import com.cupit.csv.importer.FileImporter;
import com.cupit.csv.importer.FileImporterFactory;
import com.cupit.csv.importer.ImportResult;
import com.cupit.dto.CsvValidationResponse;
import com.cupit.dto.ImportResponse;
import com.cupit.model.ImportBatch;
import com.cupit.repository.ImportBatchRepository;
import com.cupit.testsupport.CsvFiles;

/**
 * {@link OtherSettlementService} のテスト。CsvFormatValidatorFactory（静的）は実物を用い、
 * FileImporterFactory・ImportBatchRepository をモック化して、フォーマット検証ゲート
 * （isFatal）・部分登録・置き換え確認（error_count>0 の未確定バッチ）を検証する。
 */
@ExtendWith(MockitoExtension.class)
class OtherSettlementServiceTest {

    private static final String STERA_CODE = "stera code";

    @Mock
    private FileImporterFactory fileImporterFactory;

    @Mock
    private ImportBatchRepository importBatchRepository;

    @Mock
    private FileImporter importer;

    private OtherSettlementService service;

    @BeforeEach
    void setUp() {
        service = new OtherSettlementService(fileImporterFactory, importBatchRepository);
    }

    @Test
    void validateFileFormatReturnsErrorWhenFileIsNull() throws Exception {
        CsvValidationResponse response = service.validateFileFormat(null, STERA_CODE);

        assertThat(response.isValid()).isFalse();
        assertThat(response.getErrors().get(0).getMessage()).contains("ファイルが選択されていません");
    }

    @Test
    void validateFileFormatReturnsValidForWellFormedFile() throws Exception {
        CsvValidationResponse response = service.validateFileFormat(validSteraCodeFile(), STERA_CODE);

        assertThat(response.isValid()).isTrue();
    }

    @Test
    void importFileReturnsErrorWhenFileIsNull() throws Exception {
        ImportResponse response = service.importFile(null, STERA_CODE, "user001", false);

        assertThat(response.isSuccess()).isFalse();
        verify(importBatchRepository, never()).save(any());
    }

    @Test
    void importFileRejectsFatalFormatWithoutRegistering() throws Exception {
        MockMultipartFile wrongExtension = CsvFiles.utf8Bom("stera_code.txt",
                "ブランド,端末識別番号,伝票番号,決済年月日,決済時間,1:売上2:返品,決済金額,手数料金額,収納金額,サブウォレット名");

        ImportResponse response = service.importFile(wrongExtension, STERA_CODE, "user001", false);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorMessage()).contains("フォーマット検証エラー");
        verify(importBatchRepository, never()).save(any());
    }

    @Test
    void importFileRegistersAllRowsWhenNoErrors() throws Exception {
        ImportBatch saved = batch(100, 0, null);
        when(fileImporterFactory.getImporter(PaymentType.STERA_CODE)).thenReturn(importer);
        when(importBatchRepository.findByPaymentTypeAndTransferBatchIdIsNull(STERA_CODE))
                .thenReturn(List.of());
        when(importer.extractLookupKey(any())).thenReturn("7113462036751");
        when(importBatchRepository.save(any())).thenReturn(saved);
        when(importer.importFile(any(), any())).thenReturn(new ImportResult(2, 2, List.of()));

        ImportResponse response = service.importFile(validSteraCodeFile(), STERA_CODE, "user001", false);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getImportedCount()).isEqualTo(2);
        assertThat(response.getBatchId()).isEqualTo(100);
        assertThat(saved.getRecordCount()).isEqualTo(2);
        assertThat(saved.getErrorCount()).isZero();
    }

    @Test
    void importFilePerformsPartialRegistrationWhenSomeRowsFail() throws Exception {
        ImportBatch saved = batch(101, 0, null);
        when(fileImporterFactory.getImporter(PaymentType.STERA_CODE)).thenReturn(importer);
        when(importBatchRepository.findByPaymentTypeAndTransferBatchIdIsNull(STERA_CODE))
                .thenReturn(List.of());
        when(importer.extractLookupKey(any())).thenReturn("7113462036751");
        when(importBatchRepository.save(any())).thenReturn(saved);
        when(importer.importFile(any(), any())).thenReturn(new ImportResult(1, 2,
                List.of(new CsvValidationError(3, "決済金額", "数値変換エラー"))));

        ImportResponse response = service.importFile(validSteraCodeFile(), STERA_CODE, "user001", false);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getImportedCount()).isEqualTo(1);
        assertThat(response.getErrors()).hasSize(1);
        assertThat(saved.getErrorCount()).isEqualTo(1);
    }

    @Test
    void importFileRequiresReplaceConfirmationWhenErroredBatchExists() throws Exception {
        when(fileImporterFactory.getImporter(PaymentType.STERA_CODE)).thenReturn(importer);
        when(importBatchRepository.findByPaymentTypeAndTransferBatchIdIsNull(STERA_CODE))
                .thenReturn(List.of(batch(50, 3, null)));

        ImportResponse response = service.importFile(validSteraCodeFile(), STERA_CODE, "user001", false);

        assertThat(response.getReplaceConfirmation()).isNotNull();
        assertThat(response.getReplaceConfirmation().getExistingBatchId()).isEqualTo(50);
        verify(importer, never()).importFile(any(), any());
    }

    @Test
    void importFileReplacesErroredBatchWhenReplaceIsTrue() throws Exception {
        ImportBatch existing = batch(50, 3, null);
        ImportBatch saved = batch(102, 0, null);
        when(fileImporterFactory.getImporter(PaymentType.STERA_CODE)).thenReturn(importer);
        when(importBatchRepository.findByPaymentTypeAndTransferBatchIdIsNull(STERA_CODE))
                .thenReturn(List.of(existing));
        when(importer.extractLookupKey(any())).thenReturn("7113462036751");
        when(importBatchRepository.save(any())).thenReturn(saved);
        when(importer.importFile(any(), any())).thenReturn(new ImportResult(2, 2, List.of()));

        ImportResponse response = service.importFile(validSteraCodeFile(), STERA_CODE, "user001", true);

        verify(importer).deleteBatchData(50);
        verify(importBatchRepository).delete(existing);
        assertThat(response.isSuccess()).isTrue();
    }

    @Test
    void importFileDoesNotConfirmWhenUnprocessedBatchHasNoError() throws Exception {
        ImportBatch saved = batch(103, 0, null);
        when(fileImporterFactory.getImporter(PaymentType.STERA_CODE)).thenReturn(importer);
        when(importBatchRepository.findByPaymentTypeAndTransferBatchIdIsNull(STERA_CODE))
                .thenReturn(List.of(batch(40, 0, null)));
        when(importer.extractLookupKey(any())).thenReturn("7113462036751");
        when(importBatchRepository.save(any())).thenReturn(saved);
        when(importer.importFile(any(), any())).thenReturn(new ImportResult(2, 2, List.of()));

        ImportResponse response = service.importFile(validSteraCodeFile(), STERA_CODE, "user001", false);

        assertThat(response.getReplaceConfirmation()).isNull();
        assertThat(response.isSuccess()).isTrue();
    }

    @Test
    void importFileThrowsWhenLookupKeyIsBlank() throws Exception {
        when(fileImporterFactory.getImporter(PaymentType.STERA_CODE)).thenReturn(importer);
        when(importBatchRepository.findByPaymentTypeAndTransferBatchIdIsNull(STERA_CODE))
                .thenReturn(List.of());
        when(importer.extractLookupKey(any())).thenReturn("  ");

        assertThatThrownBy(() -> service.importFile(validSteraCodeFile(), STERA_CODE, "user001", false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("識別キー");
    }

    private MockMultipartFile validSteraCodeFile() {
        return CsvFiles.utf8Bom("stera_code.csv",
                "ブランド,端末識別番号,伝票番号,決済年月日,決済時間,1:売上2:返品,決済金額,手数料金額,収納金額,サブウォレット名",
                "楽天ペイ,7113462036751,03447,20251101,091102,1,5000,,,");
    }

    private ImportBatch batch(int batchId, int errorCount, Integer transferBatchId) {
        ImportBatch batch = new ImportBatch();
        batch.setBatchId(batchId);
        batch.setFileName("stera_code.csv");
        batch.setRecordCount(1);
        batch.setErrorCount(errorCount);
        batch.setTransferBatchId(transferBatchId);
        return batch;
    }
}
