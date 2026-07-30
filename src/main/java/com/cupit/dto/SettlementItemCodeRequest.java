package com.cupit.dto;

/**
 * 項目コードマスタ管理画面から送信された項目コード情報を保持するクラス。
 * 登録 (mode=new) と更新の両方で使用する。
 */
public class SettlementItemCodeRequest {

    private String mode;
    private int itemCodeId;
    private String paymentCompany;
    private String cardBrand;
    private String amountType;
    private String itemCode;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getItemCodeId() {
        return itemCodeId;
    }

    public void setItemCodeId(int itemCodeId) {
        this.itemCodeId = itemCodeId;
    }

    public String getPaymentCompany() {
        return paymentCompany;
    }

    public void setPaymentCompany(String paymentCompany) {
        this.paymentCompany = paymentCompany;
    }

    public String getCardBrand() {
        return cardBrand;
    }

    public void setCardBrand(String cardBrand) {
        this.cardBrand = cardBrand;
    }

    public String getAmountType() {
        return amountType;
    }

    public void setAmountType(String amountType) {
        this.amountType = amountType;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }
}
