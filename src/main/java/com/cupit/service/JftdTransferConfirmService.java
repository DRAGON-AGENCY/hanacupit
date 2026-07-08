package com.cupit.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cupit.model.ImportBatch;
import com.cupit.model.JftdTransferBatch;
import com.cupit.model.JftdTransferDetail;
import com.cupit.repository.ImportBatchRepository;
import com.cupit.repository.JftdTransferBatchRepository;
import com.cupit.repository.JftdTransferDetailRepository;
import com.cupit.service.settlement.TransferLineItem;

/**
 * 統合振込CSV作成の「確定」操作。集計結果をスナップショットとして保存し、
 * 対象のインポートバッチを処理済みとしてマークする（今後の集計対象から除外する）。
 */
@Service
public class JftdTransferConfirmService {

    private static final List<String> TARGET_PAYMENT_TYPES =
            List.of("JCB", "スマレジ", "ネットスターズ", "楽天ペイ", "住信SBI");

    private final JftdTransferCalculationService calculationService;
    private final JftdTransferBatchRepository transferBatchRepository;
    private final JftdTransferDetailRepository transferDetailRepository;
    private final ImportBatchRepository importBatchRepository;

    public JftdTransferConfirmService(
            JftdTransferCalculationService calculationService,
            JftdTransferBatchRepository transferBatchRepository,
            JftdTransferDetailRepository transferDetailRepository,
            ImportBatchRepository importBatchRepository) {
        this.calculationService = calculationService;
        this.transferBatchRepository = transferBatchRepository;
        this.transferDetailRepository = transferDetailRepository;
        this.importBatchRepository = importBatchRepository;
    }

    /**
     * クライアントの計算結果は信用せず、確定時点でサーバー側が再計算した値を保存する。
     *
     * まず対象5社分の未処理インポートバッチを{@code SELECT ... FOR UPDATE}で排他ロックし、
     * ロックできたバッチだけを集計・マークの対象とする。これにより、確定操作が
     * 同時に2回実行されても（ボタン連打・二重クリック等）、後発のトランザクションは
     * 先発のロック解放（コミット）を待ってから未処理データを再評価するため、
     * 同じデータが2つの確定バッチに二重計上されることを防ぐ。
     *
     * @return 確定した統合振込バッチのID
     */
    @Transactional
    public int confirm(String updateEmployee) {
        LocalDate today = LocalDate.now();

        Map<String, List<ImportBatch>> lockedBatchesByPaymentType = new LinkedHashMap<>();
        Map<String, List<Integer>> lockedBatchIdsByPaymentType = new LinkedHashMap<>();
        for (String paymentType : TARGET_PAYMENT_TYPES) {
            List<ImportBatch> locked = importBatchRepository.lockUnprocessedByPaymentType(paymentType);
            lockedBatchesByPaymentType.put(paymentType, locked);
            lockedBatchIdsByPaymentType.put(paymentType, locked.stream().map(ImportBatch::getBatchId).toList());
        }

        List<TransferLineItem> lineItems =
                calculationService.calculateAllLineItems(lockedBatchIdsByPaymentType);

        JftdTransferBatch batch = new JftdTransferBatch();
        batch.setCreatedAt(LocalDateTime.now());
        batch.setUpdateEmployee(updateEmployee);
        batch.setCreateDate(today);
        int transferBatchId = transferBatchRepository.save(batch).getTransferBatchId();

        for (TransferLineItem item : lineItems) {
            JftdTransferDetail detail = new JftdTransferDetail();
            detail.setTransferBatchId(transferBatchId);
            detail.setTradeCode(item.getTradeCode());
            detail.setItemCode(item.getItemCode());
            detail.setQuantity(item.getQuantity());
            detail.setAmount(item.getAmount());
            detail.setGrossAmount(item.getGrossAmount());
            detail.setAcquirerFeeTaxFree(item.getAcquirerFeeTaxFree());
            detail.setAcquirerFeeBase(item.getAcquirerFeeBase());
            detail.setAcquirerFeeTax(item.getAcquirerFeeTax());
            detail.setUpdateEmployee(updateEmployee);
            detail.setCreateDate(today);
            transferDetailRepository.save(detail);
        }

        for (List<ImportBatch> targetBatches : lockedBatchesByPaymentType.values()) {
            for (ImportBatch importBatch : targetBatches) {
                importBatch.setTransferBatchId(transferBatchId);
                importBatch.setUpdateEmployee(updateEmployee);
                importBatch.setUpdatedDate(today);
                importBatchRepository.save(importBatch);
            }
        }

        return transferBatchId;
    }

}
