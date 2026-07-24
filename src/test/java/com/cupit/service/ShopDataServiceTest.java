package com.cupit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import com.cupit.csv.importer.SmccMerchantNoFileImporter;
import com.cupit.csv.importer.SteraStoreFileImporter;
import com.cupit.csv.importer.SteraTerminalFileImporter;
import com.cupit.csv.validator.SmccMerchantNoCsvValidator;
import com.cupit.csv.validator.SteraStoreCsvValidator;
import com.cupit.csv.validator.SteraTerminalCsvValidator;
import com.cupit.dto.ImportResponse;
import com.cupit.model.ImportBatch;
import com.cupit.repository.ImportBatchRepository;
import com.cupit.service.ShopDataService.DataType;

/**
 * {@link ShopDataService} のテスト。ファイル未選択・致命的フォーマットエラー時の
 * 早期リターン、データ種類（店舗／端末／加盟店番号）ごとのValidator/Importerへの
 * ルーティング、m_import_batchの作成とrecordCount／errorCountの設定、
 * 正常時／部分登録エラー時のImportResponse組み立てを、各依存をモック化して検証する。
 */
@ExtendWith(MockitoExtension.class)
class ShopDataServiceTest {

    @Mock
    private SteraStoreCsvValidator steraStoreCsvValidator;

    @Mock
    private SteraStoreFileImporter steraStoreFileImporter;

    @Mock
    private SteraTerminalCsvValidator steraTerminalCsvValidator;

    @Mock
    private SteraTerminalFileImporter steraTerminalFileImporter;

    @Mock
    private SmccMerchantNoCsvValidator smccMerchantNoCsvValidator;

    @Mock
    private SmccMerchantNoFileImporter smccMerchantNoFileImporter;

    @Mock
    private ImportBatchRepository importBatchRepository;

    private ShopDataService service;
    private MockMultipartFile file;

    @BeforeEach
    void setUp() {
        service = new ShopDataService(
                steraStoreCsvValidator, steraStoreFileImporter,
                steraTerminalCsvValidator, steraTerminalFileImporter,
                smccMerchantNoCsvValidator, smccMerchantNoFileImporter,
                importBatchRepository);
        file = new MockMultipartFile("file", "shop_data.csv", "text/csv", new byte[] {1});
    }

    @Test
    void returnsErrorWhenFileIsNull() throws Exception {
        ImportResponse response = service.importFile(DataType.SHOP, null, "user001");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorMessage()).contains("ファイルが選択されていません");
    }

    @Test
    void returnsErrorWhenFileIsEmpty() throws Exception {
        MockMultipartFile emptyFile =
                new MockMultipartFile("file", "shop_data.csv", "text/csv", new byte[0]);

        ImportResponse response = service.importFile(DataType.SHOP, emptyFile, "user001");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorMessage()).contains("ファイルが選択されていません");
    }

    @Test
    void returnsFatalValidationErrorMessageWithoutCreatingBatch() throws Exception {
        CsvValidationResult fatalResult = new CsvValidationResult();
        fatalResult.addError(new CsvValidationError(1, "", "ファイルの拡張子が不正です。"));
        fatalResult.markFatal();
        when(steraStoreCsvValidator.validate(file)).thenReturn(fatalResult);

        ImportResponse response = service.importFile(DataType.SHOP, file, "user001");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorMessage()).contains("フォーマット検証エラー");
        assertThat(response.getErrorMessage()).contains("ファイルの拡張子が不正です");
        verify(importBatchRepository, never()).save(any());
    }

    @Test
    void createsImportBatchWithDataTypeLabelAndEmployee() throws Exception {
        when(steraStoreCsvValidator.validate(file)).thenReturn(new CsvValidationResult());
        ImportBatch saved = batchWithId(1);
        when(importBatchRepository.save(any())).thenReturn(saved).thenReturn(saved);
        when(steraStoreFileImporter.importFile(any(), any()))
                .thenReturn(new ImportResult(1, 1, List.of()));

        service.importFile(DataType.SHOP, file, "user001");

        ArgumentCaptor<ImportBatch> captor = ArgumentCaptor.forClass(ImportBatch.class);
        verify(importBatchRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        ImportBatch firstSave = captor.getAllValues().get(0);
        assertThat(firstSave.getPaymentType()).isEqualTo("店舗データ");
        assertThat(firstSave.getFileName()).isEqualTo("shop_data.csv");
        assertThat(firstSave.getUpdateEmployee()).isEqualTo("user001");
    }

    @Test
    void setsRecordCountAndErrorCountOnBatchAfterImport() throws Exception {
        when(steraStoreCsvValidator.validate(file)).thenReturn(new CsvValidationResult());
        ImportBatch saved = batchWithId(5);
        when(importBatchRepository.save(any())).thenReturn(saved);
        List<CsvValidationError> errors = List.of(
                new CsvValidationError(3, "取引コード", "取引コードは必須です。"));
        when(steraStoreFileImporter.importFile(any(), any()))
                .thenReturn(new ImportResult(2, 3, errors));

        service.importFile(DataType.SHOP, file, "user001");

        ArgumentCaptor<ImportBatch> captor = ArgumentCaptor.forClass(ImportBatch.class);
        verify(importBatchRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        ImportBatch lastSave = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertThat(lastSave.getRecordCount()).isEqualTo(2);
        assertThat(lastSave.getErrorCount()).isEqualTo(1);
    }

    @Test
    void returnsSuccessResponseWhenNoErrors() throws Exception {
        when(steraStoreCsvValidator.validate(file)).thenReturn(new CsvValidationResult());
        when(importBatchRepository.save(any())).thenReturn(batchWithId(7));
        when(steraStoreFileImporter.importFile(any(), any()))
                .thenReturn(new ImportResult(3, 3, List.of()));

        ImportResponse response = service.importFile(DataType.SHOP, file, "user001");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getImportedCount()).isEqualTo(3);
        assertThat(response.getBatchId()).isEqualTo(7);
        assertThat(response.getErrorMessage()).isNull();
    }

    @Test
    void returnsFailureResponseWithErrorDetailsWhenPartialErrors() throws Exception {
        when(steraStoreCsvValidator.validate(file)).thenReturn(new CsvValidationResult());
        when(importBatchRepository.save(any())).thenReturn(batchWithId(9));
        List<CsvValidationError> errors = List.of(
                new CsvValidationError(4, "取引コード", "取引コード「01-001」がCSV内で重複しています。"));
        when(steraStoreFileImporter.importFile(any(), any()))
                .thenReturn(new ImportResult(1, 2, errors));

        ImportResponse response = service.importFile(DataType.SHOP, file, "user001");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getImportedCount()).isEqualTo(1);
        assertThat(response.getBatchId()).isEqualTo(9);
        assertThat(response.getErrorMessage()).contains("登録件数: 1 件");
        assertThat(response.getErrorMessage()).contains("エラー: 1 件");
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0).getMessage()).contains("重複しています");
    }

    @Test
    void routesToShopValidatorAndImporterForShopDataType() throws Exception {
        when(steraStoreCsvValidator.validate(file)).thenReturn(new CsvValidationResult());
        when(importBatchRepository.save(any())).thenReturn(batchWithId(1));
        when(steraStoreFileImporter.importFile(any(), any()))
                .thenReturn(new ImportResult(1, 1, List.of()));

        service.importFile(DataType.SHOP, file, "user001");

        verify(steraStoreCsvValidator).validate(file);
        verify(steraStoreFileImporter).importFile(any(), any());
        verify(steraTerminalCsvValidator, never()).validate(any());
        verify(smccMerchantNoCsvValidator, never()).validate(any());
    }

    @Test
    void routesToTerminalValidatorAndImporterForTerminalDataType() throws Exception {
        when(steraTerminalCsvValidator.validate(file)).thenReturn(new CsvValidationResult());
        when(importBatchRepository.save(any())).thenReturn(batchWithId(1));
        when(steraTerminalFileImporter.importFile(any(), any()))
                .thenReturn(new ImportResult(1, 1, List.of()));

        service.importFile(DataType.TERMINAL, file, "user001");

        verify(steraTerminalCsvValidator).validate(file);
        verify(steraTerminalFileImporter).importFile(any(), any());
        verify(steraStoreCsvValidator, never()).validate(any());
        verify(smccMerchantNoCsvValidator, never()).validate(any());
    }

    @Test
    void routesToMerchantNumberValidatorAndImporterForMerchantNumberDataType() throws Exception {
        when(smccMerchantNoCsvValidator.validate(file)).thenReturn(new CsvValidationResult());
        when(importBatchRepository.save(any())).thenReturn(batchWithId(1));
        when(smccMerchantNoFileImporter.importFile(any(), any()))
                .thenReturn(new ImportResult(1, 1, List.of()));

        service.importFile(DataType.MERCHANT_NUMBER, file, "user001");

        verify(smccMerchantNoCsvValidator).validate(file);
        verify(smccMerchantNoFileImporter).importFile(any(), any());
        verify(steraStoreCsvValidator, never()).validate(any());
        verify(steraTerminalCsvValidator, never()).validate(any());
    }

    private ImportBatch batchWithId(int batchId) {
        ImportBatch batch = new ImportBatch();
        batch.setBatchId(batchId);
        return batch;
    }

}
