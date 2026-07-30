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

import com.cupit.dto.SteraJcbRow;
import com.cupit.model.ImportBatch;
import com.cupit.model.SettlementFeeRate;
import com.cupit.repository.ImportBatchRepository;
import com.cupit.repository.SettlementFeeRateRepository;
import com.cupit.repository.SteraJcbSalesDetailRepository;
import com.cupit.repository.SteraJcbSalesDetailRepository.SteraJcbStoreGroupAggregate;

/**
 * stera terminal精算情報照会(JCB)画面のビジネスロジックを担うサービス。
 * 全期間の明細を一括で返し、締め日・カード名・支払方法による絞り込みは画面側（JS）で行う。
 * 明細の粒度は取引コード×支店（store_number）×カード名×お支払方法×支払区分×締め日
 * （1取引コードに複数の支店が存在する運用があり、支店ごとに店舗名も異なるため）。
 *
 * 手数料率（仕入手数料2.75%・当社手数料0.2%）はm_settlement_fee_rate
 * （payment_company='stera terminal', card_brand='共通'の1行）を参照する
 * （{@link SteraTransferCalculationService}と同じマスタ行を使う）。以前はコード内の
 * 定数だったが、手数料率マスタの値を反映できるようマスタ参照に変更した。ただし
 * {@link SteraTransferCalculationService}は統合振込CSV作成用に取引コード単位まで合算し、
 * かつ振込手数料（129円、口座により0円）を差し引いた最終振込金額を算出するのに対し、
 * 本画面は支店単位の明細を見せるための照会であり、振込手数料（取引コード＝1回の銀行振込
 * につき1回だけ発生する）は対象外とする（支店ごとの行にまで振込手数料を配分すると
 * 二重計上になるため）。
 */
@Service
public class SteraJcbInquiryService {

    private static final String PAYMENT_TYPE_STERA_JCB = "stera JCB";

    private static final String FEE_RATE_PAYMENT_COMPANY = "stera terminal";
    private static final String FEE_RATE_CARD_BRAND = "共通";

    private final ImportBatchRepository importBatchRepository;
    private final SteraJcbSalesDetailRepository steraJcbSalesDetailRepository;
    private final SettlementFeeRateRepository settlementFeeRateRepository;

    public SteraJcbInquiryService(
            ImportBatchRepository importBatchRepository,
            SteraJcbSalesDetailRepository steraJcbSalesDetailRepository,
            SettlementFeeRateRepository settlementFeeRateRepository) {
        this.importBatchRepository = importBatchRepository;
        this.steraJcbSalesDetailRepository = steraJcbSalesDetailRepository;
        this.settlementFeeRateRepository = settlementFeeRateRepository;
    }

    public List<SteraJcbRow> findAll() {
        List<ImportBatch> batches = importBatchRepository.findAll().stream()
                .filter(b -> PAYMENT_TYPE_STERA_JCB.equals(b.getPaymentType()))
                .toList();

        // ループの内側で毎回マスタを引かないよう、手数料率は事前に1回だけ取得する。
        SettlementFeeRate feeRate = findFeeRate();
        BigDecimal acquirerFeeRate = feeRate.getAcquirerFeeRate();
        BigDecimal companyFeeRate = feeRate.getOurFeeRateBase();

        List<SteraJcbRow> rows = new ArrayList<>();
        for (Map.Entry<LocalDate, List<Integer>> group : groupBatchIdsByCutoffDate(batches).entrySet()) {
            LocalDate cutoffDate = group.getKey();
            List<Integer> batchIds = group.getValue();

            for (SteraJcbStoreGroupAggregate aggregate
                    : steraJcbSalesDetailRepository.sumByStoreCardNameAndPaymentMethod(batchIds)) {
                rows.add(buildRow(aggregate, cutoffDate, acquirerFeeRate, companyFeeRate));
            }
        }

        rows.sort(Comparator
                .comparing(SteraJcbRow::getTradeCode)
                .thenComparing(SteraJcbRow::getStoreNumber, Comparator.nullsLast(Comparator.naturalOrder())));
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

    /**
     * 締め日でグループ化する。{@code Collectors.groupingBy}はグループ化キーがnullだと
     * 例外を投げる仕様のため使えない（締め日機能が実装される前にインポートされた
     * 既存バッチはcutoff_dateがnullのままであり、これも1グループとして扱う必要がある。
     * [[hanacupit-paygate-station-inquiry-feature]]で踏んだ不具合と同じ注意点）。
     */
    private Map<LocalDate, List<Integer>> groupBatchIdsByCutoffDate(List<ImportBatch> batches) {
        Map<LocalDate, List<Integer>> result = new LinkedHashMap<>();
        for (ImportBatch batch : batches) {
            result.computeIfAbsent(batch.getCutoffDate(), k -> new ArrayList<>()).add(batch.getBatchId());
        }
        return result;
    }

    private SteraJcbRow buildRow(
            SteraJcbStoreGroupAggregate aggregate, LocalDate cutoffDate,
            BigDecimal acquirerFeeRate, BigDecimal companyFeeRate) {
        int salesAmount = aggregate.getTotalSalesAmount().intValue();
        int acquirerFee = roundHalfUp(salesAmount, acquirerFeeRate);
        int companyFee = roundHalfUp(salesAmount, companyFeeRate);
        int settlementAmount = salesAmount - acquirerFee - companyFee;
        return new SteraJcbRow(
                aggregate.getTradeCode(), aggregate.getStoreNumber(), aggregate.getStoreName(),
                aggregate.getCardName(), aggregate.getPaymentMethod(), aggregate.getPaymentType(), cutoffDate,
                salesAmount, acquirerFee, companyFee, settlementAmount);
    }

    private int roundHalfUp(long amount, BigDecimal rate) {
        return BigDecimal.valueOf(amount).multiply(rate).setScale(0, RoundingMode.HALF_UP).intValueExact();
    }

}
