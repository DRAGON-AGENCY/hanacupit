package com.cupit.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cupit.dto.ImportBatchDetail;
import com.cupit.dto.TransferBatchSummary;
import com.cupit.model.ImportBatch;
import com.cupit.model.JftdTransferBatch;
import com.cupit.model.JftdTransferDetail;
import com.cupit.model.SettlementItemCode;
import com.cupit.repository.ImportBatchRepository;
import com.cupit.repository.JftdTransferBatchRepository;
import com.cupit.repository.JftdTransferDetailRepository;
import com.cupit.repository.SettlementItemCodeRepository;
import com.cupit.service.settlement.ReportRow;
import com.cupit.service.settlement.TransferLineItem;

/**
 * 帳票（売上報告書・支払明細書）用の集計サービス。確定済みの統合振込明細
 * （m_jftd_transfer_detail）を決済会社×カードブランド単位（全店舗合算）で
 * 再集計する。統合振込CSVは取引コード単位だが、帳票はこれより粗い粒度になる点に
 * 注意（調査メモ「帳票構造確認」シート参照）。
 * 手数料②が未実装のため、FEE_BASE・FEE_TAX側は常に0円になる。
 */
@Service
public class JftdReportDataService {

    private final JftdTransferBatchRepository transferBatchRepository;
    private final JftdTransferDetailRepository transferDetailRepository;
    private final SettlementItemCodeRepository settlementItemCodeRepository;
    private final ImportBatchRepository importBatchRepository;

    public JftdReportDataService(
            JftdTransferBatchRepository transferBatchRepository,
            JftdTransferDetailRepository transferDetailRepository,
            SettlementItemCodeRepository settlementItemCodeRepository,
            ImportBatchRepository importBatchRepository) {
        this.transferBatchRepository = transferBatchRepository;
        this.transferDetailRepository = transferDetailRepository;
        this.settlementItemCodeRepository = settlementItemCodeRepository;
        this.importBatchRepository = importBatchRepository;
    }

    /**
     * 帳票出力画面の履歴一覧用に、確定済みの統合振込バッチを確定日時の新しい順に返す。
     * 帳票出力画面は「確定」操作を持たず、ここで一覧化された確定済みバッチから選んで
     * ダウンロードするだけの参照専用画面となる。各バッチの明細は、そのバッチに含まれる
     * m_import_batchのレコードをそのまま1行ずつ並べたもの（決済種別・ファイル単位で
     * 複数行になり得る）。バッチ件数が増えてもクエリ回数が増えないよう、バッチ1件ごとに
     * 問い合わせるN+1パターンは避け、バッチ一覧とインポートバッチの2クエリでまとめて
     * 取得する。
     */
    public List<TransferBatchSummary> listConfirmedBatches() {
        List<JftdTransferBatch> batches = transferBatchRepository.findAllByOrderByCreatedAtDesc();
        if (batches.isEmpty()) {
            return List.of();
        }
        List<Integer> transferBatchIds = batches.stream()
                .map(JftdTransferBatch::getTransferBatchId)
                .toList();

        Map<Integer, List<ImportBatch>> importBatchesByTransferBatchId =
                importBatchRepository.findByTransferBatchIdIn(transferBatchIds).stream()
                        .collect(Collectors.groupingBy(ImportBatch::getTransferBatchId));

        return batches.stream()
                .map(batch -> toSummary(
                        batch, importBatchesByTransferBatchId.getOrDefault(batch.getTransferBatchId(), List.of())))
                .toList();
    }

    private TransferBatchSummary toSummary(JftdTransferBatch batch, List<ImportBatch> importBatches) {
        List<ImportBatchDetail> details = importBatches.stream()
                .map(ib -> new ImportBatchDetail(
                        ib.getBatchId(),
                        ib.getPaymentType(),
                        ib.getFileName(),
                        ib.getImportedAt(),
                        ib.getRecordCount() != null ? ib.getRecordCount() : 0,
                        ib.getErrorCount() != null ? ib.getErrorCount() : 0))
                .toList();
        return new TransferBatchSummary(
                batch.getTransferBatchId(), batch.getCreatedAt(), batch.getUpdateEmployee(), details);
    }

    /**
     * 確定済みの統合振込明細（m_jftd_transfer_detail）から帳票データを集計する。
     * 帳票出力画面ではファイル（m_import_batch.batch_id）単位でチェックボックスを選択する
     * ため、ここで受け取るのは確定バッチIDではなく元ファイルのbatch_idの一覧
     * （{@code JftdTransferDetail.importBatchId}）である点に注意。複数ファイルを
     * まとめて選択した場合はそれらを1つの帳票として集計する。
     */
    public List<ReportRow> getReportRows(List<Integer> importBatchIds) {
        List<JftdTransferDetail> details = transferDetailRepository.findByImportBatchIdIn(importBatchIds);
        List<TransferLineItem> lineItems = details.stream()
                .map(d -> new TransferLineItem(
                        d.getTradeCode(), d.getItemCode(), d.getQuantity(), d.getAmount(),
                        d.getGrossAmount(), d.getAcquirerFeeTaxFree(),
                        d.getAcquirerFeeBase(), d.getAcquirerFeeTax(), d.getImportBatchId()))
                .toList();
        return summarize(lineItems);
    }

    /**
     * 確定前のプレビュー表示用に、集計サービスが計算した明細（未保存）から
     * 直接帳票相当のデータを集計する。項目コードマスタは明細行ごとに問い合わせず、
     * 事前に全件取得してMapを作ることでN+1クエリを避ける
     * （マスタ自体は少数固定のため、全件取得しても1クエリで済む）。
     */
    public List<ReportRow> summarize(List<TransferLineItem> lineItems) {
        Map<String, SettlementItemCode> itemCodeByCode = settlementItemCodeRepository.findAll().stream()
                .collect(Collectors.toMap(SettlementItemCode::getItemCode, Function.identity()));

        Map<String, String[]> companyBrandByKey = new LinkedHashMap<>();
        Map<String, int[]> totalsByKey = new LinkedHashMap<>();

        for (TransferLineItem detail : lineItems) {
            SettlementItemCode itemCode = itemCodeByCode.get(detail.getItemCode());
            if (itemCode == null) {
                throw new IllegalStateException(
                        "項目コードマスタに項目コード「" + detail.getItemCode() + "」の設定がありません。");
            }

            String key = itemCode.getPaymentCompany() + " " + itemCode.getCardBrand();
            companyBrandByKey.putIfAbsent(
                    key, new String[] {itemCode.getPaymentCompany(), itemCode.getCardBrand()});
            // [0]件数, [1]決済金額合計, [2]事業者手数料(非課税), [3]事業者手数料(課税本体),
            // [4]事業者手数料(消費税), [5]支払金額1(PAYMENT amount計), [6]弊社手数料本体, [7]弊社手数料消費税
            int[] totals = totalsByKey.computeIfAbsent(key, unused -> new int[8]);

            totals[0] += detail.getQuantity();
            switch (itemCode.getAmountType()) {
                case "PAYMENT" -> {
                    totals[1] += detail.getGrossAmount();
                    totals[2] += detail.getAcquirerFeeTaxFree();
                    totals[3] += detail.getAcquirerFeeBase();
                    totals[4] += detail.getAcquirerFeeTax();
                    totals[5] += detail.getAmount();
                }
                case "FEE_BASE" -> totals[6] += detail.getAmount();
                case "FEE_TAX" -> totals[7] += detail.getAmount();
                default -> throw new IllegalStateException(
                        "不明な金額種別です: " + itemCode.getAmountType());
            }
        }

        List<ReportRow> rows = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : totalsByKey.entrySet()) {
            String[] companyBrand = companyBrandByKey.get(entry.getKey());
            int[] totals = entry.getValue();
            rows.add(new ReportRow(
                    companyBrand[0], companyBrand[1], totals[0],
                    totals[1], totals[2], totals[3], totals[4],
                    totals[5], totals[6], totals[7]));
        }
        return rows;
    }

}
