package com.cupit.service.settlement;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.Charset;
import java.util.List;

import org.junit.jupiter.api.Test;

class SteraTransferCsvWriterTest {

    private final SteraTransferCsvWriter writer = new SteraTransferCsvWriter();

    @Test
    void writesHeaderAndOneRowPerLineItemInZenginColumnOrder() {
        SteraTransferLineItem item = new SteraTransferLineItem(
                "01-020", 14082, 415, 30, 129, 13508,
                "1020", "三菱ＵＦＪ銀行", "038", "本店", "1", "0322679", "ﾊﾅｷﾕ-ﾋﾟﾂﾄ ｴﾊﾗ");

        byte[] csvBytes = writer.writeCsv(List.of(item));
        String csv = new String(csvBytes, Charset.forName("MS932"));

        assertThat(csv).isEqualTo(
                "被仕向銀行番号,被仕向支店番号,預金種目,口座番号,受取人名,振込金額,"
                        + "EDI情報または顧客コード,識別表示\r\n"
                        + "1020,038,1,0322679,ﾊﾅｷﾕ-ﾋﾟﾂﾄ ｴﾊﾗ,13508,01-020,\r\n");
    }

    @Test
    void convertsFullWidthAccountHolderKanaToHalfWidth() {
        SteraTransferLineItem item = new SteraTransferLineItem(
                "02-030", 1000, 0, 0, 0, 1000,
                "0310", "ＧＭＯあおぞらネット銀行", "001", "本店", "1", "1234567", "ハナキューピット");

        byte[] csvBytes = writer.writeCsv(List.of(item));
        String csv = new String(csvBytes, Charset.forName("MS932"));

        assertThat(csv).contains("ﾊﾅｷｭｰﾋﾟｯﾄ");
        assertThat(csv).doesNotContain("ハナキューピット");
    }

    @Test
    void returnsHeaderOnlyWhenNoLineItems() {
        byte[] csvBytes = writer.writeCsv(List.of());
        String csv = new String(csvBytes, Charset.forName("MS932"));

        assertThat(csv).isEqualTo(
                "被仕向銀行番号,被仕向支店番号,預金種目,口座番号,受取人名,振込金額,"
                        + "EDI情報または顧客コード,識別表示\r\n");
    }

}
