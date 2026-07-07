package com.cupit.service.settlement;

/**
 * 決済会社×カードブランドごとの手数料計算モデル。
 * STRAIGHT: 直線式（JCB・AMEX・DINERS・Discover・銀聯・QUICPay・SMARTCODE・Alipay・WeChatPay）。
 * PURCHASE_COLLECT: 仕入・収代二段階式（交通系電子マネー・nanaco・WAON・PayPay・d払い・楽天ペイ）。
 * SBI_RESIDUAL: 住信SBI残差式（消費税を差額で算出、事業者手数料率は明細行の実績値を使用）。
 */
public enum FeeCalcModel {

    STRAIGHT,
    PURCHASE_COLLECT,
    SBI_RESIDUAL

}
