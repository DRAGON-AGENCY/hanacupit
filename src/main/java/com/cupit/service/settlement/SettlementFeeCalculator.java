package com.cupit.service.settlement;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.cupit.model.SettlementFeeRate;

/**
 * 決済会社×カードブランドごとの手数料計算モデルを実装する。
 * 決済手数料①（決済事業者→弊社）は取引コード単位の売上金額合計に対して
 * 切り捨て（RoundingMode.DOWN）で計算すると実データと一致することを
 * JCBの生データで確認済み。手数料②（弊社→加盟店の上乗せ手数料、本体・消費税）は
 * 実データとの逆算検証で正しい計算式を確認できなかったため未実装
 * （調査メモ「論点・オープン事項」項番6を参照。計算式が判明次第、各メソッドに追加する）。
 */
@Component
public class SettlementFeeCalculator {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.1");

    public FeeCalculationResult calculateStraight(int salesAmount, SettlementFeeRate rate) {
        // TODO: 手数料②（本体: rate.getOurFeeRateBase()、消費税: rate.getOurFeeRateTax()）の
        // 正しい計算式が判明したら、支払金額②の算出をここに追加する。
        // STRAIGHTモデルの事業者手数料は、帳票（支払明細書）上は全額を非課税として扱う
        // （例: 住信SBI/Visa/Master Cardの実データで確認済み）。
        int acquirerFeeTaxFree = truncate(salesAmount, rate.getAcquirerFeeRate());
        int payableAmount1 = salesAmount - acquirerFeeTaxFree;
        return new FeeCalculationResult(acquirerFeeTaxFree, 0, 0, payableAmount1);
    }

    /**
     * 仕入・収代二段階式。ネットスターズ(PayPay/d払い)・楽天ペイの生データで検証した結果、
     * 仕入手数料は「本体を四捨五入→消費税は本体×10%を切り捨て」の順で算出しないと
     * 実データと一致しないことが判明した（本体・消費税をそれぞれ独立した率で計算し
     * 合算する方式や、combined rateを一括で切り捨てる方式では数円のズレが生じる）。
     * rate.getAcquirerFeeRate()には仕入手数料の本体分の率を保持する
     * （例: 楽天ペイ=0.028、PayPay=0.0265、d払い=0.026）。
     */
    public FeeCalculationResult calculatePurchaseCollect(int salesAmount, SettlementFeeRate rate) {
        // TODO: 収代手数料（本体: rate.getOurFeeRateBase()、消費税: rate.getOurFeeRateTax()）の
        // 正しい計算式が判明したら、支払金額の算出をここに追加する。
        // PURCHASE_COLLECTモデルの事業者手数料は、帳票（支払明細書）上は全額を課税対象
        // （本体・消費税）として扱う（例: ネットスターズPayPay/d払い・楽天ペイの実データで確認済み）。
        int acquirerFeeBase = halfUp(salesAmount, rate.getAcquirerFeeRate());
        int acquirerFeeTax = truncate(acquirerFeeBase, TAX_RATE);
        int acquirerFee = acquirerFeeBase + acquirerFeeTax;
        int payableAmount1 = salesAmount - acquirerFee;
        return new FeeCalculationResult(0, acquirerFeeBase, acquirerFeeTax, payableAmount1);
    }

    /**
     * 帳票用に、金額に対する消費税額（切り捨て）を計算する。スマレジ（端末月額）の
     * 事業者手数料内訳（本体・消費税）の算出に使う。手数料率マスタを経由しないため
     * calculateStraight/calculatePurchaseCollectとは別メソッドとしている。
     */
    public int calculateTax(int baseAmount) {
        return truncate(baseAmount, TAX_RATE);
    }

    private int truncate(int amount, BigDecimal rate) {
        return BigDecimal.valueOf(amount)
                .multiply(rate)
                .setScale(0, RoundingMode.DOWN)
                .intValueExact();
    }

    private int halfUp(int amount, BigDecimal rate) {
        return BigDecimal.valueOf(amount)
                .multiply(rate)
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();
    }

}
