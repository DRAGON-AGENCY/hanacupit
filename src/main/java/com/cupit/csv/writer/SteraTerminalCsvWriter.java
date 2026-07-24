package com.cupit.csv.writer;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.cupit.model.SteraTerminal;

/**
 * 端末データ CSV を m_stera_terminal から書き出す。
 * ヘッダー列は{@link com.cupit.csv.importer.SteraTerminalFileImporter}と同じ列・同じ並び順
 * （取込みの逆変換）とし、そのままアップロードし直せるようにする。
 * UTF-8（BOM付き）・CRLF・RFC4180準拠のクォート処理で出力する。
 */
@Component
public class SteraTerminalCsvWriter {

    private static final DateTimeFormatter FMT_SLASH = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final String HEADER_LINE =
            "取引コード,端末識別番号,JCB加盟店番号,届出支店コード,端末利用ステータス,端末利用開始日,端末利用終了日";

    public byte[] writeCsv(List<SteraTerminal> records) {
        StringBuilder csv = new StringBuilder();
        csv.append(HEADER_LINE).append("\r\n");
        for (SteraTerminal steraTerminal : records) {
            csv.append(toCsvLine(steraTerminal));
        }
        byte[] body = csv.toString().getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[UTF8_BOM.length + body.length];
        System.arraycopy(UTF8_BOM, 0, result, 0, UTF8_BOM.length);
        System.arraycopy(body, 0, result, UTF8_BOM.length, body.length);
        return result;
    }

    private String toCsvLine(SteraTerminal steraTerminal) {
        List<String> fields = new ArrayList<>();
        fields.add(token(steraTerminal.getTradeCode()));
        fields.add(token(steraTerminal.getTerminalId()));
        fields.add(token(steraTerminal.getJcbMerchantNo()));
        fields.add(token(steraTerminal.getBranchCode()));
        fields.add(token(steraTerminal.getTerminalStatus()));
        fields.add(token(steraTerminal.getTerminalStartDate()));
        fields.add(token(steraTerminal.getTerminalEndDate()));
        return String.join(",", fields) + "\r\n";
    }

    private String token(Object value) {
        if (value == null) {
            return "";
        }
        String s = (value instanceof LocalDate date) ? date.format(FMT_SLASH) : value.toString();
        return quote(s);
    }

    private String quote(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

}
