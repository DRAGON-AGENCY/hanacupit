package com.cupit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.csv.CsvValidationResult;
import com.cupit.csv.importer.ImportResult;
import com.cupit.csv.importer.MemberInfoFileImporter;
import com.cupit.csv.validator.MemberMasterCsvValidator;
import com.cupit.dto.ImportResponse;
import com.cupit.model.ImportBatch;
import com.cupit.repository.ImportBatchRepository;

/**
 * {@link MemberMasterService} のテスト。ファイル未選択・致命的フォーマットエラー時の
 * 早期リターン、m_import_batchの作成とrecordCount／errorCountの設定、
 * 正常時／部分登録エラー時のImportResponse組み立てを、
 * {@link MemberMasterCsvValidator}・{@link MemberInfoFileImporter}・
 * {@link ImportBatchRepository}をモック化して検証する。
 */
@ExtendWith(MockitoExtension.class)
class MemberMasterServiceTest {

    @Mock
    private MemberMasterCsvValidator csvValidator;

    @Mock
    private MemberInfoFileImporter fileImporter;

    @Mock
    private ImportBatchRepository importBatchRepository;

    private MemberMasterService service;
    private MockMultipartFile file;

    @BeforeEach
    void setUp() {
        service = new MemberMasterService(csvValidator, fileImporter, importBatchRepository);
        file = new MockMultipartFile("file", "member_master.csv", "text/csv", new byte[] {1});
    }

    @Test
    void returnsErrorWhenFileIsNull() throws Exception {
        ImportResponse response = service.importFile(null, "user001");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorMessage()).contains("ファイルが選択されていません");
    }

    @Test
    void returnsErrorWhenFileIsEmpty() throws Exception {
        MockMultipartFile emptyFile =
                new MockMultipartFile("file", "member_master.csv", "text/csv", new byte[0]);

        ImportResponse response = service.importFile(emptyFile, "user001");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorMessage()).contains("ファイルが選択されていません");
    }

    @Test
    void returnsFatalValidationErrorMessageWithoutCreatingBatch() throws Exception {
        CsvValidationResult fatalResult = new CsvValidationResult();
        fatalResult.addError(new CsvValidationError(1, "", "ファイルの拡張子が不正です。"));
        fatalResult.markFatal();
        when(csvValidator.validate(file)).thenReturn(fatalResult);

        ImportResponse response = service.importFile(file, "user001");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorMessage()).contains("フォーマット検証エラー");
        assertThat(response.getErrorMessage()).contains("ファイルの拡張子が不正です");
        verify(importBatchRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void createsImportBatchWithPaymentTypeAndEmployee() throws Exception {
        when(csvValidator.validate(file)).thenReturn(new CsvValidationResult());
        ImportBatch saved = batchWithId(1);
        when(importBatchRepository.save(any())).thenReturn(saved).thenReturn(saved);
        when(fileImporter.importFile(any(), any()))
                .thenReturn(new ImportResult(1, 1, List.of()));

        service.importFile(file, "user001");

        ArgumentCaptor<ImportBatch> captor = ArgumentCaptor.forClass(ImportBatch.class);
        verify(importBatchRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        ImportBatch firstSave = captor.getAllValues().get(0);
        assertThat(firstSave.getPaymentType()).isEqualTo("会員マスター");
        assertThat(firstSave.getFileName()).isEqualTo("member_master.csv");
        assertThat(firstSave.getUpdateEmployee()).isEqualTo("user001");
    }

    @Test
    void setsRecordCountAndErrorCountOnBatchAfterImport() throws Exception {
        when(csvValidator.validate(file)).thenReturn(new CsvValidationResult());
        ImportBatch saved = batchWithId(5);
        when(importBatchRepository.save(any())).thenReturn(saved);
        List<CsvValidationError> errors = List.of(
                new CsvValidationError(3, "取引コード", "取引コードは必須です。"));
        when(fileImporter.importFile(any(), any()))
                .thenReturn(new ImportResult(2, 3, errors));

        service.importFile(file, "user001");

        ArgumentCaptor<ImportBatch> captor = ArgumentCaptor.forClass(ImportBatch.class);
        verify(importBatchRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        ImportBatch lastSave = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertThat(lastSave.getRecordCount()).isEqualTo(2);
        assertThat(lastSave.getErrorCount()).isEqualTo(1);
    }

    @Test
    void returnsSuccessResponseWhenNoErrors() throws Exception {
        when(csvValidator.validate(file)).thenReturn(new CsvValidationResult());
        when(importBatchRepository.save(any())).thenReturn(batchWithId(7));
        when(fileImporter.importFile(any(), any()))
                .thenReturn(new ImportResult(3, 3, List.of()));

        ImportResponse response = service.importFile(file, "user001");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getImportedCount()).isEqualTo(3);
        assertThat(response.getBatchId()).isEqualTo(7);
        assertThat(response.getErrorMessage()).isNull();
    }

    @Test
    void returnsFailureResponseWithErrorDetailsWhenPartialErrors() throws Exception {
        when(csvValidator.validate(file)).thenReturn(new CsvValidationResult());
        when(importBatchRepository.save(any())).thenReturn(batchWithId(9));
        List<CsvValidationError> errors = List.of(
                new CsvValidationError(4, "取引コード", "取引コード「01-001」がCSV内で重複しています。"));
        when(fileImporter.importFile(any(), any()))
                .thenReturn(new ImportResult(1, 2, errors));

        ImportResponse response = service.importFile(file, "user001");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getImportedCount()).isEqualTo(1);
        assertThat(response.getBatchId()).isEqualTo(9);
        assertThat(response.getErrorMessage()).contains("登録件数: 1 件");
        assertThat(response.getErrorMessage()).contains("エラー: 1 件");
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0).getMessage()).contains("重複しています");
    }

    private ImportBatch batchWithId(int batchId) {
        ImportBatch batch = new ImportBatch();
        batch.setBatchId(batchId);
        return batch;
    }

}
