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

import com.cupit.model.ImportBatch;
import com.cupit.model.JftdTransferBatch;
import com.cupit.model.JftdTransferDetail;
import com.cupit.repository.ImportBatchRepository;
import com.cupit.repository.JftdTransferBatchRepository;
import com.cupit.repository.JftdTransferDetailRepository;
import com.cupit.service.settlement.TransferLineItem;

/**
 * JftdTransferConfirmService のテスト。
 * 確定処理で計算結果がスナップショット保存され、対象インポートバッチに
 * transferBatchIdが設定されることを検証する。
 */
@ExtendWith(MockitoExtension.class)
class JftdTransferConfirmServiceTest {

    private static final int NEW_TRANSFER_BATCH_ID = 999;
    private static final String LOGIN_USER = "user001";

    @Mock
    private JftdTransferCalculationService calculationService;

    @Mock
    private JftdTransferBatchRepository transferBatchRepository;

    @Mock
    private JftdTransferDetailRepository transferDetailRepository;

    @Mock
    private ImportBatchRepository importBatchRepository;

    private JftdTransferConfirmService service;

    @BeforeEach
    void setUp() {
        service = new JftdTransferConfirmService(
                calculationService, transferBatchRepository, transferDetailRepository, importBatchRepository);

        when(calculationService.calculateAllLineItems()).thenReturn(List.of(
                new TransferLineItem("01-001", "3300024", 1, 14150)));

        when(transferBatchRepository.save(any(JftdTransferBatch.class))).thenAnswer(invocation -> {
            JftdTransferBatch batch = invocation.getArgument(0);
            batch.setTransferBatchId(NEW_TRANSFER_BATCH_ID);
            return batch;
        });

        for (String paymentType : List.of("JCB", "スマレジ", "ネットスターズ", "楽天ペイ", "住信SBI")) {
            when(importBatchRepository.findByPaymentTypeAndTransferBatchIdIsNull(paymentType))
                    .thenReturn(List.of());
        }
    }

    @Test
    void confirmSavesCalculatedLineItemsAsSnapshot() {
        int result = service.confirm(LOGIN_USER);

        assertThat(result).isEqualTo(NEW_TRANSFER_BATCH_ID);

        ArgumentCaptor<JftdTransferDetail> detailCaptor = ArgumentCaptor.forClass(JftdTransferDetail.class);
        verify(transferDetailRepository).save(detailCaptor.capture());
        JftdTransferDetail savedDetail = detailCaptor.getValue();
        assertThat(savedDetail.getTransferBatchId()).isEqualTo(NEW_TRANSFER_BATCH_ID);
        assertThat(savedDetail.getTradeCode()).isEqualTo("01-001");
        assertThat(savedDetail.getItemCode()).isEqualTo("3300024");
        assertThat(savedDetail.getAmount()).isEqualTo(14150);
        assertThat(savedDetail.getUpdateEmployee()).isEqualTo(LOGIN_USER);
    }

    @Test
    void confirmMarksUnprocessedImportBatchesAsTransferred() {
        ImportBatch jcbBatch = new ImportBatch();
        jcbBatch.setBatchId(1);
        when(importBatchRepository.findByPaymentTypeAndTransferBatchIdIsNull("JCB"))
                .thenReturn(List.of(jcbBatch));

        service.confirm(LOGIN_USER);

        ArgumentCaptor<ImportBatch> batchCaptor = ArgumentCaptor.forClass(ImportBatch.class);
        verify(importBatchRepository).save(batchCaptor.capture());
        ImportBatch savedBatch = batchCaptor.getValue();
        assertThat(savedBatch.getBatchId()).isEqualTo(1);
        assertThat(savedBatch.getTransferBatchId()).isEqualTo(NEW_TRANSFER_BATCH_ID);
        assertThat(savedBatch.getUpdateEmployee()).isEqualTo(LOGIN_USER);
    }

}
