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
import com.cupit.repository.ImportBatchRepository;
import com.cupit.repository.SteraJcbSalesDetailRepository;
import com.cupit.repository.SteraJcbSalesDetailRepository.SteraJcbStoreGroupAggregate;

/**
 * stera terminal精算情報照会(JCB)画面のビジネスロジックを担うサービス。
 * 全期間の明細を一括で返し、締め日・カード名・支払方法による絞り込みは画面側（JS）で行う。
 * 明細の粒度は取引コード×支店（store_number）×カード名×お支払方法×支払区分×締め日
 * （1取引コードに複数の支店が存在する運用があり、支店ごとに店舗名も異なるため）。
 *
 * 手数料率（仕入手数料2.75%・当社手数料0.2%）は{@link SteraTransferCalculationService}で
 * 実データ全件検証済みの値と同じものを使う。ただし{@link SteraTransferCalculationService}は
 * 統合振込CSV作成用に取引コード単位まで合算し、かつ振込手数料（129円、口座により0円）を
 * 差し引いた最終振込金額を算出するのに対し、本画面は支店単位の明細を見せるための照会
 * であり、振込手数料（取引コード＝1回の銀行振込につき1回だけ発生する）は対象外とする
 * （支店ごとの行にまで振込手数料を配分すると二重計上になるため）。
 */
@Service
public class SteraJcbInquiryService {

    private static final String PAYMENT_TYPE_STERA_JCB = "stera JCB";

    /** 仕入手数料率2.75%。{@link SteraTransferCalculationService}と同じ実データ検証済みの値。 */
    private static final BigDecimal ACQUIRER_FEE_RATE = new BigDecimal("0.0275");

    /** 当社手数料率0.2%。{@link SteraTransferCalculationService}と同じ実データ検証済みの値。 */
    private static final BigDecimal COMPANY_FEE_RATE = new BigDecimal("0.002");

    private final ImportBatchRepository importBatchRepository;
    private final SteraJcbSalesDetailRepository steraJcbSalesDetailRepository;

    public SteraJcbInquiryService(
            ImportBatchRepository importBatchRepository,
            SteraJcbSalesDetailRepository steraJcbSalesDetailRepository) {
        this.importBatchRepository = importBatchRepository;
        this.steraJcbSalesDetailRepository = steraJcbSalesDetailRepository;
    }

    public List<SteraJcbRow> findAll() {
        List<ImportBatch> batches = importBatchRepository.findAll().stream()
                .filter(b -> PAYMENT_TYPE_STERA_JCB.equals(b.getPaymentType()))
                .toList();

        List<SteraJcbRow> rows = new ArrayList<>();
        for (Map.Entry<LocalDate, List<Integer>> group : groupBatchIdsByCutoffDate(batches).entrySet()) {
            LocalDate cutoffDate = group.getKey();
            List<Integer> batchIds = group.getValue();

            for (SteraJcbStoreGroupAggregate aggregate
                    : steraJcbSalesDetailRepository.sumByStoreCardNameAndPaymentMethod(batchIds)) {
                rows.add(buildRow(aggregate, cutoffDate));
            }
        }

        rows.sort(Comparator
                .comparing(SteraJcbRow::getTradeCode)
                .thenComparing(SteraJcbRow::getStoreNumber, Comparator.nullsLast(Comparator.naturalOrder())));
        return rows;
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

    private SteraJcbRow buildRow(SteraJcbStoreGroupAggregate aggregate, LocalDate cutoffDate) {
        int salesAmount = aggregate.getTotalSalesAmount().intValue();
        int acquirerFee = roundHalfUp(salesAmount, ACQUIRER_FEE_RATE);
        int companyFee = roundHalfUp(salesAmount, COMPANY_FEE_RATE);
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
