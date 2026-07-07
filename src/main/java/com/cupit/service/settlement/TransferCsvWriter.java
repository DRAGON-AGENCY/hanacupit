package com.cupit.service.settlement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Component;

/**
 * 統合振込CSVを項目コード単位で書き出し、ZIPにまとめる。
 * 実データ（総合精算CSV）と同じくShift-JIS（MS932）・BOMなし・CRLFで出力する。
 */
@Component
public class TransferCsvWriter {

    private static final Charset CSV_CHARSET = Charset.forName("MS932");
    private static final String HEADER_LINE = "項目コード,取引コード,数量,金額\r\n";

    public byte[] writeZip(List<TransferLineItem> lineItems) {
        Map<String, List<TransferLineItem>> byItemCode = new LinkedHashMap<>();
        for (TransferLineItem item : lineItems) {
            byItemCode.computeIfAbsent(item.getItemCode(), key -> new java.util.ArrayList<>()).add(item);
        }

        ByteArrayOutputStream zipBytes = new ByteArrayOutputStream();
        try (ZipOutputStream zipOut = new ZipOutputStream(zipBytes)) {
            for (Map.Entry<String, List<TransferLineItem>> entry : byItemCode.entrySet()) {
                zipOut.putNextEntry(new ZipEntry(entry.getKey() + ".csv"));
                zipOut.write(buildCsv(entry.getValue()));
                zipOut.closeEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("統合振込CSVのZIP作成に失敗しました。", e);
        }
        return zipBytes.toByteArray();
    }

    private byte[] buildCsv(List<TransferLineItem> items) {
        StringBuilder csv = new StringBuilder(HEADER_LINE);
        for (TransferLineItem item : items) {
            csv.append(item.getItemCode())
                    .append(',')
                    .append(item.getTradeCode())
                    .append(',')
                    .append(item.getQuantity())
                    .append(',')
                    .append(item.getAmount())
                    .append("\r\n");
        }
        return csv.toString().getBytes(CSV_CHARSET);
    }

}
