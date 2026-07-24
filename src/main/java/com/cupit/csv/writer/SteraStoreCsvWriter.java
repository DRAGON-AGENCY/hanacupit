package com.cupit.csv.writer;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.cupit.model.SteraStore;

/**
 * 店舗データ CSV を m_stera_store から書き出す。
 * ヘッダー列は{@link com.cupit.csv.importer.SteraStoreFileImporter}と同じ列・同じ並び順
 * （取込みの逆変換）とし、そのままアップロードし直せるようにする。
 * UTF-8（BOM付き）・CRLF・RFC4180準拠のクォート処理で出力する。
 */
@Component
public class SteraStoreCsvWriter {

    private static final DateTimeFormatter FMT_SLASH = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final String HEADER_LINE =
            "取引コード,交通系社局,Edy ID,dポイント加盟店コード,dポイント店舗コード,dポイント支部コード,届出支店コード,会員種別"
            + ",店舗名,店舗名カナ,店舗名（英字）,店舗郵便番号,店舗住所（漢字）,店舗住所（カナ）,店舗電話番号,メールアドレス,緯度,経度"
            + ",金融機関名,金融機関コード,支店名,支店コード,口座種別,口座番号,口座名義（カナ）"
            + ",JCB利用状況,JCB利用開始日,dポイント利用状況,dポイント利用開始日,備考";

    public byte[] writeCsv(List<SteraStore> records) {
        StringBuilder csv = new StringBuilder();
        csv.append(HEADER_LINE).append("\r\n");
        for (SteraStore steraStore : records) {
            csv.append(toCsvLine(steraStore));
        }
        byte[] body = csv.toString().getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[UTF8_BOM.length + body.length];
        System.arraycopy(UTF8_BOM, 0, result, 0, UTF8_BOM.length);
        System.arraycopy(body, 0, result, UTF8_BOM.length, body.length);
        return result;
    }

    private String toCsvLine(SteraStore steraStore) {
        List<String> fields = new ArrayList<>();
        fields.add(token(steraStore.getTradeCode()));
        fields.add(token(steraStore.getTransitCompany()));
        fields.add(token(steraStore.getEdyId()));
        fields.add(token(steraStore.getDPointMerchantCode()));
        fields.add(token(steraStore.getDPointStoreCode()));
        fields.add(token(steraStore.getDPointBranchCode()));
        fields.add(token(steraStore.getBranchCode()));
        fields.add(token(steraStore.getMemberType()));
        fields.add(token(steraStore.getStoreName()));
        fields.add(token(steraStore.getStoreNameKana()));
        fields.add(token(steraStore.getStoreNameEn()));
        fields.add(token(steraStore.getStoreZip()));
        fields.add(token(steraStore.getStoreAddress()));
        fields.add(token(steraStore.getStoreAddressKana()));
        fields.add(token(steraStore.getStoreTel()));
        fields.add(token(steraStore.getEmail()));
        fields.add(token(steraStore.getLatitude()));
        fields.add(token(steraStore.getLongitude()));
        fields.add(token(steraStore.getBankName()));
        fields.add(token(steraStore.getBankCode()));
        fields.add(token(steraStore.getBranchName()));
        fields.add(token(steraStore.getBankBranchCode()));
        fields.add(token(steraStore.getAccountType()));
        fields.add(token(steraStore.getAccountNo()));
        fields.add(token(steraStore.getAccountHolderKana()));
        fields.add(token(steraStore.getJcbStatus()));
        fields.add(token(steraStore.getJcbStartDate()));
        fields.add(token(steraStore.getDPointStatus()));
        fields.add(token(steraStore.getDPointStartDate()));
        fields.add(token(steraStore.getRemarks()));
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
