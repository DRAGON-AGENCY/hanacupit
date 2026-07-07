package com.cupit.service.settlement;

/**
 * 帳票（売上報告書・支払明細書）の1行分の集計データ。決済会社×カードブランド単位
 * （全店舗合算）で、統合振込CSVとは異なる粒度の集計結果を保持する。
 * 手数料②が未実装のため、feeBaseAmount・feeTaxAmountは常に0になる
 * （調査メモ「論点・オープン事項」項番6を参照）。
 */
public class ReportRow {

    private final String paymentCompany;

    private final String cardBrand;

    private final int count;

    private final int paymentAmount;

    private final int feeBaseAmount;

    private final int feeTaxAmount;

    public ReportRow(
            String paymentCompany, String cardBrand, int count,
            int paymentAmount, int feeBaseAmount, int feeTaxAmount) {
        this.paymentCompany = paymentCompany;
        this.cardBrand = cardBrand;
        this.count = count;
        this.paymentAmount = paymentAmount;
        this.feeBaseAmount = feeBaseAmount;
        this.feeTaxAmount = feeTaxAmount;
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

    public int getPaymentAmount() {
        return paymentAmount;
    }

    public int getFeeBaseAmount() {
        return feeBaseAmount;
    }

    public int getFeeTaxAmount() {
        return feeTaxAmount;
    }

}
