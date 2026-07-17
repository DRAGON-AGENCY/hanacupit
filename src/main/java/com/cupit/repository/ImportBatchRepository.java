package com.cupit.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.cupit.model.ImportBatch;

public interface ImportBatchRepository extends JpaRepository<ImportBatch, Integer> {

    List<ImportBatch> findByPaymentTypeAndTransferBatchIdIsNull(String paymentType);

    List<ImportBatch> findByTransferBatchIdIn(List<Integer> transferBatchIds);

    /**
     * 精算情報照会（PAYGATE Station等）用。確定済み・未確定を問わず、指定した
     * 決済種別・締め日のインポートバッチをすべて返す（CSV作成の「未処理分のみ」制約とは異なる）。
     */
    List<ImportBatch> findByPaymentTypeAndCutoffDate(String paymentType, LocalDate cutoffDate);

    /**
     * 指定した決済種別の未処理バッチ（transfer_batch_id IS NULL）を排他ロックして返す。
     * JFTD統合振込CSV作成の確定処理（{@code JftdTransferConfirmService.confirm()}）専用。
     * 同時に2つの確定処理が実行されても、片方がロック解放を待ってから再評価するため、
     * 同じ未処理データが2つの確定バッチに二重計上されることを防ぐ。
     * プレビュー表示など参照専用の用途では {@link #findByPaymentTypeAndTransferBatchIdIsNull}
     * を使うこと（ロックを取ると他の確定処理を不必要にブロックしてしまうため）。
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM ImportBatch b WHERE b.paymentType = :paymentType AND b.transferBatchId IS NULL")
    List<ImportBatch> lockUnprocessedByPaymentType(@Param("paymentType") String paymentType);

}
