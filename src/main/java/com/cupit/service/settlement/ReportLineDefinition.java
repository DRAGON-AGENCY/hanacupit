package com.cupit.service.settlement;

import java.util.List;

/**
 * 帳票（売上報告書・支払明細書）の1行の定義（決済種別のExcelテンプレート上の行番号・
 * ラベル・集計データの参照キー）。行番号はサンプル帳票（サンプル_支払明細書.xlsx／
 * サンプル_売上報告書.xlsx）をそのままテンプレートとして使うため、そのシート上の
 * 固定行番号（1始まり）と一致させている。
 * paymentCompany・cardBrandの組が現在実装済みの決済種別のいずれとも一致しない行
 * （stera関連・電子マネー初期費用・業務委託料等、取込み機能が未実装の項目）は、
 * 対応するReportRowが無いため件数・金額とも0円のプレースホルダー行として出力される。
 * 【交通系電子マネー】【ｎａｎａｃｏ】【ＷＡＯＮ】は実装済みだが、正しい手数料計算式が
 * 未検証のためJftdTransferCalculationService側で行を生成していない
 * （こちらも結果的に0円のプレースホルダーになる）。
 */
public final class ReportLineDefinition {

    private final int row;

    private final String label;

    private final String paymentCompany;

    private final String cardBrand;

    private ReportLineDefinition(int row, String label, String paymentCompany, String cardBrand) {
        this.row = row;
        this.label = label;
        this.paymentCompany = paymentCompany;
        this.cardBrand = cardBrand;
    }

    private static ReportLineDefinition of(int row, String label, String paymentCompany, String cardBrand) {
        return new ReportLineDefinition(row, label, paymentCompany, cardBrand);
    }

    /** テンプレート上の「小計」行（この位置に集計済みの小計を書き込む）を示す。 */
    public static ReportLineDefinition subtotal(int row) {
        return new ReportLineDefinition(row, null, null, null);
    }

    public int getRow() {
        return row;
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
     * 支払明細書テンプレート（サンプル_支払明細書.xlsx）と同じ行番号・順序・
     * 3グループ構成の行定義一覧を返す。
     */
    public static List<ReportLineDefinition> supportStatementOrder() {
        return List.of(
                of(17, "Visa/Master Card", "住信SBI", "Visa/Master"),
                of(19, "AliPay", "ネットスターズ", "Alipay"),
                of(21, "PayPay", "ネットスターズ", "PayPay"),
                of(23, "ｄ払い", "ネットスターズ", "d払い"),
                of(25, "WeChatPay", "ネットスターズ", "WeChatPay"),
                of(27, "JCB", "JCB", "【ＪＣＢカード】"),
                of(29, "AMEX", "JCB", "【ＡＭＥＸカード】"),
                of(31, "Diners Club", "JCB", "【ダイナースクラブ】"),
                of(33, "DISCOVER", "JCB", "【ディスカバー】"),
                of(35, "銀聯", "JCB", "【銀聯カード】"),
                of(37, "SMARTCODE", "JCB", "【スマートコード】"),
                subtotal(39),
                of(40, "QUICPay", "JCB", "【ＱＵＩＣＰａｙ】"),
                of(42, "交通系電子マネー", "JCB", "【交通系電子マネー】"),
                of(44, "nanaco", "JCB", "【ｎａｎａｃｏ】"),
                of(46, "WAON", "JCB", "【ＷＡＯＮ】"),
                of(48, "楽天Pay", "楽天ペイ", "楽天ペイ"),
                of(50, "端末月額利用料", "スマレジ(端末月額)", "本体"),
                of(52, "電子マネー初期費用", "未実装", "電子マネー初期費用"),
                of(54, "電子マネー月額利用料", "未実装", "電子マネー月額利用料"),
                of(56, "端末追加貸与登録料", "未実装", "端末追加貸与登録料"),
                subtotal(73),
                of(74, "stera dポイント月額利用料", "未実装", "stera dポイント月額利用料"),
                of(76, "stera dポイント利用", "未実装", "stera dポイント利用"),
                of(78, "stera dポイント付与", "未実装", "stera dポイント付与"),
                of(80, "stera POS＋月額利用料", "未実装", "stera POS＋月額利用料"),
                of(82, "stera 領収書アプリ", "未実装", "stera 領収書アプリ"),
                subtotal(106));
    }

    /**
     * 売上報告書テンプレート（サンプル_売上報告書.xlsx）と同じ行番号・順序の行定義一覧を
     * 返す。こちらは単一グループ・小計無しで、テンプレート側に既にSUM式（D30〜K30）が
     * 組み込まれているため、合計行はJava側で計算・出力しない。
     */
    public static List<ReportLineDefinition> salesReportOrder() {
        return List.of(
                of(7, "Visa/Master Card", "住信SBI", "Visa/Master"),
                of(8, "AliPay", "ネットスターズ", "Alipay"),
                of(9, "PayPay", "ネットスターズ", "PayPay"),
                of(10, "ｄ払い", "ネットスターズ", "d払い"),
                of(11, "WeChatPay", "ネットスターズ", "WeChatPay"),
                of(12, "JCB", "JCB", "【ＪＣＢカード】"),
                of(13, "AMEX", "JCB", "【ＡＭＥＸカード】"),
                of(14, "Diners Club", "JCB", "【ダイナースクラブ】"),
                of(15, "DISCOVER", "JCB", "【ディスカバー】"),
                of(16, "銀聯", "JCB", "【銀聯カード】"),
                of(17, "SMARTCODE", "JCB", "【スマートコード】"),
                of(18, "交通系電子マネー", "JCB", "【交通系電子マネー】"),
                of(19, "QUICPay", "JCB", "【ＱＵＩＣＰａｙ】"),
                of(20, "nanaco", "JCB", "【ｎａｎａｃｏ】"),
                of(21, "WAON", "JCB", "【ＷＡＯＮ】"),
                of(22, "楽天Pay", "楽天ペイ", "楽天ペイ"),
                of(23, "端末月額利用料", "スマレジ(端末月額)", "本体"),
                of(24, "電子マネー初期費用", "未実装", "電子マネー初期費用"),
                of(25, "電子マネー月額利用料", "未実装", "電子マネー月額利用料"),
                of(26, "dポイント初期費用", "未実装", "dポイント初期費用"),
                of(27, "dポイント月額利用料", "未実装", "dポイント月額利用料"),
                of(28, "端末追加貸与登録料", "未実装", "端末追加貸与登録料"),
                of(29, "業務委託料", "未実装", "業務委託料"));
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
