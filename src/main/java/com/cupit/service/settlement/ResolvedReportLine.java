package com.cupit.service.settlement;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ReportLineDefinition（サンプル帳票の行定義）を実際の集計結果（ReportRow）と
 * 突き合わせた、帳票出力用の1行分のデータ。集計結果が存在しない行
 * （未実装の決済種別）は件数・金額とも0になる。
 */
public final class ResolvedReportLine {

    private final String label;

    private final int count;

    private final int grossAmount;

    private final int acquirerFeeTaxFreeAmount;

    private final int acquirerFeeBaseAmount;

    private final int acquirerFeeTaxAmount;

    private final int ourFeeBaseAmount;

    private final int ourFeeTaxAmount;

    private ResolvedReportLine(
            String label, int count, int grossAmount,
            int acquirerFeeTaxFreeAmount, int acquirerFeeBaseAmount, int acquirerFeeTaxAmount,
            int ourFeeBaseAmount, int ourFeeTaxAmount) {
        this.label = label;
        this.count = count;
        this.grossAmount = grossAmount;
        this.acquirerFeeTaxFreeAmount = acquirerFeeTaxFreeAmount;
        this.acquirerFeeBaseAmount = acquirerFeeBaseAmount;
        this.acquirerFeeTaxAmount = acquirerFeeTaxAmount;
        this.ourFeeBaseAmount = ourFeeBaseAmount;
        this.ourFeeTaxAmount = ourFeeTaxAmount;
    }

    /**
     * ReportLineDefinitionの並び順どおりに、集計結果（rows）と突き合わせた
     * ResolvedReportLineの一覧を返す。SUBTOTAL_MARKERはnullとして一覧に含める
     * （呼び出し側で小計行の挿入位置として扱う）。
     */
    public static List<ResolvedReportLine> resolveAll(
            List<ReportLineDefinition> definitions, List<ReportRow> rows) {
        Map<String, ReportRow> byKey = rows.stream()
                .collect(Collectors.toMap(
                        r -> r.getPaymentCompany() + " " + r.getCardBrand(), r -> r, (a, b) -> a));

        return definitions.stream()
                .map(def -> def.isSubtotalMarker() ? null : resolve(def, byKey))
                .collect(Collectors.toList());
    }

    private static ResolvedReportLine resolve(ReportLineDefinition def, Map<String, ReportRow> byKey) {
        ReportRow row = byKey.get(def.getKey());
        int count = row != null ? row.getCount() : 0;
        int gross = row != null ? row.getGrossAmount() : 0;
        int feeTaxFree = row != null ? row.getAcquirerFeeTaxFreeAmount() : 0;
        int feeBase = row != null ? row.getAcquirerFeeBaseAmount() : 0;
        int feeTax = row != null ? row.getAcquirerFeeTaxAmount() : 0;
        int ourFeeBase = row != null ? row.getFeeBaseAmount() : 0;
        int ourFeeTax = row != null ? row.getFeeTaxAmount() : 0;

        if (def.isSumarejoTerminalFeeRow()) {
            ReportRow adjustment = byKey.get(def.getSumarejoAdjustmentKey());
            if (adjustment != null) {
                count += adjustment.getCount();
                gross += adjustment.getGrossAmount();
                feeTaxFree += adjustment.getAcquirerFeeTaxFreeAmount();
                feeBase += adjustment.getAcquirerFeeBaseAmount();
                feeTax += adjustment.getAcquirerFeeTaxAmount();
                ourFeeBase += adjustment.getFeeBaseAmount();
                ourFeeTax += adjustment.getFeeTaxAmount();
            }
        }
        return new ResolvedReportLine(
                def.getLabel(), count, gross, feeTaxFree, feeBase, feeTax, ourFeeBase, ourFeeTax);
    }

    public String getLabel() {
        return label;
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

    public int getAcquirerFeeTotal() {
        return acquirerFeeTaxFreeAmount + acquirerFeeBaseAmount + acquirerFeeTaxAmount;
    }

    public int getAfterAcquirerFeeAmount() {
        return grossAmount - getAcquirerFeeTotal();
    }

    public int getOurFeeBaseAmount() {
        return ourFeeBaseAmount;
    }

    public int getOurFeeTaxAmount() {
        return ourFeeTaxAmount;
    }

    public int getOurFeeTotal() {
        return ourFeeBaseAmount + ourFeeTaxAmount;
    }

    public int getTotalFeeAmount() {
        return getAcquirerFeeTotal() + getOurFeeTotal();
    }

    public int getNetPayableAmount() {
        return grossAmount - getTotalFeeAmount();
    }

}
