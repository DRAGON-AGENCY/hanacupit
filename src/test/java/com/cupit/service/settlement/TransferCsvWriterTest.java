package com.cupit.service.settlement;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.jupiter.api.Test;

/**
 * TransferCsvWriter のテスト。生成したZIPを実際に読み戻し、
 * 項目コードごとにファイルが分かれ、内容がShift-JISで正しく復元できることを検証する。
 */
class TransferCsvWriterTest {

    private static final Charset MS932 = Charset.forName("MS932");

    @Test
    void writeZipGroupsLineItemsByItemCode() throws IOException {
        TransferCsvWriter writer = new TransferCsvWriter();
        List<TransferLineItem> lineItems = List.of(
                new TransferLineItem("01-001", "3300024", 1, 14150),
                new TransferLineItem("03-048", "3300024", 1, 164758),
                new TransferLineItem("35-026", "3300007", 1, 649));

        byte[] zipBytes = writer.writeZip(lineItems);

        java.util.Map<String, String> contentsByEntry = new java.util.LinkedHashMap<>();
        try (ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                zipIn.transferTo(out);
                contentsByEntry.put(entry.getName(), out.toString(MS932));
            }
        }

        assertThat(contentsByEntry).containsOnlyKeys("3300024.csv", "3300007.csv");
        String jcbCsv = contentsByEntry.get("3300024.csv");
        assertThat(jcbCsv).isEqualTo(
                "項目コード,取引コード,数量,金額\r\n"
                + "3300024,01-001,1,14150\r\n"
                + "3300024,03-048,1,164758\r\n");
        String alipayCsv = contentsByEntry.get("3300007.csv");
        assertThat(alipayCsv).isEqualTo(
                "項目コード,取引コード,数量,金額\r\n"
                + "3300007,35-026,1,649\r\n");
    }

}
