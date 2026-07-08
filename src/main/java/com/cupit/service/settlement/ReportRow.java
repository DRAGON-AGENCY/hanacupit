package com.cupit.service.settlement;

/**
 * 帳票（売上報告書・支払明細書）の1行分の集計データ。決済会社×カードブランド単位
 * （全店舗合算）で、統合振込CSVとは異なる粒度の集計結果を保持する。
 * ourFeeBaseAmount・ourFeeTaxAmountは手数料②（弊社→加盟店の上乗せ手数料）用で、
 * 未実装のため常に0になる（調査メモ「論点・オープン事項」項番6を参照）。
 * grossAmount・acquirerFeeTaxFreeAmount・acquirerFeeBaseAmount・acquirerFeeTaxAmountは
 * 決済事業者（JCB・住信SBI等）への事業者手数料の内訳で、サンプル帳票の列構成に対応する。
 */
public class ReportRow {

    private final String paymentCompany;

    private final String cardBrand;

    private final int count;

    private final int grossAmount;

    private final int acquirerFeeTaxFreeAmount;

    private final int acquirerFeeBaseAmount;

    private final int acquirerFeeTaxAmount;

    private final int paymentAmount;

    private final int ourFeeBaseAmount;

    private final int ourFeeTaxAmount;

    public ReportRow(
            String paymentCompany, String cardBrand, int count,
            int grossAmount, int acquirerFeeTaxFreeAmount, int acquirerFeeBaseAmount, int acquirerFeeTaxAmount,
            int paymentAmount, int ourFeeBaseAmount, int ourFeeTaxAmount) {
        this.paymentCompany = paymentCompany;
        this.cardBrand = cardBrand;
        this.count = count;
        this.grossAmount = grossAmount;
        this.acquirerFeeTaxFreeAmount = acquirerFeeTaxFreeAmount;
        this.acquirerFeeBaseAmount = acquirerFeeBaseAmount;
        this.acquirerFeeTaxAmount = acquirerFeeTaxAmount;
        this.paymentAmount = paymentAmount;
        this.ourFeeBaseAmount = ourFeeBaseAmount;
        this.ourFeeTaxAmount = ourFeeTaxAmount;
    }

    public String getPaymentCompany() {
        return paymentCompany;
    }

    public String getCardBrand() {
        return cardBrand;
    }

    public int getCount() {
        return count;
    }

    public int getGrossAmount() {
        return grossAmount;
    }

    public int getAcquirerFeeTaxFreeAmount() {
        return acquirerFeeTaxFreeAmount;
    }

    public int getAcquirerFeeBaseAmount() {
        return acquirerFeeBaseAmount;
    }

    public int getAcquirerFeeTaxAmount() {
        return acquirerFeeTaxAmount;
    }

    /**
     * 事業者手数料計（非課税＋課税本体＋消費税）。
     */
    public int getAcquirerFeeTotal() {
        return acquirerFeeTaxFreeAmount + acquirerFeeBaseAmount + acquirerFeeTaxAmount;
    }

    /**
     * 事業者手数料差引後決済金額（決済金額合計－事業者手数料計）。支払金額①に相当する。
     */
    public int getAfterAcquirerFeeAmount() {
        return grossAmount - getAcquirerFeeTotal();
    }

    public int getPaymentAmount() {
        return paymentAmount;
    }

    public int getFeeBaseAmount() {
        return ourFeeBaseAmount;
    }

    public int getFeeTaxAmount() {
        return ourFeeTaxAmount;
    }

    /**
     * 手数料合計（弊社手数料計＋事業者手数料計）。
     */
    public int getTotalFeeAmount() {
        return ourFeeBaseAmount + ourFeeTaxAmount + getAcquirerFeeTotal();
    }

    /**
     * 差引振込額（決済金額合計－事業者手数料計－弊社手数料計）。
     */
    public int getNetPayableAmount() {
        return grossAmount - getTotalFeeAmount();
    }

}
