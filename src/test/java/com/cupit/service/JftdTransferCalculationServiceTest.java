package com.cupit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cupit.model.ImportBatch;
import com.cupit.model.NetstarSalesSummary;
import com.cupit.model.SettlementFeeRate;
import com.cupit.model.SettlementItemCode;
import com.cupit.repository.ImportBatchRepository;
import com.cupit.repository.JcbSalesDetailRepository;
import com.cupit.repository.JcbSalesDetailRepository.JcbBrandAggregate;
import com.cupit.repository.NetstarSalesSummaryRepository;
import com.cupit.repository.RakutenPayTransactionRepository;
import com.cupit.repository.RakutenPayTransactionRepository.RakutenPayAggregate;
import com.cupit.repository.SettlementFeeRateRepository;
import com.cupit.repository.SettlementItemCodeRepository;
import com.cupit.repository.TerminalMonthlyFeeRepository;
import com.cupit.repository.TerminalMonthlyFeeRepository.TerminalFeeAggregate;
import com.cupit.repository.VisaMasterTransactionRepository;
import com.cupit.repository.VisaMasterTransactionRepository.VisaMasterAggregate;
import com.cupit.service.settlement.SettlementFeeCalculator;
import com.cupit.service.settlement.TransferLineItem;

/**
 * JftdTransferCalculationService のテスト。
 * リポジトリをモック化し、各決済会社の参照Excel実データから
 * 決済手数料①・支払金額①が1円単位で再現できることを検証する。
 * 手数料②（弊社→加盟店の上乗せ手数料）は正しい計算式が未解決のため
 * 検証対象に含めない（調査メモ「論点・オープン事項」項番6を参照）。
 */
@ExtendWith(MockitoExtension.class)
class JftdTransferCalculationServiceTest {

    private static final String JCB_BRAND = "【ＪＣＢカード】";
    private static final String JCB_PAYMENT_ITEM_CODE = "3300024";
    private static final int JCB_BATCH_ID = 100;
    private static final int SUMAREJO_BATCH_ID = 200;
    private static final int NETSTAR_BATCH_ID = 300;
    private static final int RAKUTENPAY_BATCH_ID = 400;
    private static final int VISA_MASTER_BATCH_ID = 500;

    @Mock
    private ImportBatchRepository importBatchRepository;

    @Mock
    private JcbSalesDetailRepository jcbSalesDetailRepository;

    @Mock
    private TerminalMonthlyFeeRepository terminalMonthlyFeeRepository;

    @Mock
    private NetstarSalesSummaryRepository netstarSalesSummaryRepository;

    @Mock
    private RakutenPayTransactionRepository rakutenPayTransactionRepository;

    @Mock
    private VisaMasterTransactionRepository visaMasterTransactionRepository;

    @Mock
    private SettlementFeeRateRepository settlementFeeRateRepository;

    @Mock
    private SettlementItemCodeRepository settlementItemCodeRepository;

    private JftdTransferCalculationService service;

    @BeforeEach
    void setUp() {
        service = new JftdTransferCalculationService(
                importBatchRepository,
                jcbSalesDetailRepository,
                terminalMonthlyFeeRepository,
                netstarSalesSummaryRepository,
                rakutenPayTransactionRepository,
                visaMasterTransactionRepository,
                settlementFeeRateRepository,
                settlementItemCodeRepository,
                new SettlementFeeCalculator());
    }

    private void givenUnprocessedBatch(String paymentType, int batchId) {
        ImportBatch batch = new ImportBatch();
        batch.setBatchId(batchId);
        when(importBatchRepository.findByPaymentTypeAndTransferBatchIdIsNull(paymentType))
                .thenReturn(List.of(batch));
    }

    private void givenFeeRateAndItemCode(
            String paymentCompany, String cardBrand, String calcModel,
            String acquirerFeeRate, String itemCode) {
        givenFeeRate(paymentCompany, cardBrand, calcModel, acquirerFeeRate);
        givenItemCode(paymentCompany, cardBrand, itemCode);
    }

    private void givenFeeRate(
            String paymentCompany, String cardBrand, String calcModel, String acquirerFeeRate) {
        SettlementFeeRate rate = new SettlementFeeRate();
        rate.setPaymentCompany(paymentCompany);
        rate.setCardBrand(cardBrand);
        rate.setCalcModel(calcModel);
        rate.setAcquirerFeeRate(new BigDecimal(acquirerFeeRate));
        when(settlementFeeRateRepository.findByPaymentCompanyAndCardBrand(paymentCompany, cardBrand))
                .thenReturn(Optional.of(rate));
    }

    private void givenItemCode(String paymentCompany, String cardBrand, String itemCode) {
        SettlementItemCode code = new SettlementItemCode();
        code.setPaymentCompany(paymentCompany);
        code.setCardBrand(cardBrand);
        code.setAmountType("PAYMENT");
        code.setItemCode(itemCode);
        when(settlementItemCodeRepository.findByPaymentCompanyAndCardBrandAndAmountType(
                paymentCompany, cardBrand, "PAYMENT"))
                .thenReturn(Optional.of(code));
    }

    @Test
    void calculateJcbLineItemsMatchesReferenceDataForTradeCode01001() {
        // 取引コード01-001: 売上件数5・売上金額14,550円
        // 決済手数料① = 14550*0.0275 = 400.125 → 切り捨てで400円（JCB_集計シートD4と一致）
        // 支払金額① = 14550-400 = 14150円（JCB_集計シートE4と一致）
        givenUnprocessedBatch("JCB", JCB_BATCH_ID);
        givenFeeRateAndItemCode("JCB", JCB_BRAND, "STRAIGHT", "0.0275", JCB_PAYMENT_ITEM_CODE);
        givenJcbAggregate("01-001", 5, 14550);

        List<TransferLineItem> result = service.calculateJcbLineItems();

        assertThat(result).hasSize(1);
        TransferLineItem item = result.get(0);
        assertThat(item.getTradeCode()).isEqualTo("01-001");
        assertThat(item.getItemCode()).isEqualTo(JCB_PAYMENT_ITEM_CODE);
        assertThat(item.getQuantity()).isEqualTo(1);
        assertThat(item.getAmount()).isEqualTo(14150);
    }

    @Test
    void calculateJcbLineItemsTruncatesRoundingInsteadOfHalfUp() {
        // 取引コード04-016: 売上件数6・売上金額44,020円
        // 決済手数料① = 44020*0.0275 = 1210.55 → 切り捨てで1210円（JCB_集計シートD11と一致）
        // 四捨五入だと1211円になり、シートの実際の値とズレることを確認する境界値ケース。
        givenUnprocessedBatch("JCB", JCB_BATCH_ID);
        givenFeeRateAndItemCode("JCB", JCB_BRAND, "STRAIGHT", "0.0275", JCB_PAYMENT_ITEM_CODE);
        givenJcbAggregate("04-016", 6, 44020);

        List<TransferLineItem> result = service.calculateJcbLineItems();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualTo(42810);
    }

    @Test
    void calculateJcbLineItemsMatchesReferenceDataForTradeCode03048() {
        // 取引コード03-048: 売上件数24・売上金額169,416円
        // 決済手数料① = 169416*0.0275 = 4658.94 → 切り捨てで4658円（JCB_集計シートD7と一致）
        // 支払金額① = 169416-4658 = 164758円（JCB_集計シートE7と一致）
        givenUnprocessedBatch("JCB", JCB_BATCH_ID);
        givenFeeRateAndItemCode("JCB", JCB_BRAND, "STRAIGHT", "0.0275", JCB_PAYMENT_ITEM_CODE);
        givenJcbAggregate("03-048", 24, 169416);

        List<TransferLineItem> result = service.calculateJcbLineItems();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualTo(164758);
    }

    @Test
    void calculateJcbLineItemsMatchesReferenceDataAtHigherTransactionVolume() {
        // 取引コード68-012: 売上件数75・売上金額354,528円。交通系電子マネー・住信SBIで
        // 「明細件数が多いと単一レート一括計算がズレる」問題が見つかったため、
        // これまで検証した最大件数(24件)より多いケースでJCB本体も同様の問題が
        // 無いか確認する境界値ケース。
        // 決済手数料① = 354528*0.0275 = 9749.52 → 切り捨てで9749円（JCB_集計シートと一致）
        // 支払金額① = 354528-9749 = 344779円（JCB_集計シートと一致）
        givenUnprocessedBatch("JCB", JCB_BATCH_ID);
        givenFeeRateAndItemCode("JCB", JCB_BRAND, "STRAIGHT", "0.0275", JCB_PAYMENT_ITEM_CODE);
        givenJcbAggregate("68-012", 75, 354528);

        List<TransferLineItem> result = service.calculateJcbLineItems();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualTo(344779);
    }

    @Test
    void calculateJcbLineItemsReturnsEmptyWhenNoUnprocessedBatches() {
        when(importBatchRepository.findByPaymentTypeAndTransferBatchIdIsNull("JCB"))
                .thenReturn(List.of());

        List<TransferLineItem> result = service.calculateJcbLineItems();

        assertThat(result).isEmpty();
    }

    @Test
    void calculateJcbLineItemsSkipsBrandsWithUnverifiedPurchaseCollectFormula() {
        // 交通系電子マネー・ｎａｎａｃｏ・ＷＡＯＮは、実データでどの丸め方式を試しても
        // 一致しない取引コードが見つかったため、正しい計算式が判明するまでスキップする
        // （手数料率・項目コードマスタへの問い合わせも行われないことを確認する）。
        givenUnprocessedBatch("JCB", JCB_BATCH_ID);
        JcbBrandAggregate aggregate = new JcbBrandAggregate() {
            @Override
            public String getTradeCode() {
                return "35-026";
            }

            @Override
            public String getCardName() {
                return "【交通系電子マネー】";
            }

            @Override
            public Long getTotalSalesCount() {
                return 35L;
            }

            @Override
            public Long getTotalSalesAmount() {
                return 41877L;
            }
        };
        when(jcbSalesDetailRepository.sumByTradeCodeAndCardName(List.of(JCB_BATCH_ID)))
                .thenReturn(List.of(aggregate));

        List<TransferLineItem> result = service.calculateJcbLineItems();

        assertThat(result).isEmpty();
    }

    @Test
    void calculateSumarejoLineItemsGeneratesBaseFeeOnlyForStandardTerminal() {
        // 単価700円（標準）の端末1台のみの場合、本体3300217=700円のみ生成される。
        givenUnprocessedBatch("スマレジ", SUMAREJO_BATCH_ID);
        givenItemCode("スマレジ(端末月額)", "本体", "3300217");
        givenItemCode("スマレジ(端末月額)", "調整", "3300219");

        when(terminalMonthlyFeeRepository.sumByTradeCodeAndUnitPrice(List.of(SUMAREJO_BATCH_ID)))
                .thenReturn(List.of(fixedTerminalFeeAggregate("01-001", 700, 1L)));

        List<TransferLineItem> result = service.calculateSumarejoLineItems();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTradeCode()).isEqualTo("01-001");
        assertThat(result.get(0).getItemCode()).isEqualTo("3300217");
        assertThat(result.get(0).getAmount()).isEqualTo(700);
    }

    @Test
    void calculateSumarejoLineItemsSplitsBaseAndPremiumForHigherTierTerminal() {
        // 取引コード02-030: 単価1800円の端末1台。11_月額利用料_端末.xlsxの実データと一致
        // （本体700円 + 調整1100円 = 1800円）。
        givenUnprocessedBatch("スマレジ", SUMAREJO_BATCH_ID);
        givenItemCode("スマレジ(端末月額)", "本体", "3300217");
        givenItemCode("スマレジ(端末月額)", "調整", "3300219");

        when(terminalMonthlyFeeRepository.sumByTradeCodeAndUnitPrice(List.of(SUMAREJO_BATCH_ID)))
                .thenReturn(List.of(fixedTerminalFeeAggregate("02-030", 1800, 1L)));

        List<TransferLineItem> result = service.calculateSumarejoLineItems();

        assertThat(result).hasSize(2);
        TransferLineItem baseItem = result.stream()
                .filter(i -> i.getItemCode().equals("3300217")).findFirst().orElseThrow();
        TransferLineItem premiumItem = result.stream()
                .filter(i -> i.getItemCode().equals("3300219")).findFirst().orElseThrow();
        assertThat(baseItem.getAmount()).isEqualTo(700);
        assertThat(premiumItem.getAmount()).isEqualTo(1100);
    }

    @Test
    void calculateSumarejoLineItemsSkipsHqTestTradeCode() {
        // 取引コード40-879は花キューピット自社のHQテスト端末（加盟店名「花キューピット
        // 正会員店(本部テスト用)」）。11_月額利用料_端末.xlsxのclaim_detailでは単価1800円
        // だが、3300217・3300219シートの実際の請求明細行には1件も出現しないため、
        // 本体・調整とも生成してはならない。
        givenUnprocessedBatch("スマレジ", SUMAREJO_BATCH_ID);
        givenItemCode("スマレジ(端末月額)", "本体", "3300217");
        givenItemCode("スマレジ(端末月額)", "調整", "3300219");

        when(terminalMonthlyFeeRepository.sumByTradeCodeAndUnitPrice(List.of(SUMAREJO_BATCH_ID)))
                .thenReturn(List.of(fixedTerminalFeeAggregate("40-879", 1800, 1L)));

        List<TransferLineItem> result = service.calculateSumarejoLineItems();

        assertThat(result).isEmpty();
    }

    private TerminalFeeAggregate fixedTerminalFeeAggregate(String tradeCode, int unitPrice, long terminalCount) {
        return new TerminalFeeAggregate() {
            @Override
            public String getTradeCode() {
                return tradeCode;
            }

            @Override
            public Integer getUnitPrice() {
                return unitPrice;
            }

            @Override
            public Long getTerminalCount() {
                return terminalCount;
            }
        };
    }

    @Test
    void calculateNetstarLineItemsMatchesReferenceDataForAlipayStraightModel() {
        // ネットスターズAlipay 取引コード35-026: 差引金額660円、レート1.70%（STRAIGHT）
        // 決済手数料① = 660*0.017 = 11.22 → 切り捨てで11円（Alipayシートと一致）
        // 支払金額① = 660-11 = 649円（Alipayシートの支払金額①と一致）
        givenUnprocessedBatch("ネットスターズ", NETSTAR_BATCH_ID);
        givenFeeRateAndItemCode("ネットスターズ", "Alipay", "STRAIGHT", "0.017", "3300007");

        NetstarSalesSummary row = new NetstarSalesSummary();
        row.setTradeCode("35-026");
        row.setAlipayNetAmount(660);
        when(netstarSalesSummaryRepository.findByBatchIdIn(List.of(NETSTAR_BATCH_ID)))
                .thenReturn(List.of(row));

        List<TransferLineItem> result = service.calculateNetstarLineItems();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTradeCode()).isEqualTo("35-026");
        assertThat(result.get(0).getItemCode()).isEqualTo("3300007");
        assertThat(result.get(0).getAmount()).isEqualTo(649);
    }

    @Test
    void calculateNetstarLineItemsMatchesReferenceDataForPayPayPurchaseCollectModel() {
        // ネットスターズPayPay 取引コード01-001: 差引金額3,250円、本体レート2.65%
        // 仕入手数料本体 = 四捨五入(3250*0.0265) = 86円
        // 仕入手数料消費税 = 切り捨て(86*0.1) = 8円 → 仕入手数料合計94円
        // 振込金額(預り金) = 3250-94 = 3156円（PayPayシートの振込金額(預り金)と一致）
        givenUnprocessedBatch("ネットスターズ", NETSTAR_BATCH_ID);
        givenFeeRateAndItemCode("ネットスターズ", "PayPay", "PURCHASE_COLLECT", "0.0265", "3300010");

        NetstarSalesSummary row = new NetstarSalesSummary();
        row.setTradeCode("01-001");
        row.setPaypayNetAmount(3250);
        when(netstarSalesSummaryRepository.findByBatchIdIn(List.of(NETSTAR_BATCH_ID)))
                .thenReturn(List.of(row));

        List<TransferLineItem> result = service.calculateNetstarLineItems();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualTo(3156);
    }

    @Test
    void calculateNetstarLineItemsMatchesReferenceDataForPayPayAtHighTransactionVolume() {
        // ネットスターズPayPay 取引コード68-012: 差引金額820,200円（281件分の合算）。
        // 交通系電子マネー等で見つかった「明細行数が多いと単一レート一括計算が
        // ズレる」問題がPayPayでも起きないか、高ボリュームの実データで確認する境界値ケース。
        // 仕入手数料本体 = 四捨五入(820200*0.0265) = 21735円
        // 仕入手数料消費税 = 切り捨て(21735*0.1) = 2173円 → 合計23908円
        // 振込金額(預り金) = 820200-23908 = 796292円（PayPayシートの実績値と一致）
        givenUnprocessedBatch("ネットスターズ", NETSTAR_BATCH_ID);
        givenFeeRateAndItemCode("ネットスターズ", "PayPay", "PURCHASE_COLLECT", "0.0265", "3300010");

        NetstarSalesSummary row = new NetstarSalesSummary();
        row.setTradeCode("68-012");
        row.setPaypayNetAmount(820200);
        when(netstarSalesSummaryRepository.findByBatchIdIn(List.of(NETSTAR_BATCH_ID)))
                .thenReturn(List.of(row));

        List<TransferLineItem> result = service.calculateNetstarLineItems();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualTo(796292);
    }

    @Test
    void calculateNetstarLineItemsMatchesReferenceDataForDpayPurchaseCollectModel() {
        // ネットスターズd払い 取引コード01-024: 差引金額28,850円、本体レート2.6%
        // 仕入手数料本体 = 四捨五入(28850*0.026) = 750円
        // 仕入手数料消費税 = 切り捨て(750*0.1) = 75円 → 仕入手数料合計825円
        // 振込金額(預り金) = 28850-825 = 28025円（d払いシートの振込金額(預り金)と一致）
        givenUnprocessedBatch("ネットスターズ", NETSTAR_BATCH_ID);
        givenFeeRateAndItemCode("ネットスターズ", "d払い", "PURCHASE_COLLECT", "0.026", "3300013");

        NetstarSalesSummary row = new NetstarSalesSummary();
        row.setTradeCode("01-024");
        row.setDpayNetAmount(28850);
        when(netstarSalesSummaryRepository.findByBatchIdIn(List.of(NETSTAR_BATCH_ID)))
                .thenReturn(List.of(row));

        List<TransferLineItem> result = service.calculateNetstarLineItems();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualTo(28025);
    }

    @Test
    void calculateNetstarLineItemsMatchesReferenceDataForDpayAtHighTransactionVolume() {
        // ネットスターズd払い 取引コード23-004: 差引金額381,098円（155件分の合算）。
        // 仕入手数料本体 = 四捨五入(381098*0.026) = 9909円
        // 仕入手数料消費税 = 切り捨て(9909*0.1) = 990円 → 合計10899円
        // 振込金額(預り金) = 381098-10899 = 370199円（d払いシートの実績値と一致）
        givenUnprocessedBatch("ネットスターズ", NETSTAR_BATCH_ID);
        givenFeeRateAndItemCode("ネットスターズ", "d払い", "PURCHASE_COLLECT", "0.026", "3300013");

        NetstarSalesSummary row = new NetstarSalesSummary();
        row.setTradeCode("23-004");
        row.setDpayNetAmount(381098);
        when(netstarSalesSummaryRepository.findByBatchIdIn(List.of(NETSTAR_BATCH_ID)))
                .thenReturn(List.of(row));

        List<TransferLineItem> result = service.calculateNetstarLineItems();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualTo(370199);
    }

    @Test
    void calculateNetstarLineItemsSkipsBrandsWithZeroAmount() {
        givenUnprocessedBatch("ネットスターズ", NETSTAR_BATCH_ID);
        NetstarSalesSummary row = new NetstarSalesSummary();
        row.setTradeCode("01-001");
        // 全ブランド0円のまま
        when(netstarSalesSummaryRepository.findByBatchIdIn(List.of(NETSTAR_BATCH_ID)))
                .thenReturn(List.of(row));

        List<TransferLineItem> result = service.calculateNetstarLineItems();

        assertThat(result).isEmpty();
    }

    @Test
    void calculateRakutenPayLineItemsMatchesReferenceData() {
        // 楽天ペイ 取引コード01-024: 取引金額22,550円、本体レート2.8%
        // 仕入手数料本体 = 四捨五入(22550*0.028) = 631円
        // 仕入手数料消費税 = 切り捨て(631*0.1) = 63円 → 仕入手数料合計694円
        // 振込金額(預り金) = 22550-694 = 21856円（楽天ペイシートの振込金額(預り金)と一致）
        givenUnprocessedBatch("楽天ペイ", RAKUTENPAY_BATCH_ID);
        givenFeeRateAndItemCode("楽天ペイ", "楽天ペイ", "PURCHASE_COLLECT", "0.028", "3300062");

        RakutenPayAggregate aggregate = new RakutenPayAggregate() {
            @Override
            public String getTradeCode() {
                return "01-024";
            }

            @Override
            public Long getTotalAmount() {
                return 22550L;
            }
        };
        when(rakutenPayTransactionRepository.sumByTradeCode(List.of(RAKUTENPAY_BATCH_ID)))
                .thenReturn(List.of(aggregate));

        List<TransferLineItem> result = service.calculateRakutenPayLineItems();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualTo(21856);
    }

    @Test
    void calculateRakutenPayLineItemsMatchesReferenceDataAtHigherTransactionVolume() {
        // 楽天ペイ 取引コード31-010: 取引金額58,280円（23件分の合算）。
        // 仕入手数料本体 = 四捨五入(58280*0.028) = 1632円
        // 仕入手数料消費税 = 切り捨て(1632*0.1) = 163円 → 合計1795円
        // 振込金額(預り金) = 58280-1795 = 56485円（楽天ペイシートの実績値と一致）
        givenUnprocessedBatch("楽天ペイ", RAKUTENPAY_BATCH_ID);
        givenFeeRateAndItemCode("楽天ペイ", "楽天ペイ", "PURCHASE_COLLECT", "0.028", "3300062");

        RakutenPayAggregate aggregate = new RakutenPayAggregate() {
            @Override
            public String getTradeCode() {
                return "31-010";
            }

            @Override
            public Long getTotalAmount() {
                return 58280L;
            }
        };
        when(rakutenPayTransactionRepository.sumByTradeCode(List.of(RAKUTENPAY_BATCH_ID)))
                .thenReturn(List.of(aggregate));

        List<TransferLineItem> result = service.calculateRakutenPayLineItems();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualTo(56485);
    }

    @Test
    void calculateVisaMasterLineItemsMatchesReferenceData() {
        // 住信SBI 取引コード01-001: 売上金額58,850円、明細側で計算済みの手数料(1)=1,530円
        // 支払金額① = 58850-1530 = 57320円（月度集計シートの支払金額(1)と一致）
        givenUnprocessedBatch("住信SBI", VISA_MASTER_BATCH_ID);
        givenItemCode("住信SBI", "Visa/Master", "3300001");

        when(visaMasterTransactionRepository.sumByTradeCode(List.of(VISA_MASTER_BATCH_ID)))
                .thenReturn(List.of(fixedVisaMasterAggregate("01-001", 58850L, 1530L)));

        List<TransferLineItem> result = service.calculateVisaMasterLineItems();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualTo(57320);
    }

    @Test
    void calculateVisaMasterLineItemsUsesStoredFeeAmountInsteadOfRecalculatingAtHighVolume() {
        // 住信SBI 取引コード23-004: 明細302件、売上金額1,092,607円、手数料率一律2.6%。
        // 売上金額×2.6%を自前で切り捨て計算すると28,407円になり実データ(28,411円)と
        // 一致しない（明細行数が多い店舗で丸め誤差が蓄積するため）。必ず明細側で
        // 計算済みのfee_amount_1列の合計(28,411円)を使うことを確認する境界値ケース。
        givenUnprocessedBatch("住信SBI", VISA_MASTER_BATCH_ID);
        givenItemCode("住信SBI", "Visa/Master", "3300001");

        when(visaMasterTransactionRepository.sumByTradeCode(List.of(VISA_MASTER_BATCH_ID)))
                .thenReturn(List.of(fixedVisaMasterAggregate("23-004", 1092607L, 28411L)));

        List<TransferLineItem> result = service.calculateVisaMasterLineItems();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualTo(1064196);
    }

    private VisaMasterAggregate fixedVisaMasterAggregate(
            String tradeCode, long totalSalesAmount, long totalFeeAmount1) {
        return new VisaMasterAggregate() {
            @Override
            public String getTradeCode() {
                return tradeCode;
            }

            @Override
            public Long getTotalSalesAmount() {
                return totalSalesAmount;
            }

            @Override
            public Long getTotalFeeAmount1() {
                return totalFeeAmount1;
            }
        };
    }

    private void givenJcbAggregate(String tradeCode, int salesCount, int salesAmount) {
        JcbBrandAggregate aggregate = new JcbBrandAggregate() {
            @Override
            public String getTradeCode() {
                return tradeCode;
            }

            @Override
            public String getCardName() {
                return JCB_BRAND;
            }

            @Override
            public Long getTotalSalesCount() {
                return (long) salesCount;
            }

            @Override
            public Long getTotalSalesAmount() {
                return (long) salesAmount;
            }
        };
        when(jcbSalesDetailRepository.sumByTradeCodeAndCardName(List.of(JCB_BATCH_ID)))
                .thenReturn(List.of(aggregate));
    }

}
