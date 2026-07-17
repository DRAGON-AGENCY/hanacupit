package com.cupit.service.settlement;

import java.time.LocalDate;
import java.util.List;

/**
 * 支払明細書の書き出しに必要なデータ一式。決済会社×カードブランド単位の集計行
 * （{@code rows}）に加えて、テンプレートの「お支払日」欄に出力する日付
 * （{@code paymentDate}）を持つ。売上報告書は「お支払日」欄が無いため
 * {@code List<ReportRow>}のみで足り、このクラスは支払明細書専用とする。
 */
public record SupportStatementData(List<ReportRow> rows, LocalDate paymentDate) {
}
