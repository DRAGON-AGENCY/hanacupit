package com.cupit.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * PAYGATE Station精算情報照会画面の明細1行分のDTO。
 * 取引コード×決済会社×決済種類×締め日の単位で1行になる
 * （1画面に全決済会社・全期間のデータが混在するため、この粒度が必要）。
 * 1行の金額は元ファイル（インポートバッチ）単位の内訳を合算したものであり、
 * どの元ファイルがいくら含まれているかを{@link #getDetails()}で参照できる
 * （明細行クリック時の内訳表示に使用）。
 */
public class PaygateStationRow {

    private final String tradeCode;

    private final String storeName;

    private final String paymentCompany;

    private final String cardBrand;

    private final LocalDate cutoffDate;

    private final int salesCount;

    private final int salesAmount;

    private final int acquirerFee;

    private final int payableAmount;

    private final List<PaygateStationRow> details;

    public PaygateStationRow(
            String tradeCode, String storeName, String paymentCompany, String cardBrand,
            LocalDate cutoffDate, int salesCount, int salesAmount, int acquirerFee, int payableAmount) {
        this(tradeCode, storeName, paymentCompany, cardBrand, cutoffDate,
                salesCount, salesAmount, acquirerFee, payableAmount, List.of());
    }

    public PaygateStationRow(
            String tradeCode, String storeName, String paymentCompany, String cardBrand,
            LocalDate cutoffDate, int salesCount, int salesAmount, int acquirerFee, int payableAmount,
            List<PaygateStationRow> details) {
        this.tradeCode = tradeCode;
        this.storeName = storeName;
        this.paymentCompany = paymentCompany;
        this.cardBrand = cardBrand;
        this.cutoffDate = cutoffDate;
        this.salesCount = salesCount;
        this.salesAmount = salesAmount;
        this.acquirerFee = acquirerFee;
        this.payableAmount = payableAmount;
        this.details = details;
    }

    public String getTradeCode() {
        return tradeCode;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getPaymentCompany() {
        return paymentCompany;
    }

    public String getCardBrand() {
        return cardBrand;
    }

    public LocalDate getCutoffDate() {
        return cutoffDate;
    }

    public int getSalesCount() {
        return salesCount;
    }

    public int getSalesAmount() {
        return salesAmount;
    }

    public int getAcquirerFee() {
        return acquirerFee;
    }

    public int getPayableAmount() {
        return payableAmount;
    }

    public List<PaygateStationRow> getDetails() {
        return details;
    }

}
