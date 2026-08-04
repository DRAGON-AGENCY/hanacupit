package com.cupit.dto;

/**
 * 振込手数料管理画面から送信された振込手数料情報を保持するクラス。
 * 登録 (mode=new) と更新の両方で使用する。
 */
public class TransferFeeRateRequest {

    private String mode;
    private int transferFeeId;
    private String bankCode;
    private String transferFee;
    private String note;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getTransferFeeId() {
        return transferFeeId;
    }

    public void setTransferFeeId(int transferFeeId) {
        this.transferFeeId = transferFeeId;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getTransferFee() {
        return transferFee;
    }

    public void setTransferFee(String transferFee) {
        this.transferFee = transferFee;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
