package com.cupit.service.settlement;

import java.nio.charset.Charset;
import java.util.List;

import org.springframework.stereotype.Component;

import com.cupit.util.HalfWidthKanaConverter;

/**
 * その他統合振込CSV（stera terminal）を全銀フォーマットの単純な8列CSVとして書き出す。
 * 項目コードによる分割が無いため、{@link TransferCsvWriter}と異なりZIP化はせず単一の
 * CSVファイルを返す。実データ（総合振込CSV）と同じくShift-JIS（MS932）・BOMなし・CRLFで
 * 出力する。
 *
 * EDI情報または顧客コード列は暫定でtrade_codeをそのまま使用している。取引コードの
 * 表記変換（ハイフン除去等）の要否は実物（stera_transfer_20251205.csv等）との突合で
 * 別途検証が必要（調査メモ参照）。
 */
@Component
public class SteraTransferCsvWriter {

    private static final Charset CSV_CHARSET = Charset.forName("MS932");
    private static final String HEADER_LINE =
            "被仕向銀行番号,被仕向支店番号,預金種目,口座番号,受取人名,振込金額,"
                    + "EDI情報または顧客コード,識別表示\r\n";

    public byte[] writeCsv(List<SteraTransferLineItem> lineItems) {
        StringBuilder csv = new StringBuilder(HEADER_LINE);
        for (SteraTransferLineItem item : lineItems) {
            csv.append(item.getBankCode())
                    .append(',')
                    .append(item.getBankBranchCode())
                    .append(',')
                    .append(item.getAccountType())
                    .append(',')
                    .append(item.getAccountNo())
                    .append(',')
                    .append(HalfWidthKanaConverter.toHalfWidth(item.getAccountHolderKana()))
                    .append(',')
                    .append(item.getNetAmount())
                    .append(',')
                    .append(item.getTradeCode())
                    .append(',')
                    .append("\r\n");
        }
        return csv.toString().getBytes(CSV_CHARSET);
    }

}
