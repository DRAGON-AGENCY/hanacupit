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

import com.cupit.csv.CsvValidationError;
import com.cupit.model.ApplicationFormInput;

/**
 * 各決済会社所定申込フォーム作成のINPUT CSV（230列）を解析し、
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

    private static final int EXPECTED_COLUMN_COUNT = 230;
    private static final int IDX_TRADE_CODE = 3;
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
        record.setRecordNumber(trim(fields.get(0)));
        record.setReaderSerialNo(trim(fields.get(1)));
        record.setTerminalId(trim(fields.get(2)));
        record.setTradeCode(trim(fields.get(3)));
        record.setPaygateContinuationStatus(trim(fields.get(4)));
        record.setMemberType(trim(fields.get(5)));
        record.setStoreName(trim(fields.get(6)));
        record.setStoreNameKana(trim(fields.get(7)));
        record.setStoreNameAlphabet(trim(fields.get(8)));
        record.setStoreZip(trim(fields.get(9)));
        record.setStoreAddress(trim(fields.get(10)));
        record.setStoreAddressKana(trim(fields.get(11)));
        record.setStoreTel(trim(fields.get(12)));
        record.setStoreEmail(trim(fields.get(13)));
        record.setStoreHomepageUrl(trim(fields.get(14)));
        record.setIndividualOrCorporateType(trim(fields.get(15)));
        record.setCorpNumber(trim(fields.get(16)));
        record.setCorpName(trim(fields.get(17)));
        record.setCorpNameKana(trim(fields.get(18)));
        record.setCorpNameAlphabet(trim(fields.get(19)));
        record.setCorpZip(trim(fields.get(20)));
        record.setCorpAddress(trim(fields.get(21)));
        record.setCorpAddressKana(trim(fields.get(22)));
        record.setCorpTel(trim(fields.get(23)));
        record.setEstablishmentDate(trim(fields.get(24)));
        record.setRepFullName(trim(fields.get(25)));
        record.setRepFullNameKana(trim(fields.get(26)));
        record.setRepZip(trim(fields.get(27)));
        record.setRepAddress(trim(fields.get(28)));
        record.setRepAddressKana(trim(fields.get(29)));
        record.setRepTel(trim(fields.get(30)));
        record.setRepBirthDate(trim(fields.get(31)));
        record.setBankName(trim(fields.get(32)));
        record.setBranchName(trim(fields.get(33)));
        record.setAccountNumber(trim(fields.get(34)));
        record.setAccountHolderKana(trim(fields.get(35)));
        record.setJcbUsageFlag(trim(fields.get(36)));
        record.setAnnualSales(trim(fields.get(37)));
        record.setAccountType(trim(fields.get(38)));
        record.setStatus(trim(fields.get(39)));
        record.setWorkerName(trim(fields.get(40)));
        record.setContactFullName(trim(fields.get(41)));
        record.setContactTel(trim(fields.get(42)));
        record.setStoreAddressPref(trim(fields.get(43)));
        record.setContactLastName(trim(fields.get(44)));
        record.setContactFirstName(trim(fields.get(45)));
        record.setCorpAddressPref(trim(fields.get(46)));
        record.setRepLastName(trim(fields.get(47)));
        record.setRepFirstName(trim(fields.get(48)));
        record.setRepLastNameKana(trim(fields.get(49)));
        record.setRepFirstNameKana(trim(fields.get(50)));
        record.setRepAddressPref(trim(fields.get(51)));
        record.setDPointUsageFlag(trim(fields.get(52)));
        record.setRepLastNameAlphabet(trim(fields.get(53)));
        record.setRepFirstNameAlphabet(trim(fields.get(54)));
        record.setRepGender(trim(fields.get(55)));
        record.setDoorToDoorSalesFlag(trim(fields.get(56)));
        record.setTelemarketingSalesFlag(trim(fields.get(57)));
        record.setChainSalesFlag(trim(fields.get(58)));
        record.setBusinessOpportunitySalesFlag(trim(fields.get(59)));
        record.setContinuousServiceFlag(trim(fields.get(60)));
        record.setPrepaidTransactionFlag(trim(fields.get(61)));
        record.setLegalViolationHistoryFlag(trim(fields.get(62)));
        record.setCardDataRetentionStatus(trim(fields.get(63)));
        record.setPciDssComplianceStatus(trim(fields.get(64)));
        record.setNonRetentionPlannedMonth(trim(fields.get(65)));
        record.setPciDssCompliancePlannedMonth(trim(fields.get(66)));
        record.setTerminalIcStatus(trim(fields.get(67)));
        record.setTerminalIcPlannedMonth(trim(fields.get(68)));
        record.setAcquirerUniqueKey(trim(fields.get(69)));
        record.setClassification(trim(fields.get(70)));
        record.setContractSource(trim(fields.get(71)));
        record.setGiftContractFlag(trim(fields.get(72)));
        record.setEdyContractFlag(trim(fields.get(73)));
        record.setApplicantType(trim(fields.get(74)));
        record.setServiceStartDesiredDate(parseDateSlashChecked(fields.get(75), rowNum, "サービス開始希望日", errors));
        record.setServiceEndDate(parseDateSlashChecked(fields.get(76), rowNum, "サービス終了日", errors));
        record.setVmMerchantNumber(trim(fields.get(77)));
        record.setClosingDate1(trim(fields.get(78)));
        record.setPaymentDate1(trim(fields.get(79)));
        record.setClosingDate2(trim(fields.get(80)));
        record.setPaymentDate2(trim(fields.get(81)));
        record.setSettlementCycle(trim(fields.get(82)));
        record.setBankCode(trim(fields.get(83)));
        record.setBankNameKana(trim(fields.get(84)));
        record.setBranchCode(trim(fields.get(85)));
        record.setBranchNameKana(trim(fields.get(86)));
        record.setContactLastNameKana(trim(fields.get(87)));
        record.setContactFirstNameKana(trim(fields.get(88)));
        record.setMerchantType(trim(fields.get(89)));
        record.setFranchiseFlag(trim(fields.get(90)));
        record.setPaypayFcAgreementFlag(trim(fields.get(91)));
        record.setTerminalType(trim(fields.get(92)));
        record.setIndustryCategoryMajor(trim(fields.get(93)));
        record.setIndustryCategoryMinor(trim(fields.get(94)));
        record.setStoreIndustryMajor(trim(fields.get(95)));
        record.setStoreIndustryMinor(trim(fields.get(96)));
        record.setRepAddrPrefKana(trim(fields.get(97)));
        record.setRepAddrCityKana(trim(fields.get(98)));
        record.setRepAddrTownKana(trim(fields.get(99)));
        record.setRepAddrBlockKana(trim(fields.get(100)));
        record.setRepAddrBuildingKana(trim(fields.get(101)));
        record.setFcStoreType(trim(fields.get(102)));
        record.setSecondhandDealerLicenseNumber(trim(fields.get(103)));
        record.setMapListingFlag(trim(fields.get(104)));
        record.setMapListingDesiredDateDpayRakuten(
                parseDateSlashChecked(fields.get(105), rowNum, "d払い・楽天Pay 地図掲載希望日", errors));
        record.setMapListingDesiredDatePaypayAupay(
                parseDateSlashChecked(fields.get(106), rowNum, "PayPay・auPay・ゆうちょPay・Alipay 地図掲載希望日", errors));
        record.setStoreImageListingFlag(trim(fields.get(107)));
        record.setStoreImageUrl(trim(fields.get(108)));
        record.setStoreIntroduction(trim(fields.get(109)));
        record.setFeeRateRakutenPay(
                parseBigDecimalChecked(fields.get(110), rowNum, "楽天Pay 加盟店手数料率（税込）", errors));
        record.setFeeRateLinePay(
                parseBigDecimalChecked(fields.get(111), rowNum, "LINE Pay 加盟店手数料率（税込）", errors));
        record.setFeeRatePaypay(
                parseBigDecimalChecked(fields.get(112), rowNum, "PayPay 加盟店手数料率（税込）", errors));
        record.setFeeRateDBarai(parseBigDecimalChecked(fields.get(113), rowNum, "d払い 加盟店手数料率（税込）", errors));
        record.setFeeRateAuPay(parseBigDecimalChecked(fields.get(114), rowNum, "auPay 加盟店手数料率（税込）", errors));
        record.setFeeRateMerpay(parseBigDecimalChecked(fields.get(115), rowNum, "メルペイ 加盟店手数料率（税込）", errors));
        record.setFeeRateYuchoPay(
                parseBigDecimalChecked(fields.get(116), rowNum, "ゆうちょPay 加盟店手数料率（税込）", errors));
        record.setFeeRateAeonPay(
                parseBigDecimalChecked(fields.get(117), rowNum, "AEONPay 加盟店手数料率（税込）", errors));
        record.setAtokaraRate(parseBigDecimalChecked(fields.get(118), rowNum, "アトカラ", errors));
        record.setFeeRateMdr1(parseBigDecimalChecked(fields.get(119), rowNum, "加盟店手数料率（1回）MDR", errors));
        record.setFeeRateMdr3(parseBigDecimalChecked(fields.get(120), rowNum, "加盟店手数料率（3回）MDR", errors));
        record.setFeeRateMdr4(
                parseBigDecimalChecked(fields.get(121), rowNum, "加盟店手数料率（4回）MDR+分割手数料", errors));
        record.setFeeRateInstallment5(parseBigDecimalChecked(fields.get(122), rowNum, "加盟店手数料率（5回）", errors));
        record.setFeeRateInstallment6(parseBigDecimalChecked(fields.get(123), rowNum, "加盟店手数料率（6回）", errors));
        record.setFeeRateInstallment10(
                parseBigDecimalChecked(fields.get(124), rowNum, "加盟店手数料率（10回）", errors));
        record.setFeeRateInstallment12(
                parseBigDecimalChecked(fields.get(125), rowNum, "加盟店手数料率（12回）", errors));
        record.setFeeRateInstallment15(
                parseBigDecimalChecked(fields.get(126), rowNum, "加盟店手数料率（15回）", errors));
        record.setFeeRateInstallment18(
                parseBigDecimalChecked(fields.get(127), rowNum, "加盟店手数料率（18回）", errors));
        record.setFeeRateInstallment20(
                parseBigDecimalChecked(fields.get(128), rowNum, "加盟店手数料率（20回）", errors));
        record.setFeeRateInstallment24(
                parseBigDecimalChecked(fields.get(129), rowNum, "加盟店手数料率（24回）", errors));
        record.setFeeRateInstallment30(
                parseBigDecimalChecked(fields.get(130), rowNum, "加盟店手数料率（30回）", errors));
        record.setFeeRateInstallment36(
                parseBigDecimalChecked(fields.get(131), rowNum, "加盟店手数料率（36回）", errors));
        record.setFeeRateWesmo(
                parseBigDecimalChecked(fields.get(132), rowNum, "Wesmo! 加盟店手数料率（非課税）", errors));
        record.setFeeRateBankPay(
                parseBigDecimalChecked(fields.get(133), rowNum, "BankPay 加盟店手数料率（非課税）", errors));
        record.setFeeRateWechat(
                parseBigDecimalChecked(fields.get(134), rowNum, "Wechat 加盟店手数料率（非課税）", errors));
        record.setFeeRateAlipay(
                parseBigDecimalChecked(fields.get(135), rowNum, "Alipay 加盟店手数料率（非課税）", errors));
        record.setFeeRateUnionpayQr(
                parseBigDecimalChecked(fields.get(136), rowNum, "銀聯QR 加盟店手数料率（非課税）", errors));
        record.setChangeNotes(trim(fields.get(137)));
        record.setSmccDepartment(trim(fields.get(138)));
        record.setSmccContactName(trim(fields.get(139)));
        record.setSmartCodeFlag(trim(fields.get(140)));
        record.setMkpFlag(trim(fields.get(141)));
        record.setTerminalCount(parseIntegerChecked(fields.get(142), rowNum, "端末台数", errors));
        record.setLineType(trim(fields.get(143)));
        record.setPosConnectionFlag(trim(fields.get(144)));
        record.setPosMakerName(trim(fields.get(145)));
        record.setPosVendorContactName(trim(fields.get(146)));
        record.setPosVendorContactTel(trim(fields.get(147)));
        record.setSmartCodeConnectionFlag(trim(fields.get(148)));
        record.setDPointMerchantCode(trim(fields.get(149)));
        record.setDPointStoreCode(trim(fields.get(150)));
        record.setDPointBranchCode(trim(fields.get(151)));
        record.setVisaMasterMerchantNumber(trim(fields.get(152)));
        record.setNanacoMerchantNumber(trim(fields.get(153)));
        record.setIdMerchantNumber(trim(fields.get(154)));
        record.setTransitMerchantNumber(trim(fields.get(155)));
        record.setUnionpayMerchantNumber(trim(fields.get(156)));
        record.setWaonMerchantNumber(trim(fields.get(157)));
        record.setEdyMerchantNumber(trim(fields.get(158)));
        record.setNfcMerchantNumber(trim(fields.get(159)));
        record.setTransitOperator(trim(fields.get(160)));
        record.setEdyId(trim(fields.get(161)));
        record.setSteraTerminalNumber1(trim(fields.get(162)));
        record.setSteraTerminalNumber2(trim(fields.get(163)));
        record.setSteraTerminalNumber3(trim(fields.get(164)));
        record.setSteraTerminalNumber4(trim(fields.get(165)));
        record.setSteraTerminalNumber5(trim(fields.get(166)));
        record.setFeeRateBrandRakutenPay(
                parseBigDecimalChecked(fields.get(167), rowNum, "ブランド間料率(税抜) 楽天Pay", errors));
        record.setFeeRateBrandLinePay(
                parseBigDecimalChecked(fields.get(168), rowNum, "ブランド間料率(税抜) LINE Pay", errors));
        record.setFeeRateBrandPaypay(
                parseBigDecimalChecked(fields.get(169), rowNum, "ブランド間料率(税抜) PayPay", errors));
        record.setFeeRateBrandDBarai(
                parseBigDecimalChecked(fields.get(170), rowNum, "ブランド間料率(税抜) d払い", errors));
        record.setFeeRateBrandAuPay(
                parseBigDecimalChecked(fields.get(171), rowNum, "ブランド間料率(税抜) auPay", errors));
        record.setFeeRateBrandMerpay(
                parseBigDecimalChecked(fields.get(172), rowNum, "ブランド間料率(税抜) メルペイ", errors));
        record.setFeeRateBrandYuchoPay(
                parseBigDecimalChecked(fields.get(173), rowNum, "ブランド間料率(税抜) ゆうちょPay", errors));
        record.setFeeRateBrandAeonPay(
                parseBigDecimalChecked(fields.get(174), rowNum, "ブランド間料率(税抜) AEONPay", errors));
        record.setAtokaraWholesaleRate(parseBigDecimalChecked(fields.get(175), rowNum, "アトカラ卸料率", errors));
        record.setMerchantInstallmentFee1(
                parseBigDecimalChecked(fields.get(176), rowNum, "加盟店分割払い手数料（1回）", errors));
        record.setMerchantInstallmentFee3(
                parseBigDecimalChecked(fields.get(177), rowNum, "加盟店分割払い手数料（3回）", errors));
        record.setMerchantInstallmentFee4(
                parseBigDecimalChecked(fields.get(178), rowNum, "加盟店分割払い手数料（4回）", errors));
        record.setMerchantInstallmentFee5(
                parseBigDecimalChecked(fields.get(179), rowNum, "加盟店分割払い手数料（5回）", errors));
        record.setMerchantInstallmentFee6(
                parseBigDecimalChecked(fields.get(180), rowNum, "加盟店分割払い手数料（6回）", errors));
        record.setMerchantInstallmentFee10(
                parseBigDecimalChecked(fields.get(181), rowNum, "加盟店分割払い手数料（10回）", errors));
        record.setMerchantInstallmentFee12(
                parseBigDecimalChecked(fields.get(182), rowNum, "加盟店分割払い手数料（12回）", errors));
        record.setMerchantInstallmentFee15(
                parseBigDecimalChecked(fields.get(183), rowNum, "加盟店分割払い手数料（15回）", errors));
        record.setMerchantInstallmentFee18(
                parseBigDecimalChecked(fields.get(184), rowNum, "加盟店分割払い手数料（18回）", errors));
        record.setMerchantInstallmentFee20(
                parseBigDecimalChecked(fields.get(185), rowNum, "加盟店分割払い手数料（20回）", errors));
        record.setMerchantInstallmentFee24(
                parseBigDecimalChecked(fields.get(186), rowNum, "加盟店分割払い手数料（24回）", errors));
        record.setMerchantInstallmentFee30(
                parseBigDecimalChecked(fields.get(187), rowNum, "加盟店分割払い手数料（30回）", errors));
        record.setMerchantInstallmentFee36(
                parseBigDecimalChecked(fields.get(188), rowNum, "加盟店分割払い手数料（36回）", errors));
        record.setFeeRateBrandWechat(
                parseBigDecimalChecked(fields.get(189), rowNum, "ブランド間料率(非課税) Wechat", errors));
        record.setFeeRateBrandAlipay(
                parseBigDecimalChecked(fields.get(190), rowNum, "ブランド間料率(非課税) Alipay", errors));
        record.setFeeRateBrandWesmo(
                parseBigDecimalChecked(fields.get(191), rowNum, "ブランド間料率(税抜) Wesmo!", errors));
        record.setAtokaraCustomerRate(
                parseBigDecimalChecked(fields.get(192), rowNum, "顧客手数料情報 アトカラ", errors));
        record.setCustomerInstallmentFee1(
                parseBigDecimalChecked(fields.get(193), rowNum, "顧客手数料率（1回）", errors));
        record.setCustomerInstallmentFee3(
                parseBigDecimalChecked(fields.get(194), rowNum, "顧客手数料率（3回）", errors));
        record.setCustomerInstallmentFee4(
                parseBigDecimalChecked(fields.get(195), rowNum, "顧客手数料率（4回）", errors));
        record.setCustomerInstallmentFee5(
                parseBigDecimalChecked(fields.get(196), rowNum, "顧客手数料率（5回）", errors));
        record.setCustomerInstallmentFee6(
                parseBigDecimalChecked(fields.get(197), rowNum, "顧客手数料率（6回）", errors));
        record.setCustomerInstallmentFee10(
                parseBigDecimalChecked(fields.get(198), rowNum, "顧客手数料率（10回）", errors));
        record.setCustomerInstallmentFee12(
                parseBigDecimalChecked(fields.get(199), rowNum, "顧客手数料率（12回）", errors));
        record.setCustomerInstallmentFee15(
                parseBigDecimalChecked(fields.get(200), rowNum, "顧客手数料率（15回）", errors));
        record.setCustomerInstallmentFee18(
                parseBigDecimalChecked(fields.get(201), rowNum, "顧客手数料率（18回）", errors));
        record.setCustomerInstallmentFee20(
                parseBigDecimalChecked(fields.get(202), rowNum, "顧客手数料率（20回）", errors));
        record.setCustomerInstallmentFee24(
                parseBigDecimalChecked(fields.get(203), rowNum, "顧客手数料率（24回）", errors));
        record.setCustomerInstallmentFee30(
                parseBigDecimalChecked(fields.get(204), rowNum, "顧客手数料率（30回）", errors));
        record.setCustomerInstallmentFee36(
                parseBigDecimalChecked(fields.get(205), rowNum, "顧客手数料率（36回）", errors));
        record.setCostShareFlagRakutenPay(trim(fields.get(206)));
        record.setCostShareFlagLinePay(trim(fields.get(207)));
        record.setCostShareFlagPaypay(trim(fields.get(208)));
        record.setCostShareFlagDBarai(trim(fields.get(209)));
        record.setCostShareFlagAuPay(trim(fields.get(210)));
        record.setCostShareFlagMerpay(trim(fields.get(211)));
        record.setCostShareFlagYuchoPay(trim(fields.get(212)));
        record.setCostShareFlagAeonPay(trim(fields.get(213)));
        record.setCostShareFlagWesmo(trim(fields.get(214)));
        record.setUnionpayQrMerchantNumber(trim(fields.get(215)));
        record.setAwMerchantNumber(trim(fields.get(216)));
        record.setDBaraiIpid(trim(fields.get(217)));
        record.setAlipayPid(trim(fields.get(218)));
        record.setUnionpayQrMid(trim(fields.get(219)));
        record.setRelocationRepresentativeMerchantNumber(trim(fields.get(220)));
        record.setRelocationPlatformMerchantNumber(trim(fields.get(221)));
        record.setCancelAndNewRepresentativeMerchantNumber(trim(fields.get(222)));
        record.setCancelAndNewPlatformMerchantNumber(trim(fields.get(223)));
        record.setCafisArchTerminalCount(
                parseIntegerChecked(fields.get(224), rowNum, "【CAFIS Arch】端末台数", errors));
        record.setQuoCardPayMerchantRateNss(
                parseBigDecimalChecked(fields.get(225), rowNum, "【NSSのみ】QUOカードPay加盟店間料率(税込)", errors));
        record.setQuoCardPayBrandRateNss(
                parseBigDecimalChecked(fields.get(226), rowNum, "【NSSのみ】QUOカードPayブランド間料率(税抜)", errors));
        record.setBankPayNssTid(trim(fields.get(227)));
        record.setFeeRateJcoinPayMerchant(
                parseBigDecimalChecked(fields.get(228), rowNum, "J-Coin Pay加盟店間料率(非課税)", errors));
        record.setFeeRateJcoinPayBrand(
                parseBigDecimalChecked(fields.get(229), rowNum, "J-Coin Payブランド間料率(非課税)", errors));
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
