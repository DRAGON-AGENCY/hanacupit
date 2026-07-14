package com.cupit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
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
import com.cupit.model.SteraTransferBatch;
import com.cupit.model.SteraTransferDetail;
import com.cupit.repository.ImportBatchRepository;
import com.cupit.repository.SteraTransferBatchRepository;
import com.cupit.repository.SteraTransferDetailRepository;
import com.cupit.service.settlement.SteraTransferLineItem;

/**
 * SteraTransferConfirmService のテスト。{@link JftdTransferConfirmServiceTest}と
 * 同じ構造で、確定処理で計算結果（口座情報のスナップショットを含む）が保存され、
 * 対象インポートバッチにtransferBatchIdが設定されることを検証する。
 */
@ExtendWith(MockitoExtension.class)
class SteraTransferConfirmServiceTest {

    private static final int NEW_TRANSFER_BATCH_ID = 999;
    private static final String LOGIN_USER = "user001";

    @Mock
    private SteraTransferCalculationService calculationService;

    @Mock
    private SteraTransferBatchRepository transferBatchRepository;

    @Mock
    private SteraTransferDetailRepository transferDetailRepository;

    @Mock
    private ImportBatchRepository importBatchRepository;

    private SteraTransferConfirmService service;

    @BeforeEach
    void setUp() {
        service = new SteraTransferConfirmService(
                calculationService, transferBatchRepository, transferDetailRepository, importBatchRepository);

        when(calculationService.calculateAllLineItems(anyMap())).thenReturn(List.of(
                new SteraTransferLineItem("01-030", 15070, 415, 30, 129, 14496,
                        "0100", "三菱ＵＦＪ銀行", "001", "本店", "1", "1234567", "ﾊﾅｷﾕ-ﾋﾟﾂﾄ")));

        when(transferBatchRepository.save(any(SteraTransferBatch.class))).thenAnswer(invocation -> {
            SteraTransferBatch batch = invocation.getArgument(0);
            batch.setTransferBatchId(NEW_TRANSFER_BATCH_ID);
            return batch;
        });

        for (String paymentType : List.of("stera JCB", "stera code", "steraクレジット")) {
            when(importBatchRepository.lockUnprocessedByPaymentType(paymentType))
                    .thenReturn(List.of());
        }
    }

    @Test
    void confirmSavesCalculatedLineItemsAsSnapshot() {
        int result = service.confirm(LOGIN_USER);

        assertThat(result).isEqualTo(NEW_TRANSFER_BATCH_ID);

        ArgumentCaptor<SteraTransferDetail> detailCaptor = ArgumentCaptor.forClass(SteraTransferDetail.class);
        verify(transferDetailRepository).save(detailCaptor.capture());
        SteraTransferDetail savedDetail = detailCaptor.getValue();
        assertThat(savedDetail.getTransferBatchId()).isEqualTo(NEW_TRANSFER_BATCH_ID);
        assertThat(savedDetail.getTradeCode()).isEqualTo("01-030");
        assertThat(savedDetail.getGrossAmount()).isEqualTo(15070);
        assertThat(savedDetail.getAcquirerFee()).isEqualTo(415);
        assertThat(savedDetail.getCompanyFee()).isEqualTo(30);
        assertThat(savedDetail.getTransferFee()).isEqualTo(129);
        assertThat(savedDetail.getNetAmount()).isEqualTo(14496);
        assertThat(savedDetail.getBankCode()).isEqualTo("0100");
        assertThat(savedDetail.getAccountHolderKana()).isEqualTo("ﾊﾅｷﾕ-ﾋﾟﾂﾄ");
        assertThat(savedDetail.getUpdateEmployee()).isEqualTo(LOGIN_USER);
    }

    @Test
    void confirmMarksUnprocessedImportBatchesAsTransferred() {
        ImportBatch steraJcbBatch = new ImportBatch();
        steraJcbBatch.setBatchId(1);
        when(importBatchRepository.lockUnprocessedByPaymentType("stera JCB"))
                .thenReturn(List.of(steraJcbBatch));

        service.confirm(LOGIN_USER);

        ArgumentCaptor<ImportBatch> batchCaptor = ArgumentCaptor.forClass(ImportBatch.class);
        verify(importBatchRepository).save(batchCaptor.capture());
        ImportBatch savedBatch = batchCaptor.getValue();
        assertThat(savedBatch.getBatchId()).isEqualTo(1);
        assertThat(savedBatch.getTransferBatchId()).isEqualTo(NEW_TRANSFER_BATCH_ID);
        assertThat(savedBatch.getUpdateEmployee()).isEqualTo(LOGIN_USER);
    }

}
