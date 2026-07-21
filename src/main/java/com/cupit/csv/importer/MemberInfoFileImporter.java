package com.cupit.csv.importer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.csv.validator.PaymentCompanyFormatChecker;
import com.cupit.model.ImportBatch;
import com.cupit.model.MemberInfo;
import com.cupit.repository.MemberInfoRepository;

/**
 * 加盟会員店マスターデータ CSV を解析して m_member_info に登録・更新する。
 * trade_code が m_member_info の主キーであるため、明示的な削除は行わず
 * memberInfoRecordSaver.save() を1件ずつ呼び出して upsert（存在すればUPDATE、
 * なければINSERT）する。
 * 文字コード: UTF-8 BOM付きは自動検出、なければMS932。
 * ヘッダー行: 1行目は内容によらず常にヘッダー行として扱いスキップする（列名はチェックしない）。
 * 列数不足の行、取引コード未入力の行、CSV内で取引コードが重複する行、数値・日付変換に
 * 失敗した項目を含む行、およびDB登録時に制約違反（桁数超過等）が発生した行はその行だけを
 * 登録せずスキップし、ファイルの最後まで処理を継続する（データエラーによってファイル
 * 全体をロールバックすることはしない）。DB登録は1件ずつ独立したトランザクション
 * （{@link MemberInfoRecordSaver}）で行うため、1行のDB制約違反が他の正常な行の
 * 登録に影響しない。
 */
@Component
public class MemberInfoFileImporter extends AbstractFileImporter {

    private static final int EXPECTED_COLUMN_COUNT = 255;

    private final MemberInfoRepository memberInfoRepository;
    private final MemberInfoRecordSaver memberInfoRecordSaver;
    private final PaymentCompanyFormatChecker paymentCompanyFormatChecker;

    public MemberInfoFileImporter(
            MemberInfoRepository memberInfoRepository,
            MemberInfoRecordSaver memberInfoRecordSaver,
            PaymentCompanyFormatChecker paymentCompanyFormatChecker) {
        this.memberInfoRepository = memberInfoRepository;
        this.memberInfoRecordSaver = memberInfoRecordSaver;
        this.paymentCompanyFormatChecker = paymentCompanyFormatChecker;
    }

    @Override
    public ImportResult importFile(MultipartFile file, ImportBatch batch) throws IOException {
        List<MemberInfo> records = new ArrayList<>();
        List<Integer> recordRowNumbers = new ArrayList<>();
        List<CsvValidationError> errors = new ArrayList<>();
        int rowNum = 1;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), detectCharset(file)))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                return new ImportResult(0, 0, errors);
            }

            Set<String> seenTradeCodes = new HashSet<>();
            String line;
            while ((line = reader.readLine()) != null) {
                rowNum++;
                line = stripCr(line);
                if (line.isBlank()) {
                    continue;
                }
                List<String> fields = parseLine(line);
                if (fields.size() != EXPECTED_COLUMN_COUNT) {
                    errors.add(new CsvValidationError(rowNum, "取引コード",
                            "取引コード「" + fields.get(0).trim() + "」: 列数が不正です。期待: "
                            + EXPECTED_COLUMN_COUNT + "列、実際: " + fields.size() + "列"));
                    continue;
                }
                parseDataRow(fields, rowNum, records, recordRowNumbers, errors, seenTradeCodes);
            }
        }

        applyAuditColumns(records, batch);
        int successCount = saveRecordsIndividually(records, recordRowNumbers, errors);
        int totalDataRows = rowNum - 1;
        return new ImportResult(successCount, totalDataRows, errors);
    }

    /**
     * 検証済みレコードを1件ずつ独立したトランザクションで保存する。DB制約違反
     * （桁数超過等、アプリ層のフォーマットチェック対象外の項目で発生し得る）が
     * 起きた行は登録せずエラーとして記録し、他の行の保存は継続する。
     */
    private int saveRecordsIndividually(
            List<MemberInfo> records, List<Integer> recordRowNumbers,
            List<CsvValidationError> errors) {
        int successCount = 0;
        for (int i = 0; i < records.size(); i++) {
            MemberInfo record = records.get(i);
            try {
                memberInfoRecordSaver.save(record);
                successCount++;
            } catch (DataAccessException e) {
                errors.add(new CsvValidationError(recordRowNumbers.get(i), "取引コード",
                        "取引コード「" + record.getTradeCode() + "」: データベースへの登録に失敗しました。（"
                                + rootCauseMessage(e) + "）"));
            }
        }
        return successCount;
    }

    private String rootCauseMessage(Throwable e) {
        Throwable cause = e;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause.getMessage();
    }

    private void parseDataRow(
            List<String> fields, int rowNum, List<MemberInfo> records,
            List<Integer> recordRowNumbers, List<CsvValidationError> errors,
            Set<String> seenTradeCodes) {
        String tradeCode = trim(fields.get(0));
        if (tradeCode.isEmpty()) {
            errors.add(new CsvValidationError(rowNum, "取引コード", "取引コードは必須です。"));
            return;
        }
        if (!seenTradeCodes.add(tradeCode)) {
            errors.add(new CsvValidationError(rowNum, "取引コード",
                    "取引コード「" + tradeCode + "」がCSV内で重複しています。"));
            return;
        }

        int errorCountBeforeRow = errors.size();
        paymentCompanyFormatChecker.check(fields, rowNum, errors);

        MemberInfo record = new MemberInfo();
        record.setTradeCode(tradeCode);
        record.setParentStoreCode(trim(fields.get(1)));
        record.setParentStoreName(trim(fields.get(2)));
        record.setNewTradeCode(trim(fields.get(3)));
        record.setPrevTradeCode(trim(fields.get(4)));
        record.setMidCode(parseShortChecked(fields.get(5), rowNum, "中コード", errors));
        record.setBlockCode(trim(fields.get(6)));
        record.setJoinDate(parseDateSlashChecked(fields.get(7), rowNum, "入会日", errors));
        record.setCorpAssocFlag(trim(fields.get(8)));
        record.setCooperativeFlag(trim(fields.get(9)));
        record.setBranchSupplementPeriodFrom(
                parseDateSlashChecked(fields.get(10), rowNum, "登録支店補完期間from", errors));
        record.setQualificationType(trim(fields.get(11)));
        record.setBranchSupplementPeriodTo(
                parseDateSlashChecked(fields.get(12), rowNum, "登録支店補完期間to", errors));
        record.setStoreName(trim(fields.get(13)));
        record.setStoreNameKana(trim(fields.get(14)));
        record.setStoreNameKanaShort(trim(fields.get(15)));
        record.setStoreNameShort(trim(fields.get(16)));
        record.setPrefCode(parseShortChecked(fields.get(17), rowNum, "都道府県コード", errors));
        record.setCityCode(parseIntegerChecked(fields.get(18), rowNum, "市区町村コード", errors));
        record.setCityName(trim(fields.get(19)));
        record.setAddrZip(trim(fields.get(20)));
        record.setAddrPref(trim(fields.get(21)));
        record.setAddrPrefKana(trim(fields.get(22)));
        record.setAddrCity(trim(fields.get(23)));
        record.setAddrCityKana(trim(fields.get(24)));
        record.setAddrTown(trim(fields.get(25)));
        record.setAddrTownKana(trim(fields.get(26)));
        record.setAddrBlock(trim(fields.get(27)));
        record.setAddrBlockKana(trim(fields.get(28)));
        record.setAddrBuilding(trim(fields.get(29)));
        record.setAddrBuildingKana(trim(fields.get(30)));
        record.setAddrTel(trim(fields.get(31)));
        record.setAddrFax(trim(fields.get(32)));
        record.setMailZip(trim(fields.get(33)));
        record.setMailAddress(trim(fields.get(34)));
        record.setMailTel(trim(fields.get(35)));
        record.setBusinessHoursWeekday(trim(fields.get(36)));
        record.setBusinessHoursWeekdayNote(trim(fields.get(37)));
        record.setBusinessHoursOther(trim(fields.get(38)));
        record.setBusinessHoursOtherNote(trim(fields.get(39)));
        record.setRegularHoliday(trim(fields.get(40)));
        record.setHandlingItems(trim(fields.get(41)));
        record.setClosureReceivedDate(parseDateSlashChecked(fields.get(42), rowNum, "休業受付日", errors));
        record.setClosureStartDate(parseDateSlashChecked(fields.get(43), rowNum, "休業開始日", errors));
        record.setClosureEndDate(parseDateSlashChecked(fields.get(44), rowNum, "休業終了日", errors));
        record.setClosureContact(trim(fields.get(45)));
        record.setClosureReason(trim(fields.get(46)));
        record.setClosureApprover(trim(fields.get(47)));
        record.setDeliveryAreaStatus(trim(fields.get(48)));
        record.setFreeDeliveryArea1(trim(fields.get(49)));
        record.setPaidDeliveryArea1(trim(fields.get(50)));
        record.setFreeDeliveryArea2(trim(fields.get(51)));
        record.setPaidDeliveryArea2(trim(fields.get(52)));
        record.setRemarks(trim(fields.get(53)));
        record.setAccountHolderKana(trim(fields.get(54)));
        record.setAccountHolder(trim(fields.get(55)));
        record.setAccountHolderBirth(trim(fields.get(56)));
        record.setMgmtType(trim(fields.get(57)));
        record.setCorpLegalForm(trim(fields.get(58)));
        record.setCorpName(trim(fields.get(59)));
        record.setCorpLegalFormKana(trim(fields.get(60)));
        record.setCorpNameKana(trim(fields.get(61)));
        record.setCorpZip(trim(fields.get(62)));
        record.setCorpPref(trim(fields.get(63)));
        record.setCorpPrefKana(trim(fields.get(64)));
        record.setCorpCity(trim(fields.get(65)));
        record.setCorpCityKana(trim(fields.get(66)));
        record.setCorpTown(trim(fields.get(67)));
        record.setCorpTownKana(trim(fields.get(68)));
        record.setCorpBlock(trim(fields.get(69)));
        record.setCorpBlockKana(trim(fields.get(70)));
        record.setCorpBuilding(trim(fields.get(71)));
        record.setCorpBuildingKana(trim(fields.get(72)));
        record.setRepLastNameKana(trim(fields.get(73)));
        record.setRepFirstNameKana(trim(fields.get(74)));
        record.setRepLastName(trim(fields.get(75)));
        record.setRepFirstName(trim(fields.get(76)));
        record.setRepBirth(trim(fields.get(77)));
        record.setRepPosition(trim(fields.get(78)));
        record.setRepZip(trim(fields.get(79)));
        record.setRepPref(trim(fields.get(80)));
        record.setRepPrefKana(trim(fields.get(81)));
        record.setRepCity(trim(fields.get(82)));
        record.setRepCityKana(trim(fields.get(83)));
        record.setRepTown(trim(fields.get(84)));
        record.setRepTownKana(trim(fields.get(85)));
        record.setRepBlock(trim(fields.get(86)));
        record.setRepBlockKana(trim(fields.get(87)));
        record.setRepBuilding(trim(fields.get(88)));
        record.setRepBuildingKana(trim(fields.get(89)));
        record.setGuarantorName(trim(fields.get(90)));
        record.setGuarantorZip(trim(fields.get(91)));
        record.setGuarantorAddress(trim(fields.get(92)));
        record.setCapitalYen(parseLongChecked(fields.get(93), rowNum, "資本金(円)", errors));
        record.setAppRegularEmployeeCount(
                parseIntegerChecked(fields.get(94), rowNum, "加入申込書 常時使用従業員数(人)", errors));
        record.setAppIndustry1(trim(fields.get(95)));
        record.setAppIndustry1Ratio(parseBigDecimalChecked(fields.get(96), rowNum, "加入申込書 業種1割合(%)", errors));
        record.setAppIndustry2(trim(fields.get(97)));
        record.setAppIndustry2Ratio(parseBigDecimalChecked(fields.get(98), rowNum, "加入申込書 業種2割合(%)", errors));
        record.setAppIndustry3(trim(fields.get(99)));
        record.setAppIndustry3Ratio(
                parseBigDecimalChecked(fields.get(100), rowNum, "加入申込書 業種3割合(%)", errors));
        record.setOfficer1Position(trim(fields.get(101)));
        record.setOfficer1Name(trim(fields.get(102)));
        record.setOfficer2Position(trim(fields.get(103)));
        record.setOfficer2Name(trim(fields.get(104)));
        record.setNewCodeApplyDate(trim(fields.get(105)));
        record.setCodeChangeNotifyDateStore(
                parseDateSlashChecked(fields.get(106), rowNum, "コードNo変更通知日(当該店)", errors));
        record.setCodeChangeNotifyDateBranch(
                parseDateSlashChecked(fields.get(107), rowNum, "コードNo変更通知日(支部)", errors));
        record.setCodeChangeNationwideNoticeDate(
                parseDateSlashChecked(fields.get(108), rowNum, "コードNo変更全国発送告知日", errors));
        record.setCorpAssocWithdrawType(trim(fields.get(109)));
        record.setCorpAssocWithdrawProcDate(
                parseDateSlashChecked(fields.get(110), rowNum, "社団脱退処理日", errors));
        record.setCorpAssocWithdrawReceivedDate(
                parseDateSlashChecked(fields.get(111), rowNum, "社団脱退 受付日", errors));
        record.setCorpAssocWithdrawNotifyDate(
                parseDateSlashChecked(fields.get(112), rowNum, "社団脱退当該店通知日", errors));
        record.setCorpAssocWithdrawDate(parseDateSlashChecked(fields.get(113), rowNum, "社団脱退日", errors));
        record.setCorpAssocWithdrawReason(trim(fields.get(114)));
        record.setCooperativeWithdrawType(trim(fields.get(115)));
        record.setCooperativeWithdrawProcDate(
                parseDateSlashChecked(fields.get(116), rowNum, "協同組合脱退処理日", errors));
        record.setCooperativeWithdrawReceivedDate(
                parseDateSlashChecked(fields.get(117), rowNum, "協同組合脱退 受付日", errors));
        record.setCooperativeWithdrawNotifyDate(
                parseDateSlashChecked(fields.get(118), rowNum, "協同組合脱退当該店通知日", errors));
        record.setCooperativeWithdrawDate(parseDateSlashChecked(fields.get(119), rowNum, "協同組合脱退日", errors));
        record.setCooperativeWithdrawReason(trim(fields.get(120)));
        record.setBranchTradeStartDate(parseDateSlashChecked(fields.get(121), rowNum, "届出支店取引開始日", errors));
        record.setBranchDeletedFlag(trim(fields.get(122)));
        record.setBranchDeletedDate(parseDateSlashChecked(fields.get(123), rowNum, "届出支店抹消日", errors));
        record.setBranchDeletedReason(trim(fields.get(124)));
        record.setReasonCategoryInput(trim(fields.get(125)));
        record.setTradeDirectoryStatus(trim(fields.get(126)));
        record.setOtherReturn(trim(fields.get(127)));
        record.setCorpAssocWithdrawReasonType(trim(fields.get(128)));
        record.setCooperativeWithdrawReasonType(trim(fields.get(129)));
        record.setApprovalNo(trim(fields.get(130)));
        record.setApprovalDocIssueDate(parseDateSlashChecked(fields.get(131), rowNum, "稟議書発行日", errors));
        record.setApprovalApprovedDate(parseDateSlashChecked(fields.get(132), rowNum, "稟議承認日", errors));
        record.setContractDate(parseDateSlashChecked(fields.get(133), rowNum, "契約書日", errors));
        record.setBankTransferDate(parseDateSlashChecked(fields.get(134), rowNum, "口座振込日", errors));
        record.setEntryFeeInvoiceDate(parseDateSlashChecked(fields.get(135), rowNum, "加盟金請求日", errors));
        record.setContractReceivedDate(parseDateSlashChecked(fields.get(136), rowNum, "契約書受理日", errors));
        record.setBranchReportSendDate(parseDateSlashChecked(fields.get(137), rowNum, "支部報告送付日", errors));
        record.setOfficialSealRequestDate(
                parseDateSlashChecked(fields.get(138), rowNum, "請求書・契約書の公印依頼日", errors));
        record.setEntryFeePaymentDate(parseDateSlashChecked(fields.get(139), rowNum, "加盟金入金日", errors));
        record.setAgencyToolContactDate(
                parseDateSlashChecked(fields.get(140), rowNum, "取次店ツール手配連絡日", errors));
        record.setPromotionAssocContactDate(
                parseDateSlashChecked(fields.get(141), rowNum, "振興協会連絡日", errors));
        record.setParentCorpName(trim(fields.get(142)));
        record.setParentAnnualSalesYen(parseLongChecked(fields.get(143), rowNum, "親会社 年間売上（円)", errors));
        record.setParentFoundedDate(trim(fields.get(144)));
        record.setParentBusinessYears(trim(fields.get(145)));
        record.setParentStoreCount(parseIntegerChecked(fields.get(146), rowNum, "親会社 店舗数", errors));
        record.setParentEmployeeCount(parseIntegerChecked(fields.get(147), rowNum, "親会社 従業員数", errors));
        record.setParentMainBusiness(trim(fields.get(148)));
        record.setParentAnnualPurchase(parseLongChecked(fields.get(149), rowNum, "親会社 年間仕入", errors));
        record.setParentOperatingProfitYen(parseLongChecked(fields.get(150), rowNum, "親会社 営業利益（円）", errors));
        record.setParentNetIncomeYen(parseLongChecked(fields.get(151), rowNum, "親会社 当期利益（円）", errors));
        record.setParentFiscalPeriodFrom(
                parseDateSlashChecked(fields.get(152), rowNum, "親会社 決算期間from", errors));
        record.setParentFiscalPeriodTo(parseDateSlashChecked(fields.get(153), rowNum, "親会社 決算期間to", errors));
        record.setStoreAnnualSalesYen(parseLongChecked(fields.get(154), rowNum, "店舗 年間売上（円）", errors));
        record.setStoreFoundedDate(parseDateSlashChecked(fields.get(155), rowNum, "店舗 創業年月", errors));
        record.setStoreBusinessYears(trim(fields.get(156)));
        record.setStoreCount(parseIntegerChecked(fields.get(157), rowNum, "店舗数", errors));
        record.setStoreEmployeeCount(parseIntegerChecked(fields.get(158), rowNum, "店舗 従業員数", errors));
        record.setStoreMainBusiness(trim(fields.get(159)));
        record.setStoreAnnualPurchaseYen(parseLongChecked(fields.get(160), rowNum, "店舗 年間仕入（円）", errors));
        record.setStoreOperatingProfitYen(parseLongChecked(fields.get(161), rowNum, "店舗 営業利益（円）", errors));
        record.setStoreNetIncomeYen(parseLongChecked(fields.get(162), rowNum, "店舗 当期利益（円）", errors));
        record.setStoreFiscalPeriodFrom(
                parseDateSlashChecked(fields.get(163), rowNum, "店舗 決算期間from", errors));
        record.setStoreFiscalPeriodTo(parseDateSlashChecked(fields.get(164), rowNum, "店舗 決算期間to", errors));
        record.setSalesRatioFreshFlower(parseLongChecked(fields.get(165), rowNum, "売上構成(%) 生花", errors));
        record.setSalesRatioPottedPlant(parseShortChecked(fields.get(166), rowNum, "　　　　 鉢物", errors));
        record.setSalesRatioMaterial(parseShortChecked(fields.get(167), rowNum, "　　　　 資材", errors));
        record.setSalesRatioOther(parseShortChecked(fields.get(168), rowNum, "　　　　 他", errors));
        record.setBusinessRatioStorefront(
                parseBigDecimalChecked(fields.get(169), rowNum, "営業構成(%) 店売", errors));
        record.setBusinessRatioLesson(parseShortChecked(fields.get(170), rowNum, "　　　　　　稽古", errors));
        record.setBusinessRatioWork(parseShortChecked(fields.get(171), rowNum, "　　　　　　仕事", errors));
        record.setBusinessRatioOther(parseShortChecked(fields.get(172), rowNum, "　　　　　　他", errors));
        record.setStoreArea(parseBigDecimalChecked(fields.get(173), rowNum, "店舗面積", errors));
        record.setEmployeeCount(parseIntegerChecked(fields.get(174), rowNum, "従業員", errors));
        record.setEmployeeFamilyCount(parseIntegerChecked(fields.get(175), rowNum, "従業員 内 , 家族", errors));
        record.setDeliveryVehicleCount(parseShortChecked(fields.get(176), rowNum, "配達車両", errors));
        record.setMemberOrganization(trim(fields.get(177)));
        record.setFinancialStatementExists(trim(fields.get(178)));
        record.setMarketPurchaseCertExists(trim(fields.get(179)));
        record.setStoreFloorPlanExists(trim(fields.get(180)));
        record.setStorePhotoExists(trim(fields.get(181)));
        record.setNamePhotoExists(trim(fields.get(182)));
        record.setBankAccountExists(trim(fields.get(183)));
        record.setBranchSecretary(trim(fields.get(184)));
        record.setBranchName(trim(fields.get(185)));
        record.setDirector(trim(fields.get(186)));
        record.setSealCertExists(trim(fields.get(187)));
        record.setResidentRecordExists(trim(fields.get(188)));
        record.setOpeningDate(parseDateSlashChecked(fields.get(189), rowNum, "開業年月", errors));
        record.setApplicationReceivedDate(parseDateSlashChecked(fields.get(190), rowNum, "申込み書類受付日", errors));
        record.setTrainingDate(parseDateSlashChecked(fields.get(191), rowNum, "研修日", errors));
        record.setPreliminaryReviewResult(trim(fields.get(192)));
        record.setBoardReviewResult(trim(fields.get(193)));
        record.setAgencyBranchResult(trim(fields.get(194)));
        record.setAgencyApprovalResult(trim(fields.get(195)));
        record.setAgencyPaymentResult(trim(fields.get(196)));
        record.setSealOriginalExists(trim(fields.get(197)));
        record.setSealCopyExists(trim(fields.get(198)));
        record.setPositionDirector(trim(fields.get(199)));
        record.setPositionAuditor(trim(fields.get(200)));
        record.setPositionDelegate(trim(fields.get(201)));
        record.setPositionBranchSecretary(trim(fields.get(202)));
        record.setHqPosition1(trim(fields.get(203)));
        record.setHqPosition2(trim(fields.get(204)));
        record.setHqPosition3(trim(fields.get(205)));
        record.setHqPosition4(trim(fields.get(206)));
        record.setHqPosition5(trim(fields.get(207)));
        record.setBranchPosition1(trim(fields.get(208)));
        record.setBranchPosition2(trim(fields.get(209)));
        record.setBranchPosition3(trim(fields.get(210)));
        record.setBranchPosition4(trim(fields.get(211)));
        record.setBranchPosition5(trim(fields.get(212)));
        record.setHqDispatchTransportFee1(parseLongChecked(fields.get(213), rowNum, "本部出向交通費１", errors));
        record.setHqDispatchTransportFee2(parseLongChecked(fields.get(214), rowNum, "本部出向交通費２", errors));
        record.setSettlementMailZip(trim(fields.get(215)));
        record.setSettlementMailAddress(trim(fields.get(216)));
        record.setOrderDeliveryTel(trim(fields.get(217)));
        record.setOrderDeliveryTel2(trim(fields.get(218)));
        record.setMemberTradeEmail(trim(fields.get(219)));
        record.setOrderContactEmail(trim(fields.get(220)));
        record.setOfficeContactEmail(trim(fields.get(221)));
        record.setTradeStopped(trim(fields.get(222)));
        record.setOrderFuncControlDate(parseDateSlashChecked(fields.get(223), rowNum, "注文機能制御設定日", errors));
        record.setDeliveryFuncControlDate(
                parseDateSlashChecked(fields.get(224), rowNum, "配達機能制御設定日", errors));
        record.setHcpTownUrl(trim(fields.get(225)));
        record.setRecentBusinessYears(trim(fields.get(226)));
        record.setRecentEmployeeCount(parseIntegerChecked(fields.get(227), rowNum, "直近店舗状況 従業員数", errors));
        record.setRecentStoreEmployeeCount(
                parseIntegerChecked(fields.get(228), rowNum, "直近店舗状況 店舗従業員数", errors));
        record.setRecentMainBusiness(trim(fields.get(229)));
        record.setRecentFiscalPeriodFrom(
                parseDateSlashChecked(fields.get(230), rowNum, "直近店舗状況 決算期間From", errors));
        record.setRecentFiscalPeriodTo(
                parseDateSlashChecked(fields.get(231), rowNum, "直近店舗状況 決算期間To", errors));
        record.setRecentStoreArea(trim(fields.get(232)));
        record.setRecentSalesRatioFreshFlower(
                parseLongChecked(fields.get(233), rowNum, "直近店舗状況 売上構成比(%) 生花", errors));
        record.setRecentSalesRatioPottedPlant(
                parseLongChecked(fields.get(234), rowNum, "直近店舗状況 売上構成比(%) 鉢物", errors));
        record.setRecentSalesRatioMaterial(
                parseLongChecked(fields.get(235), rowNum, "直近店舗状況 売上構成比(%) 資材", errors));
        record.setRecentSalesRatioOther(
                parseLongChecked(fields.get(236), rowNum, "直近店舗状況 売上構成比(%) その他", errors));
        record.setRecentBusinessRatioStorefront(
                parseBigDecimalChecked(fields.get(237), rowNum, "直近店舗状況 営業構成比(%) 店売", errors));
        record.setRecentBusinessRatioLesson(
                parseBigDecimalChecked(fields.get(238), rowNum, "直近店舗状況 営業構成比(%) 稽古", errors));
        record.setRecentBusinessRatioWork(
                parseBigDecimalChecked(fields.get(239), rowNum, "直近店舗状況 営業構成比(%) 仕事", errors));
        record.setRecentBusinessRatioOther(
                parseBigDecimalChecked(fields.get(240), rowNum, "直近店舗状況 営業構成比(%) 他", errors));
        record.setRecentSales(parseLongChecked(fields.get(241), rowNum, "直近店舗状況 売上", errors));
        record.setRecentPurchase(parseLongChecked(fields.get(242), rowNum, "直近店舗状況 仕入", errors));
        record.setRecentOperatingProfit(parseLongChecked(fields.get(243), rowNum, "直近店舗状況 営業利益", errors));
        record.setRecentNetIncome(parseLongChecked(fields.get(244), rowNum, "直近店舗状況 当期利益", errors));
        record.setRecentDeliveryVehicleCount(
                parseShortChecked(fields.get(245), rowNum, "直近店舗状況 配達車両", errors));
        record.setRecentStoreLocation(trim(fields.get(246)));
        record.setRecentMemberOrderCountYearly(
                parseIntegerChecked(fields.get(247), rowNum, "直近店舗状況 会員間注文件数（年間）", errors));
        record.setRecentMemberDeliveryCountYearly(
                parseIntegerChecked(fields.get(248), rowNum, "直近店舗状況 会員間配達件数（年間）", errors));
        record.setRecentMemberOrderAmountYearly(
                parseLongChecked(fields.get(249), rowNum, "直近店舗状況 会員間注文金額（年間）", errors));
        record.setRecentMemberOrderAmountYearly2(
                parseLongChecked(fields.get(250), rowNum, "直近店舗状況 会員間注文金額（年間）", errors));
        record.setStoreCategory(trim(fields.get(251)));
        record.setLatitude(parseBigDecimalChecked(fields.get(252), rowNum, "緯度", errors));
        record.setLongitude(parseBigDecimalChecked(fields.get(253), rowNum, "経度", errors));
        record.setHcpTownStatus(trim(fields.get(254)));
        if (errors.size() > errorCountBeforeRow) {
            return; // この行にデータ変換エラーがあるため登録しない
        }
        records.add(record);
        recordRowNumbers.add(rowNum);
    }

    /**
     * 登録日・更新日・更新者を設定する。新規取引コードは登録日を今日にする一方、
     * 既存の取引コードは登録日を維持し更新日のみ今日にする（upsertで登録日を
     * 上書きしないため、保存前に一括で既存取引コードを取得する）。
     */
    private void applyAuditColumns(List<MemberInfo> records, ImportBatch batch) {
        LocalDate today = LocalDate.now();
        Set<String> tradeCodes = records.stream()
                .map(MemberInfo::getTradeCode)
                .collect(Collectors.toSet());
        Map<String, LocalDate> existingCreateDates = new HashMap<>();
        for (MemberInfo existing : memberInfoRepository.findAllById(tradeCodes)) {
            existingCreateDates.put(existing.getTradeCode(), existing.getCreateDate());
        }
        for (MemberInfo record : records) {
            LocalDate existingCreateDate = existingCreateDates.get(record.getTradeCode());
            if (existingCreateDate != null) {
                record.setCreateDate(existingCreateDate);
                record.setUpdatedDate(today);
            } else {
                record.setCreateDate(today);
            }
            record.setUpdateEmployee(batch.getUpdateEmployee());
        }
    }

}
