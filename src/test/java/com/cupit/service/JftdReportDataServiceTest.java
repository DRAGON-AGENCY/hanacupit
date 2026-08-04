package com.cupit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cupit.model.SettlementItemCode;
import com.cupit.repository.ImportBatchRepository;
import com.cupit.repository.JftdTransferBatchRepository;
import com.cupit.repository.JftdTransferDetailRepository;
import com.cupit.repository.SettlementItemCodeRepository;
import com.cupit.service.settlement.ReportRow;
import com.cupit.service.settlement.TransferLineItem;

/**
 * {@link JftdReportDataService} のテスト。
 * {@code summarize()}（確定済みデータ用、厳密）と{@code summarizeForPreview()}
 * （プレビュー用、項目コードマスタ未整備でも例外を投げない）の挙動の違いを検証する。
 */
@ExtendWith(MockitoExtension.class)
class JftdReportDataServiceTest {

    @Mock
    private JftdTransferBatchRepository transferBatchRepository;

    @Mock
    private JftdTransferDetailRepository transferDetailRepository;

    @Mock
    private SettlementItemCodeRepository settlementItemCodeRepository;

    @Mock
    private ImportBatchRepository importBatchRepository;

    private JftdReportDataService service;

    @BeforeEach
    void setUp() {
        service = new JftdReportDataService(
                transferBatchRepository, transferDetailRepository,
                settlementItemCodeRepository, importBatchRepository);
    }

    private SettlementItemCode itemCode(
            String itemCode, String paymentCompany, String cardBrand, String amountType) {
        SettlementItemCode code = new SettlementItemCode();
        code.setItemCode(itemCode);
        code.setPaymentCompany(paymentCompany);
        code.setCardBrand(cardBrand);
        code.setAmountType(amountType);
        return code;
    }

    private TransferLineItem lineItem(String itemCode, int amount, int grossAmount) {
        return new TransferLineItem("01-001", itemCode, 1, amount, grossAmount, 0, 0, 0, 100);
    }

    @Test
    void summarizeThrowsWhenItemCodeNotFound() {
        when(settlementItemCodeRepository.findAll()).thenReturn(List.of());
        List<TransferLineItem> lineItems = List.of(lineItem("9999999999", 1000, 1000));

        assertThatThrownBy(() -> service.summarize(lineItems))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("9999999999");
    }

    @Test
    void summarizeAggregatesNormallyWhenItemCodeExists() {
        when(settlementItemCodeRepository.findAll())
                .thenReturn(List.of(itemCode("3300024", "JCB", "【ＪＣＢカード】", "PAYMENT")));
        List<TransferLineItem> lineItems = List.of(lineItem("3300024", 14150, 14550));

        List<ReportRow> rows = service.summarize(lineItems);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getPaymentCompany()).isEqualTo("JCB");
        assertThat(rows.get(0).getCardBrand()).isEqualTo("【ＪＣＢカード】");
        assertThat(rows.get(0).getPaymentAmount()).isEqualTo(14150);
    }

    /**
     * プレビュー専用の{@code summarizeForPreview()}は、
     * {@link JftdTransferCalculationService#calculateJcbLineItemsForInquiry}が未登録
     * カードブランドの代わりにitemCodeへ設定したカードブランド名を、項目コードマスタで
     * 解決できなくても例外を投げない（実際に発生した不具合の回帰テスト：確定処理と同じ
     * 厳密な集計をプレビューに使っていたため、未登録ブランドを含むデータをアップロード
     * するとプレビュー取得の時点でHTTP 500になっていた）。
     */
    @Test
    void summarizeForPreviewReturnsFallbackRowWhenItemCodeNotFound() {
        when(settlementItemCodeRepository.findAll()).thenReturn(List.of());
        List<TransferLineItem> lineItems = List.of(lineItem("【結合テスト用ブランド】", 5000, 5000));

        List<ReportRow> rows = service.summarizeForPreview(lineItems);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getPaymentCompany()).isEqualTo("JCB");
        assertThat(rows.get(0).getCardBrand()).isEqualTo("【結合テスト用ブランド】");
        assertThat(rows.get(0).getPaymentAmount()).isEqualTo(5000);
    }

    @Test
    void summarizeForPreviewAggregatesNormallyWhenItemCodeExists() {
        when(settlementItemCodeRepository.findAll())
                .thenReturn(List.of(itemCode("3300024", "JCB", "【ＪＣＢカード】", "PAYMENT")));
        List<TransferLineItem> lineItems = List.of(lineItem("3300024", 14150, 14550));

        List<ReportRow> rows = service.summarizeForPreview(lineItems);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getCardBrand()).isEqualTo("【ＪＣＢカード】");
        assertThat(rows.get(0).getPaymentAmount()).isEqualTo(14150);
    }

    @Test
    void summarizeForPreviewMergesFallbackAndNormalRowsSeparately() {
        when(settlementItemCodeRepository.findAll())
                .thenReturn(List.of(itemCode("3300024", "JCB", "【ＪＣＢカード】", "PAYMENT")));
        List<TransferLineItem> lineItems = List.of(
                lineItem("3300024", 14150, 14550),
                lineItem("【結合テスト用ブランド】", 5000, 5000));

        List<ReportRow> rows = service.summarizeForPreview(lineItems);

        assertThat(rows).hasSize(2);
        assertThat(rows).extracting(ReportRow::getCardBrand)
                .containsExactlyInAnyOrder("【ＪＣＢカード】", "【結合テスト用ブランド】");
    }
}
