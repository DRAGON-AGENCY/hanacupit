package com.cupit.csv.writer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.cupit.model.SmccMerchantNo;

/**
 * 加盟店番号データ CSV を m_smcc_merchant_no から書き出す。
 * ヘッダー列は{@link com.cupit.csv.importer.SmccMerchantNoFileImporter}と同じ列・同じ並び順
 * （取込みの逆変換）とし、そのままアップロードし直せるようにする。
 * UTF-8（BOM付き）・CRLF・RFC4180準拠のクォート処理で出力する。
 */
@Component
public class SmccMerchantNoCsvWriter {

    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final String HEADER_LINE = "取引コード,SMCC加盟店番号,種別,届出支店コード";

    public byte[] writeCsv(List<SmccMerchantNo> records) {
        StringBuilder csv = new StringBuilder();
        csv.append(HEADER_LINE).append("\r\n");
        for (SmccMerchantNo smccMerchantNo : records) {
            csv.append(toCsvLine(smccMerchantNo));
        }
        byte[] body = csv.toString().getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[UTF8_BOM.length + body.length];
        System.arraycopy(UTF8_BOM, 0, result, 0, UTF8_BOM.length);
        System.arraycopy(body, 0, result, UTF8_BOM.length, body.length);
        return result;
    }

    private String toCsvLine(SmccMerchantNo smccMerchantNo) {
        List<String> fields = new ArrayList<>();
        fields.add(token(smccMerchantNo.getTradeCode()));
        fields.add(token(smccMerchantNo.getMerchantNo()));
        fields.add(token(smccMerchantNo.getType()));
        fields.add(token(smccMerchantNo.getBranchCode()));
        return String.join(",", fields) + "\r\n";
    }

    private String token(Object value) {
        if (value == null) {
            return "";
        }
        return quote(value.toString());
    }

    private String quote(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

}
