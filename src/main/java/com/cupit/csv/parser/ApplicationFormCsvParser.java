package com.cupit.csv.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.ApplicationFormColumn;
import com.cupit.csv.CsvValidationError;
import com.cupit.model.ApplicationFormInput;

/**
 * 各決済会社所定申込フォーム作成のINPUT CSV（{@link ApplicationFormColumn}で定義した列数）を解析し、
 * {@link ApplicationFormInput}のリストとして返す。DBへの永続化は行わない
 * （アップロードされたCSVをその場でExcel生成に使う一時的な変換処理のため、
 * バッチ登録を前提とする{@code AbstractFileImporter}は継承せず、
 * 必要なCSV解析ユーティリティのみを自前で持つ）。
 * 文字コード: UTF-8 BOM付きは自動検出、なければMS932。
 * ヘッダー行: 1行目は内容によらず常にヘッダー行として扱いスキップする（列名はチェックしない）。
 * 列数不足の行、取引コード未入力の行、日付・数値変換に失敗した項目を含む行はその行だけを
 * スキップし、ファイルの最後まで処理を継続する。CSV内で取引コードが重複する場合はどちらの
 * 行が正しいか判断できないため、該当する取引コードの行を（先着1件目も含めて）全てスキップ
 * する。取引コード以外の全項目は任意とする。
 */
@Component
public class ApplicationFormCsvParser {

    private static final int EXPECTED_COLUMN_COUNT = ApplicationFormColumn.values().length;
    private static final int IDX_TRADE_CODE = ApplicationFormColumn.TRADE_CODE.ordinal();
    private static final DateTimeFormatter FMT_YYYY_SLASH_MM_SLASH_DD =
            DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /**
     * CSVを解析し、正常な行のみを{@link ApplicationFormInput}として返す。
     *
     * @param file アップロードファイル
     * @return 解析結果（正常行のリストと全エラー）
     * @throws IOException ファイル読み込みエラー
     */
    public ParseResult parse(MultipartFile file) throws IOException {
        List<ApplicationFormInput> records = new ArrayList<>();
        List<CsvValidationError> errors = new ArrayList<>();
        int rowNum = 1;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), detectCharset(file)))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                return new ParseResult(records, errors, 0);
            }

            List<String> dataLines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                dataLines.add(stripCr(line));
            }

            Map<String, Integer> tradeCodeCounts = countTradeCodes(dataLines);

            for (String dataLine : dataLines) {
                rowNum++;
                if (dataLine.isBlank()) {
                    continue;
                }
                List<String> fields = parseLine(dataLine);
                if (fields.size() != EXPECTED_COLUMN_COUNT) {
                    errors.add(new CsvValidationError(rowNum, "取引コード",
                            "取引コード「" + fields.get(IDX_TRADE_CODE).trim() + "」: 列数が不正です。"
                            + "期待: " + EXPECTED_COLUMN_COUNT + "列、実際: " + fields.size() + "列"));
                    continue;
                }
                parseDataRow(fields, rowNum, records, errors, tradeCodeCounts);
            }
        }

        int totalDataRows = rowNum - 1;
        return new ParseResult(records, errors, totalDataRows);
    }

    /**
     * データ行を1回走査し、取引コードごとの出現回数を数える。列数が不正な行は
     * 取引コード自体を安全に取得できない可能性があるため集計対象から除く
     * （その行は別途「列数が不正です」エラーになる）。
     */
    private Map<String, Integer> countTradeCodes(List<String> dataLines) {
        Map<String, Integer> counts = new HashMap<>();
        for (String dataLine : dataLines) {
            if (dataLine.isBlank()) {
                continue;
            }
            List<String> fields = parseLine(dataLine);
            if (fields.size() != EXPECTED_COLUMN_COUNT) {
                continue;
            }
            String tradeCode = trim(fields.get(IDX_TRADE_CODE));
            if (!tradeCode.isEmpty()) {
                counts.merge(tradeCode, 1, Integer::sum);
            }
        }
        return counts;
    }

    private void parseDataRow(
            List<String> fields, int rowNum, List<ApplicationFormInput> records,
            List<CsvValidationError> errors, Map<String, Integer> tradeCodeCounts) {
        String tradeCode = trim(fields.get(IDX_TRADE_CODE));
        if (tradeCode.isEmpty()) {
            errors.add(new CsvValidationError(rowNum, "取引コード", "取引コードは必須です。"));
            return;
        }
        if (tradeCodeCounts.getOrDefault(tradeCode, 0) > 1) {
            errors.add(new CsvValidationError(rowNum, "取引コード",
                    "取引コード「" + tradeCode + "」がCSV内で重複しています。"));
            return;
        }

        int errorCountBeforeRow = errors.size();
        ApplicationFormInput record = new ApplicationFormInput();
        record.setRowNumber(rowNum);
        record.setRecordNumber(trim(fields.get(ApplicationFormColumn.RECORD_NUMBER.ordinal())));
        record.setReaderSerialNo(trim(fields.get(ApplicationFormColumn.READER_SERIAL_NO.ordinal())));
        record.setTerminalId(trim(fields.get(ApplicationFormColumn.TERMINAL_ID.ordinal())));
        record.setTradeCode(trim(fields.get(ApplicationFormColumn.TRADE_CODE.ordinal())));
        record.setPaygateContinuationStatus(trim(fields.get(ApplicationFormColumn.PAYGATE_CONTINUATION_STATUS.ordinal())));
        record.setJcbApplicationClassification(trim(fields.get(ApplicationFormColumn.JCB_APPLICATION_CLASSIFICATION.ordinal())));
        record.setSmccApplicationClassification(trim(fields.get(ApplicationFormColumn.SMCC_APPLICATION_CLASSIFICATION.ordinal())));
        record.setMemberType(trim(fields.get(ApplicationFormColumn.MEMBER_TYPE.ordinal())));
        record.setStoreName(trim(fields.get(ApplicationFormColumn.STORE_NAME.ordinal())));
        record.setStoreNameKana(trim(fields.get(ApplicationFormColumn.STORE_NAME_KANA.ordinal())));
        record.setStoreNameAlphabet(trim(fields.get(ApplicationFormColumn.STORE_NAME_ALPHABET.ordinal())));
        record.setStoreZip(trim(fields.get(ApplicationFormColumn.STORE_ZIP.ordinal())));
        record.setStoreAddress(trim(fields.get(ApplicationFormColumn.STORE_ADDRESS.ordinal())));
        record.setStoreAddressKana(trim(fields.get(ApplicationFormColumn.STORE_ADDRESS_KANA.ordinal())));
        record.setStoreTel(trim(fields.get(ApplicationFormColumn.STORE_TEL.ordinal())));
        record.setStoreEmail(trim(fields.get(ApplicationFormColumn.STORE_EMAIL.ordinal())));
        record.setStoreHomepageUrl(trim(fields.get(ApplicationFormColumn.STORE_HOMEPAGE_URL.ordinal())));
        record.setIndividualOrCorporateType(trim(fields.get(ApplicationFormColumn.INDIVIDUAL_OR_CORPORATE_TYPE.ordinal())));
        record.setCorpNumber(trim(fields.get(ApplicationFormColumn.CORP_NUMBER.ordinal())));
        record.setCorpName(trim(fields.get(ApplicationFormColumn.CORP_NAME.ordinal())));
        record.setCorpNameKana(trim(fields.get(ApplicationFormColumn.CORP_NAME_KANA.ordinal())));
        record.setCorpNameAlphabet(trim(fields.get(ApplicationFormColumn.CORP_NAME_ALPHABET.ordinal())));
        record.setCorpZip(trim(fields.get(ApplicationFormColumn.CORP_ZIP.ordinal())));
        record.setCorpAddress(trim(fields.get(ApplicationFormColumn.CORP_ADDRESS.ordinal())));
        record.setCorpAddressKana(trim(fields.get(ApplicationFormColumn.CORP_ADDRESS_KANA.ordinal())));
        record.setCorpTel(trim(fields.get(ApplicationFormColumn.CORP_TEL.ordinal())));
        record.setEstablishmentDate(trim(fields.get(ApplicationFormColumn.ESTABLISHMENT_DATE.ordinal())));
        record.setRepFullName(trim(fields.get(ApplicationFormColumn.REP_FULL_NAME.ordinal())));
        record.setRepFullNameKana(trim(fields.get(ApplicationFormColumn.REP_FULL_NAME_KANA.ordinal())));
        record.setRepZip(trim(fields.get(ApplicationFormColumn.REP_ZIP.ordinal())));
        record.setRepAddress(trim(fields.get(ApplicationFormColumn.REP_ADDRESS.ordinal())));
        record.setRepAddressKana(trim(fields.get(ApplicationFormColumn.REP_ADDRESS_KANA.ordinal())));
        record.setRepTel(trim(fields.get(ApplicationFormColumn.REP_TEL.ordinal())));
        record.setRepBirthDate(trim(fields.get(ApplicationFormColumn.REP_BIRTH_DATE.ordinal())));
        record.setBankName(trim(fields.get(ApplicationFormColumn.BANK_NAME.ordinal())));
        record.setBranchName(trim(fields.get(ApplicationFormColumn.BRANCH_NAME.ordinal())));
        record.setAccountNumber(trim(fields.get(ApplicationFormColumn.ACCOUNT_NUMBER.ordinal())));
        record.setAccountHolderKana(trim(fields.get(ApplicationFormColumn.ACCOUNT_HOLDER_KANA.ordinal())));
        record.setJcbUsageFlag(trim(fields.get(ApplicationFormColumn.JCB_USAGE_FLAG.ordinal())));
        record.setAnnualSales(trim(fields.get(ApplicationFormColumn.ANNUAL_SALES.ordinal())));
        record.setAccountType(trim(fields.get(ApplicationFormColumn.ACCOUNT_TYPE.ordinal())));
        record.setStatus(trim(fields.get(ApplicationFormColumn.STATUS.ordinal())));
        record.setWorkerName(trim(fields.get(ApplicationFormColumn.WORKER_NAME.ordinal())));
        record.setContactFullName(trim(fields.get(ApplicationFormColumn.CONTACT_FULL_NAME.ordinal())));
        record.setContactTel(trim(fields.get(ApplicationFormColumn.CONTACT_TEL.ordinal())));
        record.setStoreAddressPref(trim(fields.get(ApplicationFormColumn.STORE_ADDRESS_PREF.ordinal())));
        record.setContactLastName(trim(fields.get(ApplicationFormColumn.CONTACT_LAST_NAME.ordinal())));
        record.setContactFirstName(trim(fields.get(ApplicationFormColumn.CONTACT_FIRST_NAME.ordinal())));
        record.setCorpAddressPref(trim(fields.get(ApplicationFormColumn.CORP_ADDRESS_PREF.ordinal())));
        record.setRepLastName(trim(fields.get(ApplicationFormColumn.REP_LAST_NAME.ordinal())));
        record.setRepFirstName(trim(fields.get(ApplicationFormColumn.REP_FIRST_NAME.ordinal())));
        record.setRepLastNameKana(trim(fields.get(ApplicationFormColumn.REP_LAST_NAME_KANA.ordinal())));
        record.setRepFirstNameKana(trim(fields.get(ApplicationFormColumn.REP_FIRST_NAME_KANA.ordinal())));
        record.setRepAddressPref(trim(fields.get(ApplicationFormColumn.REP_ADDRESS_PREF.ordinal())));
        record.setDPointUsageFlag(trim(fields.get(ApplicationFormColumn.D_POINT_USAGE_FLAG.ordinal())));
        record.setRepLastNameAlphabet(trim(fields.get(ApplicationFormColumn.REP_LAST_NAME_ALPHABET.ordinal())));
        record.setRepFirstNameAlphabet(trim(fields.get(ApplicationFormColumn.REP_FIRST_NAME_ALPHABET.ordinal())));
        record.setRepGender(trim(fields.get(ApplicationFormColumn.REP_GENDER.ordinal())));
        record.setDoorToDoorSalesFlag(trim(fields.get(ApplicationFormColumn.DOOR_TO_DOOR_SALES_FLAG.ordinal())));
        record.setTelemarketingSalesFlag(trim(fields.get(ApplicationFormColumn.TELEMARKETING_SALES_FLAG.ordinal())));
        record.setChainSalesFlag(trim(fields.get(ApplicationFormColumn.CHAIN_SALES_FLAG.ordinal())));
        record.setBusinessOpportunitySalesFlag(trim(fields.get(ApplicationFormColumn.BUSINESS_OPPORTUNITY_SALES_FLAG.ordinal())));
        record.setContinuousServiceFlag(trim(fields.get(ApplicationFormColumn.CONTINUOUS_SERVICE_FLAG.ordinal())));
        record.setPrepaidTransactionFlag(trim(fields.get(ApplicationFormColumn.PREPAID_TRANSACTION_FLAG.ordinal())));
        record.setLegalViolationHistoryFlag(trim(fields.get(ApplicationFormColumn.LEGAL_VIOLATION_HISTORY_FLAG.ordinal())));
        record.setCardDataRetentionStatus(trim(fields.get(ApplicationFormColumn.CARD_DATA_RETENTION_STATUS.ordinal())));
        record.setPciDssComplianceStatus(trim(fields.get(ApplicationFormColumn.PCI_DSS_COMPLIANCE_STATUS.ordinal())));
        record.setNonRetentionPlannedMonth(trim(fields.get(ApplicationFormColumn.NON_RETENTION_PLANNED_MONTH.ordinal())));
        record.setPciDssCompliancePlannedMonth(trim(fields.get(ApplicationFormColumn.PCI_DSS_COMPLIANCE_PLANNED_MONTH.ordinal())));
        record.setTerminalIcStatus(trim(fields.get(ApplicationFormColumn.TERMINAL_IC_STATUS.ordinal())));
        record.setTerminalIcPlannedMonth(trim(fields.get(ApplicationFormColumn.TERMINAL_IC_PLANNED_MONTH.ordinal())));
        record.setAcquirerUniqueKey(trim(fields.get(ApplicationFormColumn.ACQUIRER_UNIQUE_KEY.ordinal())));
        record.setClassification(trim(fields.get(ApplicationFormColumn.CLASSIFICATION.ordinal())));
        record.setContractSource(trim(fields.get(ApplicationFormColumn.CONTRACT_SOURCE.ordinal())));
        record.setGiftContractFlag(trim(fields.get(ApplicationFormColumn.GIFT_CONTRACT_FLAG.ordinal())));
        record.setEdyContractFlag(trim(fields.get(ApplicationFormColumn.EDY_CONTRACT_FLAG.ordinal())));
        record.setApplicantType(trim(fields.get(ApplicationFormColumn.APPLICANT_TYPE.ordinal())));
        record.setServiceStartDesiredDate(parseDateSlashChecked(fields.get(ApplicationFormColumn.SERVICE_START_DESIRED_DATE.ordinal()), rowNum, "サービス開始希望日", errors));
        record.setServiceEndDate(parseDateSlashChecked(fields.get(ApplicationFormColumn.SERVICE_END_DATE.ordinal()), rowNum, "サービス終了日", errors));
        record.setVmMerchantNumber(trim(fields.get(ApplicationFormColumn.VM_MERCHANT_NUMBER.ordinal())));
        record.setClosingDate1(trim(fields.get(ApplicationFormColumn.CLOSING_DATE1.ordinal())));
        record.setPaymentDate1(trim(fields.get(ApplicationFormColumn.PAYMENT_DATE1.ordinal())));
        record.setClosingDate2(trim(fields.get(ApplicationFormColumn.CLOSING_DATE2.ordinal())));
        record.setPaymentDate2(trim(fields.get(ApplicationFormColumn.PAYMENT_DATE2.ordinal())));
        record.setSettlementCycle(trim(fields.get(ApplicationFormColumn.SETTLEMENT_CYCLE.ordinal())));
        record.setBankCode(trim(fields.get(ApplicationFormColumn.BANK_CODE.ordinal())));
        record.setBankNameKana(trim(fields.get(ApplicationFormColumn.BANK_NAME_KANA.ordinal())));
        record.setBranchCode(trim(fields.get(ApplicationFormColumn.BRANCH_CODE.ordinal())));
        record.setBranchNameKana(trim(fields.get(ApplicationFormColumn.BRANCH_NAME_KANA.ordinal())));
        record.setContactLastNameKana(trim(fields.get(ApplicationFormColumn.CONTACT_LAST_NAME_KANA.ordinal())));
        record.setContactFirstNameKana(trim(fields.get(ApplicationFormColumn.CONTACT_FIRST_NAME_KANA.ordinal())));
        record.setMerchantType(trim(fields.get(ApplicationFormColumn.MERCHANT_TYPE.ordinal())));
        record.setFranchiseFlag(trim(fields.get(ApplicationFormColumn.FRANCHISE_FLAG.ordinal())));
        record.setPaypayFcAgreementFlag(trim(fields.get(ApplicationFormColumn.PAYPAY_FC_AGREEMENT_FLAG.ordinal())));
        record.setTerminalType(trim(fields.get(ApplicationFormColumn.TERMINAL_TYPE.ordinal())));
        record.setIndustryCategoryMajor(trim(fields.get(ApplicationFormColumn.INDUSTRY_CATEGORY_MAJOR.ordinal())));
        record.setIndustryCategoryMinor(trim(fields.get(ApplicationFormColumn.INDUSTRY_CATEGORY_MINOR.ordinal())));
        record.setStoreIndustryMajor(trim(fields.get(ApplicationFormColumn.STORE_INDUSTRY_MAJOR.ordinal())));
        record.setStoreIndustryMinor(trim(fields.get(ApplicationFormColumn.STORE_INDUSTRY_MINOR.ordinal())));
        record.setRepAddrPrefKana(trim(fields.get(ApplicationFormColumn.REP_ADDR_PREF_KANA.ordinal())));
        record.setRepAddrCityKana(trim(fields.get(ApplicationFormColumn.REP_ADDR_CITY_KANA.ordinal())));
        record.setRepAddrTownKana(trim(fields.get(ApplicationFormColumn.REP_ADDR_TOWN_KANA.ordinal())));
        record.setRepAddrBlockKana(trim(fields.get(ApplicationFormColumn.REP_ADDR_BLOCK_KANA.ordinal())));
        record.setRepAddrBuildingKana(trim(fields.get(ApplicationFormColumn.REP_ADDR_BUILDING_KANA.ordinal())));
        record.setFcStoreType(trim(fields.get(ApplicationFormColumn.FC_STORE_TYPE.ordinal())));
        record.setSecondhandDealerLicenseNumber(trim(fields.get(ApplicationFormColumn.SECONDHAND_DEALER_LICENSE_NUMBER.ordinal())));
        record.setMapListingFlag(trim(fields.get(ApplicationFormColumn.MAP_LISTING_FLAG.ordinal())));
        record.setMapListingDesiredDateDpayRakuten(
                parseDateSlashChecked(fields.get(ApplicationFormColumn.MAP_LISTING_DESIRED_DATE_DPAY_RAKUTEN.ordinal()), rowNum, "d払い・楽天Pay 地図掲載希望日", errors));
        record.setMapListingDesiredDatePaypayAupay(
                parseDateSlashChecked(fields.get(ApplicationFormColumn.MAP_LISTING_DESIRED_DATE_PAYPAY_AUPAY.ordinal()), rowNum, "PayPay・auPay・ゆうちょPay・Alipay 地図掲載希望日", errors));
        record.setStoreImageListingFlag(trim(fields.get(ApplicationFormColumn.STORE_IMAGE_LISTING_FLAG.ordinal())));
        record.setStoreImageUrl(trim(fields.get(ApplicationFormColumn.STORE_IMAGE_URL.ordinal())));
        record.setStoreIntroduction(trim(fields.get(ApplicationFormColumn.STORE_INTRODUCTION.ordinal())));
        record.setFeeRateRakutenPay(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_RAKUTEN_PAY.ordinal()), rowNum, "楽天Pay 加盟店手数料率（税込）", errors));
        record.setFeeRateLinePay(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_LINE_PAY.ordinal()), rowNum, "LINE Pay 加盟店手数料率（税込）", errors));
        record.setFeeRatePaypay(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_PAYPAY.ordinal()), rowNum, "PayPay 加盟店手数料率（税込）", errors));
        record.setFeeRateDBarai(parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_D_BARAI.ordinal()), rowNum, "d払い 加盟店手数料率（税込）", errors));
        record.setFeeRateAuPay(parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_AU_PAY.ordinal()), rowNum, "auPay 加盟店手数料率（税込）", errors));
        record.setFeeRateMerpay(parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_MERPAY.ordinal()), rowNum, "メルペイ 加盟店手数料率（税込）", errors));
        record.setFeeRateYuchoPay(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_YUCHO_PAY.ordinal()), rowNum, "ゆうちょPay 加盟店手数料率（税込）", errors));
        record.setFeeRateAeonPay(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_AEON_PAY.ordinal()), rowNum, "AEONPay 加盟店手数料率（税込）", errors));
        record.setAtokaraRate(parseBigDecimalChecked(fields.get(ApplicationFormColumn.ATOKARA_RATE.ordinal()), rowNum, "アトカラ", errors));
        record.setFeeRateMdr1(parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_MDR1.ordinal()), rowNum, "加盟店手数料率（1回）MDR", errors));
        record.setFeeRateMdr3(parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_MDR3.ordinal()), rowNum, "加盟店手数料率（3回）MDR", errors));
        record.setFeeRateMdr4(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_MDR4.ordinal()), rowNum, "加盟店手数料率（4回）MDR+分割手数料", errors));
        record.setFeeRateInstallment5(parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_INSTALLMENT5.ordinal()), rowNum, "加盟店手数料率（5回）", errors));
        record.setFeeRateInstallment6(parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_INSTALLMENT6.ordinal()), rowNum, "加盟店手数料率（6回）", errors));
        record.setFeeRateInstallment10(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_INSTALLMENT10.ordinal()), rowNum, "加盟店手数料率（10回）", errors));
        record.setFeeRateInstallment12(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_INSTALLMENT12.ordinal()), rowNum, "加盟店手数料率（12回）", errors));
        record.setFeeRateInstallment15(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_INSTALLMENT15.ordinal()), rowNum, "加盟店手数料率（15回）", errors));
        record.setFeeRateInstallment18(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_INSTALLMENT18.ordinal()), rowNum, "加盟店手数料率（18回）", errors));
        record.setFeeRateInstallment20(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_INSTALLMENT20.ordinal()), rowNum, "加盟店手数料率（20回）", errors));
        record.setFeeRateInstallment24(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_INSTALLMENT24.ordinal()), rowNum, "加盟店手数料率（24回）", errors));
        record.setFeeRateInstallment30(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_INSTALLMENT30.ordinal()), rowNum, "加盟店手数料率（30回）", errors));
        record.setFeeRateInstallment36(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_INSTALLMENT36.ordinal()), rowNum, "加盟店手数料率（36回）", errors));
        record.setFeeRateWesmo(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_WESMO.ordinal()), rowNum, "Wesmo! 加盟店手数料率（非課税）", errors));
        record.setFeeRateBankPay(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_BANK_PAY.ordinal()), rowNum, "BankPay 加盟店手数料率（非課税）", errors));
        record.setFeeRateWechat(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_WECHAT.ordinal()), rowNum, "Wechat 加盟店手数料率（非課税）", errors));
        record.setFeeRateAlipay(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_ALIPAY.ordinal()), rowNum, "Alipay 加盟店手数料率（非課税）", errors));
        record.setFeeRateUnionpayQr(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_UNIONPAY_QR.ordinal()), rowNum, "銀聯QR 加盟店手数料率（非課税）", errors));
        record.setChangeNotes(trim(fields.get(ApplicationFormColumn.CHANGE_NOTES.ordinal())));
        record.setSmccDepartment(trim(fields.get(ApplicationFormColumn.SMCC_DEPARTMENT.ordinal())));
        record.setSmccContactName(trim(fields.get(ApplicationFormColumn.SMCC_CONTACT_NAME.ordinal())));
        record.setSmartCodeFlag(trim(fields.get(ApplicationFormColumn.SMART_CODE_FLAG.ordinal())));
        record.setMkpFlag(trim(fields.get(ApplicationFormColumn.MKP_FLAG.ordinal())));
        record.setTerminalCount(parseIntegerChecked(fields.get(ApplicationFormColumn.TERMINAL_COUNT.ordinal()), rowNum, "端末台数", errors));
        record.setLineType(trim(fields.get(ApplicationFormColumn.LINE_TYPE.ordinal())));
        record.setPosConnectionFlag(trim(fields.get(ApplicationFormColumn.POS_CONNECTION_FLAG.ordinal())));
        record.setPosMakerName(trim(fields.get(ApplicationFormColumn.POS_MAKER_NAME.ordinal())));
        record.setPosVendorContactName(trim(fields.get(ApplicationFormColumn.POS_VENDOR_CONTACT_NAME.ordinal())));
        record.setPosVendorContactTel(trim(fields.get(ApplicationFormColumn.POS_VENDOR_CONTACT_TEL.ordinal())));
        record.setSmartCodeConnectionFlag(trim(fields.get(ApplicationFormColumn.SMART_CODE_CONNECTION_FLAG.ordinal())));
        record.setDPointMerchantCode(trim(fields.get(ApplicationFormColumn.D_POINT_MERCHANT_CODE.ordinal())));
        record.setDPointStoreCode(trim(fields.get(ApplicationFormColumn.D_POINT_STORE_CODE.ordinal())));
        record.setDPointBranchCode(trim(fields.get(ApplicationFormColumn.D_POINT_BRANCH_CODE.ordinal())));
        record.setVisaMasterMerchantNumber(trim(fields.get(ApplicationFormColumn.VISA_MASTER_MERCHANT_NUMBER.ordinal())));
        record.setNanacoMerchantNumber(trim(fields.get(ApplicationFormColumn.NANACO_MERCHANT_NUMBER.ordinal())));
        record.setIdMerchantNumber(trim(fields.get(ApplicationFormColumn.ID_MERCHANT_NUMBER.ordinal())));
        record.setTransitMerchantNumber(trim(fields.get(ApplicationFormColumn.TRANSIT_MERCHANT_NUMBER.ordinal())));
        record.setUnionpayMerchantNumber(trim(fields.get(ApplicationFormColumn.UNIONPAY_MERCHANT_NUMBER.ordinal())));
        record.setWaonMerchantNumber(trim(fields.get(ApplicationFormColumn.WAON_MERCHANT_NUMBER.ordinal())));
        record.setEdyMerchantNumber(trim(fields.get(ApplicationFormColumn.EDY_MERCHANT_NUMBER.ordinal())));
        record.setNfcMerchantNumber(trim(fields.get(ApplicationFormColumn.NFC_MERCHANT_NUMBER.ordinal())));
        record.setTransitOperator(trim(fields.get(ApplicationFormColumn.TRANSIT_OPERATOR.ordinal())));
        record.setEdyId(trim(fields.get(ApplicationFormColumn.EDY_ID.ordinal())));
        record.setSteraTerminalNumber1(trim(fields.get(ApplicationFormColumn.STERA_TERMINAL_NUMBER1.ordinal())));
        record.setSteraTerminalNumber2(trim(fields.get(ApplicationFormColumn.STERA_TERMINAL_NUMBER2.ordinal())));
        record.setSteraTerminalNumber3(trim(fields.get(ApplicationFormColumn.STERA_TERMINAL_NUMBER3.ordinal())));
        record.setSteraTerminalNumber4(trim(fields.get(ApplicationFormColumn.STERA_TERMINAL_NUMBER4.ordinal())));
        record.setSteraTerminalNumber5(trim(fields.get(ApplicationFormColumn.STERA_TERMINAL_NUMBER5.ordinal())));
        record.setFeeRateBrandRakutenPay(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_BRAND_RAKUTEN_PAY.ordinal()), rowNum, "ブランド間料率(税抜) 楽天Pay", errors));
        record.setFeeRateBrandLinePay(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_BRAND_LINE_PAY.ordinal()), rowNum, "ブランド間料率(税抜) LINE Pay", errors));
        record.setFeeRateBrandPaypay(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_BRAND_PAYPAY.ordinal()), rowNum, "ブランド間料率(税抜) PayPay", errors));
        record.setFeeRateBrandDBarai(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_BRAND_D_BARAI.ordinal()), rowNum, "ブランド間料率(税抜) d払い", errors));
        record.setFeeRateBrandAuPay(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_BRAND_AU_PAY.ordinal()), rowNum, "ブランド間料率(税抜) auPay", errors));
        record.setFeeRateBrandMerpay(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_BRAND_MERPAY.ordinal()), rowNum, "ブランド間料率(税抜) メルペイ", errors));
        record.setFeeRateBrandYuchoPay(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_BRAND_YUCHO_PAY.ordinal()), rowNum, "ブランド間料率(税抜) ゆうちょPay", errors));
        record.setFeeRateBrandAeonPay(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_BRAND_AEON_PAY.ordinal()), rowNum, "ブランド間料率(税抜) AEONPay", errors));
        record.setAtokaraWholesaleRate(parseBigDecimalChecked(fields.get(ApplicationFormColumn.ATOKARA_WHOLESALE_RATE.ordinal()), rowNum, "アトカラ卸料率", errors));
        record.setMerchantInstallmentFee1(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.MERCHANT_INSTALLMENT_FEE1.ordinal()), rowNum, "加盟店分割払い手数料（1回）", errors));
        record.setMerchantInstallmentFee3(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.MERCHANT_INSTALLMENT_FEE3.ordinal()), rowNum, "加盟店分割払い手数料（3回）", errors));
        record.setMerchantInstallmentFee4(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.MERCHANT_INSTALLMENT_FEE4.ordinal()), rowNum, "加盟店分割払い手数料（4回）", errors));
        record.setMerchantInstallmentFee5(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.MERCHANT_INSTALLMENT_FEE5.ordinal()), rowNum, "加盟店分割払い手数料（5回）", errors));
        record.setMerchantInstallmentFee6(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.MERCHANT_INSTALLMENT_FEE6.ordinal()), rowNum, "加盟店分割払い手数料（6回）", errors));
        record.setMerchantInstallmentFee10(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.MERCHANT_INSTALLMENT_FEE10.ordinal()), rowNum, "加盟店分割払い手数料（10回）", errors));
        record.setMerchantInstallmentFee12(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.MERCHANT_INSTALLMENT_FEE12.ordinal()), rowNum, "加盟店分割払い手数料（12回）", errors));
        record.setMerchantInstallmentFee15(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.MERCHANT_INSTALLMENT_FEE15.ordinal()), rowNum, "加盟店分割払い手数料（15回）", errors));
        record.setMerchantInstallmentFee18(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.MERCHANT_INSTALLMENT_FEE18.ordinal()), rowNum, "加盟店分割払い手数料（18回）", errors));
        record.setMerchantInstallmentFee20(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.MERCHANT_INSTALLMENT_FEE20.ordinal()), rowNum, "加盟店分割払い手数料（20回）", errors));
        record.setMerchantInstallmentFee24(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.MERCHANT_INSTALLMENT_FEE24.ordinal()), rowNum, "加盟店分割払い手数料（24回）", errors));
        record.setMerchantInstallmentFee30(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.MERCHANT_INSTALLMENT_FEE30.ordinal()), rowNum, "加盟店分割払い手数料（30回）", errors));
        record.setMerchantInstallmentFee36(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.MERCHANT_INSTALLMENT_FEE36.ordinal()), rowNum, "加盟店分割払い手数料（36回）", errors));
        record.setFeeRateBrandWechat(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_BRAND_WECHAT.ordinal()), rowNum, "ブランド間料率(非課税) Wechat", errors));
        record.setFeeRateBrandAlipay(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_BRAND_ALIPAY.ordinal()), rowNum, "ブランド間料率(非課税) Alipay", errors));
        record.setFeeRateBrandWesmo(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_BRAND_WESMO.ordinal()), rowNum, "ブランド間料率(税抜) Wesmo!", errors));
        record.setAtokaraCustomerRate(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.ATOKARA_CUSTOMER_RATE.ordinal()), rowNum, "顧客手数料情報 アトカラ", errors));
        record.setCustomerInstallmentFee1(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.CUSTOMER_INSTALLMENT_FEE1.ordinal()), rowNum, "顧客手数料率（1回）", errors));
        record.setCustomerInstallmentFee3(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.CUSTOMER_INSTALLMENT_FEE3.ordinal()), rowNum, "顧客手数料率（3回）", errors));
        record.setCustomerInstallmentFee4(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.CUSTOMER_INSTALLMENT_FEE4.ordinal()), rowNum, "顧客手数料率（4回）", errors));
        record.setCustomerInstallmentFee5(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.CUSTOMER_INSTALLMENT_FEE5.ordinal()), rowNum, "顧客手数料率（5回）", errors));
        record.setCustomerInstallmentFee6(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.CUSTOMER_INSTALLMENT_FEE6.ordinal()), rowNum, "顧客手数料率（6回）", errors));
        record.setCustomerInstallmentFee10(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.CUSTOMER_INSTALLMENT_FEE10.ordinal()), rowNum, "顧客手数料率（10回）", errors));
        record.setCustomerInstallmentFee12(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.CUSTOMER_INSTALLMENT_FEE12.ordinal()), rowNum, "顧客手数料率（12回）", errors));
        record.setCustomerInstallmentFee15(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.CUSTOMER_INSTALLMENT_FEE15.ordinal()), rowNum, "顧客手数料率（15回）", errors));
        record.setCustomerInstallmentFee18(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.CUSTOMER_INSTALLMENT_FEE18.ordinal()), rowNum, "顧客手数料率（18回）", errors));
        record.setCustomerInstallmentFee20(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.CUSTOMER_INSTALLMENT_FEE20.ordinal()), rowNum, "顧客手数料率（20回）", errors));
        record.setCustomerInstallmentFee24(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.CUSTOMER_INSTALLMENT_FEE24.ordinal()), rowNum, "顧客手数料率（24回）", errors));
        record.setCustomerInstallmentFee30(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.CUSTOMER_INSTALLMENT_FEE30.ordinal()), rowNum, "顧客手数料率（30回）", errors));
        record.setCustomerInstallmentFee36(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.CUSTOMER_INSTALLMENT_FEE36.ordinal()), rowNum, "顧客手数料率（36回）", errors));
        record.setCostShareFlagRakutenPay(trim(fields.get(ApplicationFormColumn.COST_SHARE_FLAG_RAKUTEN_PAY.ordinal())));
        record.setCostShareFlagLinePay(trim(fields.get(ApplicationFormColumn.COST_SHARE_FLAG_LINE_PAY.ordinal())));
        record.setCostShareFlagPaypay(trim(fields.get(ApplicationFormColumn.COST_SHARE_FLAG_PAYPAY.ordinal())));
        record.setCostShareFlagDBarai(trim(fields.get(ApplicationFormColumn.COST_SHARE_FLAG_D_BARAI.ordinal())));
        record.setCostShareFlagAuPay(trim(fields.get(ApplicationFormColumn.COST_SHARE_FLAG_AU_PAY.ordinal())));
        record.setCostShareFlagMerpay(trim(fields.get(ApplicationFormColumn.COST_SHARE_FLAG_MERPAY.ordinal())));
        record.setCostShareFlagYuchoPay(trim(fields.get(ApplicationFormColumn.COST_SHARE_FLAG_YUCHO_PAY.ordinal())));
        record.setCostShareFlagAeonPay(trim(fields.get(ApplicationFormColumn.COST_SHARE_FLAG_AEON_PAY.ordinal())));
        record.setCostShareFlagWesmo(trim(fields.get(ApplicationFormColumn.COST_SHARE_FLAG_WESMO.ordinal())));
        record.setUnionpayQrMerchantNumber(trim(fields.get(ApplicationFormColumn.UNIONPAY_QR_MERCHANT_NUMBER.ordinal())));
        record.setAwMerchantNumber(trim(fields.get(ApplicationFormColumn.AW_MERCHANT_NUMBER.ordinal())));
        record.setDBaraiIpid(trim(fields.get(ApplicationFormColumn.D_BARAI_IPID.ordinal())));
        record.setAlipayPid(trim(fields.get(ApplicationFormColumn.ALIPAY_PID.ordinal())));
        record.setUnionpayQrMid(trim(fields.get(ApplicationFormColumn.UNIONPAY_QR_MID.ordinal())));
        record.setRelocationRepresentativeMerchantNumber(trim(fields.get(ApplicationFormColumn.RELOCATION_REPRESENTATIVE_MERCHANT_NUMBER.ordinal())));
        record.setRelocationPlatformMerchantNumber(trim(fields.get(ApplicationFormColumn.RELOCATION_PLATFORM_MERCHANT_NUMBER.ordinal())));
        record.setCancelAndNewRepresentativeMerchantNumber(trim(fields.get(ApplicationFormColumn.CANCEL_AND_NEW_REPRESENTATIVE_MERCHANT_NUMBER.ordinal())));
        record.setCancelAndNewPlatformMerchantNumber(trim(fields.get(ApplicationFormColumn.CANCEL_AND_NEW_PLATFORM_MERCHANT_NUMBER.ordinal())));
        record.setCafisArchTerminalCount(
                parseIntegerChecked(fields.get(ApplicationFormColumn.CAFIS_ARCH_TERMINAL_COUNT.ordinal()), rowNum, "【CAFIS Arch】端末台数", errors));
        record.setQuoCardPayMerchantRateNss(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.QUO_CARD_PAY_MERCHANT_RATE_NSS.ordinal()), rowNum, "【NSSのみ】QUOカードPay加盟店間料率(税込)", errors));
        record.setQuoCardPayBrandRateNss(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.QUO_CARD_PAY_BRAND_RATE_NSS.ordinal()), rowNum, "【NSSのみ】QUOカードPayブランド間料率(税抜)", errors));
        record.setBankPayNssTid(trim(fields.get(ApplicationFormColumn.BANK_PAY_NSS_TID.ordinal())));
        record.setFeeRateJcoinPayMerchant(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_JCOIN_PAY_MERCHANT.ordinal()), rowNum, "J-Coin Pay加盟店間料率(非課税)", errors));
        record.setFeeRateJcoinPayBrand(
                parseBigDecimalChecked(fields.get(ApplicationFormColumn.FEE_RATE_JCOIN_PAY_BRAND.ordinal()), rowNum, "J-Coin Payブランド間料率(非課税)", errors));
        if (errors.size() > errorCountBeforeRow) {
            return; // この行にデータ変換エラーがあるため対象外とする
        }
        records.add(record);
    }

    /**
     * CSV解析結果。正常行のリストと発生した全エラーを保持する。
     */
    public static class ParseResult {

        private final List<ApplicationFormInput> records;
        private final List<CsvValidationError> errors;
        private final int totalRowCount;

        public ParseResult(
                List<ApplicationFormInput> records, List<CsvValidationError> errors,
                int totalRowCount) {
            this.records = records;
            this.errors = errors;
            this.totalRowCount = totalRowCount;
        }

        public List<ApplicationFormInput> getRecords() {
            return records;
        }

        public List<CsvValidationError> getErrors() {
            return errors;
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public int getTotalRowCount() {
            return totalRowCount;
        }
    }

    // ──────────────────────────────────────────────────────
    // CSV解析ユーティリティ（AbstractFileImporter/AbstractCsvFormatValidatorと同等の実装）
    // ──────────────────────────────────────────────────────

    private Charset detectCharset(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream()) {
            byte[] bom = new byte[3];
            int read = is.read(bom);
            if (read >= 3
                    && (bom[0] & 0xFF) == 0xEF
                    && (bom[1] & 0xFF) == 0xBB
                    && (bom[2] & 0xFF) == 0xBF) {
                return StandardCharsets.UTF_8;
            }
            if (read >= 2
                    && (((bom[0] & 0xFF) == 0xFF && (bom[1] & 0xFF) == 0xFE)
                        || ((bom[0] & 0xFF) == 0xFE && (bom[1] & 0xFF) == 0xFF))) {
                throw new IllegalArgumentException(
                        "ファイルの文字コードがサポートされていません（UTF-16）。"
                        + "INPUTファイルをUTF-8（BOM付き）またはShift-JIS（MS932）で保存し直してください。");
            }
        }
        return Charset.forName("MS932");
    }

    private List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        int i = 0;
        while (i <= line.length()) {
            if (i < line.length() && line.charAt(i) == '"') {
                i++;
                StringBuilder sb = new StringBuilder();
                while (i < line.length()) {
                    if (line.charAt(i) == '"') {
                        if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                            sb.append('"');
                            i += 2;
                        } else {
                            i++;
                            break;
                        }
                    } else {
                        sb.append(line.charAt(i++));
                    }
                }
                fields.add(sb.toString());
                if (i < line.length() && line.charAt(i) == ',') {
                    i++;
                } else {
                    break;
                }
            } else {
                int start = i;
                while (i < line.length() && line.charAt(i) != ',') {
                    i++;
                }
                fields.add(line.substring(start, i));
                if (i < line.length()) {
                    i++;
                } else {
                    break;
                }
            }
        }
        return fields;
    }

    private String stripCr(String line) {
        if (line != null && line.endsWith("\r")) {
            return line.substring(0, line.length() - 1);
        }
        return line;
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }

    private LocalDate parseDateSlashChecked(
            String s, int rowNum, String colName, List<CsvValidationError> errors) {
        if (s == null || s.isBlank()) {
            return null;
        }
        String v = s.trim();
        try {
            return LocalDate.parse(v, FMT_YYYY_SLASH_MM_SLASH_DD);
        } catch (DateTimeParseException e) {
            errors.add(new CsvValidationError(rowNum, colName,
                    "日付変換エラー（YYYY/MM/DD形式）。値: 「" + v + "」"));
            return null;
        }
    }

    private Integer parseIntegerChecked(
            String s, int rowNum, String colName, List<CsvValidationError> errors) {
        if (s == null || s.isBlank()) {
            return null;
        }
        String v = s.trim();
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            errors.add(new CsvValidationError(rowNum, colName, "数値変換エラー。値: 「" + v + "」"));
            return null;
        }
    }

    private BigDecimal parseBigDecimalChecked(
            String s, int rowNum, String colName, List<CsvValidationError> errors) {
        if (s == null || s.isBlank()) {
            return null;
        }
        String v = s.trim();
        try {
            return new BigDecimal(v);
        } catch (NumberFormatException e) {
            errors.add(new CsvValidationError(rowNum, colName, "小数変換エラー。値: 「" + v + "」"));
            return null;
        }
    }

}
