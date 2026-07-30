package com.cupit.dto;

/**
 * 手数料率マスタ管理画面から送信された手数料率情報を保持するクラス。
 * 登録 (mode=new) と更新の両方で使用する。
 */
public class SettlementFeeRateRequest {

    private String mode;
    private int feeRateId;
    private String paymentCompany;
    private String cardBrand;
    private String calcModel;
    private String acquirerFeeRate;
    private String ourFeeRateBase;
    private String ourFeeRateTax;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getFeeRateId() {
        return feeRateId;
    }

    public void setFeeRateId(int feeRateId) {
        this.feeRateId = feeRateId;
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

    public String getCalcModel() {
        return calcModel;
    }

    public void setCalcModel(String calcModel) {
        this.calcModel = calcModel;
    }

    public String getAcquirerFeeRate() {
        return acquirerFeeRate;
    }

    public void setAcquirerFeeRate(String acquirerFeeRate) {
        this.acquirerFeeRate = acquirerFeeRate;
    }

    public String getOurFeeRateBase() {
        return ourFeeRateBase;
    }

    public void setOurFeeRateBase(String ourFeeRateBase) {
        this.ourFeeRateBase = ourFeeRateBase;
    }

    public String getOurFeeRateTax() {
        return ourFeeRateTax;
    }

    public void setOurFeeRateTax(String ourFeeRateTax) {
        this.ourFeeRateTax = ourFeeRateTax;
    }
}
