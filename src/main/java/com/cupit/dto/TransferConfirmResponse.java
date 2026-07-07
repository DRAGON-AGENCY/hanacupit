package com.cupit.dto;

/**
 * 統合振込CSV確定処理の結果を返す DTO。
 */
public class TransferConfirmResponse {

    private final boolean success;
    private final Integer transferBatchId;
    private final String errorMessage;

    public TransferConfirmResponse(boolean success, Integer transferBatchId, String errorMessage) {
        this.success = success;
        this.transferBatchId = transferBatchId;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public Integer getTransferBatchId() {
        return transferBatchId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
