package com.cupit.dto;

import java.time.LocalDate;

/**
 * stera terminal精算情報照会(JCB)画面の明細1行分のDTO。
 * 取引コード×支店（store_number）×カード名×お支払方法×支払区分×締め日の単位で1行になる。
 */
public class SteraJcbRow {

    private final String tradeCode;

    private final String storeNumber;

    private final String storeName;

    private final String cardName;

    private final String paymentMethod;

    private final String paymentType;

    private final LocalDate cutoffDate;

    private final int salesAmount;

    private final int acquirerFee;

    private final int companyFee;

    private final int settlementAmount;

    public SteraJcbRow(
            String tradeCode, String storeNumber, String storeName, String cardName, String paymentMethod,
            String paymentType, LocalDate cutoffDate, int salesAmount, int acquirerFee, int companyFee,
            int settlementAmount) {
        this.tradeCode = tradeCode;
        this.storeNumber = storeNumber;
        this.storeName = storeName;
        this.cardName = cardName;
        this.paymentMethod = paymentMethod;
        this.paymentType = paymentType;
        this.cutoffDate = cutoffDate;
        this.salesAmount = salesAmount;
        this.acquirerFee = acquirerFee;
        this.companyFee = companyFee;
        this.settlementAmount = settlementAmount;
    }

    public String getTradeCode() {
        return tradeCode;
    }

    public String getStoreNumber() {
        return storeNumber;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getCardName() {
        return cardName;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public LocalDate getCutoffDate() {
        return cutoffDate;
    }

    public int getSalesAmount() {
        return salesAmount;
    }

    public int getAcquirerFee() {
        return acquirerFee;
    }

    public int getCompanyFee() {
        return companyFee;
    }

    public int getSettlementAmount() {
        return settlementAmount;
    }

}
