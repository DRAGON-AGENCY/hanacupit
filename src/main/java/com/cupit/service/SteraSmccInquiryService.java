package com.cupit.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.cupit.dto.SteraSmccRow;
import com.cupit.model.ImportBatch;
import com.cupit.model.SettlementFeeRate;
import com.cupit.repository.ImportBatchRepository;
import com.cupit.repository.SettlementFeeRateRepository;
import com.cupit.repository.SteraCodeSettlementDetailRepository;
import com.cupit.repository.SteraCodeSettlementDetailRepository.SteraCodeStoreGroupAggregate;
import com.cupit.repository.SteraCreditSalesDetailRepository;
import com.cupit.repository.SteraCreditSalesDetailRepository.SteraCreditStoreGroupAggregate;
import com.cupit.repository.SteraStoreRepository;

/**
 * stera terminal精算情報照会(SMCC)画面のビジネスロジックを担うサービス。
 * steraクレジット（m_stera_credit_sales_detail）とstera code（m_stera_code_settlement_detail）の
 * 2フォーマットを1つの一覧にまとめて返す。全期間の明細を一括で返し、締め日・決済フォーマット・
 * カード名・取扱区分による絞り込みは画面側（JS）で行う。
 *
 * 手数料率（仕入手数料2.75%・当社手数料0.2%）はm_settlement_fee_rate
 * （payment_company='stera terminal', card_brand='共通'の1行）を参照する
 * （{@link SteraTransferCalculationService}と同じマスタ行を使う）。以前はコード内の
 * 定数だったが、手数料率マスタの値を反映できるようマスタ参照に変更した。ただし
 * {@link SteraTransferCalculationService}は統合振込CSV作成用に取引コード単位まで合算し、
 * 振込手数料も差し引くのに対し、本画面は識別番号（merchant_id／terminal_id）単位の明細を
 * 見せるための照会であり、振込手数料（取引コード＝1回の銀行振込につき1回だけ発生する）は
 * 対象外とする（[[hanacupit-stera-jcb-inquiry-feature]]と同じ設計判断）。
 */
@Service
public class SteraSmccInquiryService {

    private static final String PAYMENT_TYPE_STERA_CREDIT = "steraクレジット";
    private static final String PAYMENT_TYPE_STERA_CODE = "stera code";

    private static final String PAYMENT_FORMAT_CREDIT = "steraクレジット";
    private static final String PAYMENT_FORMAT_CODE = "stera code";

    private static final String FEE_RATE_PAYMENT_COMPANY = "stera terminal";
    private static final String FEE_RATE_CARD_BRAND = "共通";

    private final ImportBatchRepository importBatchRepository;
    private final SteraCreditSalesDetailRepository steraCreditSalesDetailRepository;
    private final SteraCodeSettlementDetailRepository steraCodeSettlementDetailRepository;
    private final SteraStoreRepository steraStoreRepository;
    private final SettlementFeeRateRepository settlementFeeRateRepository;

    public SteraSmccInquiryService(
            ImportBatchRepository importBatchRepository,
            SteraCreditSalesDetailRepository steraCreditSalesDetailRepository,
            SteraCodeSettlementDetailRepository steraCodeSettlementDetailRepository,
            SteraStoreRepository steraStoreRepository,
            SettlementFeeRateRepository settlementFeeRateRepository) {
        this.importBatchRepository = importBatchRepository;
        this.steraCreditSalesDetailRepository = steraCreditSalesDetailRepository;
        this.steraCodeSettlementDetailRepository = steraCodeSettlementDetailRepository;
        this.steraStoreRepository = steraStoreRepository;
        this.settlementFeeRateRepository = settlementFeeRateRepository;
    }

    public List<SteraSmccRow> findAll() {
        List<ImportBatch> allBatches = importBatchRepository.findAll();

        List<ImportBatch> creditBatches = allBatches.stream()
                .filter(b -> PAYMENT_TYPE_STERA_CREDIT.equals(b.getPaymentType()))
                .toList();
        List<ImportBatch> codeBatches = allBatches.stream()
                .filter(b -> PAYMENT_TYPE_STERA_CODE.equals(b.getPaymentType()))
                .toList();

        Map<String, String> storeNameByTradeCode = buildStoreNameMap();

        // ループの内側で毎回マスタを引かないよう、手数料率は事前に1回だけ取得する。
        SettlementFeeRate feeRate = findFeeRate();
        BigDecimal acquirerFeeRate = feeRate.getAcquirerFeeRate();
        BigDecimal companyFeeRate = feeRate.getOurFeeRateBase();

        List<SteraSmccRow> rows = new ArrayList<>();
        rows.addAll(collectCreditRows(creditBatches, acquirerFeeRate, companyFeeRate));
        rows.addAll(collectCodeRows(codeBatches, storeNameByTradeCode, acquirerFeeRate, companyFeeRate));

        rows.sort(Comparator
                .comparing(SteraSmccRow::getTradeCode)
                .thenComparing(SteraSmccRow::getStoreNumber, Comparator.nullsLast(Comparator.naturalOrder())));
        return rows;
    }

    private SettlementFeeRate findFeeRate() {
        return settlementFeeRateRepository
                .findByPaymentCompanyAndCardBrand(FEE_RATE_PAYMENT_COMPANY, FEE_RATE_CARD_BRAND)
                .orElseThrow(() -> new IllegalStateException(
                        "手数料率マスタにstera terminal分の設定（payment_company='"
                                + FEE_RATE_PAYMENT_COMPANY + "', card_brand='" + FEE_RATE_CARD_BRAND
                                + "'）がありません。"));
    }

    private Map<String, String> buildStoreNameMap() {
        Map<String, String> map = new LinkedHashMap<>();
        for (var store : steraStoreRepository.findAll()) {
            map.putIfAbsent(store.getTradeCode(), store.getStoreName());
        }
        return map;
    }

    /**
     * 締め日でグループ化する。{@code Collectors.groupingBy}はグループ化キーがnullだと
     * 例外を投げる仕様のため使えない（[[hanacupit-paygate-station-inquiry-feature]]で
     * 踏んだ不具合と同じ注意点。締め日機能が実装される前にインポートされた既存バッチは
     * cutoff_dateがnullのままであり、これも1グループとして扱う必要がある）。
     */
    private Map<LocalDate, List<Integer>> groupBatchIdsByCutoffDate(List<ImportBatch> batches) {
        Map<LocalDate, List<Integer>> result = new LinkedHashMap<>();
        for (ImportBatch batch : batches) {
            result.computeIfAbsent(batch.getCutoffDate(), k -> new ArrayList<>()).add(batch.getBatchId());
        }
        return result;
    }

    private List<SteraSmccRow> collectCreditRows(
            List<ImportBatch> batches, BigDecimal acquirerFeeRate, BigDecimal companyFeeRate) {
        List<SteraSmccRow> rows = new ArrayList<>();
        for (Map.Entry<LocalDate, List<Integer>> group : groupBatchIdsByCutoffDate(batches).entrySet()) {
            LocalDate cutoffDate = group.getKey();
            for (SteraCreditStoreGroupAggregate aggregate
                    : steraCreditSalesDetailRepository.sumByMerchantCardBrandAndTransactionType(group.getValue())) {
                int salesAmount = aggregate.getTotalBillingAmount().intValue();
                int acquirerFee = roundHalfUp(salesAmount, acquirerFeeRate);
                int companyFee = roundHalfUp(salesAmount, companyFeeRate);
                rows.add(new SteraSmccRow(
                        aggregate.getTradeCode(), aggregate.getMerchantId(), aggregate.getStoreName(),
                        PAYMENT_FORMAT_CREDIT, aggregate.getCardBrand(), aggregate.getTransactionType(),
                        cutoffDate, salesAmount, acquirerFee, companyFee,
                        salesAmount - acquirerFee - companyFee));
            }
        }
        return rows;
    }

    private List<SteraSmccRow> collectCodeRows(
            List<ImportBatch> batches, Map<String, String> storeNameByTradeCode,
            BigDecimal acquirerFeeRate, BigDecimal companyFeeRate) {
        List<SteraSmccRow> rows = new ArrayList<>();
        for (Map.Entry<LocalDate, List<Integer>> group : groupBatchIdsByCutoffDate(batches).entrySet()) {
            LocalDate cutoffDate = group.getKey();
            for (SteraCodeStoreGroupAggregate aggregate
                    : steraCodeSettlementDetailRepository.sumByTerminalAndBrand(group.getValue())) {
                int salesAmount = aggregate.getTotalSettlementAmount().intValue();
                int acquirerFee = roundHalfUp(salesAmount, acquirerFeeRate);
                int companyFee = roundHalfUp(salesAmount, companyFeeRate);
                rows.add(new SteraSmccRow(
                        aggregate.getTradeCode(), aggregate.getTerminalId(),
                        storeNameByTradeCode.get(aggregate.getTradeCode()),
                        PAYMENT_FORMAT_CODE, aggregate.getBrand(), null,
                        cutoffDate, salesAmount, acquirerFee, companyFee,
                        salesAmount - acquirerFee - companyFee));
            }
        }
        return rows;
    }

    private int roundHalfUp(long amount, BigDecimal rate) {
        return BigDecimal.valueOf(amount).multiply(rate).setScale(0, RoundingMode.HALF_UP).intValueExact();
    }

}
