package com.cupit.service.settlement;

import java.util.List;

/**
 * 帳票（売上報告書・支払明細書）に出力する行の定義（決済種別の表示順・ラベル・
 * 集計データの参照キー）。サンプル帳票（サンプル_支払明細書.xlsx／
 * サンプル_売上報告書.xlsx）の行構成に合わせている。
 * paymentCompany・cardBrandの組が現在実装済みの決済種別のいずれとも一致しない行
 * （stera関連・電子マネー初期費用・業務委託料等、取込み機能が未実装の項目）は、
 * 対応するReportRowが無いため件数・金額とも0円のプレースホルダー行として出力される。
 * 【交通系電子マネー】【ｎａｎａｃｏ】【ＷＡＯＮ】は実装済みだが、正しい手数料計算式が
 * 未検証のためJftdTransferCalculationService側で行を生成していない
 * （こちらも結果的に0円のプレースホルダーになる）。
 */
public final class ReportLineDefinition {

    /** 「小計」を挿入する区切り位置（この行の直後に小計行を出力する）を示すマーカー。 */
    public static final ReportLineDefinition SUBTOTAL_MARKER =
            new ReportLineDefinition(null, null, null);

    private final String label;

    private final String paymentCompany;

    private final String cardBrand;

    private ReportLineDefinition(String label, String paymentCompany, String cardBrand) {
        this.label = label;
        this.paymentCompany = paymentCompany;
        this.cardBrand = cardBrand;
    }

    private static ReportLineDefinition of(String label, String paymentCompany, String cardBrand) {
        return new ReportLineDefinition(label, paymentCompany, cardBrand);
    }

    public String getLabel() {
        return label;
    }

    public String getKey() {
        return paymentCompany + " " + cardBrand;
    }

    public boolean isSubtotalMarker() {
        return label == null;
    }

    /**
     * サンプル帳票と同じ順序・グルーピングの行定義一覧を返す。
     * SUBTOTAL_MARKERの直後に小計行が出力される（サンプルの3グループ構成に対応）。
     */
    public static List<ReportLineDefinition> defaultOrder() {
        return List.of(
                of("Visa/Master Card", "住信SBI", "Visa/Master"),
                of("AliPay", "ネットスターズ", "Alipay"),
                of("PayPay", "ネットスターズ", "PayPay"),
                of("ｄ払い", "ネットスターズ", "d払い"),
                of("WeChatPay", "ネットスターズ", "WeChatPay"),
                of("JCB", "JCB", "【ＪＣＢカード】"),
                of("AMEX", "JCB", "【ＡＭＥＸカード】"),
                of("Diners Club", "JCB", "【ダイナースクラブ】"),
                of("DISCOVER", "JCB", "【ディスカバー】"),
                of("銀聯", "JCB", "【銀聯カード】"),
                of("SMARTCODE", "JCB", "【スマートコード】"),
                SUBTOTAL_MARKER,
                of("QUICPay", "JCB", "【ＱＵＩＣＰａｙ】"),
                of("交通系電子マネー", "JCB", "【交通系電子マネー】"),
                of("nanaco", "JCB", "【ｎａｎａｃｏ】"),
                of("WAON", "JCB", "【ＷＡＯＮ】"),
                of("楽天Pay", "楽天ペイ", "楽天ペイ"),
                of("端末月額利用料", "スマレジ(端末月額)", "本体"),
                of("電子マネー初期費用", "未実装", "電子マネー初期費用"),
                of("電子マネー月額利用料", "未実装", "電子マネー月額利用料"),
                of("端末追加貸与登録料", "未実装", "端末追加貸与登録料"),
                of("業務委託料", "未実装", "業務委託料"),
                SUBTOTAL_MARKER,
                of("stera dポイント月額利用料", "未実装", "stera dポイント月額利用料"),
                of("stera dポイント利用", "未実装", "stera dポイント利用"),
                of("stera dポイント付与", "未実装", "stera dポイント付与"),
                of("stera POS＋月額利用料", "未実装", "stera POS＋月額利用料"),
                of("stera 領収書アプリ", "未実装", "stera 領収書アプリ"),
                SUBTOTAL_MARKER);
    }

    /**
     * 「端末月額利用料」行は、スマレジ(端末月額)の本体(3300217)・調整(3300219)の
     * 2項目コードをサンプル帳票と同じく1行に合算して表示するため、この行に限り
     * 通常の単一キー参照に加えて「調整」分も合算する必要がある。
     */
    public boolean isSumarejoTerminalFeeRow() {
        return "スマレジ(端末月額)".equals(paymentCompany) && "本体".equals(cardBrand);
    }

    public String getSumarejoAdjustmentKey() {
        return "スマレジ(端末月額) 調整";
    }

}
