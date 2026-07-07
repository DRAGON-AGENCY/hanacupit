package com.cupit.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

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
import com.cupit.service.settlement.FeeCalcModel;
import com.cupit.service.settlement.FeeCalculationResult;
import com.cupit.service.settlement.SettlementFeeCalculator;
import com.cupit.service.settlement.TransferLineItem;

/**
 * JFTD統合振込CSV作成のための集計サービス。取引コード×カードブランド単位で
 * 支払金額①（決済事業者手数料控除後、弊社手数料控除前）を計算する。
 * 対象範囲は「まだ振込CSVに含めていない（m_import_batch.transfer_batch_idが
 * NULLの）インポート分」とする（振込確定操作自体は{@code JftdTransferConfirmService}が担う）。
 */
@Service
public class JftdTransferCalculationService {

    private static final String PAYMENT_TYPE_JCB = "JCB";
    private static final String PAYMENT_TYPE_SUMAREJO = "スマレジ";
    private static final String PAYMENT_TYPE_NETSTAR = "ネットスターズ";
    private static final String PAYMENT_TYPE_RAKUTENPAY = "楽天ペイ";
    private static final String PAYMENT_TYPE_VISA_MASTER = "住信SBI";

    private static final String MASTER_COMPANY_SUMAREJO = "スマレジ(端末月額)";
    private static final String MASTER_COMPANY_VISA_MASTER = "Visa/Master";

    private static final int SUMAREJO_BASE_UNIT_PRICE = 700;

    /**
     * 取引コード「40-879」は花キューピット自社のHQテスト端末
     * （加盟店名「花キューピット正会員店(本部テスト用)」、決済金額・トランザクション数は
     * すべて0）であり、3300217（本体）・3300219（調整）とも請求対象外。
     * 11_月額利用料_端末.xlsxのclaim_detailシートで単価1800円のterminalとして
     * 存在するが、3300217・3300219シートの実際の請求明細行には1件も出現しない
     * ことで確認済み（本部テスト用のため、本体・調整とも計上しない）。
     */
    private static final String SUMAREJO_EXCLUDED_TEST_TRADE_CODE = "40-879";

    private static final String AMOUNT_TYPE_PAYMENT = "PAYMENT";

    private final ImportBatchRepository importBatchRepository;
    private final JcbSalesDetailRepository jcbSalesDetailRepository;
    private final TerminalMonthlyFeeRepository terminalMonthlyFeeRepository;
    private final NetstarSalesSummaryRepository netstarSalesSummaryRepository;
    private final RakutenPayTransactionRepository rakutenPayTransactionRepository;
    private final VisaMasterTransactionRepository visaMasterTransactionRepository;
    private final SettlementFeeRateRepository settlementFeeRateRepository;
    private final SettlementItemCodeRepository settlementItemCodeRepository;
    private final SettlementFeeCalculator settlementFeeCalculator;

    public JftdTransferCalculationService(
            ImportBatchRepository importBatchRepository,
            JcbSalesDetailRepository jcbSalesDetailRepository,
            TerminalMonthlyFeeRepository terminalMonthlyFeeRepository,
            NetstarSalesSummaryRepository netstarSalesSummaryRepository,
            RakutenPayTransactionRepository rakutenPayTransactionRepository,
            VisaMasterTransactionRepository visaMasterTransactionRepository,
            SettlementFeeRateRepository settlementFeeRateRepository,
            SettlementItemCodeRepository settlementItemCodeRepository,
            SettlementFeeCalculator settlementFeeCalculator) {
        this.importBatchRepository = importBatchRepository;
        this.jcbSalesDetailRepository = jcbSalesDetailRepository;
        this.terminalMonthlyFeeRepository = terminalMonthlyFeeRepository;
        this.netstarSalesSummaryRepository = netstarSalesSummaryRepository;
        this.rakutenPayTransactionRepository = rakutenPayTransactionRepository;
        this.visaMasterTransactionRepository = visaMasterTransactionRepository;
        this.settlementFeeRateRepository = settlementFeeRateRepository;
        this.settlementItemCodeRepository = settlementItemCodeRepository;
        this.settlementFeeCalculator = settlementFeeCalculator;
    }

    /**
     * 5社すべての未処理インポート分をまとめて集計する。
     */
    public List<TransferLineItem> calculateAllLineItems() {
        List<TransferLineItem> all = new ArrayList<>();
        all.addAll(calculateJcbLineItems());
        all.addAll(calculateSumarejoLineItems());
        all.addAll(calculateNetstarLineItems());
        all.addAll(calculateRakutenPayLineItems());
        all.addAll(calculateVisaMasterLineItems());
        return all;
    }

    /**
     * JCBの未処理インポート分を取引コード×カードブランド単位で集計し、
     * 支払金額①をPAYMENT項目の行として返す。手数料②が未実装のため、
     * FEE_BASE・FEE_TAX項目の行はまだ生成しない
     * （調査メモ「論点・オープン事項」項番6を参照）。
     *
     * 【交通系電子マネー】【ｎａｎａｃｏ】【ＷＡＯＮ】はPURCHASE_COLLECTモデルの
     * 決済手数料①（仕入手数料）についても、行単位／集計単位・四捨五入／切り捨て／
     * 銀行丸めのどの組み合わせを試しても実データと一致しない取引コードが複数見つかった
     * （例: 交通系電子マネー42-006・56-023・35-026、WAON41-127・72-026）。
     * PayPay・d払い・楽天ペイでは同モデルが実データと一致することを確認済みのため、
     * この3ブランド固有の未解決問題として、正しい計算式が判明するまで行を生成せず
     * スキップする（誤った金額を計上しないため）。
     */
    private static final Set<String> UNVERIFIED_PURCHASE_COLLECT_BRANDS = Set.of(
            "【交通系電子マネー】", "【ｎａｎａｃｏ】", "【ＷＡＯＮ】");

    public List<TransferLineItem> calculateJcbLineItems() {
        List<Integer> batchIds = unprocessedBatchIds(PAYMENT_TYPE_JCB);
        if (batchIds.isEmpty()) {
            return List.of();
        }

        List<JcbBrandAggregate> aggregates =
                jcbSalesDetailRepository.sumByTradeCodeAndCardName(batchIds);

        List<TransferLineItem> lineItems = new ArrayList<>();
        for (JcbBrandAggregate aggregate : aggregates) {
            String cardBrand = aggregate.getCardName();
            if (UNVERIFIED_PURCHASE_COLLECT_BRANDS.contains(cardBrand)) {
                continue;
            }
            SettlementFeeRate rate = findFeeRate(PAYMENT_TYPE_JCB, cardBrand);
            SettlementItemCode itemCode = findItemCode(PAYMENT_TYPE_JCB, cardBrand);
            int totalSalesAmount = aggregate.getTotalSalesAmount().intValue();
            FeeCalculationResult result = calculate(totalSalesAmount, rate);
            lineItems.add(new TransferLineItem(
                    aggregate.getTradeCode(), itemCode.getItemCode(), 1, result.getPayableAmount1()));
        }
        return lineItems;
    }

    /**
     * スマレジ(端末月額)はamount_total（決済金額合計）とは無関係で、単価(unit_price)に
     * 基づく端末レンタルの定額料金である（11_月額利用料_端末.xlsxの生データで確認済み）。
     * 単価700円が基本料（項目コード3300217・本体）、それを超える端末（実データでは
     * 1800円）は差額（1800-700=1100円）を調整項目（3300219）として別建てで計上する。
     * ただし{@link #SUMAREJO_EXCLUDED_TEST_TRADE_CODE}（自社HQテスト端末）は
     * 本体・調整とも計上しない。
     */
    public List<TransferLineItem> calculateSumarejoLineItems() {
        List<Integer> batchIds = unprocessedBatchIds(PAYMENT_TYPE_SUMAREJO);
        if (batchIds.isEmpty()) {
            return List.of();
        }

        List<TerminalFeeAggregate> aggregates =
                terminalMonthlyFeeRepository.sumByTradeCodeAndUnitPrice(batchIds);
        SettlementItemCode baseItemCode = findItemCode(MASTER_COMPANY_SUMAREJO, "本体");
        SettlementItemCode adjustmentItemCode = findItemCode(MASTER_COMPANY_SUMAREJO, "調整");

        Map<String, Integer> baseAmountByTradeCode = new LinkedHashMap<>();
        Map<String, Integer> premiumAmountByTradeCode = new LinkedHashMap<>();
        for (TerminalFeeAggregate aggregate : aggregates) {
            if (SUMAREJO_EXCLUDED_TEST_TRADE_CODE.equals(aggregate.getTradeCode())) {
                continue;
            }
            int terminalCount = aggregate.getTerminalCount().intValue();
            int premiumPerTerminal = Math.max(aggregate.getUnitPrice() - SUMAREJO_BASE_UNIT_PRICE, 0);
            baseAmountByTradeCode.merge(
                    aggregate.getTradeCode(), SUMAREJO_BASE_UNIT_PRICE * terminalCount, Integer::sum);
            if (premiumPerTerminal > 0) {
                premiumAmountByTradeCode.merge(
                        aggregate.getTradeCode(), premiumPerTerminal * terminalCount, Integer::sum);
            }
        }

        List<TransferLineItem> lineItems = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : baseAmountByTradeCode.entrySet()) {
            lineItems.add(new TransferLineItem(entry.getKey(), baseItemCode.getItemCode(), 1, entry.getValue()));
        }
        for (Map.Entry<String, Integer> entry : premiumAmountByTradeCode.entrySet()) {
            lineItems.add(new TransferLineItem(
                    entry.getKey(), adjustmentItemCode.getItemCode(), 1, entry.getValue()));
        }
        return lineItems;
    }

    /**
     * m_netstar_sales_summaryは店舗単位1行で、ブランド別の内訳は
     * alipay_net_amount・dpay_net_amount・paypay_net_amount・wechat_net_amount
     * （rakuten_*・smartcode_*は項目コード未対応のため未使用）として既に分かれているため
     * GROUP BYは不要。計算基準は売上ではなく差引金額(_net_amount)である点に注意
     * （11_NETSTARS.xlsxのピボット列見出し「合計 / Alipay差引金額」で確認済み）。
     */
    public List<TransferLineItem> calculateNetstarLineItems() {
        List<Integer> batchIds = unprocessedBatchIds(PAYMENT_TYPE_NETSTAR);
        if (batchIds.isEmpty()) {
            return List.of();
        }

        List<NetstarSalesSummary> rows = netstarSalesSummaryRepository.findByBatchIdIn(batchIds);

        List<TransferLineItem> lineItems = new ArrayList<>();
        for (NetstarSalesSummary row : rows) {
            addNetstarBrandLineItem(lineItems, row.getTradeCode(), "Alipay", row.getAlipayNetAmount());
            addNetstarBrandLineItem(lineItems, row.getTradeCode(), "PayPay", row.getPaypayNetAmount());
            addNetstarBrandLineItem(lineItems, row.getTradeCode(), "d払い", row.getDpayNetAmount());
            addNetstarBrandLineItem(lineItems, row.getTradeCode(), "WeChatPay", row.getWechatNetAmount());
        }
        return lineItems;
    }

    private void addNetstarBrandLineItem(
            List<TransferLineItem> lineItems, String tradeCode, String cardBrand, int netAmount) {
        if (netAmount == 0) {
            return;
        }
        SettlementFeeRate rate = findFeeRate(PAYMENT_TYPE_NETSTAR, cardBrand);
        SettlementItemCode itemCode = findItemCode(PAYMENT_TYPE_NETSTAR, cardBrand);
        FeeCalculationResult result = calculate(netAmount, rate);
        lineItems.add(new TransferLineItem(tradeCode, itemCode.getItemCode(), 1, result.getPayableAmount1()));
    }

    /**
     * m_rakuten_pay_transactionを取引コード単位でtotal_amountを合計し、
     * PURCHASE_COLLECTモデルで計算する。
     */
    public List<TransferLineItem> calculateRakutenPayLineItems() {
        List<Integer> batchIds = unprocessedBatchIds(PAYMENT_TYPE_RAKUTENPAY);
        if (batchIds.isEmpty()) {
            return List.of();
        }

        List<RakutenPayAggregate> aggregates = rakutenPayTransactionRepository.sumByTradeCode(batchIds);
        SettlementFeeRate rate = findFeeRate(PAYMENT_TYPE_RAKUTENPAY, PAYMENT_TYPE_RAKUTENPAY);
        SettlementItemCode itemCode = findItemCode(PAYMENT_TYPE_RAKUTENPAY, PAYMENT_TYPE_RAKUTENPAY);

        List<TransferLineItem> lineItems = new ArrayList<>();
        for (RakutenPayAggregate aggregate : aggregates) {
            FeeCalculationResult result = calculate(aggregate.getTotalAmount().intValue(), rate);
            lineItems.add(new TransferLineItem(
                    aggregate.getTradeCode(), itemCode.getItemCode(), 1, result.getPayableAmount1()));
        }
        return lineItems;
    }

    /**
     * 住信SBI(Visa/Master)は、決済手数料①を売上金額×手数料率から自前で再計算しては
     * いけない。生データ検証の結果、明細行数が多い店舗（実データで300件超）では、
     * 単一レートでの一括計算（切り捨て・四捨五入いずれも）が実データと数円〜十数円
     * 単位でズレることを確認した。一方、明細行ごとに実データ側で計算済みの
     * fee_amount_1列の値をそのまま合計すると全件一致することを確認済みのため、
     * 必ずfee_amount_1（DB取込時に明細行から転記済みの値）を合計する。
     * brand_type（Visa国内/海外、Master国内/海外）では分割しない（実データ上、
     * 項目コードはbrand_typeを問わず1系統のみのため）。
     */
    public List<TransferLineItem> calculateVisaMasterLineItems() {
        List<Integer> batchIds = unprocessedBatchIds(PAYMENT_TYPE_VISA_MASTER);
        if (batchIds.isEmpty()) {
            return List.of();
        }

        List<VisaMasterAggregate> aggregates = visaMasterTransactionRepository.sumByTradeCode(batchIds);
        SettlementItemCode itemCode = findItemCode(PAYMENT_TYPE_VISA_MASTER, MASTER_COMPANY_VISA_MASTER);

        List<TransferLineItem> lineItems = new ArrayList<>();
        for (VisaMasterAggregate aggregate : aggregates) {
            int payableAmount1 = aggregate.getTotalSalesAmount().intValue()
                    - aggregate.getTotalFeeAmount1().intValue();
            lineItems.add(new TransferLineItem(aggregate.getTradeCode(), itemCode.getItemCode(), 1, payableAmount1));
        }
        return lineItems;
    }

    private List<Integer> unprocessedBatchIds(String paymentType) {
        return importBatchRepository.findByPaymentTypeAndTransferBatchIdIsNull(paymentType).stream()
                .map(ImportBatch::getBatchId)
                .toList();
    }

    private SettlementFeeRate findFeeRate(String paymentCompany, String cardBrand) {
        return settlementFeeRateRepository
                .findByPaymentCompanyAndCardBrand(paymentCompany, cardBrand)
                .orElseThrow(() -> new IllegalStateException(
                        "手数料率マスタにカードブランド「" + cardBrand + "」の設定がありません。"));
    }

    private SettlementItemCode findItemCode(String paymentCompany, String cardBrand) {
        return settlementItemCodeRepository
                .findByPaymentCompanyAndCardBrandAndAmountType(paymentCompany, cardBrand, AMOUNT_TYPE_PAYMENT)
                .orElseThrow(() -> new IllegalStateException(
                        "項目コードマスタにカードブランド「" + cardBrand + "」の設定がありません。"));
    }

    private FeeCalculationResult calculate(int totalSalesAmount, SettlementFeeRate rate) {
        FeeCalcModel calcModel = FeeCalcModel.valueOf(rate.getCalcModel());
        return switch (calcModel) {
            case STRAIGHT -> settlementFeeCalculator.calculateStraight(totalSalesAmount, rate);
            case PURCHASE_COLLECT -> settlementFeeCalculator.calculatePurchaseCollect(totalSalesAmount, rate);
            case SBI_RESIDUAL -> throw new IllegalStateException(
                    "SBI_RESIDUALモデルはcalculateVisaMasterLineItems()専用です。");
        };
    }

}
