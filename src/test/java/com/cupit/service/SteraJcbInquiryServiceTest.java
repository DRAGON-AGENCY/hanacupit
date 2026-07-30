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

import com.cupit.dto.SteraJcbRow;
import com.cupit.model.ImportBatch;
import com.cupit.model.SettlementFeeRate;
import com.cupit.repository.ImportBatchRepository;
import com.cupit.repository.SettlementFeeRateRepository;
import com.cupit.repository.SteraJcbSalesDetailRepository;
import com.cupit.repository.SteraJcbSalesDetailRepository.SteraJcbStoreGroupAggregate;

/**
 * {@link SteraJcbInquiryService} のテスト。
 * ①締め日でのグループ化（nullを含む）②支店（store_number）単位での明細分離
 * ③仕入手数料2.75%・当社手数料0.2%のグループ単位丸め ④取引コード・支店コード順ソート、
 * を検証する。
 */
@ExtendWith(MockitoExtension.class)
class SteraJcbInquiryServiceTest {

    private static final LocalDate CUTOFF_DATE = LocalDate.of(2025, 11, 30);
    private static final String PAYMENT_TYPE_STERA_JCB = "stera JCB";

    @Mock
    private ImportBatchRepository importBatchRepository;

    @Mock
    private SteraJcbSalesDetailRepository steraJcbSalesDetailRepository;

    @Mock
    private SettlementFeeRateRepository settlementFeeRateRepository;

    private SteraJcbInquiryService service;

    @BeforeEach
    void setUp() {
        service = new SteraJcbInquiryService(
                importBatchRepository, steraJcbSalesDetailRepository, settlementFeeRateRepository);
        when(settlementFeeRateRepository.findByPaymentCompanyAndCardBrand("stera terminal", "共通"))
                .thenReturn(java.util.Optional.of(feeRate("0.0275", "0.002")));
    }

    private SettlementFeeRate feeRate(String acquirerFeeRate, String companyFeeRate) {
        SettlementFeeRate rate = new SettlementFeeRate();
        rate.setAcquirerFeeRate(new java.math.BigDecimal(acquirerFeeRate));
        rate.setOurFeeRateBase(new java.math.BigDecimal(companyFeeRate));
        return rate;
    }

    private ImportBatch batch(int batchId, LocalDate cutoffDate) {
        ImportBatch b = new ImportBatch();
        b.setBatchId(batchId);
        b.setPaymentType(PAYMENT_TYPE_STERA_JCB);
        b.setCutoffDate(cutoffDate);
        return b;
    }

    private SteraJcbStoreGroupAggregate aggregate(
            String tradeCode, String storeNumber, String storeName,
            String cardName, String paymentMethod, int batchId, long totalSalesAmount) {
        return aggregate(tradeCode, storeNumber, storeName, cardName, paymentMethod, "1回払い",
                batchId, totalSalesAmount);
    }

    private SteraJcbStoreGroupAggregate aggregate(
            String tradeCode, String storeNumber, String storeName,
            String cardName, String paymentMethod, String paymentType, int batchId, long totalSalesAmount) {
        return new SteraJcbStoreGroupAggregate() {
            @Override
            public String getTradeCode() {
                return tradeCode;
            }

            @Override
            public String getStoreNumber() {
                return storeNumber;
            }

            @Override
            public String getStoreName() {
                return storeName;
            }

            @Override
            public String getCardName() {
                return cardName;
            }

            @Override
            public String getPaymentMethod() {
                return paymentMethod;
            }

            @Override
            public String getPaymentType() {
                return paymentType;
            }

            @Override
            public Integer getBatchId() {
                return batchId;
            }

            @Override
            public Long getTotalSalesAmount() {
                return totalSalesAmount;
            }
        };
    }

    @Test
    void findAllReturnsEmptyWhenNoBatchesExist() {
        when(importBatchRepository.findAll()).thenReturn(List.of());

        assertThat(service.findAll()).isEmpty();
    }

    @Test
    void findAllHandlesNullCutoffDateWithoutThrowing() {
        when(importBatchRepository.findAll()).thenReturn(List.of(batch(100, null)));
        when(steraJcbSalesDetailRepository.sumByStoreCardNameAndPaymentMethod(List.of(100)))
                .thenReturn(List.of(aggregate(
                        "01-020", "01-020000", "フラワーブティック エハラ", "クレジット", "1回払い", 100, 2090L)));

        List<SteraJcbRow> rows = service.findAll();

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getCutoffDate()).isNull();
    }

    @Test
    void findAllComputesAcquirerAndCompanyFeePerGroup() {
        // 仕入手数料 = 四捨五入(2090*0.0275) = 57円、当社手数料 = 四捨五入(2090*0.002) = 4円
        // 精算金額 = 2090-57-4 = 2029円（SteraTransferCalculationServiceの既存検証済みレートと同じ）
        when(importBatchRepository.findAll()).thenReturn(List.of(batch(100, CUTOFF_DATE)));
        when(steraJcbSalesDetailRepository.sumByStoreCardNameAndPaymentMethod(List.of(100)))
                .thenReturn(List.of(aggregate(
                        "01-020", "01-020000", "フラワーブティック エハラ", "クレジット", "1回払い", 100, 2090L)));

        List<SteraJcbRow> rows = service.findAll();

        assertThat(rows).hasSize(1);
        SteraJcbRow row = rows.get(0);
        assertThat(row.getSalesAmount()).isEqualTo(2090);
        assertThat(row.getAcquirerFee()).isEqualTo(57);
        assertThat(row.getCompanyFee()).isEqualTo(4);
        assertThat(row.getSettlementAmount()).isEqualTo(2029);
        assertThat(row.getCutoffDate()).isEqualTo(CUTOFF_DATE);
    }

    @Test
    void findAllUsesFeeRateFromMasterNotHardcodedValue() {
        // 手数料率マスタ（m_settlement_fee_rate）の値を旧ハードコード値（2.75%/0.2%）とは
        // 異なる値に差し替え、計算結果がマスタ側の値に追従することを確認する
        // （固定値を参照しているのではないことの証明）。
        when(settlementFeeRateRepository.findByPaymentCompanyAndCardBrand("stera terminal", "共通"))
                .thenReturn(java.util.Optional.of(feeRate("0.03", "0.005")));
        when(importBatchRepository.findAll()).thenReturn(List.of(batch(100, CUTOFF_DATE)));
        when(steraJcbSalesDetailRepository.sumByStoreCardNameAndPaymentMethod(List.of(100)))
                .thenReturn(List.of(aggregate(
                        "01-020", "01-020000", "店舗A", "クレジット", "1回払い", 100, 2000L)));

        List<SteraJcbRow> rows = service.findAll();

        assertThat(rows).hasSize(1);
        SteraJcbRow row = rows.get(0);
        assertThat(row.getAcquirerFee()).isEqualTo(60);
        assertThat(row.getCompanyFee()).isEqualTo(10);
        assertThat(row.getSettlementAmount()).isEqualTo(1930);
    }

    @Test
    void findAllThrowsWhenFeeRateMasterRowMissing() {
        when(settlementFeeRateRepository.findByPaymentCompanyAndCardBrand("stera terminal", "共通"))
                .thenReturn(java.util.Optional.empty());
        when(importBatchRepository.findAll()).thenReturn(List.of(batch(100, CUTOFF_DATE)));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.findAll())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("手数料率マスタ");
    }

    @Test
    void findAllKeepsDifferentStoreNumbersUnderSameTradeCodeAsSeparateRows() {
        // 1取引コードに複数の支店（store_number）が存在する運用があり、支店ごとに
        // 店舗名も異なるため、合算せず別々の行として返すことを確認する。
        when(importBatchRepository.findAll()).thenReturn(List.of(batch(200, CUTOFF_DATE)));
        when(steraJcbSalesDetailRepository.sumByStoreCardNameAndPaymentMethod(List.of(200)))
                .thenReturn(List.of(
                        aggregate("02-044", "02-044000", "尾田新生園", "クレジット", "1回払い", 200, 4400L),
                        aggregate("02-044", "02-044001", "尾田新生園 フラワーショップおだいっきゅう店",
                                "クレジット", "1回払い", 200, 8130L)));

        List<SteraJcbRow> rows = service.findAll();

        assertThat(rows).hasSize(2);
        assertThat(rows).extracting(SteraJcbRow::getStoreNumber)
                .containsExactly("02-044000", "02-044001");
        assertThat(rows).extracting(SteraJcbRow::getStoreName)
                .containsExactly("尾田新生園", "尾田新生園 フラワーショップおだいっきゅう店");
    }

    @Test
    void findAllGroupsBatchesByCutoffDateSeparately() {
        LocalDate otherDate = LocalDate.of(2025, 12, 31);
        when(importBatchRepository.findAll()).thenReturn(List.of(
                batch(100, CUTOFF_DATE),
                batch(101, otherDate)));
        when(steraJcbSalesDetailRepository.sumByStoreCardNameAndPaymentMethod(List.of(100)))
                .thenReturn(List.of(aggregate("01-020", "01-020000", "店舗A", "クレジット", "1回払い", 100, 1000L)));
        when(steraJcbSalesDetailRepository.sumByStoreCardNameAndPaymentMethod(List.of(101)))
                .thenReturn(List.of(aggregate("01-020", "01-020000", "店舗A", "クレジット", "1回払い", 101, 2000L)));

        List<SteraJcbRow> rows = service.findAll();

        assertThat(rows).hasSize(2);
        assertThat(rows).extracting(SteraJcbRow::getCutoffDate)
                .containsExactlyInAnyOrder(CUTOFF_DATE, otherDate);
    }

    @Test
    void findAllKeepsPaymentTypeSeparateFromPaymentMethod() {
        // payment_method（お支払方法、クレジット/QUICPay等）とpayment_type（支払区分、
        // 1回払い/2回払い等）は別次元であり、同じカード名・お支払方法でも支払区分が
        // 異なれば別の行として保持することを確認する（課題表項番28の教訓）。
        when(importBatchRepository.findAll()).thenReturn(List.of(batch(100, CUTOFF_DATE)));
        when(steraJcbSalesDetailRepository.sumByStoreCardNameAndPaymentMethod(List.of(100)))
                .thenReturn(List.of(
                        aggregate("01-020", "01-020000", "店舗A", "【ＪＣＢカード】",
                                "◆クレジット", "１回払い", 100, 1000L),
                        aggregate("01-020", "01-020000", "店舗A", "【ＪＣＢカード】",
                                "◆クレジット", "２回払い", 100, 2000L)));

        List<SteraJcbRow> rows = service.findAll();

        assertThat(rows).hasSize(2);
        assertThat(rows).extracting(SteraJcbRow::getPaymentType)
                .containsExactlyInAnyOrder("１回払い", "２回払い");
        assertThat(rows).allSatisfy(row -> assertThat(row.getPaymentMethod()).isEqualTo("◆クレジット"));
    }

    @Test
    void findAllSortsByTradeCodeThenStoreNumber() {
        when(importBatchRepository.findAll()).thenReturn(List.of(batch(100, CUTOFF_DATE)));
        when(steraJcbSalesDetailRepository.sumByStoreCardNameAndPaymentMethod(List.of(100)))
                .thenReturn(List.of(
                        aggregate("02-044", "02-044001", "店舗B", "クレジット", "1回払い", 100, 1000L),
                        aggregate("01-020", "01-020000", "店舗A", "クレジット", "1回払い", 100, 1000L),
                        aggregate("02-044", "02-044000", "店舗C", "クレジット", "1回払い", 100, 1000L)));

        List<SteraJcbRow> rows = service.findAll();

        assertThat(rows).extracting(SteraJcbRow::getStoreNumber)
                .containsExactly("01-020000", "02-044000", "02-044001");
    }

}
