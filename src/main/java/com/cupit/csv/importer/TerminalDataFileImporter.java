package com.cupit.csv.importer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.model.ImportBatch;
import com.cupit.model.TerminalData;
import com.cupit.repository.TerminalDataRepository;

/**
 * 端末データ CSV を解析して m_terminal_data に取引コード単位で洗い替え登録する
 * （取引コードが存在しなければ新規登録、存在すれば該当取引コードのレコードのみを
 * 削除してから登録する）。CSVに含まれない取引コードの既存データは保持する。
 * 1取引コードに複数行（複数端末等）が存在する運用のため、取引コード自体の重複は
 * 許容し、複数行分がまとめて同一取引コードで登録される。
 * 文字コード: UTF-8 BOM付きは自動検出、なければMS932。
 * ヘッダー行: 1行目は内容によらず常にヘッダー行として扱いスキップする（列名はチェックしない）。
 * 列数不足の行や、取引コード未入力の行、日付・数値変換に失敗した項目を含む行はその行だけを
 * 登録せずスキップし、ファイルの最後まで処理を継続する
 * （データエラーによってファイル全体をロールバックすることはしない）。取引コード以外の
 * 全項目は任意とする。
 */
@Component
public class TerminalDataFileImporter extends AbstractFileImporter {

    private static final int EXPECTED_COLUMN_COUNT = 95;

    private final TerminalDataRepository terminalDataRepository;

    public TerminalDataFileImporter(TerminalDataRepository terminalDataRepository) {
        this.terminalDataRepository = terminalDataRepository;
    }

    @Override
    public ImportResult importFile(MultipartFile file, ImportBatch batch) throws IOException {
        List<TerminalData> records = new ArrayList<>();
        List<CsvValidationError> errors = new ArrayList<>();
        int rowNum = 1;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), detectCharset(file)))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                return new ImportResult(0, 0, errors);
            }

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
                parseDataRow(fields, rowNum, batch, records, errors);
            }
        }

        Set<String> tradeCodes = records.stream()
                .map(TerminalData::getTradeCode)
                .collect(Collectors.toSet());
        terminalDataRepository.deleteByTradeCodeIn(tradeCodes);
        terminalDataRepository.saveAll(records);
        int totalDataRows = rowNum - 1;
        return new ImportResult(records.size(), totalDataRows, errors);
    }

    private void parseDataRow(
            List<String> fields, int rowNum, ImportBatch batch,
            List<TerminalData> records, List<CsvValidationError> errors) {
        String tradeCode = trim(fields.get(0));
        if (tradeCode.isEmpty()) {
            errors.add(new CsvValidationError(rowNum, "取引コード", "取引コードは必須です。"));
            return;
        }

        int errorCountBeforeRow = errors.size();
        TerminalData record = new TerminalData();
        record.setTradeCode(tradeCode);
        record.setApplicationCategory(trim(fields.get(1)));
        record.setApplicantType(trim(fields.get(2)));
        record.setApplicationOrCancellationDate(
                parseDateSlashChecked(fields.get(3), rowNum, "申込日／解約日", errors));
        record.setServiceStartDesiredDate(parseDateSlashChecked(fields.get(4), rowNum, "サービス開始希望日", errors));
        record.setServiceEndDate(parseDateSlashChecked(fields.get(5), rowNum, "サービス終了日", errors));
        record.setBrandNameEnglish(trim(fields.get(6)));
        record.setRepresentativeMerchantNumber(trim(fields.get(7)));
        record.setVmMerchantNumber(trim(fields.get(8)));
        record.setTerminalId(trim(fields.get(9)));
        record.setClosingDate1(trim(fields.get(10)));
        record.setPaymentDate1(trim(fields.get(11)));
        record.setClosingDate2(trim(fields.get(12)));
        record.setPaymentDate2(trim(fields.get(13)));
        record.setSettlementCycle(trim(fields.get(14)));
        record.setBankCode(trim(fields.get(15)));
        record.setBankName(trim(fields.get(16)));
        record.setBankNameKana(trim(fields.get(17)));
        record.setBranchCode(trim(fields.get(18)));
        record.setBranchName(trim(fields.get(19)));
        record.setBranchNameKana(trim(fields.get(20)));
        record.setAccountType(trim(fields.get(21)));
        record.setAccountNumber(trim(fields.get(22)));
        record.setContactLastName(trim(fields.get(23)));
        record.setContactFirstName(trim(fields.get(24)));
        record.setContactLastNameKana(trim(fields.get(25)));
        record.setContactFirstNameKana(trim(fields.get(26)));
        record.setMerchantType(trim(fields.get(27)));
        record.setFranchiseFlag(trim(fields.get(28)));
        record.setPaypayFcAgreementFlag(trim(fields.get(29)));
        record.setStoreCountApplied(parseIntegerChecked(fields.get(30), rowNum, "店舗数（申請数）", errors));
        record.setTerminalType(trim(fields.get(31)));
        record.setCorpNameEnglish(trim(fields.get(32)));
        record.setIndustryCategoryMajor(trim(fields.get(33)));
        record.setIndustryCategoryMinor(trim(fields.get(34)));
        record.setCorporateNumber(trim(fields.get(35)));
        record.setRepLastNameEnglish(trim(fields.get(36)));
        record.setRepFirstNameEnglish(trim(fields.get(37)));
        record.setGender(trim(fields.get(38)));
        record.setRepAddrPrefKana(trim(fields.get(39)));
        record.setRepAddrCityKana(trim(fields.get(40)));
        record.setRepAddrTownKana(trim(fields.get(41)));
        record.setRepAddrBlockKana(trim(fields.get(42)));
        record.setRepAddrBuildingKana(trim(fields.get(43)));
        record.setDoorToDoorSalesFlag(trim(fields.get(44)));
        record.setContinuousServiceFlag(trim(fields.get(45)));
        record.setTelemarketingSalesFlag(trim(fields.get(46)));
        record.setChainSalesFlag(trim(fields.get(47)));
        record.setBusinessOpportunitySalesFlag(trim(fields.get(48)));
        record.setPrepaidTransactionFlag(trim(fields.get(49)));
        record.setLegalViolationHistoryFlag(trim(fields.get(50)));
        record.setFcStoreType(trim(fields.get(51)));
        record.setRepresentativeStoreFlag(trim(fields.get(52)));
        record.setStoreIndustryMajor(trim(fields.get(53)));
        record.setStoreIndustryMinor(trim(fields.get(54)));
        record.setSecondhandDealerLicenseNumber(trim(fields.get(55)));
        record.setStoreNameEnglish(trim(fields.get(56)));
        record.setMapListingFlag(trim(fields.get(57)));
        record.setMapListingDesiredDateDpayRakuten(
                parseDateSlashChecked(fields.get(58), rowNum, "d払い・楽天Pay 地図掲載希望日", errors));
        record.setMapListingDesiredDatePaypayAupay(
                parseDateSlashChecked(fields.get(59), rowNum, "PayPay・auPay・ゆうちょPay・Alipay 地図掲載希望日", errors));
        record.setStoreImageListingFlag(trim(fields.get(60)));
        record.setStoreImageUrl(trim(fields.get(61)));
        record.setStoreIntroduction(trim(fields.get(62)));
        record.setFeeRateRakutenPay(
                parseBigDecimalChecked(fields.get(63), rowNum, "楽天Pay 加盟店手数料率（税込）", errors));
        record.setFeeRateLinePay(
                parseBigDecimalChecked(fields.get(64), rowNum, "LINE Pay 加盟店手数料率（税込）", errors));
        record.setFeeRatePaypay(parseBigDecimalChecked(fields.get(65), rowNum, "PayPay 加盟店手数料率（税込）", errors));
        record.setFeeRateDBarai(parseBigDecimalChecked(fields.get(66), rowNum, "d払い 加盟店手数料率（税込）", errors));
        record.setFeeRateAuPay(parseBigDecimalChecked(fields.get(67), rowNum, "auPay 加盟店手数料率（税込）", errors));
        record.setFeeRateMerpay(parseBigDecimalChecked(fields.get(68), rowNum, "メルペイ 加盟店手数料率（税込）", errors));
        record.setFeeRateYuchoPay(
                parseBigDecimalChecked(fields.get(69), rowNum, "ゆうちょPay 加盟店手数料率（税込）", errors));
        record.setFeeRateAeonPay(
                parseBigDecimalChecked(fields.get(70), rowNum, "AEONPay 加盟店手数料率（税込）", errors));
        record.setAtokaraRate(parseBigDecimalChecked(fields.get(71), rowNum, "アトカラ", errors));
        record.setFeeRateMdr1(parseBigDecimalChecked(fields.get(72), rowNum, "加盟店手数料率（1回）MDR", errors));
        record.setFeeRateMdr3(parseBigDecimalChecked(fields.get(73), rowNum, "加盟店手数料率（3回）MDR", errors));
        record.setFeeRateMdr4(parseBigDecimalChecked(fields.get(74), rowNum, "加盟店手数料率（4回）MDR+分割手数料", errors));
        record.setFeeRateInstallment5(parseBigDecimalChecked(fields.get(75), rowNum, "加盟店手数料率（5回）", errors));
        record.setFeeRateInstallment6(parseBigDecimalChecked(fields.get(76), rowNum, "加盟店手数料率（6回）", errors));
        record.setFeeRateInstallment10(
                parseBigDecimalChecked(fields.get(77), rowNum, "加盟店手数料率（10回）", errors));
        record.setFeeRateInstallment12(
                parseBigDecimalChecked(fields.get(78), rowNum, "加盟店手数料率（12回）", errors));
        record.setFeeRateInstallment15(
                parseBigDecimalChecked(fields.get(79), rowNum, "加盟店手数料率（15回）", errors));
        record.setFeeRateInstallment18(
                parseBigDecimalChecked(fields.get(80), rowNum, "加盟店手数料率（18回）", errors));
        record.setFeeRateInstallment20(
                parseBigDecimalChecked(fields.get(81), rowNum, "加盟店手数料率（20回）", errors));
        record.setFeeRateInstallment24(
                parseBigDecimalChecked(fields.get(82), rowNum, "加盟店手数料率（24回）", errors));
        record.setFeeRateInstallment30(
                parseBigDecimalChecked(fields.get(83), rowNum, "加盟店手数料率（30回）", errors));
        record.setFeeRateInstallment36(
                parseBigDecimalChecked(fields.get(84), rowNum, "加盟店手数料率（36回）", errors));
        record.setFeeRateWesmo(parseBigDecimalChecked(fields.get(85), rowNum, "Wesmo! 加盟店手数料率（非課税）", errors));
        record.setFeeRateBankPay(
                parseBigDecimalChecked(fields.get(86), rowNum, "BankPay 加盟店手数料率（非課税）", errors));
        record.setFeeRateWechat(
                parseBigDecimalChecked(fields.get(87), rowNum, "Wechat 加盟店手数料率（非課税）", errors));
        record.setFeeRateAlipay(
                parseBigDecimalChecked(fields.get(88), rowNum, "Alipay 加盟店手数料率（非課税）", errors));
        record.setFeeRateUnionpayQr(
                parseBigDecimalChecked(fields.get(89), rowNum, "銀聯QR 加盟店手数料率（非課税）", errors));
        record.setChangeNotes(trim(fields.get(90)));
        record.setSmccDepartment(trim(fields.get(91)));
        record.setSmccContactName(trim(fields.get(92)));
        record.setSmartCodeFlag(trim(fields.get(93)));
        record.setMkpFlag(trim(fields.get(94)));
        if (errors.size() > errorCountBeforeRow) {
            return; // この行にデータ変換エラーがあるため登録しない
        }
        record.setRegisteredDate(LocalDate.now());
        record.setUpdatedBy(batch.getUpdateEmployee());
        records.add(record);
    }

}
