package com.cupit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cupit.dto.SteraSmccRow;
import com.cupit.model.ImportBatch;
import com.cupit.model.SteraStore;
import com.cupit.repository.ImportBatchRepository;
import com.cupit.repository.SteraCodeSettlementDetailRepository;
import com.cupit.repository.SteraCodeSettlementDetailRepository.SteraCodeStoreGroupAggregate;
import com.cupit.repository.SteraCreditSalesDetailRepository;
import com.cupit.repository.SteraCreditSalesDetailRepository.SteraCreditStoreGroupAggregate;
import com.cupit.repository.SteraStoreRepository;

/**
 * {@link SteraSmccInquiryService} のテスト。
 * ①steraクレジット・stera codeの2フォーマットを1つの一覧にまとめること
 * ②締め日でのグループ化（nullを含む）③stera codeの店舗名はm_stera_storeから解決すること
 * ④stera codeのtransactionTypeは常にnullであること ⑤手数料のグループ単位丸め
 * ⑥ソート順、を検証する。
 */
@ExtendWith(MockitoExtension.class)
class SteraSmccInquiryServiceTest {

    private static final LocalDate CUTOFF_DATE = LocalDate.of(2025, 11, 30);

    @Mock
    private ImportBatchRepository importBatchRepository;

    @Mock
    private SteraCreditSalesDetailRepository steraCreditSalesDetailRepository;

    @Mock
    private SteraCodeSettlementDetailRepository steraCodeSettlementDetailRepository;

    @Mock
    private SteraStoreRepository steraStoreRepository;

    private SteraSmccInquiryService service;

    @BeforeEach
    void setUp() {
        service = new SteraSmccInquiryService(
                importBatchRepository, steraCreditSalesDetailRepository,
                steraCodeSettlementDetailRepository, steraStoreRepository);
        when(steraStoreRepository.findAll()).thenReturn(List.of());
    }

    private ImportBatch batch(int batchId, String paymentType, LocalDate cutoffDate) {
        ImportBatch b = new ImportBatch();
        b.setBatchId(batchId);
        b.setPaymentType(paymentType);
        b.setCutoffDate(cutoffDate);
        return b;
    }

    private SteraStore steraStore(String tradeCode, String storeName) {
        SteraStore store = new SteraStore();
        store.setTradeCode(tradeCode);
        store.setStoreName(storeName);
        return store;
    }

    private SteraCreditStoreGroupAggregate creditAggregate(
            String tradeCode, String merchantId, String storeName, String cardBrand,
            String transactionType, int batchId, long totalBillingAmount) {
        return new SteraCreditStoreGroupAggregate() {
            @Override
            public String getTradeCode() {
                return tradeCode;
            }

            @Override
            public String getMerchantId() {
                return merchantId;
            }

            @Override
            public String getStoreName() {
                return storeName;
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
            public Integer getBatchId() {
                return batchId;
            }

            @Override
            public Long getTotalBillingAmount() {
                return totalBillingAmount;
            }
        };
    }

    private SteraCodeStoreGroupAggregate codeAggregate(
            String tradeCode, String terminalId, String brand, int batchId, long totalSettlementAmount) {
        return new SteraCodeStoreGroupAggregate() {
            @Override
            public String getTradeCode() {
                return tradeCode;
            }

            @Override
            public String getTerminalId() {
                return terminalId;
            }

            @Override
            public String getBrand() {
                return brand;
            }

            @Override
            public Integer getBatchId() {
                return batchId;
            }

            @Override
            public Long getTotalSettlementAmount() {
                return totalSettlementAmount;
            }
        };
    }

    @Test
    void findAllReturnsEmptyWhenNoBatchesExist() {
        when(importBatchRepository.findAll()).thenReturn(List.of());

        assertThat(service.findAll()).isEmpty();
    }

    @Test
    void findAllMergesCreditAndCodeIntoOneList() {
        when(importBatchRepository.findAll()).thenReturn(List.of(
                batch(400, "steraクレジット", CUTOFF_DATE),
                batch(500, "stera code", CUTOFF_DATE)));
        when(steraCreditSalesDetailRepository.sumByMerchantCardBrandAndTransactionType(List.of(400)))
                .thenReturn(List.of(creditAggregate(
                        "01-020", "79890505", "花キュ－ピット　フラワ－ズギフト花久", "ＶＭ", "１回払", 400, 2200L)));
        when(steraCodeSettlementDetailRepository.sumByTerminalAndBrand(List.of(500)))
                .thenReturn(List.of(codeAggregate("02-044", "7113462036751", "PayPay", 500, 1000L)));
        when(steraStoreRepository.findAll())
                .thenReturn(List.of(steraStore("02-044", "尾田新生園")));

        List<SteraSmccRow> rows = service.findAll();

        assertThat(rows).hasSize(2);
        assertThat(rows).extracting(SteraSmccRow::getPaymentFormat)
                .containsExactlyInAnyOrder("steraクレジット", "stera code");
    }

    @Test
    void findAllResolvesCodeStoreNameFromSteraStoreAndLeavesTransactionTypeNull() {
        when(importBatchRepository.findAll()).thenReturn(List.of(batch(500, "stera code", CUTOFF_DATE)));
        when(steraCodeSettlementDetailRepository.sumByTerminalAndBrand(List.of(500)))
                .thenReturn(List.of(codeAggregate("02-044", "7113462036751", "PayPay", 500, 1000L)));
        when(steraStoreRepository.findAll())
                .thenReturn(List.of(steraStore("02-044", "尾田新生園")));

        List<SteraSmccRow> rows = service.findAll();

        assertThat(rows).hasSize(1);
        SteraSmccRow row = rows.get(0);
        assertThat(row.getStoreName()).isEqualTo("尾田新生園");
        assertThat(row.getStoreNumber()).isEqualTo("7113462036751");
        assertThat(row.getTransactionType()).isNull();
    }

    @Test
    void findAllComputesFeePerGroupForCredit() {
        // 仕入手数料 = 四捨五入(2200*0.0275) = 61円、当社手数料 = 四捨五入(2200*0.002) = 4円
        // 精算金額 = 2200-61-4 = 2135円
        when(importBatchRepository.findAll()).thenReturn(List.of(batch(400, "steraクレジット", CUTOFF_DATE)));
        when(steraCreditSalesDetailRepository.sumByMerchantCardBrandAndTransactionType(List.of(400)))
                .thenReturn(List.of(creditAggregate(
                        "01-020", "79890505", "花キュ－ピット　フラワ－ズギフト花久", "ＶＭ", "１回払", 400, 2200L)));

        List<SteraSmccRow> rows = service.findAll();

        assertThat(rows).hasSize(1);
        SteraSmccRow row = rows.get(0);
        assertThat(row.getSalesAmount()).isEqualTo(2200);
        assertThat(row.getAcquirerFee()).isEqualTo(61);
        assertThat(row.getCompanyFee()).isEqualTo(4);
        assertThat(row.getSettlementAmount()).isEqualTo(2135);
        assertThat(row.getCardBrand()).isEqualTo("ＶＭ");
        assertThat(row.getTransactionType()).isEqualTo("１回払");
    }

    @Test
    void findAllHandlesNullCutoffDateForBothFormatsWithoutThrowing() {
        when(importBatchRepository.findAll()).thenReturn(List.of(
                batch(400, "steraクレジット", null),
                batch(500, "stera code", null)));
        when(steraCreditSalesDetailRepository.sumByMerchantCardBrandAndTransactionType(List.of(400)))
                .thenReturn(List.of(creditAggregate("01-020", "79890505", "店舗A", "ＶＭ", "１回払", 400, 1000L)));
        when(steraCodeSettlementDetailRepository.sumByTerminalAndBrand(List.of(500)))
                .thenReturn(List.of(codeAggregate("02-044", "7113462036751", "PayPay", 500, 1000L)));

        List<SteraSmccRow> rows = service.findAll();

        assertThat(rows).hasSize(2);
        assertThat(rows).allSatisfy(row -> assertThat(row.getCutoffDate()).isNull());
    }

    @Test
    void findAllIgnoresUnrelatedPaymentTypes() {
        when(importBatchRepository.findAll()).thenReturn(List.of(batch(1, "stera JCB", CUTOFF_DATE)));

        assertThat(service.findAll()).isEmpty();
    }

    @Test
    void findAllSortsByTradeCodeThenStoreNumber() {
        when(importBatchRepository.findAll()).thenReturn(List.of(batch(400, "steraクレジット", CUTOFF_DATE)));
        when(steraCreditSalesDetailRepository.sumByMerchantCardBrandAndTransactionType(List.of(400)))
                .thenReturn(List.of(
                        creditAggregate("02-044", "79890513", "店舗B", "ＶＭ", "１回払", 400, 1000L),
                        creditAggregate("01-020", "79890505", "店舗A", "ＶＭ", "１回払", 400, 1000L)));

        List<SteraSmccRow> rows = service.findAll();

        assertThat(rows).extracting(SteraSmccRow::getTradeCode)
                .containsExactly("01-020", "02-044");
    }

}
