package com.cupit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cupit.dto.PaygateStationRow;
import com.cupit.model.ImportBatch;
import com.cupit.model.JftdTransferDetail;
import com.cupit.model.NetstarSalesSummary;
import com.cupit.model.PaygateStoreMapping;
import com.cupit.model.SettlementItemCode;
import com.cupit.repository.ImportBatchRepository;
import com.cupit.repository.JcbSalesDetailRepository;
import com.cupit.repository.JcbSalesDetailRepository.JcbBrandAggregate;
import com.cupit.repository.JftdTransferDetailRepository;
import com.cupit.repository.NetstarSalesSummaryRepository;
import com.cupit.repository.PaygateMappingRepository;
import com.cupit.repository.RakutenPayTransactionRepository;
import com.cupit.repository.RakutenPayTransactionRepository.RakutenPayAggregate;
import com.cupit.repository.SettlementItemCodeRepository;
import com.cupit.repository.TerminalMonthlyFeeRepository;
import com.cupit.repository.TerminalMonthlyFeeRepository.TerminalFeeAggregate;
import com.cupit.repository.VisaMasterTransactionRepository;
import com.cupit.repository.VisaMasterTransactionRepository.VisaMasterAggregate;
import com.cupit.service.settlement.TransferLineItem;

/**
 * {@link PaygateStationInquiryService} のテスト。
 * {@link JftdTransferCalculationService}をモック化し、5決済会社それぞれについて
 * ①締め日ごとのグループ化 ②itemCodeからのcard_brand逆引き ③各社固有の生データからの
 * 売上件数取得 ④店舗名の付与 ⑤取引コード順ソート、を検証する。
 */
@ExtendWith(MockitoExtension.class)
class PaygateStationInquiryServiceTest {

    private static final LocalDate CUTOFF_DATE = LocalDate.of(2026, 5, 31);
    private static final String AMOUNT_TYPE_PAYMENT = "PAYMENT";

    @Mock
    private ImportBatchRepository importBatchRepository;

    @Mock
    private JftdTransferCalculationService jftdTransferCalculationService;

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
    private SettlementItemCodeRepository settlementItemCodeRepository;

    @Mock
    private PaygateMappingRepository paygateMappingRepository;

    @Mock
    private JftdTransferDetailRepository jftdTransferDetailRepository;

    private PaygateStationInquiryService service;

    @BeforeEach
    void setUp() {
        service = new PaygateStationInquiryService(
                importBatchRepository,
                jftdTransferCalculationService,
                jcbSalesDetailRepository,
                terminalMonthlyFeeRepository,
                netstarSalesSummaryRepository,
                rakutenPayTransactionRepository,
                visaMasterTransactionRepository,
                settlementItemCodeRepository,
                paygateMappingRepository,
                jftdTransferDetailRepository);

        lenient().when(paygateMappingRepository.findAllByOrderByTradeCodeAscTerminalIdAsc()).thenReturn(List.of());
        lenient().when(settlementItemCodeRepository.findAll()).thenReturn(List.of());
        lenient().when(jftdTransferCalculationService.calculateJcbLineItemsForInquiry(anyIntList()))
                .thenReturn(List.of());
        lenient().when(jftdTransferCalculationService.calculateNetstarLineItems(anyIntList())).thenReturn(List.of());
        lenient().when(jftdTransferCalculationService.calculateSumarejoLineItems(anyIntList())).thenReturn(List.of());
        lenient().when(jftdTransferCalculationService.calculateRakutenPayLineItems(anyIntList())).thenReturn(List.of());
        lenient().when(jftdTransferCalculationService.calculateVisaMasterLineItems(anyIntList())).thenReturn(List.of());
    }

    private static List<Integer> anyIntList() {
        return org.mockito.ArgumentMatchers.anyList();
    }

    private ImportBatch batch(int batchId, String paymentType, LocalDate cutoffDate) {
        ImportBatch b = new ImportBatch();
        b.setBatchId(batchId);
        b.setPaymentType(paymentType);
        b.setCutoffDate(cutoffDate);
        return b;
    }

    private ImportBatch confirmedBatch(
            int batchId, String paymentType, LocalDate cutoffDate, int transferBatchId) {
        ImportBatch b = batch(batchId, paymentType, cutoffDate);
        b.setTransferBatchId(transferBatchId);
        return b;
    }

    private JftdTransferDetail transferDetail(
            int transferBatchId, int importBatchId, String tradeCode, String itemCode,
            int amount, int grossAmount, int feeTaxFree, int feeBase, int feeTax) {
        JftdTransferDetail detail = new JftdTransferDetail();
        detail.setTransferBatchId(transferBatchId);
        detail.setImportBatchId(importBatchId);
        detail.setTradeCode(tradeCode);
        detail.setItemCode(itemCode);
        detail.setQuantity(1);
        detail.setAmount(amount);
        detail.setGrossAmount(grossAmount);
        detail.setAcquirerFeeTaxFree(feeTaxFree);
        detail.setAcquirerFeeBase(feeBase);
        detail.setAcquirerFeeTax(feeTax);
        return detail;
    }

    private void givenStoreName(String tradeCode, String storeName) {
        PaygateStoreMapping mapping = new PaygateStoreMapping();
        mapping.setTradeCode(tradeCode);
        mapping.setStoreName(storeName);
        when(paygateMappingRepository.findAllByOrderByTradeCodeAscTerminalIdAsc())
                .thenReturn(List.of(mapping));
    }

    private void givenCardBrand(String itemCode, String cardBrand) {
        SettlementItemCode code = new SettlementItemCode();
        code.setItemCode(itemCode);
        code.setCardBrand(cardBrand);
        code.setAmountType(AMOUNT_TYPE_PAYMENT);
        when(settlementItemCodeRepository.findAll()).thenReturn(List.of(code));
    }

    private TransferLineItem lineItem(
            String tradeCode, String itemCode, int amount, int grossAmount,
            int feeTaxFree, int feeBase, int feeTax) {
        return lineItem(tradeCode, itemCode, amount, grossAmount, feeTaxFree, feeBase, feeTax, 100);
    }

    private TransferLineItem lineItem(
            String tradeCode, String itemCode, int amount, int grossAmount,
            int feeTaxFree, int feeBase, int feeTax, int batchId) {
        return new TransferLineItem(
                tradeCode, itemCode, 1, amount, grossAmount, feeTaxFree, feeBase, feeTax, batchId);
    }

    private JcbBrandAggregate jcbAggregate(
            String tradeCode, String cardName, int batchId, long salesCount, long salesAmount) {
        return new JcbBrandAggregate() {
            @Override
            public String getTradeCode() {
                return tradeCode;
            }

            @Override
            public String getCardName() {
                return cardName;
            }

            @Override
            public Integer getBatchId() {
                return batchId;
            }

            @Override
            public Long getTotalSalesCount() {
                return salesCount;
            }

            @Override
            public Long getTotalSalesAmount() {
                return salesAmount;
            }
        };
    }

    @Test
    void findAllReturnsEmptyWhenNoBatchesExist() {
        when(importBatchRepository.findAll()).thenReturn(List.of());

        List<PaygateStationRow> rows = service.findAll();

        assertThat(rows).isEmpty();
    }

    @Test
    void findAllIgnoresUnknownPaymentTypes() {
        when(importBatchRepository.findAll()).thenReturn(List.of(batch(1, "stera JCB", CUTOFF_DATE)));

        List<PaygateStationRow> rows = service.findAll();

        assertThat(rows).isEmpty();
    }

    @Test
    void findAllHandlesBatchesWithNullCutoffDateWithoutThrowing() {
        // 締め日機能の実装前にインポートされた既存バッチはcutoff_dateがnullのまま残っている。
        // Collectors.groupingByはnullキーで例外を投げるため、これを再現するテスト。
        when(importBatchRepository.findAll()).thenReturn(List.of(batch(100, "JCB", null)));
        givenCardBrand("3300024", "【ＪＣＢカード】");
        when(jftdTransferCalculationService.calculateJcbLineItemsForInquiry(List.of(100)))
                .thenReturn(List.of(lineItem("01-001", "3300024", 1000, 1000, 0, 0, 0)));

        List<PaygateStationRow> rows = service.findAll();

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getCutoffDate()).isNull();
    }

    @Test
    void findAllResolvesJcbCardBrandFromItemCodeAndMergesStoreNameAndCount() {
        when(importBatchRepository.findAll()).thenReturn(List.of(batch(100, "JCB", CUTOFF_DATE)));
        givenStoreName("01-001", "花のいのうえ");
        givenCardBrand("3300024", "【ＪＣＢカード】");
        when(jftdTransferCalculationService.calculateJcbLineItemsForInquiry(List.of(100)))
                .thenReturn(List.of(lineItem("01-001", "3300024", 14150, 14550, 400, 0, 0)));

        JcbBrandAggregate aggregate = new JcbBrandAggregate() {
            @Override
            public String getTradeCode() {
                return "01-001";
            }

            @Override
            public String getCardName() {
                return "【ＪＣＢカード】";
            }

            @Override
            public Integer getBatchId() {
                return 100;
            }

            @Override
            public Long getTotalSalesCount() {
                return 5L;
            }

            @Override
            public Long getTotalSalesAmount() {
                return 14550L;
            }
        };
        when(jcbSalesDetailRepository.sumByTradeCodeAndCardName(List.of(100))).thenReturn(List.of(aggregate));

        List<PaygateStationRow> rows = service.findAll();

        assertThat(rows).hasSize(1);
        PaygateStationRow row = rows.get(0);
        assertThat(row.getTradeCode()).isEqualTo("01-001");
        assertThat(row.getStoreName()).isEqualTo("花のいのうえ");
        assertThat(row.getPaymentCompany()).isEqualTo("JCB");
        assertThat(row.getCardBrand()).isEqualTo("【ＪＣＢカード】");
        assertThat(row.getCutoffDate()).isEqualTo(CUTOFF_DATE);
        assertThat(row.getSalesCount()).isEqualTo(5);
        assertThat(row.getSalesAmount()).isEqualTo(14550);
        assertThat(row.getAcquirerFee()).isEqualTo(400);
        assertThat(row.getPayableAmount()).isEqualTo(14150);
    }

    /**
     * 同一取引コード・同一カードブランド・同一締め日で複数ファイル（インポートバッチ）
     * 分のデータが確定・アップロードされている場合、集計行は合算されて1行になるが、
     * その内訳（{@link PaygateStationRow#getDetails()}）で元ファイル単位の金額を
     * 確認できることを検証する（実際にユーザーから「合算された金額の内訳が
     * わからない」という指摘を受けて追加した機能の回帰テスト）。
     */
    @Test
    void findAllPopulatesDetailsForEachContributingBatch() {
        when(importBatchRepository.findAll()).thenReturn(List.of(
                batch(100, "JCB", CUTOFF_DATE), batch(101, "JCB", CUTOFF_DATE)));
        givenCardBrand("3300024", "【ＪＣＢカード】");
        when(jftdTransferCalculationService.calculateJcbLineItemsForInquiry(List.of(100, 101)))
                .thenReturn(List.of(
                        lineItem("IT-001", "3300024", 17505, 18000, 495, 0, 0, 100),
                        lineItem("IT-001", "3300024", 24313, 25000, 687, 0, 0, 101)));
        when(jcbSalesDetailRepository.sumByTradeCodeAndCardName(List.of(100, 101))).thenReturn(List.of(
                jcbAggregate("IT-001", "【ＪＣＢカード】", 100, 1L, 18000L),
                jcbAggregate("IT-001", "【ＪＣＢカード】", 101, 1L, 25000L)));

        List<PaygateStationRow> rows = service.findAll();

        assertThat(rows).hasSize(1);
        PaygateStationRow row = rows.get(0);
        assertThat(row.getSalesCount()).isEqualTo(2);
        assertThat(row.getSalesAmount()).isEqualTo(43000);
        assertThat(row.getPayableAmount()).isEqualTo(41818);

        assertThat(row.getDetails()).hasSize(2);
        assertThat(row.getDetails()).extracting(PaygateStationRow::getPayableAmount)
                .containsExactlyInAnyOrder(17505, 24313);
        // 内訳の合算が親行と一致すること
        assertThat(row.getDetails().stream().mapToInt(PaygateStationRow::getSalesAmount).sum())
                .isEqualTo(row.getSalesAmount());
        assertThat(row.getDetails().stream().mapToInt(PaygateStationRow::getPayableAmount).sum())
                .isEqualTo(row.getPayableAmount());
        // 内訳行も画面表示に必要な決済会社・決済種類(カードブランド)を持つ
        assertThat(row.getDetails()).allSatisfy(detail -> {
            assertThat(detail.getPaymentCompany()).isEqualTo("JCB");
            assertThat(detail.getCardBrand()).isEqualTo("【ＪＣＢカード】");
        });
    }

    @Test
    void findAllGroupsJcbBatchesByCutoffDateSeparately() {
        LocalDate otherDate = LocalDate.of(2026, 6, 30);
        when(importBatchRepository.findAll()).thenReturn(List.of(
                batch(100, "JCB", CUTOFF_DATE),
                batch(101, "JCB", otherDate)));
        givenCardBrand("3300024", "【ＪＣＢカード】");
        when(jftdTransferCalculationService.calculateJcbLineItemsForInquiry(List.of(100)))
                .thenReturn(List.of(lineItem("01-001", "3300024", 1000, 1000, 0, 0, 0)));
        when(jftdTransferCalculationService.calculateJcbLineItemsForInquiry(List.of(101)))
                .thenReturn(List.of(lineItem("01-001", "3300024", 2000, 2000, 0, 0, 0)));

        List<PaygateStationRow> rows = service.findAll();

        assertThat(rows).hasSize(2);
        assertThat(rows).extracting(PaygateStationRow::getCutoffDate)
                .containsExactlyInAnyOrder(CUTOFF_DATE, otherDate);
    }

    @Test
    void findAllUsesBrandSpecificNetstarSalesCount() {
        when(importBatchRepository.findAll()).thenReturn(List.of(batch(300, "ネットスターズ", CUTOFF_DATE)));
        givenCardBrand("3300040", "PayPay");
        when(jftdTransferCalculationService.calculateNetstarLineItems(List.of(300)))
                .thenReturn(List.of(lineItem("02-007", "3300040", 9800, 10000, 200, 0, 0)));

        NetstarSalesSummary row = new NetstarSalesSummary();
        row.setTradeCode("02-007");
        row.setBatchId(300);
        row.setPaypaySalesCount(12);
        when(netstarSalesSummaryRepository.findByBatchIdIn(List.of(300))).thenReturn(List.of(row));

        List<PaygateStationRow> rows = service.findAll();

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getPaymentCompany()).isEqualTo("NETSTARS");
        assertThat(rows.get(0).getCardBrand()).isEqualTo("PayPay");
        assertThat(rows.get(0).getSalesCount()).isEqualTo(12);
    }

    @Test
    void findAllSumarejoHasNullCardBrandAndSumsTerminalCountAcrossUnitPrices() {
        when(importBatchRepository.findAll()).thenReturn(List.of(batch(200, "スマレジ", CUTOFF_DATE)));
        when(jftdTransferCalculationService.calculateSumarejoLineItems(List.of(200)))
                .thenReturn(List.of(
                        lineItem("01-001", "3300217", 700, 0, 0, 0, 700),
                        lineItem("01-001", "3300219", 1100, 0, 0, 0, 1100)));

        when(terminalMonthlyFeeRepository.sumByTradeCodeAndUnitPrice(List.of(200))).thenReturn(List.of(
                terminalFeeAggregate("01-001", 700, 200, 3L),
                terminalFeeAggregate("01-001", 1800, 200, 1L)));

        List<PaygateStationRow> rows = service.findAll();

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getPaymentCompany()).isEqualTo("スマレジ");
        assertThat(rows.get(0).getCardBrand()).isNull();
        assertThat(rows.get(0).getSalesCount()).isEqualTo(4);
        assertThat(rows.get(0).getPayableAmount()).isEqualTo(1800);
    }

    @Test
    void findAllRakutenPayUsesTransactionCountField() {
        when(importBatchRepository.findAll()).thenReturn(List.of(batch(400, "楽天ペイ", CUTOFF_DATE)));
        when(jftdTransferCalculationService.calculateRakutenPayLineItems(List.of(400)))
                .thenReturn(List.of(lineItem("01-024", "3300062", 21856, 22550, 0, 631, 63)));

        RakutenPayAggregate aggregate = new RakutenPayAggregate() {
            @Override
            public String getTradeCode() {
                return "01-024";
            }

            @Override
            public Integer getBatchId() {
                return 400;
            }

            @Override
            public Long getTotalAmount() {
                return 22550L;
            }

            @Override
            public Long getTransactionCount() {
                return 11L;
            }
        };
        when(rakutenPayTransactionRepository.sumByTradeCode(List.of(400))).thenReturn(List.of(aggregate));

        List<PaygateStationRow> rows = service.findAll();

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getPaymentCompany()).isEqualTo("楽天ペイ");
        assertThat(rows.get(0).getSalesCount()).isEqualTo(11);
    }

    /**
     * 統合振込確定後にm_settlement_fee_rateが変更されても、確定済み取引の決済手数料①・
     * 支払金額①が帳票側と食い違わないよう、確定済みバッチはm_jftd_transfer_detailの
     * スナップショットを使い、未確定バッチのみJftdTransferCalculationServiceで
     * ライブ再計算することを検証する。
     */
    @Test
    void findAllUsesSnapshotForConfirmedBatchAndLiveCalculationForUnconfirmedBatch() {
        ImportBatch confirmed = confirmedBatch(401, "楽天ペイ", CUTOFF_DATE, 999);
        ImportBatch unconfirmed = batch(402, "楽天ペイ", CUTOFF_DATE);
        when(importBatchRepository.findAll()).thenReturn(List.of(confirmed, unconfirmed));

        JftdTransferDetail snapshot = transferDetail(
                999, 401, "01-024", "3300062", 21856, 22550, 0, 631, 63);
        when(jftdTransferDetailRepository.findByImportBatchIdIn(List.of(401))).thenReturn(List.of(snapshot));

        when(jftdTransferCalculationService.calculateRakutenPayLineItems(List.of(402)))
                .thenReturn(List.of(lineItem("02-007", "3300062", 500, 500, 0, 0, 0)));

        when(rakutenPayTransactionRepository.sumByTradeCode(List.of(401, 402))).thenReturn(List.of());

        List<PaygateStationRow> rows = service.findAll();

        assertThat(rows).extracting(PaygateStationRow::getTradeCode)
                .containsExactlyInAnyOrder("01-024", "02-007");
        PaygateStationRow confirmedRow = rows.stream()
                .filter(r -> "01-024".equals(r.getTradeCode()))
                .findFirst().orElseThrow();
        assertThat(confirmedRow.getSalesAmount()).isEqualTo(22550);
        assertThat(confirmedRow.getAcquirerFee()).isEqualTo(694);
        assertThat(confirmedRow.getPayableAmount()).isEqualTo(21856);

        verify(jftdTransferCalculationService, never())
                .calculateRakutenPayLineItems(argThat(ids -> ids.contains(401)));
    }

    @Test
    void findAllVisaMasterUsesTransactionCountFieldAndLabelsCompanyAsVisaMaster() {
        when(importBatchRepository.findAll()).thenReturn(List.of(batch(500, "住信SBI", CUTOFF_DATE)));
        when(jftdTransferCalculationService.calculateVisaMasterLineItems(List.of(500)))
                .thenReturn(List.of(lineItem("01-001", "3300001", 57320, 58850, 1530, 0, 0)));

        VisaMasterAggregate aggregate = new VisaMasterAggregate() {
            @Override
            public String getTradeCode() {
                return "01-001";
            }

            @Override
            public Integer getBatchId() {
                return 500;
            }

            @Override
            public Long getTotalSalesAmount() {
                return 58850L;
            }

            @Override
            public Long getTotalFeeAmount1() {
                return 1530L;
            }

            @Override
            public Long getTransactionCount() {
                return 302L;
            }
        };
        when(visaMasterTransactionRepository.sumByTradeCode(List.of(500))).thenReturn(List.of(aggregate));

        List<PaygateStationRow> rows = service.findAll();

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getPaymentCompany()).isEqualTo("VISA・Master");
        assertThat(rows.get(0).getSalesCount()).isEqualTo(302);
    }

    @Test
    void findAllSortsRowsByTradeCodeAscending() {
        when(importBatchRepository.findAll()).thenReturn(List.of(batch(400, "楽天ペイ", CUTOFF_DATE)));
        when(jftdTransferCalculationService.calculateRakutenPayLineItems(List.of(400)))
                .thenReturn(List.of(
                        lineItem("05-010", "3300062", 100, 100, 0, 0, 0),
                        lineItem("01-001", "3300062", 200, 200, 0, 0, 0)));
        when(rakutenPayTransactionRepository.sumByTradeCode(List.of(400))).thenReturn(List.of());

        List<PaygateStationRow> rows = service.findAll();

        assertThat(rows).extracting(PaygateStationRow::getTradeCode)
                .containsExactly("01-001", "05-010");
    }

    /**
     * 手数料率マスタ・項目コードマスタに存在しないカードブランド名を持つJCBデータが
     * あっても、JCB分の照会（{@code calculateJcbLineItemsForInquiry}）は例外を投げず、
     * 該当ブランドを手数料0円・支払金額＝売上金額として表示する。他の正常なブランド・
     * 他の決済会社の表示に影響しないことを検証する（実際に本番データで発生した
     * 不具合の回帰テスト。以前はJCB分が丸ごと非表示になっていた）。
     */
    @Test
    void findAllShowsUnregisteredCardBrandWithZeroFeeInsteadOfHidingWholeCompany() {
        when(importBatchRepository.findAll()).thenReturn(List.of(
                batch(500, "JCB", CUTOFF_DATE),
                batch(501, "楽天ペイ", CUTOFF_DATE)));
        when(jftdTransferCalculationService.calculateJcbLineItemsForInquiry(List.of(500)))
                .thenReturn(List.of(lineItem("35-232", "【結合テスト用ブランド】", 5000, 5000, 0, 0, 0)));
        when(jftdTransferCalculationService.calculateRakutenPayLineItems(List.of(501)))
                .thenReturn(List.of(lineItem("01-024", "3300062", 21856, 22550, 0, 631, 63)));
        when(rakutenPayTransactionRepository.sumByTradeCode(List.of(501))).thenReturn(List.of());

        List<PaygateStationRow> rows = service.findAll();

        assertThat(rows).hasSize(2);
        PaygateStationRow jcbRow = rows.stream()
                .filter(r -> "JCB".equals(r.getPaymentCompany()))
                .findFirst().orElseThrow();
        // 項目コードマスタに存在しない値のため、itemCode代わりに入れたカードブランド名が
        // そのまま表示される（cardBrandByItemCodeで解決できないためitemCode自体を使う）。
        assertThat(jcbRow.getCardBrand()).isEqualTo("【結合テスト用ブランド】");
        assertThat(jcbRow.getSalesAmount()).isEqualTo(5000);
        assertThat(jcbRow.getAcquirerFee()).isZero();
        assertThat(jcbRow.getPayableAmount()).isEqualTo(5000);
        assertThat(rows.stream().anyMatch(r -> "楽天ペイ".equals(r.getPaymentCompany()))).isTrue();
    }

    private TerminalFeeAggregate terminalFeeAggregate(
            String tradeCode, int unitPrice, int batchId, long terminalCount) {
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
            public Integer getBatchId() {
                return batchId;
            }

            @Override
            public Long getTerminalCount() {
                return terminalCount;
            }
        };
    }

}
