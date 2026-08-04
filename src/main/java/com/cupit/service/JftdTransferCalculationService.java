package com.cupit.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
     * 5社すべての未処理インポート分をまとめて集計する。プレビュー表示用。
     * どのインポートバッチが対象になるかは呼び出し時点の状態をそのまま検索する
     * （ロックを取らないため、確定処理では使わないこと。確定処理には
     * {@link #calculateAllLineItems(Map)} を使う）。
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
     * JFTD・その他統合振込CSV作成画面の「JFTD CSV作成」ボタン押下時（確定前の
     * プレビュー表示専用）に使う。{@link #calculateAllLineItems()}と異なり、JCBで
     * 手数料率・項目コードマスタに存在しないカードブランドがあっても例外を投げず、
     * 手数料0円の行として表示を継続する。
     * （実際に発生した不具合の修正: 未登録カードブランドを含むデータをアップロード
     * すると、確定ボタンを押す前のプレビュー取得の時点で{@code calculateJcbLineItems()}が
     * 例外を投げてHTTP 500になり、フロントエンド側がそのエラーレスポンスを
     * 「対象データ0件」と誤解釈して「まだ振込CSVに含めていない未処理のデータが
     * ありません。」という紛らわしいメッセージを表示してしまっていた。
     * 確定処理本体（{@link JftdTransferConfirmService}が呼ぶ
     * {@link #calculateAllLineItems(Map)}）は従来通り厳密に例外を投げて確定を
     * ブロックするため、「プレビューは表示できるが確定は失敗する」という
     * 結合テスト仕様書の想定通りの挙動になる。）
     */
    public List<TransferLineItem> calculateAllLineItemsForPreview() {
        List<TransferLineItem> all = new ArrayList<>();
        all.addAll(calculateJcbLineItemsForInquiry());
        all.addAll(calculateSumarejoLineItems());
        all.addAll(calculateNetstarLineItems());
        all.addAll(calculateRakutenPayLineItems());
        all.addAll(calculateVisaMasterLineItems());
        return all;
    }

    /**
     * 5社すべてを、呼び出し側が指定したバッチIDの集合に限定して集計する。
     * JFTD統合振込CSV作成の確定処理専用。確定処理はまず対象バッチを排他ロックで
     * 確保してからこのメソッドを呼び出すことで、集計対象と実際にマークするバッチが
     * 一致すること（ロック後に他の確定処理が割り込めないこと）を保証する。
     *
     * @param batchIdsByPaymentType 決済種別（"JCB"・"スマレジ"・"ネットスターズ"・
     *                              "楽天ペイ"・"住信SBI"）をキーとした対象バッチIDのマップ
     */
    public List<TransferLineItem> calculateAllLineItems(Map<String, List<Integer>> batchIdsByPaymentType) {
        List<TransferLineItem> all = new ArrayList<>();
        all.addAll(calculateJcbLineItems(
                batchIdsByPaymentType.getOrDefault(PAYMENT_TYPE_JCB, List.of())));
        all.addAll(calculateSumarejoLineItems(
                batchIdsByPaymentType.getOrDefault(PAYMENT_TYPE_SUMAREJO, List.of())));
        all.addAll(calculateNetstarLineItems(
                batchIdsByPaymentType.getOrDefault(PAYMENT_TYPE_NETSTAR, List.of())));
        all.addAll(calculateRakutenPayLineItems(
                batchIdsByPaymentType.getOrDefault(PAYMENT_TYPE_RAKUTENPAY, List.of())));
        all.addAll(calculateVisaMasterLineItems(
                batchIdsByPaymentType.getOrDefault(PAYMENT_TYPE_VISA_MASTER, List.of())));
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
        return calculateJcbLineItems(unprocessedBatchIds(PAYMENT_TYPE_JCB));
    }

    public List<TransferLineItem> calculateJcbLineItems(List<Integer> batchIds) {
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
                    aggregate.getTradeCode(), itemCode.getItemCode(), 1, result.getPayableAmount1(),
                    totalSalesAmount, result.getAcquirerFeeTaxFree(),
                    result.getAcquirerFeeBase(), result.getAcquirerFeeTax(), aggregate.getBatchId()));
        }
        return lineItems;
    }

    /**
     * PAYGATE Station精算情報照会（参照専用）向けのJCB集計。{@link #calculateJcbLineItems}
     * とは異なり、手数料率マスタ・項目コードマスタにカードブランドの設定が無くても
     * 例外を投げない。該当ブランドは手数料0円・支払金額＝売上金額として行を生成し、
     * 他の正常なブランドの表示まで巻き添えで消えないようにする（確定処理
     * （{@link #calculateJcbLineItems}）は精算金額を誤って計上しないため従来通り
     * 例外を投げて処理を止める。参照専用のこのメソッドとは用途が異なるため、
     * 挙動を分けている）。
     */
    public List<TransferLineItem> calculateJcbLineItemsForInquiry() {
        return calculateJcbLineItemsForInquiry(unprocessedBatchIds(PAYMENT_TYPE_JCB));
    }

    public List<TransferLineItem> calculateJcbLineItemsForInquiry(List<Integer> batchIds) {
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
            int totalSalesAmount = aggregate.getTotalSalesAmount().intValue();
            Optional<SettlementFeeRate> rate = settlementFeeRateRepository
                    .findByPaymentCompanyAndCardBrand(PAYMENT_TYPE_JCB, cardBrand);
            Optional<SettlementItemCode> itemCode = settlementItemCodeRepository
                    .findByPaymentCompanyAndCardBrandAndAmountType(
                            PAYMENT_TYPE_JCB, cardBrand, AMOUNT_TYPE_PAYMENT);

            if (rate.isPresent() && itemCode.isPresent()) {
                FeeCalculationResult result = calculate(totalSalesAmount, rate.get());
                lineItems.add(new TransferLineItem(
                        aggregate.getTradeCode(), itemCode.get().getItemCode(), 1,
                        result.getPayableAmount1(), totalSalesAmount, result.getAcquirerFeeTaxFree(),
                        result.getAcquirerFeeBase(), result.getAcquirerFeeTax(), aggregate.getBatchId()));
            } else {
                // マスタ未整備のブランドは手数料0円・支払金額＝売上金額として表示する。
                // 項目コードも未登録の場合は、画面表示用にカードブランド名をそのまま
                // itemCode代わりに使う（PaygateStationInquiryServiceのbuildRows()側で
                // 登録済み項目コードに該当しなければそのままブランド名として表示する）。
                String fallbackItemCode = itemCode.map(SettlementItemCode::getItemCode).orElse(cardBrand);
                lineItems.add(new TransferLineItem(
                        aggregate.getTradeCode(), fallbackItemCode, 1,
                        totalSalesAmount, totalSalesAmount, 0, 0, 0, aggregate.getBatchId()));
            }
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
        return calculateSumarejoLineItems(unprocessedBatchIds(PAYMENT_TYPE_SUMAREJO));
    }

    public List<TransferLineItem> calculateSumarejoLineItems(List<Integer> batchIds) {
        if (batchIds.isEmpty()) {
            return List.of();
        }

        List<TerminalFeeAggregate> aggregates =
                terminalMonthlyFeeRepository.sumByTradeCodeAndUnitPrice(batchIds);
        SettlementItemCode baseItemCode = findItemCode(MASTER_COMPANY_SUMAREJO, "本体");
        SettlementItemCode adjustmentItemCode = findItemCode(MASTER_COMPANY_SUMAREJO, "調整");

        Map<SumarejoGroupKey, Integer> baseAmountByGroup = new LinkedHashMap<>();
        Map<SumarejoGroupKey, Integer> premiumAmountByGroup = new LinkedHashMap<>();
        for (TerminalFeeAggregate aggregate : aggregates) {
            if (SUMAREJO_EXCLUDED_TEST_TRADE_CODE.equals(aggregate.getTradeCode())) {
                continue;
            }
            SumarejoGroupKey key = new SumarejoGroupKey(aggregate.getTradeCode(), aggregate.getBatchId());
            int terminalCount = aggregate.getTerminalCount().intValue();
            int premiumPerTerminal = Math.max(aggregate.getUnitPrice() - SUMAREJO_BASE_UNIT_PRICE, 0);
            baseAmountByGroup.merge(
                    key, SUMAREJO_BASE_UNIT_PRICE * terminalCount, Integer::sum);
            if (premiumPerTerminal > 0) {
                premiumAmountByGroup.merge(
                        key, premiumPerTerminal * terminalCount, Integer::sum);
            }
        }

        List<TransferLineItem> lineItems = new ArrayList<>();
        for (Map.Entry<SumarejoGroupKey, Integer> entry : baseAmountByGroup.entrySet()) {
            lineItems.add(sumarejoLineItem(
                    entry.getKey().tradeCode(), baseItemCode.getItemCode(), entry.getValue(),
                    entry.getKey().batchId()));
        }
        for (Map.Entry<SumarejoGroupKey, Integer> entry : premiumAmountByGroup.entrySet()) {
            lineItems.add(sumarejoLineItem(
                    entry.getKey().tradeCode(), adjustmentItemCode.getItemCode(), entry.getValue(),
                    entry.getKey().batchId()));
        }
        return lineItems;
    }

    /**
     * スマレジの端末月額集計を、帳票出力画面でファイル単位に絞り込めるようにするための
     * 集計キー（取引コード×元ファイル）。同じ取引コードでも元ファイルが異なれば
     * 別々のTransferLineItemとして保存する。
     */
    private record SumarejoGroupKey(String tradeCode, int batchId) {
    }

    /**
     * スマレジ(端末月額利用料)は「売上」ではなく端末レンタルの定額料金（決済事業者への
     * 手数料に相当するもの）のため、帳票（支払明細書）上は決済金額合計(A)を0円とし、
     * 月額料金そのものを事業者手数料（課税対象：本体・消費税）として計上する
     * （サンプル帳票の実データで確認済み。事業者手数料差引後決済金額(A)-(B)は
     * 0-本体-消費税で負数になる）。amount（統合振込CSVで使う支払金額①）自体は
     * 従来どおり月額料金の金額をそのまま使う（符号は変更しない）。
     */
    private TransferLineItem sumarejoLineItem(
            String tradeCode, String itemCode, int monthlyFeeAmount, int batchId) {
        int tax = settlementFeeCalculator.calculateTax(monthlyFeeAmount);
        return new TransferLineItem(
                tradeCode, itemCode, 1, monthlyFeeAmount, 0, 0, monthlyFeeAmount, tax, batchId);
    }

    /**
     * m_netstar_sales_summaryは店舗単位1行で、ブランド別の内訳は
     * alipay_net_amount・dpay_net_amount・paypay_net_amount・wechat_net_amount
     * （rakuten_*・smartcode_*は項目コード未対応のため未使用）として既に分かれているため
     * GROUP BYは不要。計算基準は売上ではなく差引金額(_net_amount)である点に注意
     * （11_NETSTARS.xlsxのピボット列見出し「合計 / Alipay差引金額」で確認済み）。
     */
    public List<TransferLineItem> calculateNetstarLineItems() {
        return calculateNetstarLineItems(unprocessedBatchIds(PAYMENT_TYPE_NETSTAR));
    }

    public List<TransferLineItem> calculateNetstarLineItems(List<Integer> batchIds) {
        if (batchIds.isEmpty()) {
            return List.of();
        }

        List<NetstarSalesSummary> rows = netstarSalesSummaryRepository.findByBatchIdIn(batchIds);

        List<TransferLineItem> lineItems = new ArrayList<>();
        for (NetstarSalesSummary row : rows) {
            addNetstarBrandLineItem(lineItems, row.getTradeCode(), "Alipay", row.getAlipayNetAmount(), row.getBatchId());
            addNetstarBrandLineItem(lineItems, row.getTradeCode(), "PayPay", row.getPaypayNetAmount(), row.getBatchId());
            addNetstarBrandLineItem(lineItems, row.getTradeCode(), "d払い", row.getDpayNetAmount(), row.getBatchId());
            addNetstarBrandLineItem(lineItems, row.getTradeCode(), "WeChatPay", row.getWechatNetAmount(), row.getBatchId());
        }
        return lineItems;
    }

    private void addNetstarBrandLineItem(
            List<TransferLineItem> lineItems, String tradeCode, String cardBrand, int netAmount, int batchId) {
        if (netAmount == 0) {
            return;
        }
        SettlementFeeRate rate = findFeeRate(PAYMENT_TYPE_NETSTAR, cardBrand);
        SettlementItemCode itemCode = findItemCode(PAYMENT_TYPE_NETSTAR, cardBrand);
        FeeCalculationResult result = calculate(netAmount, rate);
        lineItems.add(new TransferLineItem(tradeCode, itemCode.getItemCode(), 1, result.getPayableAmount1(),
                netAmount, result.getAcquirerFeeTaxFree(), result.getAcquirerFeeBase(), result.getAcquirerFeeTax(),
                batchId));
    }

    /**
     * m_rakuten_pay_transactionを取引コード単位でtotal_amountを合計し、
     * PURCHASE_COLLECTモデルで計算する。
     */
    public List<TransferLineItem> calculateRakutenPayLineItems() {
        return calculateRakutenPayLineItems(unprocessedBatchIds(PAYMENT_TYPE_RAKUTENPAY));
    }

    public List<TransferLineItem> calculateRakutenPayLineItems(List<Integer> batchIds) {
        if (batchIds.isEmpty()) {
            return List.of();
        }

        List<RakutenPayAggregate> aggregates = rakutenPayTransactionRepository.sumByTradeCode(batchIds);
        SettlementFeeRate rate = findFeeRate(PAYMENT_TYPE_RAKUTENPAY, PAYMENT_TYPE_RAKUTENPAY);
        SettlementItemCode itemCode = findItemCode(PAYMENT_TYPE_RAKUTENPAY, PAYMENT_TYPE_RAKUTENPAY);

        List<TransferLineItem> lineItems = new ArrayList<>();
        for (RakutenPayAggregate aggregate : aggregates) {
            int totalAmount = aggregate.getTotalAmount().intValue();
            FeeCalculationResult result = calculate(totalAmount, rate);
            lineItems.add(new TransferLineItem(
                    aggregate.getTradeCode(), itemCode.getItemCode(), 1, result.getPayableAmount1(),
                    totalAmount, result.getAcquirerFeeTaxFree(),
                    result.getAcquirerFeeBase(), result.getAcquirerFeeTax(), aggregate.getBatchId()));
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
        return calculateVisaMasterLineItems(unprocessedBatchIds(PAYMENT_TYPE_VISA_MASTER));
    }

    public List<TransferLineItem> calculateVisaMasterLineItems(List<Integer> batchIds) {
        if (batchIds.isEmpty()) {
            return List.of();
        }

        List<VisaMasterAggregate> aggregates = visaMasterTransactionRepository.sumByTradeCode(batchIds);
        SettlementItemCode itemCode = findItemCode(PAYMENT_TYPE_VISA_MASTER, MASTER_COMPANY_VISA_MASTER);

        List<TransferLineItem> lineItems = new ArrayList<>();
        for (VisaMasterAggregate aggregate : aggregates) {
            int grossAmount = aggregate.getTotalSalesAmount().intValue();
            int acquirerFeeTaxFree = aggregate.getTotalFeeAmount1().intValue();
            int payableAmount1 = grossAmount - acquirerFeeTaxFree;
            lineItems.add(new TransferLineItem(aggregate.getTradeCode(), itemCode.getItemCode(), 1, payableAmount1,
                    grossAmount, acquirerFeeTaxFree, 0, 0, aggregate.getBatchId()));
        }
        return lineItems;
    }

    private List<Integer> unprocessedBatchIds(String paymentType) {
        return unprocessedBatches(paymentType).stream()
                .map(ImportBatch::getBatchId)
                .toList();
    }

    private List<ImportBatch> unprocessedBatches(String paymentType) {
        return importBatchRepository.findByPaymentTypeAndTransferBatchIdIsNull(paymentType);
    }

    /**
     * 5社すべての未確定インポートバッチをまとめて返す。統合振込CSV作成のプレビュー画面で、
     * 確定前に「どのファイルが対象になるか」をユーザーに提示するために使う
     * （集計対象の決定ロジック自体には影響しない）。
     */
    public List<ImportBatch> findTargetImportBatches() {
        List<ImportBatch> all = new ArrayList<>();
        all.addAll(unprocessedBatches(PAYMENT_TYPE_JCB));
        all.addAll(unprocessedBatches(PAYMENT_TYPE_SUMAREJO));
        all.addAll(unprocessedBatches(PAYMENT_TYPE_NETSTAR));
        all.addAll(unprocessedBatches(PAYMENT_TYPE_RAKUTENPAY));
        all.addAll(unprocessedBatches(PAYMENT_TYPE_VISA_MASTER));
        return all;
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
