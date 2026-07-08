package com.cupit.service.settlement;

/**
 * 手数料計算の結果。
 * 事業者手数料（決済事業者→弊社）は、帳票（支払明細書・売上報告書）の列構成に
 * 合わせて非課税・課税本体・消費税の3種に内訳を分けて保持する。STRAIGHTモデルは
 * 全額を非課税（acquirerFeeTaxFree）として扱い、PURCHASE_COLLECTモデルは
 * 全額を課税対象（acquirerFeeBase・acquirerFeeTax）として扱う。
 * 手数料②（弊社→加盟店の上乗せ手数料、本体・消費税）・支払金額②は、
 * 実データとの逆算検証で正しい計算式を確認できなかったため未実装
 * （調査メモ「論点・オープン事項」項番6を参照）。
 */
public class FeeCalculationResult {

    private final int acquirerFeeTaxFree;

    private final int acquirerFeeBase;

    private final int acquirerFeeTax;

    private final int payableAmount1;

    public FeeCalculationResult(
            int acquirerFeeTaxFree, int acquirerFeeBase, int acquirerFeeTax, int payableAmount1) {
        this.acquirerFeeTaxFree = acquirerFeeTaxFree;
        this.acquirerFeeBase = acquirerFeeBase;
        this.acquirerFeeTax = acquirerFeeTax;
        this.payableAmount1 = payableAmount1;
    }

    public int getAcquirerFeeTaxFree() {
        return acquirerFeeTaxFree;
    }

    public int getAcquirerFeeBase() {
        return acquirerFeeBase;
    }

    public int getAcquirerFeeTax() {
        return acquirerFeeTax;
    }

    public int getAcquirerFee() {
        return acquirerFeeTaxFree + acquirerFeeBase + acquirerFeeTax;
    }

    public int getPayableAmount1() {
        return payableAmount1;
    }

}
