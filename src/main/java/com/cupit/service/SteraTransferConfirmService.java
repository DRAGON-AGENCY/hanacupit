package com.cupit.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cupit.model.ImportBatch;
import com.cupit.model.SteraTransferBatch;
import com.cupit.model.SteraTransferDetail;
import com.cupit.repository.ImportBatchRepository;
import com.cupit.repository.SteraTransferBatchRepository;
import com.cupit.repository.SteraTransferDetailRepository;
import com.cupit.service.settlement.SteraTransferLineItem;

/**
 * その他統合振込CSV作成（stera terminal）の「確定」操作。{@link JftdTransferConfirmService}
 * と同じ構造で、対象3決済種別の未処理インポートバッチをファイル単位でロック・確定する。
 * stera terminal側は振込先口座（m_stera_store）との突合をインポート時点で完了済みの
 * 前提のため、JFTD側と異なりこの時点で追加のマスタ突合は行わない
 * （明細テーブル側への行単位の処理済みマーカーが不要な理由。CLAUDE.md
 * 「口座マスタ（m_stera_store）の解決規則」を参照）。
 */
@Service
public class SteraTransferConfirmService {

    private static final List<String> TARGET_PAYMENT_TYPES =
            List.of("stera JCB", "stera code", "steraクレジット");

    private final SteraTransferCalculationService calculationService;
    private final SteraTransferBatchRepository transferBatchRepository;
    private final SteraTransferDetailRepository transferDetailRepository;
    private final ImportBatchRepository importBatchRepository;

    public SteraTransferConfirmService(
            SteraTransferCalculationService calculationService,
            SteraTransferBatchRepository transferBatchRepository,
            SteraTransferDetailRepository transferDetailRepository,
            ImportBatchRepository importBatchRepository) {
        this.calculationService = calculationService;
        this.transferBatchRepository = transferBatchRepository;
        this.transferDetailRepository = transferDetailRepository;
        this.importBatchRepository = importBatchRepository;
    }

    /**
     * クライアントの計算結果は信用せず、確定時点でサーバー側が再計算した値を保存する。
     *
     * まず対象3決済種別分の未処理インポートバッチを{@code SELECT ... FOR UPDATE}で
     * 排他ロックし、ロックできたバッチだけを集計・マークの対象とする。これにより、
     * 確定操作が同時に2回実行されても、後発のトランザクションは先発のロック解放
     * （コミット）を待ってから未処理データを再評価するため、同じデータが2つの確定バッチに
     * 二重計上されることを防ぐ。
     *
     * @return 確定したその他統合振込バッチのID
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

        List<SteraTransferLineItem> lineItems =
                calculationService.calculateAllLineItems(lockedBatchIdsByPaymentType);

        SteraTransferBatch batch = new SteraTransferBatch();
        batch.setCreatedAt(LocalDateTime.now());
        batch.setUpdateEmployee(updateEmployee);
        batch.setCreateDate(today);
        int transferBatchId = transferBatchRepository.save(batch).getTransferBatchId();

        for (SteraTransferLineItem item : lineItems) {
            SteraTransferDetail detail = new SteraTransferDetail();
            detail.setTransferBatchId(transferBatchId);
            detail.setTradeCode(item.getTradeCode());
            detail.setGrossAmount(item.getGrossAmount());
            detail.setAcquirerFee(item.getAcquirerFee());
            detail.setCompanyFee(item.getCompanyFee());
            detail.setTransferFee(item.getTransferFee());
            detail.setNetAmount(item.getNetAmount());
            detail.setBankCode(item.getBankCode());
            detail.setBankName(item.getBankName());
            detail.setBankBranchCode(item.getBankBranchCode());
            detail.setBranchName(item.getBranchName());
            detail.setAccountType(item.getAccountType());
            detail.setAccountNo(item.getAccountNo());
            detail.setAccountHolderKana(item.getAccountHolderKana());
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
