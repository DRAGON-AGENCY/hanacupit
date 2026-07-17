package com.cupit.csv.writer;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.cupit.model.ShopData;

/**
 * 店舗データ CSV を m_shop_data から書き出す。
 * ヘッダー列は{@link com.cupit.csv.importer.ShopDataFileImporter}と同じ列・同じ並び順
 * （取込みの逆変換）とし、そのままアップロードし直せるようにする。
 * UTF-8（BOM付き）・CRLF・RFC4180準拠のクォート処理で出力する。
 */
@Component
public class ShopDataCsvWriter {

    private static final DateTimeFormatter FMT_SLASH = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final String HEADER_LINE =
            "取引コード,新規・変更・解約フラグ,店舗名アルファベット,代表者住所（カナ）,JCB加盟店番号,法人番号,訪問販売（有・無）,電話勧誘販売（有・無）,連鎖販売取引（有・無）"
            + ",業務提供誘引販売（有・無）,特定継続的役務（有・無）,カード情報保持状況,PCIDSS準拠状況,非保持化予定年月,PCIDSS準拠予定年月,決済端末IC化実施状況"
            + ",決済端末IC化実施予定年月,包括元と各アクワイアラの固有キー項目,stera端末識別番号,連携日,既存契約（有／無）,分類,契約元,ギフト契約（有／無）,Edy契約（有／無）"
            + ",解約意思確認,（解約）手続状況";

    public byte[] writeCsv(List<ShopData> records) {
        StringBuilder csv = new StringBuilder();
        csv.append(HEADER_LINE).append("\r\n");
        for (ShopData shopData : records) {
            csv.append(toCsvLine(shopData));
        }
        byte[] body = csv.toString().getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[UTF8_BOM.length + body.length];
        System.arraycopy(UTF8_BOM, 0, result, 0, UTF8_BOM.length);
        System.arraycopy(body, 0, result, UTF8_BOM.length, body.length);
        return result;
    }

    private String toCsvLine(ShopData shopData) {
        List<String> fields = new ArrayList<>();
        fields.add(token(shopData.getTradeCode()));
        fields.add(token(shopData.getApplicationTypeFlag()));
        fields.add(token(shopData.getStoreNameAlphabet()));
        fields.add(token(shopData.getRepAddressKana()));
        fields.add(token(shopData.getJcbMerchantNumber()));
        fields.add(token(shopData.getCorporateNumber()));
        fields.add(token(shopData.getDoorToDoorSalesFlag()));
        fields.add(token(shopData.getTelemarketingSalesFlag()));
        fields.add(token(shopData.getChainSalesFlag()));
        fields.add(token(shopData.getBusinessOpportunitySalesFlag()));
        fields.add(token(shopData.getContinuousServiceFlag()));
        fields.add(token(shopData.getCardDataRetentionStatus()));
        fields.add(token(shopData.getPciDssComplianceStatus()));
        fields.add(token(shopData.getNonRetentionPlannedMonth()));
        fields.add(token(shopData.getPciDssCompliancePlannedMonth()));
        fields.add(token(shopData.getTerminalIcStatus()));
        fields.add(token(shopData.getTerminalIcPlannedMonth()));
        fields.add(token(shopData.getAcquirerUniqueKey()));
        fields.add(token(shopData.getSteraTerminalId()));
        fields.add(token(shopData.getLinkageDate()));
        fields.add(token(shopData.getExistingContractFlag()));
        fields.add(token(shopData.getClassification()));
        fields.add(token(shopData.getContractSource()));
        fields.add(token(shopData.getGiftContractFlag()));
        fields.add(token(shopData.getEdyContractFlag()));
        fields.add(token(shopData.getCancellationConfirmation()));
        fields.add(token(shopData.getCancellationProcessStatus()));
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
