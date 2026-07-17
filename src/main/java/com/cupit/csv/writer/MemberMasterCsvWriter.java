package com.cupit.csv.writer;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.cupit.model.MemberInfo;

/**
 * 加盟会員店マスターデータ CSV を m_member_info から書き出す。
 * ヘッダー列は「加盟会員店マスターデータ 登録・更新」CSVフォーマット仕様書と同じ
 * 255列・同じ並び順（{@link com.cupit.csv.importer.MemberInfoFileImporter}の逆変換）
 * とし、そのままアップロードし直せるようにする。
 * UTF-8（BOM付き）・CRLF・RFC4180準拠のクォート処理で出力する。
 */
@Component
public class MemberMasterCsvWriter {

    private static final DateTimeFormatter FMT_SLASH = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final String HEADER_LINE =
            "取引コード,親店コード,親店名,新取引コード,前取引コード,中コード,ブロックコード,入会日,社団法人フラグ,協同組合フラグ,登録支店補完期間from,資格区分,"
            + "登録支店補完期間to,店名,店名カナ,店名カナ略称,店名略称,都道府県コード,市区町村コード,都市名,所在地郵便番号,所在地都道府県,所在地都道府県カナ,所在地市区町村,"
            + "所在地市区町村カナ,所在地町名,所在地町名カナ,所在地丁目・番・番地・号,所在地丁目・番・番地・号カナ,所在地建物名・部屋番号,所在地建物名・部屋番号カナ,所在地電話番号,"
            + "所在地FAX番号,郵送先郵便番号,郵送先住所,郵送先電話番号,営業時間（平日）,営業時間（平日）備考,営業時間（その他）,営業時間（その他）備考,定休日,取扱品目,休業受付日,"
            + "休業開始日,休業終了日,休業期間連絡先,休業理由,承認者,配達地域の状況,無料配達地域1,有料配達地域1,無料配達地域2,有料配達地域2,備考,名義人カナ,名義人,名義人生年月日,"
            + "経営区分,法人格,法人名,法人格カナ,法人名カナ,法人郵便番号,法人所在地都道府県,法人所在地都道府県カナ,法人所在地市区町村,法人所在地市区町村カナ,法人所在地町名,"
            + "法人所在地町名カナ,法人所在地丁目・番・番地・号,法人所在地丁目・番・番地・号カナ,法人所在地建物名・部屋番号,法人所在地建物名・部屋番号カナ,代表者姓カナ,代表者名カナ,"
            + "代表者姓,代表者名,代表者生年月日,代表者役職,代表者郵便番号,代表者住所：都道府県,代表者住所：都道府県カナ,代表者住所：市区町村,代表者住所：市区町村カナ,代表者住所：町名,"
            + "代表者住所：町名カナ,代表者住所：丁目・番・番地・号,代表者住所：丁目・番・番地・号カナ,代表者住所：建物名・部屋番号,代表者住所：建物名・部屋番号カナ,連帯保証人氏名,"
            + "連帯保証人郵便番号,連帯保証人住所,資本金(円),加入申込書 常時使用従業員数(人),加入申込書 業種1,加入申込書 業種1割合(%),加入申込書 業種2,"
            + "加入申込書 業種2割合(%),加入申込書 業種3,加入申込書 業種3割合(%),登記簿謄本 役員1 役職,登記簿謄本 役員1 氏名,登記簿謄本 役員2 役職,"
            + "登記簿謄本 役員2 氏名,新コード適用日,コードNo変更通知日(当該店),コードNo変更通知日(支部),コードNo変更全国発送告知日,社団脱退区分,社団脱退処理日,"
            + "社団脱退 受付日,社団脱退当該店通知日,社団脱退日,社団脱退理由,協同組合脱退区分,協同組合脱退処理日,協同組合脱退 受付日,協同組合脱退当該店通知日,協同組合脱退日,"
            + "協同組合脱退理由,届出支店取引開始日,届出支店抹消フラグ,届出支店抹消日,届出支店抹消理由,理由カテゴリ入力欄,取引名簿,その他 返却,社団脱退理由区分,協同組合脱退理由区分,"
            + "稟議No,稟議書発行日,稟議承認日,契約書日,口座振込日,加盟金請求日,契約書受理日,支部報告送付日,請求書・契約書の公印依頼日,加盟金入金日,取次店ツール手配連絡日,"
            + "振興協会連絡日,親会社 法人名,親会社 年間売上（円),親会社 創業年月,親会社 営業暦,親会社 店舗数,親会社 従業員数,親会社 主たる業,親会社 年間仕入,"
            + "親会社 営業利益（円）,親会社 当期利益（円）,親会社 決算期間from,親会社 決算期間to,店舗 年間売上（円）,店舗 創業年月,店舗 営業暦,店舗数,店舗 従業員数,"
            + "店舗 主たる業,店舗 年間仕入（円）,店舗 営業利益（円）,店舗 当期利益（円）,店舗 決算期間from,店舗 決算期間to,売上構成(%) 生花,　　　　 鉢物,　　　　 資材,"
            + "　　　　 他,営業構成(%) 店売,　　　　　　稽古,　　　　　　仕事,　　　　　　他,店舗面積,従業員,\"従業員 内 , 家族\",配達車両,加入団体,決算書有無,"
            + "市場買上証明有無,店舗平面図有無,店舗写真有無,名義写真有無,取引銀行有無,支部幹事,支部,理事,印鑑証明有無,住民票有無,開業年月,申込み書類受付日,研修日,予備審査結果,"
            + "理事会審査結果,取次店支部結果,取次店稟議結果,取次店入金結果,印・本有無,印・保有無,役職理事,役職監事,役職総代,役職支部幹事,本部役職１,本部役職２,本部役職３,本部役職４,"
            + "本部役職５,支部役職１,支部役職２,支部役職３,支部役職４,支部役職５,本部出向交通費１,本部出向交通費２,精算書郵送先郵便番号,精算書郵送先住所,注文配達向け電話番号,"
            + "注文配達向け電話番号2,会員間取引メールアドレス,注文主連絡用メールアドレス,事務局連絡用メールアドレス,取引停止,注文機能制御設定日,配達機能制御設定日,"
            + "花キューピットタウンURL,直近店舗状況 営業暦,直近店舗状況 従業員数,直近店舗状況 店舗従業員数,直近店舗状況 主たる業務,直近店舗状況 決算期間From,"
            + "直近店舗状況 決算期間To,直近店舗状況 店舗面積,直近店舗状況 売上構成比(%) 生花,直近店舗状況 売上構成比(%) 鉢物,直近店舗状況 売上構成比(%) 資材,"
            + "直近店舗状況 売上構成比(%) その他,直近店舗状況 営業構成比(%) 店売,直近店舗状況 営業構成比(%) 稽古,直近店舗状況 営業構成比(%) 仕事,"
            + "直近店舗状況 営業構成比(%) 他,直近店舗状況 売上,直近店舗状況 仕入,直近店舗状況 営業利益,直近店舗状況 当期利益,直近店舗状況 配達車両,直近店舗状況 店舗立地,"
            + "直近店舗状況 会員間注文件数（年間）,直近店舗状況 会員間配達件数（年間）,直近店舗状況 会員間注文金額（年間）,直近店舗状況 会員間注文金額（年間）,店舗カテゴリ,緯度,経度,"
            + "花キューピットタウン参加区分";

    public byte[] writeCsv(List<MemberInfo> records) {
        StringBuilder csv = new StringBuilder();
        csv.append(HEADER_LINE).append("\r\n");
        for (MemberInfo info : records) {
            csv.append(toCsvLine(info));
        }
        byte[] body = csv.toString().getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[UTF8_BOM.length + body.length];
        System.arraycopy(UTF8_BOM, 0, result, 0, UTF8_BOM.length);
        System.arraycopy(body, 0, result, UTF8_BOM.length, body.length);
        return result;
    }

    private String toCsvLine(MemberInfo info) {
        List<String> fields = new ArrayList<>();
        fields.add(token(info.getTradeCode()));
        fields.add(token(info.getParentStoreCode()));
        fields.add(token(info.getParentStoreName()));
        fields.add(token(info.getNewTradeCode()));
        fields.add(token(info.getPrevTradeCode()));
        fields.add(token(info.getMidCode()));
        fields.add(token(info.getBlockCode()));
        fields.add(token(info.getJoinDate()));
        fields.add(token(info.getCorpAssocFlag()));
        fields.add(token(info.getCooperativeFlag()));
        fields.add(token(info.getBranchSupplementPeriodFrom()));
        fields.add(token(info.getQualificationType()));
        fields.add(token(info.getBranchSupplementPeriodTo()));
        fields.add(token(info.getStoreName()));
        fields.add(token(info.getStoreNameKana()));
        fields.add(token(info.getStoreNameKanaShort()));
        fields.add(token(info.getStoreNameShort()));
        fields.add(token(info.getPrefCode()));
        fields.add(token(info.getCityCode()));
        fields.add(token(info.getCityName()));
        fields.add(token(info.getAddrZip()));
        fields.add(token(info.getAddrPref()));
        fields.add(token(info.getAddrPrefKana()));
        fields.add(token(info.getAddrCity()));
        fields.add(token(info.getAddrCityKana()));
        fields.add(token(info.getAddrTown()));
        fields.add(token(info.getAddrTownKana()));
        fields.add(token(info.getAddrBlock()));
        fields.add(token(info.getAddrBlockKana()));
        fields.add(token(info.getAddrBuilding()));
        fields.add(token(info.getAddrBuildingKana()));
        fields.add(token(info.getAddrTel()));
        fields.add(token(info.getAddrFax()));
        fields.add(token(info.getMailZip()));
        fields.add(token(info.getMailAddress()));
        fields.add(token(info.getMailTel()));
        fields.add(token(info.getBusinessHoursWeekday()));
        fields.add(token(info.getBusinessHoursWeekdayNote()));
        fields.add(token(info.getBusinessHoursOther()));
        fields.add(token(info.getBusinessHoursOtherNote()));
        fields.add(token(info.getRegularHoliday()));
        fields.add(token(info.getHandlingItems()));
        fields.add(token(info.getClosureReceivedDate()));
        fields.add(token(info.getClosureStartDate()));
        fields.add(token(info.getClosureEndDate()));
        fields.add(token(info.getClosureContact()));
        fields.add(token(info.getClosureReason()));
        fields.add(token(info.getClosureApprover()));
        fields.add(token(info.getDeliveryAreaStatus()));
        fields.add(token(info.getFreeDeliveryArea1()));
        fields.add(token(info.getPaidDeliveryArea1()));
        fields.add(token(info.getFreeDeliveryArea2()));
        fields.add(token(info.getPaidDeliveryArea2()));
        fields.add(token(info.getRemarks()));
        fields.add(token(info.getAccountHolderKana()));
        fields.add(token(info.getAccountHolder()));
        fields.add(token(info.getAccountHolderBirth()));
        fields.add(token(info.getMgmtType()));
        fields.add(token(info.getCorpLegalForm()));
        fields.add(token(info.getCorpName()));
        fields.add(token(info.getCorpLegalFormKana()));
        fields.add(token(info.getCorpNameKana()));
        fields.add(token(info.getCorpZip()));
        fields.add(token(info.getCorpPref()));
        fields.add(token(info.getCorpPrefKana()));
        fields.add(token(info.getCorpCity()));
        fields.add(token(info.getCorpCityKana()));
        fields.add(token(info.getCorpTown()));
        fields.add(token(info.getCorpTownKana()));
        fields.add(token(info.getCorpBlock()));
        fields.add(token(info.getCorpBlockKana()));
        fields.add(token(info.getCorpBuilding()));
        fields.add(token(info.getCorpBuildingKana()));
        fields.add(token(info.getRepLastNameKana()));
        fields.add(token(info.getRepFirstNameKana()));
        fields.add(token(info.getRepLastName()));
        fields.add(token(info.getRepFirstName()));
        fields.add(token(info.getRepBirth()));
        fields.add(token(info.getRepPosition()));
        fields.add(token(info.getRepZip()));
        fields.add(token(info.getRepPref()));
        fields.add(token(info.getRepPrefKana()));
        fields.add(token(info.getRepCity()));
        fields.add(token(info.getRepCityKana()));
        fields.add(token(info.getRepTown()));
        fields.add(token(info.getRepTownKana()));
        fields.add(token(info.getRepBlock()));
        fields.add(token(info.getRepBlockKana()));
        fields.add(token(info.getRepBuilding()));
        fields.add(token(info.getRepBuildingKana()));
        fields.add(token(info.getGuarantorName()));
        fields.add(token(info.getGuarantorZip()));
        fields.add(token(info.getGuarantorAddress()));
        fields.add(token(info.getCapitalYen()));
        fields.add(token(info.getAppRegularEmployeeCount()));
        fields.add(token(info.getAppIndustry1()));
        fields.add(token(info.getAppIndustry1Ratio()));
        fields.add(token(info.getAppIndustry2()));
        fields.add(token(info.getAppIndustry2Ratio()));
        fields.add(token(info.getAppIndustry3()));
        fields.add(token(info.getAppIndustry3Ratio()));
        fields.add(token(info.getOfficer1Position()));
        fields.add(token(info.getOfficer1Name()));
        fields.add(token(info.getOfficer2Position()));
        fields.add(token(info.getOfficer2Name()));
        fields.add(token(info.getNewCodeApplyDate()));
        fields.add(token(info.getCodeChangeNotifyDateStore()));
        fields.add(token(info.getCodeChangeNotifyDateBranch()));
        fields.add(token(info.getCodeChangeNationwideNoticeDate()));
        fields.add(token(info.getCorpAssocWithdrawType()));
        fields.add(token(info.getCorpAssocWithdrawProcDate()));
        fields.add(token(info.getCorpAssocWithdrawReceivedDate()));
        fields.add(token(info.getCorpAssocWithdrawNotifyDate()));
        fields.add(token(info.getCorpAssocWithdrawDate()));
        fields.add(token(info.getCorpAssocWithdrawReason()));
        fields.add(token(info.getCooperativeWithdrawType()));
        fields.add(token(info.getCooperativeWithdrawProcDate()));
        fields.add(token(info.getCooperativeWithdrawReceivedDate()));
        fields.add(token(info.getCooperativeWithdrawNotifyDate()));
        fields.add(token(info.getCooperativeWithdrawDate()));
        fields.add(token(info.getCooperativeWithdrawReason()));
        fields.add(token(info.getBranchTradeStartDate()));
        fields.add(token(info.getBranchDeletedFlag()));
        fields.add(token(info.getBranchDeletedDate()));
        fields.add(token(info.getBranchDeletedReason()));
        fields.add(token(info.getReasonCategoryInput()));
        fields.add(token(info.getTradeDirectoryStatus()));
        fields.add(token(info.getOtherReturn()));
        fields.add(token(info.getCorpAssocWithdrawReasonType()));
        fields.add(token(info.getCooperativeWithdrawReasonType()));
        fields.add(token(info.getApprovalNo()));
        fields.add(token(info.getApprovalDocIssueDate()));
        fields.add(token(info.getApprovalApprovedDate()));
        fields.add(token(info.getContractDate()));
        fields.add(token(info.getBankTransferDate()));
        fields.add(token(info.getEntryFeeInvoiceDate()));
        fields.add(token(info.getContractReceivedDate()));
        fields.add(token(info.getBranchReportSendDate()));
        fields.add(token(info.getOfficialSealRequestDate()));
        fields.add(token(info.getEntryFeePaymentDate()));
        fields.add(token(info.getAgencyToolContactDate()));
        fields.add(token(info.getPromotionAssocContactDate()));
        fields.add(token(info.getParentCorpName()));
        fields.add(token(info.getParentAnnualSalesYen()));
        fields.add(token(info.getParentFoundedDate()));
        fields.add(token(info.getParentBusinessYears()));
        fields.add(token(info.getParentStoreCount()));
        fields.add(token(info.getParentEmployeeCount()));
        fields.add(token(info.getParentMainBusiness()));
        fields.add(token(info.getParentAnnualPurchase()));
        fields.add(token(info.getParentOperatingProfitYen()));
        fields.add(token(info.getParentNetIncomeYen()));
        fields.add(token(info.getParentFiscalPeriodFrom()));
        fields.add(token(info.getParentFiscalPeriodTo()));
        fields.add(token(info.getStoreAnnualSalesYen()));
        fields.add(token(info.getStoreFoundedDate()));
        fields.add(token(info.getStoreBusinessYears()));
        fields.add(token(info.getStoreCount()));
        fields.add(token(info.getStoreEmployeeCount()));
        fields.add(token(info.getStoreMainBusiness()));
        fields.add(token(info.getStoreAnnualPurchaseYen()));
        fields.add(token(info.getStoreOperatingProfitYen()));
        fields.add(token(info.getStoreNetIncomeYen()));
        fields.add(token(info.getStoreFiscalPeriodFrom()));
        fields.add(token(info.getStoreFiscalPeriodTo()));
        fields.add(token(info.getSalesRatioFreshFlower()));
        fields.add(token(info.getSalesRatioPottedPlant()));
        fields.add(token(info.getSalesRatioMaterial()));
        fields.add(token(info.getSalesRatioOther()));
        fields.add(token(info.getBusinessRatioStorefront()));
        fields.add(token(info.getBusinessRatioLesson()));
        fields.add(token(info.getBusinessRatioWork()));
        fields.add(token(info.getBusinessRatioOther()));
        fields.add(token(info.getStoreArea()));
        fields.add(token(info.getEmployeeCount()));
        fields.add(token(info.getEmployeeFamilyCount()));
        fields.add(token(info.getDeliveryVehicleCount()));
        fields.add(token(info.getMemberOrganization()));
        fields.add(token(info.getFinancialStatementExists()));
        fields.add(token(info.getMarketPurchaseCertExists()));
        fields.add(token(info.getStoreFloorPlanExists()));
        fields.add(token(info.getStorePhotoExists()));
        fields.add(token(info.getNamePhotoExists()));
        fields.add(token(info.getBankAccountExists()));
        fields.add(token(info.getBranchSecretary()));
        fields.add(token(info.getBranchName()));
        fields.add(token(info.getDirector()));
        fields.add(token(info.getSealCertExists()));
        fields.add(token(info.getResidentRecordExists()));
        fields.add(token(info.getOpeningDate()));
        fields.add(token(info.getApplicationReceivedDate()));
        fields.add(token(info.getTrainingDate()));
        fields.add(token(info.getPreliminaryReviewResult()));
        fields.add(token(info.getBoardReviewResult()));
        fields.add(token(info.getAgencyBranchResult()));
        fields.add(token(info.getAgencyApprovalResult()));
        fields.add(token(info.getAgencyPaymentResult()));
        fields.add(token(info.getSealOriginalExists()));
        fields.add(token(info.getSealCopyExists()));
        fields.add(token(info.getPositionDirector()));
        fields.add(token(info.getPositionAuditor()));
        fields.add(token(info.getPositionDelegate()));
        fields.add(token(info.getPositionBranchSecretary()));
        fields.add(token(info.getHqPosition1()));
        fields.add(token(info.getHqPosition2()));
        fields.add(token(info.getHqPosition3()));
        fields.add(token(info.getHqPosition4()));
        fields.add(token(info.getHqPosition5()));
        fields.add(token(info.getBranchPosition1()));
        fields.add(token(info.getBranchPosition2()));
        fields.add(token(info.getBranchPosition3()));
        fields.add(token(info.getBranchPosition4()));
        fields.add(token(info.getBranchPosition5()));
        fields.add(token(info.getHqDispatchTransportFee1()));
        fields.add(token(info.getHqDispatchTransportFee2()));
        fields.add(token(info.getSettlementMailZip()));
        fields.add(token(info.getSettlementMailAddress()));
        fields.add(token(info.getOrderDeliveryTel()));
        fields.add(token(info.getOrderDeliveryTel2()));
        fields.add(token(info.getMemberTradeEmail()));
        fields.add(token(info.getOrderContactEmail()));
        fields.add(token(info.getOfficeContactEmail()));
        fields.add(token(info.getTradeStopped()));
        fields.add(token(info.getOrderFuncControlDate()));
        fields.add(token(info.getDeliveryFuncControlDate()));
        fields.add(token(info.getHcpTownUrl()));
        fields.add(token(info.getRecentBusinessYears()));
        fields.add(token(info.getRecentEmployeeCount()));
        fields.add(token(info.getRecentStoreEmployeeCount()));
        fields.add(token(info.getRecentMainBusiness()));
        fields.add(token(info.getRecentFiscalPeriodFrom()));
        fields.add(token(info.getRecentFiscalPeriodTo()));
        fields.add(token(info.getRecentStoreArea()));
        fields.add(token(info.getRecentSalesRatioFreshFlower()));
        fields.add(token(info.getRecentSalesRatioPottedPlant()));
        fields.add(token(info.getRecentSalesRatioMaterial()));
        fields.add(token(info.getRecentSalesRatioOther()));
        fields.add(token(info.getRecentBusinessRatioStorefront()));
        fields.add(token(info.getRecentBusinessRatioLesson()));
        fields.add(token(info.getRecentBusinessRatioWork()));
        fields.add(token(info.getRecentBusinessRatioOther()));
        fields.add(token(info.getRecentSales()));
        fields.add(token(info.getRecentPurchase()));
        fields.add(token(info.getRecentOperatingProfit()));
        fields.add(token(info.getRecentNetIncome()));
        fields.add(token(info.getRecentDeliveryVehicleCount()));
        fields.add(token(info.getRecentStoreLocation()));
        fields.add(token(info.getRecentMemberOrderCountYearly()));
        fields.add(token(info.getRecentMemberDeliveryCountYearly()));
        fields.add(token(info.getRecentMemberOrderAmountYearly()));
        fields.add(token(info.getRecentMemberOrderAmountYearly2()));
        fields.add(token(info.getStoreCategory()));
        fields.add(token(info.getLatitude()));
        fields.add(token(info.getLongitude()));
        fields.add(token(info.getHcpTownStatus()));
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
