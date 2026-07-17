package com.cupit.dto;

import java.time.LocalDate;

/**
 * 統合振込CSV作成のプレビュー画面で表示する、確定対象ファイル1件分のDTO。
 * m_import_batchの1レコード（＝取込ファイル1件）に対応する。締め日は
 * その他精算データ作成（stera terminal）側のみ設定され、JFTD側は常にnullになる。
 */
public class TransferTargetFile {

    private final int batchId;

    private final String paymentType;

    private final String fileName;

    private final LocalDate cutoffDate;

    private final int recordCount;

    public TransferTargetFile(
            int batchId, String paymentType, String fileName, LocalDate cutoffDate, int recordCount) {
        this.batchId = batchId;
        this.paymentType = paymentType;
        this.fileName = fileName;
        this.cutoffDate = cutoffDate;
        this.recordCount = recordCount;
    }

    public int getBatchId() {
        return batchId;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public String getFileName() {
        return fileName;
    }

    public LocalDate getCutoffDate() {
        return cutoffDate;
    }

    public int getRecordCount() {
        return recordCount;
    }

}
