package com.cupit.csv.writer;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.cupit.model.PaygateStoreMapping;

/**
 * PAYGATE 会員コード紐付 CSV を m_paygate_store_mapping から書き出す。
 * ヘッダー列は「取引コード紐付データ作成」CSVフォーマット（{@link
 * com.cupit.csv.importer.PaygateMappingFileImporter}の逆変換）と同じ13列・同じ並び順
 * とし、そのままアップロードし直せるようにする。
 * UTF-8（BOM付き）・CRLF・RFC4180準拠のクォート処理で出力する。
 */
@Component
public class PaygateMappingCsvWriter {

    private static final DateTimeFormatter FMT_SLASH = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final String HEADER_LINE =
            "hana cupid管理番号,店舗名,会員種別,端末識別番号,リーダーシリアル番号,"
            + "加盟店番号(住信SBI),StarPay店舗コード,加盟店番号(JCB),DNP管理番号,GW店舗コード(楽天Pay),"
            + "利用ステータス,利用意思,利用意思更新日";

    public byte[] writeCsv(List<PaygateStoreMapping> records) {
        StringBuilder csv = new StringBuilder();
        csv.append(HEADER_LINE).append("\r\n");
        for (PaygateStoreMapping mapping : records) {
            csv.append(toCsvLine(mapping));
        }
        byte[] body = csv.toString().getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[UTF8_BOM.length + body.length];
        System.arraycopy(UTF8_BOM, 0, result, 0, UTF8_BOM.length);
        System.arraycopy(body, 0, result, UTF8_BOM.length, body.length);
        return result;
    }

    private String toCsvLine(PaygateStoreMapping mapping) {
        List<String> fields = new ArrayList<>();
        fields.add(token(mapping.getTradeCode()));
        fields.add(token(mapping.getStoreName()));
        fields.add(token(mapping.getMemberType()));
        fields.add(token(mapping.getTerminalId()));
        fields.add(token(mapping.getReaderSerialNo()));
        fields.add(token(mapping.getSbiMerchantId()));
        fields.add(token(mapping.getNetstarStoreCode()));
        fields.add(token(mapping.getJcbMerchantNo()));
        fields.add(token(mapping.getDnpMgmtNo()));
        fields.add(token(mapping.getRpayStoreCode()));
        fields.add(token(mapping.getTerminalStatus()));
        fields.add(token(mapping.getUsageIntention()));
        fields.add(token(mapping.getUsageIntentionUpdated()));
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
