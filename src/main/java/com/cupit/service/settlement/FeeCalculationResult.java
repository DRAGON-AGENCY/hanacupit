package com.cupit.service.settlement;

/**
 * 手数料計算の結果。
 * 手数料②（弊社→加盟店の上乗せ手数料、本体・消費税）・支払金額②は、
 * 実データとの逆算検証で正しい計算式を確認できなかったため未実装
 * （調査メモ「論点・オープン事項」項番6を参照）。
 */
public class FeeCalculationResult {

    private final int acquirerFee;

    private final int payableAmount1;

    public FeeCalculationResult(int acquirerFee, int payableAmount1) {
        this.acquirerFee = acquirerFee;
        this.payableAmount1 = payableAmount1;
    }

    public int getAcquirerFee() {
        return acquirerFee;
    }

    public int getPayableAmount1() {
        return payableAmount1;
    }

}
