package com.cupit.csv.writer;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.cupit.model.MerchantNumberData;

/**
 * 加盟店番号データ CSV を m_merchant_number_data から書き出す。
 * ヘッダー列は{@link com.cupit.csv.importer.MerchantNumberDataFileImporter}と同じ列・同じ並び順
 * （取込みの逆変換）とし、そのままアップロードし直せるようにする。
 * UTF-8（BOM付き）・CRLF・RFC4180準拠のクォート処理で出力する。
 */
@Component
public class MerchantNumberDataCsvWriter {

    private static final DateTimeFormatter FMT_SLASH = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final String HEADER_LINE =
            "取引コード,端末台数,回線 有線／無線,店舗連絡先 担当者名,店舗担当者 連絡先,POS接続有無,POSメーカー名,POSベンダー 担当者名,POSベンダー 連絡先電話番号"
            + ",dポイント機能 搭載有無,dポイント 加盟店コード,dポイント 店舗コード,dポイント 支部コード,VISA Master 加盟店番号,nanaco 加盟店番号,iD 加盟店番号"
            + ",交通系 加盟店番号,銀聯 加盟店番号,WAON 加盟店番号,Edy 加盟店番号,NFC 加盟店番号,交通系社局,EdyID,stera 端末番号,JCB 接続有無"
            + ",SmartCode 接続有無";

    public byte[] writeCsv(List<MerchantNumberData> records) {
        StringBuilder csv = new StringBuilder();
        csv.append(HEADER_LINE).append("\r\n");
        for (MerchantNumberData merchantNumberData : records) {
            csv.append(toCsvLine(merchantNumberData));
        }
        byte[] body = csv.toString().getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[UTF8_BOM.length + body.length];
        System.arraycopy(UTF8_BOM, 0, result, 0, UTF8_BOM.length);
        System.arraycopy(body, 0, result, UTF8_BOM.length, body.length);
        return result;
    }

    private String toCsvLine(MerchantNumberData merchantNumberData) {
        List<String> fields = new ArrayList<>();
        fields.add(token(merchantNumberData.getTradeCode()));
        fields.add(token(merchantNumberData.getTerminalCount()));
        fields.add(token(merchantNumberData.getLineType()));
        fields.add(token(merchantNumberData.getStoreContactName()));
        fields.add(token(merchantNumberData.getStoreContactTel()));
        fields.add(token(merchantNumberData.getPosConnectionFlag()));
        fields.add(token(merchantNumberData.getPosMakerName()));
        fields.add(token(merchantNumberData.getPosVendorContactName()));
        fields.add(token(merchantNumberData.getPosVendorContactTel()));
        fields.add(token(merchantNumberData.getDPointEnabledFlag()));
        fields.add(token(merchantNumberData.getDPointMerchantCode()));
        fields.add(token(merchantNumberData.getDPointStoreCode()));
        fields.add(token(merchantNumberData.getDPointBranchCode()));
        fields.add(token(merchantNumberData.getVisaMasterMerchantNumber()));
        fields.add(token(merchantNumberData.getNanacoMerchantNumber()));
        fields.add(token(merchantNumberData.getIdMerchantNumber()));
        fields.add(token(merchantNumberData.getTransitMerchantNumber()));
        fields.add(token(merchantNumberData.getUnionpayMerchantNumber()));
        fields.add(token(merchantNumberData.getWaonMerchantNumber()));
        fields.add(token(merchantNumberData.getEdyMerchantNumber()));
        fields.add(token(merchantNumberData.getNfcMerchantNumber()));
        fields.add(token(merchantNumberData.getTransitOperator()));
        fields.add(token(merchantNumberData.getEdyId()));
        fields.add(token(merchantNumberData.getSteraTerminalNumber()));
        fields.add(token(merchantNumberData.getJcbConnectionFlag()));
        fields.add(token(merchantNumberData.getSmartCodeConnectionFlag()));
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
