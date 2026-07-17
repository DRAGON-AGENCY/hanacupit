package com.cupit.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

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
 * その他統合振込CSV作成（stera terminal）のための集計サービス。項目コード方式ではないため
 * {@link JftdTransferCalculationService}とは計算モデルが異なる。取引コード単位で
 * stera JCB・stera code・steraクレジット3フォーマットの明細を合算し、振込金額を算出する。
 * 振込先口座（m_stera_store）との突合はインポート時点で完了済みの前提のため、ここでは
 * 突合ロジックを持たず、口座情報を確定時点のスナップショットとして取得するだけに留める
 * （CLAUDE.md「口座マスタ（m_stera_store）の解決規則」を参照）。
 */
@Service
public class SteraTransferCalculationService {

    private static final String PAYMENT_TYPE_STERA_JCB = "stera JCB";
    private static final String PAYMENT_TYPE_STERA_CODE = "stera code";
    private static final String PAYMENT_TYPE_STERA_CREDIT = "steraクレジット";

    /** 仕入手数料率2.75%。実データ全件検証済み（調査メモ参照）。 */
    private static final BigDecimal ACQUIRER_FEE_RATE = new BigDecimal("0.0275");

    /** 当社手数料率0.2%。実データ全件検証済み。 */
    private static final BigDecimal COMPANY_FEE_RATE = new BigDecimal("0.002");

    /** 振込手数料129円が0円になる振込先金融機関コード（ＧＭＯあおぞらネット銀行）。 */
    private static final String ZERO_TRANSFER_FEE_BANK_CODE = "0310";

    private static final int TRANSFER_FEE = 129;

    private final ImportBatchRepository importBatchRepository;
    private final SteraJcbSalesDetailRepository steraJcbSalesDetailRepository;
    private final SteraCodeSettlementDetailRepository steraCodeSettlementDetailRepository;
    private final SteraCreditSalesDetailRepository steraCreditSalesDetailRepository;
    private final SteraStoreRepository steraStoreRepository;

    public SteraTransferCalculationService(
            ImportBatchRepository importBatchRepository,
            SteraJcbSalesDetailRepository steraJcbSalesDetailRepository,
            SteraCodeSettlementDetailRepository steraCodeSettlementDetailRepository,
            SteraCreditSalesDetailRepository steraCreditSalesDetailRepository,
            SteraStoreRepository steraStoreRepository) {
        this.importBatchRepository = importBatchRepository;
        this.steraJcbSalesDetailRepository = steraJcbSalesDetailRepository;
        this.steraCodeSettlementDetailRepository = steraCodeSettlementDetailRepository;
        this.steraCreditSalesDetailRepository = steraCreditSalesDetailRepository;
        this.steraStoreRepository = steraStoreRepository;
    }

    /**
     * 3フォーマットすべての未処理インポート分をまとめて集計する。プレビュー表示用。
     * ロックを取らないため、確定処理では使わないこと
     * （確定処理には{@link #calculateAllLineItems(Map)}を使う）。
     */
    public List<SteraTransferLineItem> calculateAllLineItems() {
        Map<String, List<Integer>> batchIdsByPaymentType = new LinkedHashMap<>();
        batchIdsByPaymentType.put(PAYMENT_TYPE_STERA_JCB, unprocessedBatchIds(PAYMENT_TYPE_STERA_JCB));
        batchIdsByPaymentType.put(PAYMENT_TYPE_STERA_CODE, unprocessedBatchIds(PAYMENT_TYPE_STERA_CODE));
        batchIdsByPaymentType.put(PAYMENT_TYPE_STERA_CREDIT, unprocessedBatchIds(PAYMENT_TYPE_STERA_CREDIT));
        return calculateAllLineItems(batchIdsByPaymentType);
    }

    /**
     * 3フォーマットすべてを、呼び出し側が指定したバッチIDの集合に限定して集計する。
     * その他統合振込CSV作成の確定処理専用。確定処理はまず対象バッチを排他ロックで
     * 確保してからこのメソッドを呼び出すこと。
     *
     * @param batchIdsByPaymentType 決済種別（"stera JCB"・"stera code"・"steraクレジット"）
     *                              をキーとした対象バッチIDのマップ
     */
    public List<SteraTransferLineItem> calculateAllLineItems(Map<String, List<Integer>> batchIdsByPaymentType) {
        Map<String, Long> grossAmountByTradeCode = new LinkedHashMap<>();
        Map<String, Long> acquirerFeeByTradeCode = new LinkedHashMap<>();
        Map<String, Long> companyFeeByTradeCode = new LinkedHashMap<>();

        accumulateJcb(batchIdsByPaymentType.getOrDefault(PAYMENT_TYPE_STERA_JCB, List.of()),
                grossAmountByTradeCode, acquirerFeeByTradeCode, companyFeeByTradeCode);
        accumulateCode(batchIdsByPaymentType.getOrDefault(PAYMENT_TYPE_STERA_CODE, List.of()),
                grossAmountByTradeCode, acquirerFeeByTradeCode, companyFeeByTradeCode);
        accumulateCredit(batchIdsByPaymentType.getOrDefault(PAYMENT_TYPE_STERA_CREDIT, List.of()),
                grossAmountByTradeCode, acquirerFeeByTradeCode, companyFeeByTradeCode);

        List<SteraTransferLineItem> lineItems = new ArrayList<>();
        for (String tradeCode : grossAmountByTradeCode.keySet()) {
            lineItems.add(buildLineItem(tradeCode, grossAmountByTradeCode, acquirerFeeByTradeCode,
                    companyFeeByTradeCode));
        }
        return lineItems;
    }

    private SteraTransferLineItem buildLineItem(
            String tradeCode, Map<String, Long> grossAmountByTradeCode,
            Map<String, Long> acquirerFeeByTradeCode, Map<String, Long> companyFeeByTradeCode) {
        SteraStore store = steraStoreRepository.findByTradeCode(tradeCode)
                .orElseThrow(() -> new IllegalStateException(
                        "取引コード「" + tradeCode + "」に対応する振込先口座情報がm_stera_storeに"
                                + "存在しません。その他精算データ作成の時点で突合済みのはずのため、"
                                + "確定処理までの間にマスタが削除された可能性があります。"));

        int grossAmount = grossAmountByTradeCode.get(tradeCode).intValue();
        int acquirerFee = acquirerFeeByTradeCode.getOrDefault(tradeCode, 0L).intValue();
        int companyFee = companyFeeByTradeCode.getOrDefault(tradeCode, 0L).intValue();
        int transferFee = ZERO_TRANSFER_FEE_BANK_CODE.equals(store.getBankCode()) ? 0 : TRANSFER_FEE;
        int netAmount = grossAmount - acquirerFee - companyFee - transferFee;

        return new SteraTransferLineItem(tradeCode, grossAmount, acquirerFee, companyFee, transferFee, netAmount,
                store.getBankCode(), store.getBankName(), store.getBankBranchCode(), store.getBranchName(),
                store.getAccountType(), store.getAccountNo(), store.getAccountHolderKana());
    }

    /**
     * stera JCBは取引コード×お取扱カード名×支払区分でGROUP BYした金額を対象にする。
     */
    private void accumulateJcb(
            List<Integer> batchIds, Map<String, Long> grossAmountByTradeCode,
            Map<String, Long> acquirerFeeByTradeCode, Map<String, Long> companyFeeByTradeCode) {
        if (batchIds.isEmpty()) {
            return;
        }
        List<SteraJcbGroupAggregate> aggregates =
                steraJcbSalesDetailRepository.sumByTradeCodeCardNameAndPaymentMethod(batchIds);
        for (SteraJcbGroupAggregate aggregate : aggregates) {
            accumulateGroup(aggregate.getTradeCode(), aggregate.getTotalSalesAmount(),
                    grossAmountByTradeCode, acquirerFeeByTradeCode, companyFeeByTradeCode);
        }
    }

    /**
     * stera codeは取引コード×ブランドでGROUP BYした金額を対象にする（QR決済は
     * 支払回数の概念が無いため、stera JCB・steraクレジットと異なりブランドのみで足りる）。
     */
    private void accumulateCode(
            List<Integer> batchIds, Map<String, Long> grossAmountByTradeCode,
            Map<String, Long> acquirerFeeByTradeCode, Map<String, Long> companyFeeByTradeCode) {
        if (batchIds.isEmpty()) {
            return;
        }
        List<SteraCodeGroupAggregate> aggregates =
                steraCodeSettlementDetailRepository.sumByTradeCodeAndBrand(batchIds);
        for (SteraCodeGroupAggregate aggregate : aggregates) {
            accumulateGroup(aggregate.getTradeCode(), aggregate.getTotalSettlementAmount(),
                    grossAmountByTradeCode, acquirerFeeByTradeCode, companyFeeByTradeCode);
        }
    }

    /**
     * steraクレジットは取引コード×カードブランド×取扱区分でGROUP BYした金額を対象にする。
     */
    private void accumulateCredit(
            List<Integer> batchIds, Map<String, Long> grossAmountByTradeCode,
            Map<String, Long> acquirerFeeByTradeCode, Map<String, Long> companyFeeByTradeCode) {
        if (batchIds.isEmpty()) {
            return;
        }
        List<SteraCreditGroupAggregate> aggregates =
                steraCreditSalesDetailRepository.sumByTradeCodeCardBrandAndTransactionType(batchIds);
        for (SteraCreditGroupAggregate aggregate : aggregates) {
            accumulateGroup(aggregate.getTradeCode(), aggregate.getTotalBillingAmount(),
                    grossAmountByTradeCode, acquirerFeeByTradeCode, companyFeeByTradeCode);
        }
    }

    /**
     * 1グループ（取引コード×決済種別ごとの手数料計算単位）分の金額を、取引コード単位の
     * 売上金額・仕入手数料・当社手数料それぞれの合計へ積み上げる。グループを跨いでから
     * 丸めると実データと1円ズレることを確認済みのため、必ずグループ単位で丸めてから合算する
     * （調査メモ「01-047」の1円ズレの教訓）。
     */
    private void accumulateGroup(
            String tradeCode, long groupTotalAmount, Map<String, Long> grossAmountByTradeCode,
            Map<String, Long> acquirerFeeByTradeCode, Map<String, Long> companyFeeByTradeCode) {
        grossAmountByTradeCode.merge(tradeCode, groupTotalAmount, Long::sum);
        acquirerFeeByTradeCode.merge(
                tradeCode, (long) roundHalfUp(groupTotalAmount, ACQUIRER_FEE_RATE), Long::sum);
        companyFeeByTradeCode.merge(
                tradeCode, (long) roundHalfUp(groupTotalAmount, COMPANY_FEE_RATE), Long::sum);
    }

    private int roundHalfUp(long amount, BigDecimal rate) {
        return BigDecimal.valueOf(amount).multiply(rate).setScale(0, RoundingMode.HALF_UP).intValueExact();
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
     * 3フォーマットすべての未確定インポートバッチをまとめて返す。統合振込CSV作成の
     * プレビュー画面で、確定前に「どのファイルが対象になるか」（締め日を含む）を
     * ユーザーに提示するために使う（集計対象の決定ロジック自体には影響しない）。
     */
    public List<ImportBatch> findTargetImportBatches() {
        List<ImportBatch> all = new ArrayList<>();
        all.addAll(unprocessedBatches(PAYMENT_TYPE_STERA_JCB));
        all.addAll(unprocessedBatches(PAYMENT_TYPE_STERA_CODE));
        all.addAll(unprocessedBatches(PAYMENT_TYPE_STERA_CREDIT));
        return all;
    }

}
