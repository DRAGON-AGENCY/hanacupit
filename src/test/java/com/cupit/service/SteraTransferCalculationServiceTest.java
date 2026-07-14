package com.cupit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cupit.model.ImportBatch;
import com.cupit.model.SteraStore;
import com.cupit.repository.ImportBatchRepository;
import com.cupit.repository.SteraCodeSettlementDetailRepository;
import com.cupit.repository.SteraCodeSettlementDetailRepository.SteraCodeGroupAggregate;
import com.cupit.repository.SteraCreditSalesDetailRepository;
import com.cupit.repository.SteraCreditSalesDetailRepository.SteraCreditGroupAggregate;
import com.cupit.repository.SteraJcbSalesDetailRepository;
import com.cupit.repository.SteraJcbSalesDetailRepository.SteraJcbGroupAggregate;
import com.cupit.repository.SteraStoreRepository;
import com.cupit.service.settlement.SteraTransferLineItem;

/**
 * SteraTransferCalculationService のテスト。リポジトリをモック化し、
 * 調査で実データ全件検証済みの手数料計算単位・丸め規則（取引コード「01-030」
 * 「01-047」、集計作業EXCEL参照）が1円単位で再現できることを検証する。
 */
@ExtendWith(MockitoExtension.class)
class SteraTransferCalculationServiceTest {

    private static final String PAYMENT_TYPE_JCB = "stera JCB";
    private static final String PAYMENT_TYPE_CODE = "stera code";
    private static final String PAYMENT_TYPE_CREDIT = "steraクレジット";
    private static final int CREDIT_BATCH_ID = 700;
    private static final int JCB_BATCH_ID = 800;
    private static final int CODE_BATCH_ID = 900;

    @Mock
    private ImportBatchRepository importBatchRepository;

    @Mock
    private SteraJcbSalesDetailRepository steraJcbSalesDetailRepository;

    @Mock
    private SteraCodeSettlementDetailRepository steraCodeSettlementDetailRepository;

    @Mock
    private SteraCreditSalesDetailRepository steraCreditSalesDetailRepository;

    @Mock
    private SteraStoreRepository steraStoreRepository;

    private SteraTransferCalculationService service;

    @BeforeEach
    void setUp() {
        service = new SteraTransferCalculationService(
                importBatchRepository,
                steraJcbSalesDetailRepository,
                steraCodeSettlementDetailRepository,
                steraCreditSalesDetailRepository,
                steraStoreRepository);
    }

    private void givenNoUnprocessedBatches(String paymentType) {
        when(importBatchRepository.findByPaymentTypeAndTransferBatchIdIsNull(paymentType))
                .thenReturn(List.of());
    }

    private void givenUnprocessedCreditBatch() {
        ImportBatch batch = new ImportBatch();
        batch.setBatchId(CREDIT_BATCH_ID);
        when(importBatchRepository.findByPaymentTypeAndTransferBatchIdIsNull(PAYMENT_TYPE_CREDIT))
                .thenReturn(List.of(batch));
    }

    private void givenStore(String tradeCode, String bankCode) {
        SteraStore store = new SteraStore();
        store.setTradeCode(tradeCode);
        store.setBankCode(bankCode);
        store.setBankName("三菱ＵＦＪ銀行");
        store.setBankBranchCode("001");
        store.setBranchName("本店");
        store.setAccountType("1");
        store.setAccountNo("1234567");
        store.setAccountHolderKana("ﾊﾅｷﾕ-ﾋﾟﾂﾄ");
        when(steraStoreRepository.findByTradeCode(tradeCode)).thenReturn(Optional.of(store));
    }

    private SteraCreditGroupAggregate creditAggregate(
            String tradeCode, String cardBrand, String transactionType, long totalBillingAmount) {
        return new SteraCreditGroupAggregate() {
            @Override
            public String getTradeCode() {
                return tradeCode;
            }

            @Override
            public String getCardBrand() {
                return cardBrand;
            }

            @Override
            public String getTransactionType() {
                return transactionType;
            }

            @Override
            public Long getTotalBillingAmount() {
                return totalBillingAmount;
            }
        };
    }

    /**
     * 取引コード「01-030」（VM 11,000円＋pWmc 4,070円、いずれも1回払）。
     * 仕入手数料415円・当社手数料30円は実データと完全一致する値
     * （11000×0.0275=302.5→303、4070×0.0275=111.925→112、303+112=415／
     * 11000×0.002=22、4070×0.002=8.14→8、22+8=30）。
     */
    @Test
    void matchesReferenceDataForTradeCode01030() {
        givenNoUnprocessedBatches(PAYMENT_TYPE_JCB);
        givenNoUnprocessedBatches(PAYMENT_TYPE_CODE);
        givenUnprocessedCreditBatch();
        when(steraCreditSalesDetailRepository.sumByTradeCodeCardBrandAndTransactionType(List.of(CREDIT_BATCH_ID)))
                .thenReturn(List.of(
                        creditAggregate("01-030", "VM", "1回払", 11000),
                        creditAggregate("01-030", "pWmc", "1回払", 4070)));
        givenStore("01-030", "0100");

        List<SteraTransferLineItem> result = service.calculateAllLineItems();

        assertThat(result).hasSize(1);
        SteraTransferLineItem item = result.get(0);
        assertThat(item.getTradeCode()).isEqualTo("01-030");
        assertThat(item.getGrossAmount()).isEqualTo(15070);
        assertThat(item.getAcquirerFee()).isEqualTo(415);
        assertThat(item.getCompanyFee()).isEqualTo(30);
        assertThat(item.getTransferFee()).isEqualTo(129);
        assertThat(item.getNetAmount()).isEqualTo(15070 - 415 - 30 - 129);
    }

    /**
     * 取引コード「01-047」（iD11,000＋pWmc40,326＋VM(1回払)236,767＋VM(2回払)24,750）。
     * 同一ブランド（VM）でも支払回数ごとに別グループとして丸めないと、実データと1円ズレる
     * ことを確認した回帰テスト（調査メモの教訓）。仕入手数料8,604円・当社手数料627円は
     * 実データと完全一致する値。
     */
    @Test
    void matchesReferenceDataForTradeCode01047GroupingByInstallmentCount() {
        givenNoUnprocessedBatches(PAYMENT_TYPE_JCB);
        givenNoUnprocessedBatches(PAYMENT_TYPE_CODE);
        givenUnprocessedCreditBatch();
        when(steraCreditSalesDetailRepository.sumByTradeCodeCardBrandAndTransactionType(List.of(CREDIT_BATCH_ID)))
                .thenReturn(List.of(
                        creditAggregate("01-047", "iD", "1回払", 11000),
                        creditAggregate("01-047", "pWmc", "1回払", 40326),
                        creditAggregate("01-047", "VM", "1回払", 236767),
                        creditAggregate("01-047", "VM", "2回払", 24750)));
        givenStore("01-047", "0100");

        List<SteraTransferLineItem> result = service.calculateAllLineItems();

        assertThat(result).hasSize(1);
        SteraTransferLineItem item = result.get(0);
        assertThat(item.getGrossAmount()).isEqualTo(11000 + 40326 + 236767 + 24750);
        assertThat(item.getAcquirerFee()).isEqualTo(8604);
        assertThat(item.getCompanyFee()).isEqualTo(627);
    }

    @Test
    void transferFeeIsZeroForGmoAozoraBankCode0310() {
        givenNoUnprocessedBatches(PAYMENT_TYPE_JCB);
        givenNoUnprocessedBatches(PAYMENT_TYPE_CODE);
        givenUnprocessedCreditBatch();
        when(steraCreditSalesDetailRepository.sumByTradeCodeCardBrandAndTransactionType(List.of(CREDIT_BATCH_ID)))
                .thenReturn(List.of(creditAggregate("02-200", "VM", "1回払", 10000)));
        givenStore("02-200", "0310");

        List<SteraTransferLineItem> result = service.calculateAllLineItems();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTransferFee()).isZero();
    }

    @Test
    void grossAmountSumsAcrossAllThreeFormatsForSameTradeCode() {
        ImportBatch jcbBatch = new ImportBatch();
        jcbBatch.setBatchId(JCB_BATCH_ID);
        when(importBatchRepository.findByPaymentTypeAndTransferBatchIdIsNull(PAYMENT_TYPE_JCB))
                .thenReturn(List.of(jcbBatch));
        ImportBatch codeBatch = new ImportBatch();
        codeBatch.setBatchId(CODE_BATCH_ID);
        when(importBatchRepository.findByPaymentTypeAndTransferBatchIdIsNull(PAYMENT_TYPE_CODE))
                .thenReturn(List.of(codeBatch));
        givenUnprocessedCreditBatch();

        when(steraJcbSalesDetailRepository.sumByTradeCodeCardNameAndPaymentMethod(List.of(JCB_BATCH_ID)))
                .thenReturn(List.of(new SteraJcbGroupAggregate() {
                    @Override
                    public String getTradeCode() {
                        return "03-300";
                    }

                    @Override
                    public String getCardName() {
                        return "【ＪＣＢカード】";
                    }

                    @Override
                    public String getPaymentMethod() {
                        return "１回払い";
                    }

                    @Override
                    public Long getTotalSalesAmount() {
                        return 1000L;
                    }
                }));
        when(steraCodeSettlementDetailRepository.sumByTradeCodeAndBrand(List.of(CODE_BATCH_ID)))
                .thenReturn(List.of(new SteraCodeGroupAggregate() {
                    @Override
                    public String getTradeCode() {
                        return "03-300";
                    }

                    @Override
                    public String getBrand() {
                        return "楽天ペイ";
                    }

                    @Override
                    public Long getTotalSettlementAmount() {
                        return 2000L;
                    }
                }));
        when(steraCreditSalesDetailRepository.sumByTradeCodeCardBrandAndTransactionType(List.of(CREDIT_BATCH_ID)))
                .thenReturn(List.of(creditAggregate("03-300", "VM", "1回払", 3000)));
        givenStore("03-300", "0100");

        List<SteraTransferLineItem> result = service.calculateAllLineItems();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTradeCode()).isEqualTo("03-300");
        assertThat(result.get(0).getGrossAmount()).isEqualTo(1000 + 2000 + 3000);
    }

    @Test
    void throwsWhenStoreAccountMissingAtConfirmTime() {
        givenNoUnprocessedBatches(PAYMENT_TYPE_JCB);
        givenNoUnprocessedBatches(PAYMENT_TYPE_CODE);
        givenUnprocessedCreditBatch();
        when(steraCreditSalesDetailRepository.sumByTradeCodeCardBrandAndTransactionType(List.of(CREDIT_BATCH_ID)))
                .thenReturn(List.of(creditAggregate("99-999", "VM", "1回払", 1000)));
        when(steraStoreRepository.findByTradeCode("99-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.calculateAllLineItems())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("振込先口座情報");
    }

}
