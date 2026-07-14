package com.cupit.dto;

import java.time.LocalDateTime;

/**
 * 帳票出力画面の履歴一覧における、確定済みバッチ配下の明細1行分。
 * m_import_batchの1レコード（＝取込ファイル1件）をそのまま表示する。
 */
public class ImportBatchDetail {

    private final int batchId;
    private final String paymentType;
    private final String fileName;
    private final LocalDateTime importedAt;
    private final int recordCount;
    private final int errorCount;

    public ImportBatchDetail(
            int batchId, String paymentType, String fileName, LocalDateTime importedAt,
            int recordCount, int errorCount) {
        this.batchId = batchId;
        this.paymentType = paymentType;
        this.fileName = fileName;
        this.importedAt = importedAt;
        this.recordCount = recordCount;
        this.errorCount = errorCount;
    }

    /**
     * m_import_batch.batch_id。帳票出力画面でファイル単位に選択するためのチェックボックス
     * の値として使用する。
     */
    public int getBatchId() {
        return batchId;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public String getFileName() {
        return fileName;
    }

    public LocalDateTime getImportedAt() {
        return importedAt;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

}
