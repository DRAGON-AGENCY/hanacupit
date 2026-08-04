package com.cupit.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cupit.dto.PaygateStationRow;
import com.cupit.model.ImportBatch;
import com.cupit.model.JftdTransferDetail;
import com.cupit.model.NetstarSalesSummary;
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
 * PAYGATE Station精算情報照会画面のビジネスロジックを担うサービス。
 * 全決済会社・全期間の明細を一括で返し、締め日・決済会社・決済種類による絞り込みは
 * 画面側（JS）で行う（{@link #findAll()}はフィルタ条件を受け取らない）。
 * 明細の粒度は取引コード×決済会社×決済種類×締め日（1画面に全決済会社・全期間が
 * 混在するため、単純な取引コード単位には出来ない）。
 *
 * {@link TransferLineItem#getQuantity()}は全決済会社で常に1固定（実際の売上件数ではない）
 * ため、「売上件数」は決済会社ごとの生データ集計から別途取得する
 * （[[hanacupit-jftd-transfer-csv-feature]]の既存実装と同じ注意点）。
 */
@Service
public class PaygateStationInquiryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaygateStationInquiryService.class);

    private static final String COMPANY_JCB = "JCB";
    private static final String COMPANY_NETSTARS = "NETSTARS";
    private static final String COMPANY_VISA_MASTER = "VISA・Master";
    private static final String COMPANY_RAKUTENPAY = "楽天ペイ";
    private static final String COMPANY_SUMAREJO = "スマレジ";

    private static final String PAYMENT_TYPE_JCB = "JCB";
    private static final String PAYMENT_TYPE_NETSTAR = "ネットスターズ";
    private static final String PAYMENT_TYPE_VISA_MASTER = "住信SBI";
    private static final String PAYMENT_TYPE_RAKUTENPAY = "楽天ペイ";
    private static final String PAYMENT_TYPE_SUMAREJO = "スマレジ";

    private static final String AMOUNT_TYPE_PAYMENT = "PAYMENT";

    private static final String NETSTAR_BRAND_ALIPAY = "Alipay";
    private static final String NETSTAR_BRAND_PAYPAY = "PayPay";
    private static final String NETSTAR_BRAND_DPAY = "d払い";
    private static final String NETSTAR_BRAND_WECHAT = "WeChatPay";

    private final ImportBatchRepository importBatchRepository;
    private final JftdTransferCalculationService jftdTransferCalculationService;
    private final JcbSalesDetailRepository jcbSalesDetailRepository;
    private final TerminalMonthlyFeeRepository terminalMonthlyFeeRepository;
    private final NetstarSalesSummaryRepository netstarSalesSummaryRepository;
    private final RakutenPayTransactionRepository rakutenPayTransactionRepository;
    private final VisaMasterTransactionRepository visaMasterTransactionRepository;
    private final SettlementItemCodeRepository settlementItemCodeRepository;
    private final PaygateMappingRepository paygateMappingRepository;
    private final JftdTransferDetailRepository jftdTransferDetailRepository;

    public PaygateStationInquiryService(
            ImportBatchRepository importBatchRepository,
            JftdTransferCalculationService jftdTransferCalculationService,
            JcbSalesDetailRepository jcbSalesDetailRepository,
            TerminalMonthlyFeeRepository terminalMonthlyFeeRepository,
            NetstarSalesSummaryRepository netstarSalesSummaryRepository,
            RakutenPayTransactionRepository rakutenPayTransactionRepository,
            VisaMasterTransactionRepository visaMasterTransactionRepository,
            SettlementItemCodeRepository settlementItemCodeRepository,
            PaygateMappingRepository paygateMappingRepository,
            JftdTransferDetailRepository jftdTransferDetailRepository) {
        this.importBatchRepository = importBatchRepository;
        this.jftdTransferCalculationService = jftdTransferCalculationService;
        this.jcbSalesDetailRepository = jcbSalesDetailRepository;
        this.terminalMonthlyFeeRepository = terminalMonthlyFeeRepository;
        this.netstarSalesSummaryRepository = netstarSalesSummaryRepository;
        this.rakutenPayTransactionRepository = rakutenPayTransactionRepository;
        this.visaMasterTransactionRepository = visaMasterTransactionRepository;
        this.settlementItemCodeRepository = settlementItemCodeRepository;
        this.paygateMappingRepository = paygateMappingRepository;
        this.jftdTransferDetailRepository = jftdTransferDetailRepository;
    }

    public List<PaygateStationRow> findAll() {
        Map<String, String> storeNameByTradeCode = buildStoreNameMap();
        Map<String, String> cardBrandByItemCode = buildCardBrandMap();

        Map<String, List<ImportBatch>> batchesByPaymentType = importBatchRepository.findAll().stream()
                .filter(b -> isKnownPaymentType(b.getPaymentType()))
                .collect(Collectors.groupingBy(ImportBatch::getPaymentType));

        List<PaygateStationRow> rows = new ArrayList<>();
        rows.addAll(collectSafely(COMPANY_JCB, () -> collectJcbRows(
                batchesByPaymentType.getOrDefault(PAYMENT_TYPE_JCB, List.of()),
                storeNameByTradeCode, cardBrandByItemCode)));
        rows.addAll(collectSafely(COMPANY_NETSTARS, () -> collectNetstarRows(
                batchesByPaymentType.getOrDefault(PAYMENT_TYPE_NETSTAR, List.of()),
                storeNameByTradeCode, cardBrandByItemCode)));
        rows.addAll(collectSafely(COMPANY_SUMAREJO, () -> collectSumarejoRows(
                batchesByPaymentType.getOrDefault(PAYMENT_TYPE_SUMAREJO, List.of()), storeNameByTradeCode)));
        rows.addAll(collectSafely(COMPANY_RAKUTENPAY, () -> collectRakutenPayRows(
                batchesByPaymentType.getOrDefault(PAYMENT_TYPE_RAKUTENPAY, List.of()), storeNameByTradeCode)));
        rows.addAll(collectSafely(COMPANY_VISA_MASTER, () -> collectVisaMasterRows(
                batchesByPaymentType.getOrDefault(PAYMENT_TYPE_VISA_MASTER, List.of()), storeNameByTradeCode)));

        rows.sort(Comparator.comparing(PaygateStationRow::getTradeCode));
        return rows;
    }

    /**
     * 決済会社ごとの集計を分離して実行する。手数料率マスタ・項目コードマスタに
     * 存在しないカードブランド名など、1決済会社分のデータ不備で例外が起きても、
     * その決済会社の行が0件になるだけで他の決済会社の表示は継続する
     * （実データ不備で画面全体がクラッシュしないようにするための防御）。
     * 振込金額の計算そのもの（{@link JftdTransferCalculationService}・
     * 統合振込CSV確定処理）はこの防御の対象外とし、従来どおり例外を伝播させる
     * （不明なブランドの金額を気づかずに計上してしまわないようにするため）。
     */
    private List<PaygateStationRow> collectSafely(
            String paymentCompany, Supplier<List<PaygateStationRow>> collector) {
        try {
            return collector.get();
        } catch (RuntimeException e) {
            LOGGER.error(
                    "PAYGATE Station精算情報照会: {}分の集計中にエラーが発生したため、"
                    + "{}分の表示をスキップします。", paymentCompany, paymentCompany, e);
            return List.of();
        }
    }

    private boolean isKnownPaymentType(String paymentType) {
        return PAYMENT_TYPE_JCB.equals(paymentType)
                || PAYMENT_TYPE_NETSTAR.equals(paymentType)
                || PAYMENT_TYPE_VISA_MASTER.equals(paymentType)
                || PAYMENT_TYPE_RAKUTENPAY.equals(paymentType)
                || PAYMENT_TYPE_SUMAREJO.equals(paymentType);
    }

    private Map<String, String> buildStoreNameMap() {
        Map<String, String> map = new LinkedHashMap<>();
        for (var mapping : paygateMappingRepository.findAllByOrderByTradeCodeAscTerminalIdAsc()) {
            map.putIfAbsent(mapping.getTradeCode(), mapping.getStoreName());
        }
        return map;
    }

    private Map<String, String> buildCardBrandMap() {
        return settlementItemCodeRepository.findAll().stream()
                .filter(c -> AMOUNT_TYPE_PAYMENT.equals(c.getAmountType()))
                .collect(Collectors.toMap(
                        SettlementItemCode::getItemCode, SettlementItemCode::getCardBrand, (a, b) -> a));
    }

    /**
     * 締め日でグループ化する。{@code Collectors.groupingBy}はグループ化キーがnullだと
     * 例外を投げる仕様のため使えない（締め日機能が実装される前にインポートされた
     * 既存バッチは{@code cutoff_date}がnullのままであり、これも1グループとして
     * 扱う必要がある）。
     */
    private Map<LocalDate, List<ImportBatch>> groupBatchesByCutoffDate(List<ImportBatch> batches) {
        Map<LocalDate, List<ImportBatch>> result = new LinkedHashMap<>();
        for (ImportBatch batch : batches) {
            result.computeIfAbsent(batch.getCutoffDate(), k -> new ArrayList<>()).add(batch);
        }
        return result;
    }

    private List<Integer> batchIdsOf(List<ImportBatch> batchGroup) {
        return batchGroup.stream().map(ImportBatch::getBatchId).collect(Collectors.toList());
    }

    /**
     * 確定済み（{@code transferBatchId}が設定済み）のバッチは、統合振込確定時点の
     * スナップショット（{@code m_jftd_transfer_detail}）をそのまま使う。確定後に
     * 手数料率マスタ（{@code m_settlement_fee_rate}）が変更されても、確定済み取引の
     * 決済手数料①・支払金額①が変動しないようにするため（帳票側の確定金額と一致させる）。
     * 未確定のバッチのみ、従来通りその場でライブ再計算する。
     */
    private List<TransferLineItem> resolveLineItems(
            List<ImportBatch> batchGroup, Function<List<Integer>, List<TransferLineItem>> liveCalculator) {
        List<Integer> confirmedBatchIds = batchGroup.stream()
                .filter(b -> b.getTransferBatchId() != null)
                .map(ImportBatch::getBatchId)
                .collect(Collectors.toList());
        List<Integer> unconfirmedBatchIds = batchGroup.stream()
                .filter(b -> b.getTransferBatchId() == null)
                .map(ImportBatch::getBatchId)
                .collect(Collectors.toList());

        List<TransferLineItem> lineItems = new ArrayList<>();
        if (!confirmedBatchIds.isEmpty()) {
            lineItems.addAll(jftdTransferDetailRepository.findByImportBatchIdIn(confirmedBatchIds).stream()
                    .map(this::toTransferLineItem)
                    .collect(Collectors.toList()));
        }
        if (!unconfirmedBatchIds.isEmpty()) {
            lineItems.addAll(liveCalculator.apply(unconfirmedBatchIds));
        }
        return lineItems;
    }

    private TransferLineItem toTransferLineItem(JftdTransferDetail detail) {
        return new TransferLineItem(
                detail.getTradeCode(), detail.getItemCode(), detail.getQuantity(), detail.getAmount(),
                detail.getGrossAmount(), detail.getAcquirerFeeTaxFree(), detail.getAcquirerFeeBase(),
                detail.getAcquirerFeeTax(), detail.getImportBatchId());
    }

    private List<PaygateStationRow> collectJcbRows(
            List<ImportBatch> batches, Map<String, String> storeNameByTradeCode,
            Map<String, String> cardBrandByItemCode) {
        List<PaygateStationRow> rows = new ArrayList<>();
        for (Map.Entry<LocalDate, List<ImportBatch>> group : groupBatchesByCutoffDate(batches).entrySet()) {
            LocalDate cutoffDate = group.getKey();
            List<ImportBatch> batchGroup = group.getValue();
            List<Integer> batchIds = batchIdsOf(batchGroup);

            List<JcbBrandAggregate> jcbAggregates =
                    jcbSalesDetailRepository.sumByTradeCodeAndCardName(batchIds);
            Map<String, Long> countByKey = jcbAggregates.stream()
                    .collect(Collectors.groupingBy(
                            a -> a.getTradeCode() + " " + a.getCardName(),
                            Collectors.summingLong(JcbBrandAggregate::getTotalSalesCount)));
            Map<String, Long> countByBatchKey = jcbAggregates.stream()
                    .collect(Collectors.toMap(
                            a -> a.getTradeCode() + " " + a.getCardName() + " " + a.getBatchId(),
                            JcbBrandAggregate::getTotalSalesCount, Long::sum));

            List<TransferLineItem> feeItems = resolveLineItems(
                    batchGroup, jftdTransferCalculationService::calculateJcbLineItemsForInquiry);
            rows.addAll(buildRows(
                    feeItems, COMPANY_JCB, cutoffDate, storeNameByTradeCode, cardBrandByItemCode,
                    countByKey, countByBatchKey));
        }
        return rows;
    }

    private List<PaygateStationRow> collectNetstarRows(
            List<ImportBatch> batches, Map<String, String> storeNameByTradeCode,
            Map<String, String> cardBrandByItemCode) {
        List<PaygateStationRow> rows = new ArrayList<>();
        for (Map.Entry<LocalDate, List<ImportBatch>> group : groupBatchesByCutoffDate(batches).entrySet()) {
            LocalDate cutoffDate = group.getKey();
            List<ImportBatch> batchGroup = group.getValue();
            List<Integer> batchIds = batchIdsOf(batchGroup);

            Map<String, Long> countByKey = new LinkedHashMap<>();
            Map<String, Long> countByBatchKey = new LinkedHashMap<>();
            for (NetstarSalesSummary row : netstarSalesSummaryRepository.findByBatchIdIn(batchIds)) {
                addNetstarBrandCount(countByKey, countByBatchKey, row, NETSTAR_BRAND_ALIPAY, row.getAlipaySalesCount());
                addNetstarBrandCount(countByKey, countByBatchKey, row, NETSTAR_BRAND_PAYPAY, row.getPaypaySalesCount());
                addNetstarBrandCount(countByKey, countByBatchKey, row, NETSTAR_BRAND_DPAY, row.getDpaySalesCount());
                addNetstarBrandCount(countByKey, countByBatchKey, row, NETSTAR_BRAND_WECHAT, row.getWechatSalesCount());
            }

            List<TransferLineItem> feeItems = resolveLineItems(
                    batchGroup, jftdTransferCalculationService::calculateNetstarLineItems);
            rows.addAll(buildRows(
                    feeItems, COMPANY_NETSTARS, cutoffDate, storeNameByTradeCode, cardBrandByItemCode,
                    countByKey, countByBatchKey));
        }
        return rows;
    }

    private void addNetstarBrandCount(
            Map<String, Long> countByKey, Map<String, Long> countByBatchKey,
            NetstarSalesSummary row, String brand, int count) {
        if (count == 0) {
            return;
        }
        String key = row.getTradeCode() + " " + brand;
        countByKey.merge(key, (long) count, Long::sum);
        countByBatchKey.merge(key + " " + row.getBatchId(), (long) count, Long::sum);
    }

    private List<PaygateStationRow> collectSumarejoRows(
            List<ImportBatch> batches, Map<String, String> storeNameByTradeCode) {
        List<PaygateStationRow> rows = new ArrayList<>();
        for (Map.Entry<LocalDate, List<ImportBatch>> group : groupBatchesByCutoffDate(batches).entrySet()) {
            LocalDate cutoffDate = group.getKey();
            List<ImportBatch> batchGroup = group.getValue();
            List<Integer> batchIds = batchIdsOf(batchGroup);

            List<TerminalFeeAggregate> terminalAggregates =
                    terminalMonthlyFeeRepository.sumByTradeCodeAndUnitPrice(batchIds);
            Map<String, Long> countByTradeCode = terminalAggregates.stream()
                    .collect(Collectors.groupingBy(
                            TerminalFeeAggregate::getTradeCode,
                            Collectors.summingLong(TerminalFeeAggregate::getTerminalCount)));
            Map<String, Long> countByBatchTradeCode = terminalAggregates.stream()
                    .collect(Collectors.toMap(
                            a -> a.getTradeCode() + " " + a.getBatchId(),
                            TerminalFeeAggregate::getTerminalCount, Long::sum));

            List<TransferLineItem> feeItems = resolveLineItems(
                    batchGroup, jftdTransferCalculationService::calculateSumarejoLineItems);
            rows.addAll(buildRowsWithoutBrand(
                    feeItems, COMPANY_SUMAREJO, cutoffDate, storeNameByTradeCode,
                    countByTradeCode, countByBatchTradeCode));
        }
        return rows;
    }

    private List<PaygateStationRow> collectRakutenPayRows(
            List<ImportBatch> batches, Map<String, String> storeNameByTradeCode) {
        List<PaygateStationRow> rows = new ArrayList<>();
        for (Map.Entry<LocalDate, List<ImportBatch>> group : groupBatchesByCutoffDate(batches).entrySet()) {
            LocalDate cutoffDate = group.getKey();
            List<ImportBatch> batchGroup = group.getValue();
            List<Integer> batchIds = batchIdsOf(batchGroup);

            List<RakutenPayAggregate> rakutenPayAggregates = rakutenPayTransactionRepository.sumByTradeCode(batchIds);
            Map<String, Long> countByTradeCode = rakutenPayAggregates.stream()
                    .collect(Collectors.groupingBy(
                            RakutenPayAggregate::getTradeCode,
                            Collectors.summingLong(RakutenPayAggregate::getTransactionCount)));
            Map<String, Long> countByBatchTradeCode = rakutenPayAggregates.stream()
                    .collect(Collectors.toMap(
                            a -> a.getTradeCode() + " " + a.getBatchId(),
                            RakutenPayAggregate::getTransactionCount, Long::sum));

            List<TransferLineItem> feeItems = resolveLineItems(
                    batchGroup, jftdTransferCalculationService::calculateRakutenPayLineItems);
            rows.addAll(buildRowsWithoutBrand(
                    feeItems, COMPANY_RAKUTENPAY, cutoffDate, storeNameByTradeCode,
                    countByTradeCode, countByBatchTradeCode));
        }
        return rows;
    }

    private List<PaygateStationRow> collectVisaMasterRows(
            List<ImportBatch> batches, Map<String, String> storeNameByTradeCode) {
        List<PaygateStationRow> rows = new ArrayList<>();
        for (Map.Entry<LocalDate, List<ImportBatch>> group : groupBatchesByCutoffDate(batches).entrySet()) {
            LocalDate cutoffDate = group.getKey();
            List<ImportBatch> batchGroup = group.getValue();
            List<Integer> batchIds = batchIdsOf(batchGroup);

            List<VisaMasterAggregate> visaMasterAggregates = visaMasterTransactionRepository.sumByTradeCode(batchIds);
            Map<String, Long> countByTradeCode = visaMasterAggregates.stream()
                    .collect(Collectors.groupingBy(
                            VisaMasterAggregate::getTradeCode,
                            Collectors.summingLong(VisaMasterAggregate::getTransactionCount)));
            Map<String, Long> countByBatchTradeCode = visaMasterAggregates.stream()
                    .collect(Collectors.toMap(
                            a -> a.getTradeCode() + " " + a.getBatchId(),
                            VisaMasterAggregate::getTransactionCount, Long::sum));

            List<TransferLineItem> feeItems = resolveLineItems(
                    batchGroup, jftdTransferCalculationService::calculateVisaMasterLineItems);
            rows.addAll(buildRowsWithoutBrand(
                    feeItems, COMPANY_VISA_MASTER, cutoffDate, storeNameByTradeCode,
                    countByTradeCode, countByBatchTradeCode));
        }
        return rows;
    }

    /**
     * 決済種類（card_brand）の概念を持つ決済会社（JCB・ネットスターズ）用。
     * itemCodeからcard_brandを逆引きし、取引コード×card_brand単位で合算する。
     * 明細行クリック時の内訳表示用に、元ファイル（インポートバッチ）単位の内訳
     * （{@link PaygateStationRow#getDetails()}）も同時に組み立てる。集計行自体の
     * 件数・金額は内訳の合算ではなく従来通り{@code countByKey}・{@code feeItems}から
     * 直接算出するため、内訳側の仕様変更が集計行の値に影響することはない。
     */
    private List<PaygateStationRow> buildRows(
            List<TransferLineItem> feeItems, String paymentCompany, LocalDate cutoffDate,
            Map<String, String> storeNameByTradeCode, Map<String, String> cardBrandByItemCode,
            Map<String, Long> countByKey, Map<String, Long> countByBatchKey) {
        Map<String, int[]> totalsByKey = new LinkedHashMap<>();
        Map<String, String> tradeCodeByKey = new LinkedHashMap<>();
        Map<String, String> cardBrandByKey = new LinkedHashMap<>();
        Map<String, List<PaygateStationRow>> detailsByKey = new LinkedHashMap<>();
        for (TransferLineItem item : feeItems) {
            // 手数料率・項目コードマスタ未整備のブランドは項目コードが存在しないため、
            // JftdTransferCalculationService#calculateJcbLineItemsForInquiry()側で
            // itemCodeの代わりにカードブランド名そのものを入れている。該当する
            // 項目コードが見つからない場合はその値をそのままブランド名として使う。
            String cardBrand = cardBrandByItemCode.getOrDefault(item.getItemCode(), item.getItemCode());
            String key = item.getTradeCode() + " " + cardBrand;
            int fee = item.getAcquirerFeeTaxFree() + item.getAcquirerFeeBase() + item.getAcquirerFeeTax();
            int[] totals = totalsByKey.computeIfAbsent(key, k -> new int[3]);
            totals[0] += item.getGrossAmount();
            totals[1] += fee;
            totals[2] += item.getAmount();
            tradeCodeByKey.put(key, item.getTradeCode());
            cardBrandByKey.put(key, cardBrand);

            long itemCount = countByBatchKey.getOrDefault(key + " " + item.getImportBatchId(), 0L);
            detailsByKey.computeIfAbsent(key, k -> new ArrayList<>()).add(new PaygateStationRow(
                    item.getTradeCode(), storeNameByTradeCode.get(item.getTradeCode()), paymentCompany, cardBrand,
                    cutoffDate, (int) itemCount, item.getGrossAmount(), fee, item.getAmount()));
        }

        List<PaygateStationRow> rows = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : totalsByKey.entrySet()) {
            String key = entry.getKey();
            String tradeCode = tradeCodeByKey.get(key);
            int[] totals = entry.getValue();
            long count = countByKey.getOrDefault(key, 0L);
            rows.add(new PaygateStationRow(
                    tradeCode, storeNameByTradeCode.get(tradeCode), paymentCompany, cardBrandByKey.get(key),
                    cutoffDate, (int) count, totals[0], totals[1], totals[2], detailsByKey.get(key)));
        }
        return rows;
    }

    /**
     * 決済種類の概念を持たない決済会社（スマレジ・楽天ペイ・住信SBI）用。
     * 取引コード単位で合算する（card_brandは常にnull）。buildRows()と同様、
     * 集計行の件数・金額は内訳の合算に依存せず従来通りの計算方法を維持する。
     */
    private List<PaygateStationRow> buildRowsWithoutBrand(
            List<TransferLineItem> feeItems, String paymentCompany, LocalDate cutoffDate,
            Map<String, String> storeNameByTradeCode, Map<String, Long> countByTradeCode,
            Map<String, Long> countByBatchTradeCode) {
        Map<String, int[]> totalsByTradeCode = new LinkedHashMap<>();
        Map<String, List<PaygateStationRow>> detailsByTradeCode = new LinkedHashMap<>();
        for (TransferLineItem item : feeItems) {
            String tradeCode = item.getTradeCode();
            int fee = item.getAcquirerFeeTaxFree() + item.getAcquirerFeeBase() + item.getAcquirerFeeTax();
            int[] totals = totalsByTradeCode.computeIfAbsent(tradeCode, k -> new int[3]);
            totals[0] += item.getGrossAmount();
            totals[1] += fee;
            totals[2] += item.getAmount();

            long itemCount = countByBatchTradeCode.getOrDefault(tradeCode + " " + item.getImportBatchId(), 0L);
            detailsByTradeCode.computeIfAbsent(tradeCode, k -> new ArrayList<>()).add(new PaygateStationRow(
                    tradeCode, storeNameByTradeCode.get(tradeCode), paymentCompany, null,
                    cutoffDate, (int) itemCount, item.getGrossAmount(), fee, item.getAmount()));
        }

        List<PaygateStationRow> rows = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : totalsByTradeCode.entrySet()) {
            String tradeCode = entry.getKey();
            int[] totals = entry.getValue();
            long count = countByTradeCode.getOrDefault(tradeCode, 0L);
            rows.add(new PaygateStationRow(
                    tradeCode, storeNameByTradeCode.get(tradeCode), paymentCompany, null,
                    cutoffDate, (int) count, totals[0], totals[1], totals[2],
                    detailsByTradeCode.get(tradeCode)));
        }
        return rows;
    }

}
