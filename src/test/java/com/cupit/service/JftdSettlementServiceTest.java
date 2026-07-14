package com.cupit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.MessageDigest;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockMultipartFile;

import com.cupit.csv.PaymentType;
import com.cupit.csv.importer.FileImporter;
import com.cupit.csv.importer.FileImporterFactory;
import com.cupit.csv.importer.ImportResult;
import com.cupit.dto.ImportResponse;
import com.cupit.model.ImportBatch;
import com.cupit.repository.ImportBatchRepository;
import com.cupit.testsupport.CsvFiles;

/**
 * {@link JftdSettlementService} のテスト。OtherSettlementServiceと共通の
 * 重複登録防止ロジック（エラーを含む未確定バッチ、またはファイルハッシュが一致する
 * 未確定バッチの検知と置き換え確認）を検証する。フォーマット検証・部分登録自体は
 * 各CsvFormatValidator／FileImporter実装のテストでカバー済みのため、ここでは
 * サービス層の置き換え確認フローに絞ってテストする。
 */
@ExtendWith(MockitoExtension.class)
class JftdSettlementServiceTest {

    private static final String JCB = "JCB";

    @Mock
    private FileImporterFactory fileImporterFactory;

    @Mock
    private ImportBatchRepository importBatchRepository;

    @Mock
    private FileImporter importer;

    private JftdSettlementService service;

    @BeforeEach
    void setUp() {
        service = new JftdSettlementService(fileImporterFactory, importBatchRepository);
    }

    @Test
    void importFileReturnsErrorWhenFileIsNull() throws Exception {
        ImportResponse response = service.importFile(null, JCB, "user001", false);

        assertThat(response.isSuccess()).isFalse();
        verify(importBatchRepository, never()).save(any());
    }

    @Test
    void importFileRegistersAllRowsWhenNoErrors() throws Exception {
        ImportBatch saved = batch(200, 0, null);
        when(fileImporterFactory.getImporter(PaymentType.JCB)).thenReturn(importer);
        when(importBatchRepository.findByPaymentTypeAndTransferBatchIdIsNull(JCB))
                .thenReturn(List.of());
        when(importer.extractLookupKey(any())).thenReturn("11111111111111");
        when(importer.extractAllLookupKeys(any()))
                .thenReturn(List.of("11111111111111", "22222222222222"));
        when(importBatchRepository.save(any())).thenReturn(saved);
        when(importer.importFile(any(), any())).thenReturn(new ImportResult(3, 3, List.of()));

        ImportResponse response = service.importFile(validJcbFile(), JCB, "user001", false);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getImportedCount()).isEqualTo(3);
        assertThat(saved.getRecordCount()).isEqualTo(3);
        assertThat(saved.getErrorCount()).isZero();
    }

    @Test
    void importFileRequiresReplaceConfirmationWhenErroredBatchExists() throws Exception {
        when(fileImporterFactory.getImporter(PaymentType.JCB)).thenReturn(importer);
        when(importBatchRepository.findByPaymentTypeAndTransferBatchIdIsNull(JCB))
                .thenReturn(List.of(batch(70, 2, null)));

        ImportResponse response = service.importFile(validJcbFile(), JCB, "user001", false);

        assertThat(response.getReplaceConfirmation()).isNotNull();
        assertThat(response.getReplaceConfirmation().getExistingBatchId()).isEqualTo(70);
        verify(importer, never()).importFile(any(), any());
    }

    @Test
    void importFileReplacesErroredBatchWhenReplaceIsTrue() throws Exception {
        ImportBatch existing = batch(70, 2, null);
        ImportBatch saved = batch(201, 0, null);
        when(fileImporterFactory.getImporter(PaymentType.JCB)).thenReturn(importer);
        when(importBatchRepository.findByPaymentTypeAndTransferBatchIdIsNull(JCB))
                .thenReturn(List.of(existing));
        when(importer.extractLookupKey(any())).thenReturn("11111111111111");
        when(importer.extractAllLookupKeys(any())).thenReturn(List.of("11111111111111"));
        when(importBatchRepository.save(any())).thenReturn(saved);
        when(importer.importFile(any(), any())).thenReturn(new ImportResult(3, 3, List.of()));

        ImportResponse response = service.importFile(validJcbFile(), JCB, "user001", true);

        verify(importer).deleteBatchData(70);
        verify(importBatchRepository).delete(existing);
        assertThat(response.isSuccess()).isTrue();
    }

    @Test
    void importFileDoesNotConfirmWhenUnprocessedBatchHasNoErrorAndDifferentHash() throws Exception {
        ImportBatch saved = batch(202, 0, null);
        ImportBatch other = batch(71, 0, null);
        other.setFileHash("別ファイルのハッシュ値とは一致しないダミー値");
        when(fileImporterFactory.getImporter(PaymentType.JCB)).thenReturn(importer);
        when(importBatchRepository.findByPaymentTypeAndTransferBatchIdIsNull(JCB))
                .thenReturn(List.of(other));
        when(importer.extractLookupKey(any())).thenReturn("11111111111111");
        when(importer.extractAllLookupKeys(any())).thenReturn(List.of("11111111111111"));
        when(importBatchRepository.save(any())).thenReturn(saved);
        when(importer.importFile(any(), any())).thenReturn(new ImportResult(3, 3, List.of()));

        ImportResponse response = service.importFile(validJcbFile(), JCB, "user001", false);

        assertThat(response.getReplaceConfirmation()).isNull();
        assertThat(response.isSuccess()).isTrue();
    }

    @Test
    void importFileRequiresReplaceConfirmationWhenSameFileHashExistsWithoutErrors() throws Exception {
        ImportBatch existing = batch(72, 0, null);
        existing.setFileHash(sha256Hex(validJcbFile().getBytes()));
        existing.setLookupKeys("11111111111111,22222222222222");
        when(fileImporterFactory.getImporter(PaymentType.JCB)).thenReturn(importer);
        when(importBatchRepository.findByPaymentTypeAndTransferBatchIdIsNull(JCB))
                .thenReturn(List.of(existing));

        ImportResponse response = service.importFile(validJcbFile(), JCB, "user001", false);

        assertThat(response.getReplaceConfirmation()).isNotNull();
        assertThat(response.getReplaceConfirmation().getExistingBatchId()).isEqualTo(72);
        assertThat(response.getReplaceConfirmation().getLookupKeys())
                .containsExactly("11111111111111", "22222222222222");
        verify(importer, never()).importFile(any(), any());
    }

    private MockMultipartFile validJcbFile() {
        return CsvFiles.fromClasspath("jftd", "jcb_duplicate_key.csv");
    }

    private ImportBatch batch(int batchId, int errorCount, Integer transferBatchId) {
        ImportBatch batch = new ImportBatch();
        batch.setBatchId(batchId);
        batch.setFileName("jcb_duplicate_key.csv");
        batch.setRecordCount(1);
        batch.setErrorCount(errorCount);
        batch.setTransferBatchId(transferBatchId);
        return batch;
    }

    private String sha256Hex(byte[] content) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(content);
        StringBuilder sb = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
