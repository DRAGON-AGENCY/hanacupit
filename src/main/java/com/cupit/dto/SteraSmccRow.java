package com.cupit.dto;

import java.time.LocalDate;

/**
 * stera terminal精算情報照会(SMCC)画面の明細1行分のDTO。steraクレジット
 * （m_stera_credit_sales_detail）とstera code（m_stera_code_settlement_detail）の
 * 2フォーマットを1つの一覧にまとめる。取引コード×識別番号（merchant_id／terminal_id）×
 * カード名×取扱区分×締め日の単位で1行になる。
 * 取扱区分（transactionType）はsteraクレジットのみ持つ概念で、stera codeでは常にnull
 * （QR決済のため支払回数の概念が無い）。
 */
public class SteraSmccRow {

    private final String tradeCode;

    private final String storeNumber;

    private final String storeName;

    private final String paymentFormat;

    private final String cardBrand;

    private final String transactionType;

    private final LocalDate cutoffDate;

    private final int salesAmount;

    private final int acquirerFee;

    private final int companyFee;

    private final int settlementAmount;

    public SteraSmccRow(
            String tradeCode, String storeNumber, String storeName, String paymentFormat, String cardBrand,
            String transactionType, LocalDate cutoffDate, int salesAmount, int acquirerFee, int companyFee,
            int settlementAmount) {
        this.tradeCode = tradeCode;
        this.storeNumber = storeNumber;
        this.storeName = storeName;
        this.paymentFormat = paymentFormat;
        this.cardBrand = cardBrand;
        this.transactionType = transactionType;
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

    public String getPaymentFormat() {
        return paymentFormat;
    }

    public String getCardBrand() {
        return cardBrand;
    }

    public String getTransactionType() {
        return transactionType;
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
