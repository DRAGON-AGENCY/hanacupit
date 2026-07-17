package com.cupit.csv.writer;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.cupit.model.TerminalData;

/**
 * 端末データ CSV を m_terminal_data から書き出す。
 * ヘッダー列は{@link com.cupit.csv.importer.TerminalDataFileImporter}と同じ列・同じ並び順
 * （取込みの逆変換）とし、そのままアップロードし直せるようにする。
 * UTF-8（BOM付き）・CRLF・RFC4180準拠のクォート処理で出力する。
 */
@Component
public class TerminalDataCsvWriter {

    private static final DateTimeFormatter FMT_SLASH = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final String HEADER_LINE =
            "取引コード,申請区分,申込者区分,申込日／解約日,サービス開始希望日,サービス終了日,ブランド名・屋号（英語）,代表加盟店番号,VM基盤加盟店番号,端末識別番号,締め①,支払①"
            + ",締め②,支払②,精算サイクル,金融機関コード,金融機関名,金融機関名（カナ）,支店コード,支店名,支店名（カナ）,口座種別,口座番号,担当者姓,担当者名,担当者姓（カナ）"
            + ",担当者名（カナ）,加盟店種別,フランチャイズ店有無,PayPay「FC加盟店管理に関する特約」合意済,店舗数（申請数）,利用端末区分,企業名（英字）,業種業態（大分類）"
            + ",業種業態（小分類）,法人番号,代表者姓（英字）,代表者名（英字）,性別,代表者住所：都道府県（カナ）,代表者住所：市区町村（カナ）,代表者住所：町名（カナ）"
            + ",代表者住所：丁目・番・番地・号（カナ）,代表者住所：建物名・部屋番号（カナ）,訪問販売,特定継続的役務提供,電話勧誘販売,連鎖販売取引,業務提供誘引販売,前払い式取引の提供"
            + ",特商法違反・消契法敗訴歴有,FC店舗種別,代表店舗フラグ,店舗業種（大分類）,店舗業種（小分類）,古物商許可証番号,店舗名（英字）,地図掲載要否"
            + ",d払い・楽天Pay 地図掲載希望日,PayPay・auPay・ゆうちょPay・Alipay 地図掲載希望日,店舗画像掲載要否,店舗画像URL,店舗紹介"
            + ",楽天Pay 加盟店手数料率（税込）,LINE Pay 加盟店手数料率（税込）,PayPay 加盟店手数料率（税込）,d払い 加盟店手数料率（税込）"
            + ",auPay 加盟店手数料率（税込）,メルペイ 加盟店手数料率（税込）,ゆうちょPay 加盟店手数料率（税込）,AEONPay 加盟店手数料率（税込）,アトカラ"
            + ",加盟店手数料率（1回）MDR,加盟店手数料率（3回）MDR,加盟店手数料率（4回）MDR+分割手数料,加盟店手数料率（5回）,加盟店手数料率（6回）,加盟店手数料率（10回）"
            + ",加盟店手数料率（12回）,加盟店手数料率（15回）,加盟店手数料率（18回）,加盟店手数料率（20回）,加盟店手数料率（24回）,加盟店手数料率（30回）"
            + ",加盟店手数料率（36回）,Wesmo! 加盟店手数料率（非課税）,BankPay 加盟店手数料率（非課税）,Wechat 加盟店手数料率（非課税）"
            + ",Alipay 加盟店手数料率（非課税）,銀聯QR 加盟店手数料率（非課税）,変更点,SMCC担当部,SMCC担当者名,Smart Code 導入フラグ,MKP 導入フラグ";

    public byte[] writeCsv(List<TerminalData> records) {
        StringBuilder csv = new StringBuilder();
        csv.append(HEADER_LINE).append("\r\n");
        for (TerminalData terminalData : records) {
            csv.append(toCsvLine(terminalData));
        }
        byte[] body = csv.toString().getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[UTF8_BOM.length + body.length];
        System.arraycopy(UTF8_BOM, 0, result, 0, UTF8_BOM.length);
        System.arraycopy(body, 0, result, UTF8_BOM.length, body.length);
        return result;
    }

    private String toCsvLine(TerminalData terminalData) {
        List<String> fields = new ArrayList<>();
        fields.add(token(terminalData.getTradeCode()));
        fields.add(token(terminalData.getApplicationCategory()));
        fields.add(token(terminalData.getApplicantType()));
        fields.add(token(terminalData.getApplicationOrCancellationDate()));
        fields.add(token(terminalData.getServiceStartDesiredDate()));
        fields.add(token(terminalData.getServiceEndDate()));
        fields.add(token(terminalData.getBrandNameEnglish()));
        fields.add(token(terminalData.getRepresentativeMerchantNumber()));
        fields.add(token(terminalData.getVmMerchantNumber()));
        fields.add(token(terminalData.getTerminalId()));
        fields.add(token(terminalData.getClosingDate1()));
        fields.add(token(terminalData.getPaymentDate1()));
        fields.add(token(terminalData.getClosingDate2()));
        fields.add(token(terminalData.getPaymentDate2()));
        fields.add(token(terminalData.getSettlementCycle()));
        fields.add(token(terminalData.getBankCode()));
        fields.add(token(terminalData.getBankName()));
        fields.add(token(terminalData.getBankNameKana()));
        fields.add(token(terminalData.getBranchCode()));
        fields.add(token(terminalData.getBranchName()));
        fields.add(token(terminalData.getBranchNameKana()));
        fields.add(token(terminalData.getAccountType()));
        fields.add(token(terminalData.getAccountNumber()));
        fields.add(token(terminalData.getContactLastName()));
        fields.add(token(terminalData.getContactFirstName()));
        fields.add(token(terminalData.getContactLastNameKana()));
        fields.add(token(terminalData.getContactFirstNameKana()));
        fields.add(token(terminalData.getMerchantType()));
        fields.add(token(terminalData.getFranchiseFlag()));
        fields.add(token(terminalData.getPaypayFcAgreementFlag()));
        fields.add(token(terminalData.getStoreCountApplied()));
        fields.add(token(terminalData.getTerminalType()));
        fields.add(token(terminalData.getCorpNameEnglish()));
        fields.add(token(terminalData.getIndustryCategoryMajor()));
        fields.add(token(terminalData.getIndustryCategoryMinor()));
        fields.add(token(terminalData.getCorporateNumber()));
        fields.add(token(terminalData.getRepLastNameEnglish()));
        fields.add(token(terminalData.getRepFirstNameEnglish()));
        fields.add(token(terminalData.getGender()));
        fields.add(token(terminalData.getRepAddrPrefKana()));
        fields.add(token(terminalData.getRepAddrCityKana()));
        fields.add(token(terminalData.getRepAddrTownKana()));
        fields.add(token(terminalData.getRepAddrBlockKana()));
        fields.add(token(terminalData.getRepAddrBuildingKana()));
        fields.add(token(terminalData.getDoorToDoorSalesFlag()));
        fields.add(token(terminalData.getContinuousServiceFlag()));
        fields.add(token(terminalData.getTelemarketingSalesFlag()));
        fields.add(token(terminalData.getChainSalesFlag()));
        fields.add(token(terminalData.getBusinessOpportunitySalesFlag()));
        fields.add(token(terminalData.getPrepaidTransactionFlag()));
        fields.add(token(terminalData.getLegalViolationHistoryFlag()));
        fields.add(token(terminalData.getFcStoreType()));
        fields.add(token(terminalData.getRepresentativeStoreFlag()));
        fields.add(token(terminalData.getStoreIndustryMajor()));
        fields.add(token(terminalData.getStoreIndustryMinor()));
        fields.add(token(terminalData.getSecondhandDealerLicenseNumber()));
        fields.add(token(terminalData.getStoreNameEnglish()));
        fields.add(token(terminalData.getMapListingFlag()));
        fields.add(token(terminalData.getMapListingDesiredDateDpayRakuten()));
        fields.add(token(terminalData.getMapListingDesiredDatePaypayAupay()));
        fields.add(token(terminalData.getStoreImageListingFlag()));
        fields.add(token(terminalData.getStoreImageUrl()));
        fields.add(token(terminalData.getStoreIntroduction()));
        fields.add(token(terminalData.getFeeRateRakutenPay()));
        fields.add(token(terminalData.getFeeRateLinePay()));
        fields.add(token(terminalData.getFeeRatePaypay()));
        fields.add(token(terminalData.getFeeRateDBarai()));
        fields.add(token(terminalData.getFeeRateAuPay()));
        fields.add(token(terminalData.getFeeRateMerpay()));
        fields.add(token(terminalData.getFeeRateYuchoPay()));
        fields.add(token(terminalData.getFeeRateAeonPay()));
        fields.add(token(terminalData.getAtokaraRate()));
        fields.add(token(terminalData.getFeeRateMdr1()));
        fields.add(token(terminalData.getFeeRateMdr3()));
        fields.add(token(terminalData.getFeeRateMdr4()));
        fields.add(token(terminalData.getFeeRateInstallment5()));
        fields.add(token(terminalData.getFeeRateInstallment6()));
        fields.add(token(terminalData.getFeeRateInstallment10()));
        fields.add(token(terminalData.getFeeRateInstallment12()));
        fields.add(token(terminalData.getFeeRateInstallment15()));
        fields.add(token(terminalData.getFeeRateInstallment18()));
        fields.add(token(terminalData.getFeeRateInstallment20()));
        fields.add(token(terminalData.getFeeRateInstallment24()));
        fields.add(token(terminalData.getFeeRateInstallment30()));
        fields.add(token(terminalData.getFeeRateInstallment36()));
        fields.add(token(terminalData.getFeeRateWesmo()));
        fields.add(token(terminalData.getFeeRateBankPay()));
        fields.add(token(terminalData.getFeeRateWechat()));
        fields.add(token(terminalData.getFeeRateAlipay()));
        fields.add(token(terminalData.getFeeRateUnionpayQr()));
        fields.add(token(terminalData.getChangeNotes()));
        fields.add(token(terminalData.getSmccDepartment()));
        fields.add(token(terminalData.getSmccContactName()));
        fields.add(token(terminalData.getSmartCodeFlag()));
        fields.add(token(terminalData.getMkpFlag()));
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
