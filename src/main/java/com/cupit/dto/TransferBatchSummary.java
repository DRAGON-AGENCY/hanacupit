package com.cupit.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帳票出力画面の履歴一覧に表示する、確定済み統合振込バッチ1件分のサマリ。
 * 明細（{@link #getImportBatches()}）は、この確定に含まれるm_import_batchの
 * レコードをそのまま1行ずつ並べたもの（1確定に複数の決済種別・複数ファイルが
 * 含まれる場合は、その件数分の明細行になる）。
 */
public class TransferBatchSummary {

    private final int transferBatchId;
    private final LocalDateTime createdAt;
    private final String updateEmployee;
    private final List<ImportBatchDetail> importBatches;

    public TransferBatchSummary(
            int transferBatchId, LocalDateTime createdAt, String updateEmployee,
            List<ImportBatchDetail> importBatches) {
        this.transferBatchId = transferBatchId;
        this.createdAt = createdAt;
        this.updateEmployee = updateEmployee;
        this.importBatches = importBatches;
    }

    public int getTransferBatchId() {
        return transferBatchId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getUpdateEmployee() {
        return updateEmployee;
    }

    /**
     * この確定バッチに含まれるインポートバッチ（m_import_batchのレコード）の明細一覧。
     * 1確定＝1つ以上のm_import_batchレコードなので、通常1件以上を持つ。
     */
    public List<ImportBatchDetail> getImportBatches() {
        return importBatches;
    }

}
