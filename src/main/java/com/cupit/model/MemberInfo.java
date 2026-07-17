package com.cupit.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 会員情報を表すエンティティ。
 * m_member_info テーブルの 1 行に対応する。
 */
@Entity
@Table(name = "m_member_info")
public class MemberInfo {

    @Id
    @Column(name = "trade_code")
    private String tradeCode;

    @Column(name = "parent_store_code")
    private String parentStoreCode;

    @Column(name = "parent_store_name")
    private String parentStoreName;

    @Column(name = "new_trade_code")
    private String newTradeCode;

    @Column(name = "prev_trade_code")
    private String prevTradeCode;

    @Column(name = "mid_code")
    private Short midCode;

    @Column(name = "block_code")
    private String blockCode;

    @Column(name = "join_date")
    private LocalDate joinDate;

    @Column(name = "corp_assoc_flag")
    private String corpAssocFlag;

    @Column(name = "cooperative_flag")
    private String cooperativeFlag;

    @Column(name = "branch_supplement_period_from")
    private LocalDate branchSupplementPeriodFrom;

    @Column(name = "qualification_type")
    private String qualificationType;

    @Column(name = "branch_supplement_period_to")
    private LocalDate branchSupplementPeriodTo;

    @Column(name = "store_name")
    private String storeName;

    @Column(name = "store_name_kana")
    private String storeNameKana;

    @Column(name = "store_name_kana_short")
    private String storeNameKanaShort;

    @Column(name = "store_name_short")
    private String storeNameShort;

    @Column(name = "pref_code")
    private Short prefCode;

    @Column(name = "city_code")
    private Integer cityCode;

    @Column(name = "city_name")
    private String cityName;

    @Column(name = "addr_zip")
    private String addrZip;

    @Column(name = "addr_pref")
    private String addrPref;

    @Column(name = "addr_pref_kana")
    private String addrPrefKana;

    @Column(name = "addr_city")
    private String addrCity;

    @Column(name = "addr_city_kana")
    private String addrCityKana;

    @Column(name = "addr_town")
    private String addrTown;

    @Column(name = "addr_town_kana")
    private String addrTownKana;

    @Column(name = "addr_block")
    private String addrBlock;

    @Column(name = "addr_block_kana")
    private String addrBlockKana;

    @Column(name = "addr_building")
    private String addrBuilding;

    @Column(name = "addr_building_kana")
    private String addrBuildingKana;

    @Column(name = "addr_tel")
    private String addrTel;

    @Column(name = "addr_fax")
    private String addrFax;

    @Column(name = "mail_zip")
    private String mailZip;

    @Column(name = "mail_address")
    private String mailAddress;

    @Column(name = "mail_tel")
    private String mailTel;

    @Column(name = "business_hours_weekday")
    private String businessHoursWeekday;

    @Column(name = "business_hours_weekday_note")
    private String businessHoursWeekdayNote;

    @Column(name = "business_hours_other")
    private String businessHoursOther;

    @Column(name = "business_hours_other_note")
    private String businessHoursOtherNote;

    @Column(name = "regular_holiday")
    private String regularHoliday;

    @Column(name = "handling_items")
    private String handlingItems;

    @Column(name = "closure_received_date")
    private LocalDate closureReceivedDate;

    @Column(name = "closure_start_date")
    private LocalDate closureStartDate;

    @Column(name = "closure_end_date")
    private LocalDate closureEndDate;

    @Column(name = "closure_contact")
    private String closureContact;

    @Column(name = "closure_reason")
    private String closureReason;

    @Column(name = "closure_approver")
    private String closureApprover;

    @Column(name = "delivery_area_status")
    private String deliveryAreaStatus;

    @Column(name = "free_delivery_area_1")
    private String freeDeliveryArea1;

    @Column(name = "paid_delivery_area_1")
    private String paidDeliveryArea1;

    @Column(name = "free_delivery_area_2")
    private String freeDeliveryArea2;

    @Column(name = "paid_delivery_area_2")
    private String paidDeliveryArea2;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "account_holder_kana")
    private String accountHolderKana;

    @Column(name = "account_holder")
    private String accountHolder;

    @Column(name = "account_holder_birth")
    private String accountHolderBirth;

    @Column(name = "mgmt_type")
    private String mgmtType;

    @Column(name = "corp_legal_form")
    private String corpLegalForm;

    @Column(name = "corp_name")
    private String corpName;

    @Column(name = "corp_legal_form_kana")
    private String corpLegalFormKana;

    @Column(name = "corp_name_kana")
    private String corpNameKana;

    @Column(name = "corp_zip")
    private String corpZip;

    @Column(name = "corp_pref")
    private String corpPref;

    @Column(name = "corp_pref_kana")
    private String corpPrefKana;

    @Column(name = "corp_city")
    private String corpCity;

    @Column(name = "corp_city_kana")
    private String corpCityKana;

    @Column(name = "corp_town")
    private String corpTown;

    @Column(name = "corp_town_kana")
    private String corpTownKana;

    @Column(name = "corp_block")
    private String corpBlock;

    @Column(name = "corp_block_kana")
    private String corpBlockKana;

    @Column(name = "corp_building")
    private String corpBuilding;

    @Column(name = "corp_building_kana")
    private String corpBuildingKana;

    @Column(name = "rep_last_name_kana")
    private String repLastNameKana;

    @Column(name = "rep_first_name_kana")
    private String repFirstNameKana;

    @Column(name = "rep_last_name")
    private String repLastName;

    @Column(name = "rep_first_name")
    private String repFirstName;

    @Column(name = "rep_birth")
    private String repBirth;

    @Column(name = "rep_position")
    private String repPosition;

    @Column(name = "rep_zip")
    private String repZip;

    @Column(name = "rep_pref")
    private String repPref;

    @Column(name = "rep_pref_kana")
    private String repPrefKana;

    @Column(name = "rep_city")
    private String repCity;

    @Column(name = "rep_city_kana")
    private String repCityKana;

    @Column(name = "rep_town")
    private String repTown;

    @Column(name = "rep_town_kana")
    private String repTownKana;

    @Column(name = "rep_block")
    private String repBlock;

    @Column(name = "rep_block_kana")
    private String repBlockKana;

    @Column(name = "rep_building")
    private String repBuilding;

    @Column(name = "rep_building_kana")
    private String repBuildingKana;

    @Column(name = "guarantor_name")
    private String guarantorName;

    @Column(name = "guarantor_zip")
    private String guarantorZip;

    @Column(name = "guarantor_address")
    private String guarantorAddress;

    @Column(name = "capital_yen")
    private Long capitalYen;

    @Column(name = "app_regular_employee_count")
    private Integer appRegularEmployeeCount;

    @Column(name = "app_industry_1")
    private String appIndustry1;

    @Column(name = "app_industry_1_ratio")
    private BigDecimal appIndustry1Ratio;

    @Column(name = "app_industry_2")
    private String appIndustry2;

    @Column(name = "app_industry_2_ratio")
    private BigDecimal appIndustry2Ratio;

    @Column(name = "app_industry_3")
    private String appIndustry3;

    @Column(name = "app_industry_3_ratio")
    private BigDecimal appIndustry3Ratio;

    @Column(name = "officer_1_position")
    private String officer1Position;

    @Column(name = "officer_1_name")
    private String officer1Name;

    @Column(name = "officer_2_position")
    private String officer2Position;

    @Column(name = "officer_2_name")
    private String officer2Name;

    @Column(name = "new_code_apply_date")
    private String newCodeApplyDate;

    @Column(name = "code_change_notify_date_store")
    private LocalDate codeChangeNotifyDateStore;

    @Column(name = "code_change_notify_date_branch")
    private LocalDate codeChangeNotifyDateBranch;

    @Column(name = "code_change_nationwide_notice_date")
    private LocalDate codeChangeNationwideNoticeDate;

    @Column(name = "corp_assoc_withdraw_type")
    private String corpAssocWithdrawType;

    @Column(name = "corp_assoc_withdraw_proc_date")
    private LocalDate corpAssocWithdrawProcDate;

    @Column(name = "corp_assoc_withdraw_received_date")
    private LocalDate corpAssocWithdrawReceivedDate;

    @Column(name = "corp_assoc_withdraw_notify_date")
    private LocalDate corpAssocWithdrawNotifyDate;

    @Column(name = "corp_assoc_withdraw_date")
    private LocalDate corpAssocWithdrawDate;

    @Column(name = "corp_assoc_withdraw_reason")
    private String corpAssocWithdrawReason;

    @Column(name = "cooperative_withdraw_type")
    private String cooperativeWithdrawType;

    @Column(name = "cooperative_withdraw_proc_date")
    private LocalDate cooperativeWithdrawProcDate;

    @Column(name = "cooperative_withdraw_received_date")
    private LocalDate cooperativeWithdrawReceivedDate;

    @Column(name = "cooperative_withdraw_notify_date")
    private LocalDate cooperativeWithdrawNotifyDate;

    @Column(name = "cooperative_withdraw_date")
    private LocalDate cooperativeWithdrawDate;

    @Column(name = "cooperative_withdraw_reason")
    private String cooperativeWithdrawReason;

    @Column(name = "branch_trade_start_date")
    private LocalDate branchTradeStartDate;

    @Column(name = "branch_deleted_flag")
    private String branchDeletedFlag;

    @Column(name = "branch_deleted_date")
    private LocalDate branchDeletedDate;

    @Column(name = "branch_deleted_reason")
    private String branchDeletedReason;

    @Column(name = "reason_category_input")
    private String reasonCategoryInput;

    @Column(name = "trade_directory_status")
    private String tradeDirectoryStatus;

    @Column(name = "other_return")
    private String otherReturn;

    @Column(name = "corp_assoc_withdraw_reason_type")
    private String corpAssocWithdrawReasonType;

    @Column(name = "cooperative_withdraw_reason_type")
    private String cooperativeWithdrawReasonType;

    @Column(name = "approval_no")
    private String approvalNo;

    @Column(name = "approval_doc_issue_date")
    private LocalDate approvalDocIssueDate;

    @Column(name = "approval_approved_date")
    private LocalDate approvalApprovedDate;

    @Column(name = "contract_date")
    private LocalDate contractDate;

    @Column(name = "bank_transfer_date")
    private LocalDate bankTransferDate;

    @Column(name = "entry_fee_invoice_date")
    private LocalDate entryFeeInvoiceDate;

    @Column(name = "contract_received_date")
    private LocalDate contractReceivedDate;

    @Column(name = "branch_report_send_date")
    private LocalDate branchReportSendDate;

    @Column(name = "official_seal_request_date")
    private LocalDate officialSealRequestDate;

    @Column(name = "entry_fee_payment_date")
    private LocalDate entryFeePaymentDate;

    @Column(name = "agency_tool_contact_date")
    private LocalDate agencyToolContactDate;

    @Column(name = "promotion_assoc_contact_date")
    private LocalDate promotionAssocContactDate;

    @Column(name = "parent_corp_name")
    private String parentCorpName;

    @Column(name = "parent_annual_sales_yen")
    private Long parentAnnualSalesYen;

    @Column(name = "parent_founded_date")
    private String parentFoundedDate;

    @Column(name = "parent_business_years")
    private String parentBusinessYears;

    @Column(name = "parent_store_count")
    private Integer parentStoreCount;

    @Column(name = "parent_employee_count")
    private Integer parentEmployeeCount;

    @Column(name = "parent_main_business")
    private String parentMainBusiness;

    @Column(name = "parent_annual_purchase")
    private Long parentAnnualPurchase;

    @Column(name = "parent_operating_profit_yen")
    private Long parentOperatingProfitYen;

    @Column(name = "parent_net_income_yen")
    private Long parentNetIncomeYen;

    @Column(name = "parent_fiscal_period_from")
    private LocalDate parentFiscalPeriodFrom;

    @Column(name = "parent_fiscal_period_to")
    private LocalDate parentFiscalPeriodTo;

    @Column(name = "store_annual_sales_yen")
    private Long storeAnnualSalesYen;

    @Column(name = "store_founded_date")
    private LocalDate storeFoundedDate;

    @Column(name = "store_business_years")
    private String storeBusinessYears;

    @Column(name = "store_count")
    private Integer storeCount;

    @Column(name = "store_employee_count")
    private Integer storeEmployeeCount;

    @Column(name = "store_main_business")
    private String storeMainBusiness;

    @Column(name = "store_annual_purchase_yen")
    private Long storeAnnualPurchaseYen;

    @Column(name = "store_operating_profit_yen")
    private Long storeOperatingProfitYen;

    @Column(name = "store_net_income_yen")
    private Long storeNetIncomeYen;

    @Column(name = "store_fiscal_period_from")
    private LocalDate storeFiscalPeriodFrom;

    @Column(name = "store_fiscal_period_to")
    private LocalDate storeFiscalPeriodTo;

    @Column(name = "sales_ratio_fresh_flower")
    private Long salesRatioFreshFlower;

    @Column(name = "sales_ratio_potted_plant")
    private Short salesRatioPottedPlant;

    @Column(name = "sales_ratio_material")
    private Short salesRatioMaterial;

    @Column(name = "sales_ratio_other")
    private Short salesRatioOther;

    @Column(name = "business_ratio_storefront")
    private BigDecimal businessRatioStorefront;

    @Column(name = "business_ratio_lesson")
    private Short businessRatioLesson;

    @Column(name = "business_ratio_work")
    private Short businessRatioWork;

    @Column(name = "business_ratio_other")
    private Short businessRatioOther;

    @Column(name = "store_area")
    private BigDecimal storeArea;

    @Column(name = "employee_count")
    private Integer employeeCount;

    @Column(name = "employee_family_count")
    private Integer employeeFamilyCount;

    @Column(name = "delivery_vehicle_count")
    private Short deliveryVehicleCount;

    @Column(name = "member_organization")
    private String memberOrganization;

    @Column(name = "financial_statement_exists")
    private String financialStatementExists;

    @Column(name = "market_purchase_cert_exists")
    private String marketPurchaseCertExists;

    @Column(name = "store_floor_plan_exists")
    private String storeFloorPlanExists;

    @Column(name = "store_photo_exists")
    private String storePhotoExists;

    @Column(name = "name_photo_exists")
    private String namePhotoExists;

    @Column(name = "bank_account_exists")
    private String bankAccountExists;

    @Column(name = "branch_secretary")
    private String branchSecretary;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "director")
    private String director;

    @Column(name = "seal_cert_exists")
    private String sealCertExists;

    @Column(name = "resident_record_exists")
    private String residentRecordExists;

    @Column(name = "opening_date")
    private LocalDate openingDate;

    @Column(name = "application_received_date")
    private LocalDate applicationReceivedDate;

    @Column(name = "training_date")
    private LocalDate trainingDate;

    @Column(name = "preliminary_review_result")
    private String preliminaryReviewResult;

    @Column(name = "board_review_result")
    private String boardReviewResult;

    @Column(name = "agency_branch_result")
    private String agencyBranchResult;

    @Column(name = "agency_approval_result")
    private String agencyApprovalResult;

    @Column(name = "agency_payment_result")
    private String agencyPaymentResult;

    @Column(name = "seal_original_exists")
    private String sealOriginalExists;

    @Column(name = "seal_copy_exists")
    private String sealCopyExists;

    @Column(name = "position_director")
    private String positionDirector;

    @Column(name = "position_auditor")
    private String positionAuditor;

    @Column(name = "position_delegate")
    private String positionDelegate;

    @Column(name = "position_branch_secretary")
    private String positionBranchSecretary;

    @Column(name = "hq_position_1")
    private String hqPosition1;

    @Column(name = "hq_position_2")
    private String hqPosition2;

    @Column(name = "hq_position_3")
    private String hqPosition3;

    @Column(name = "hq_position_4")
    private String hqPosition4;

    @Column(name = "hq_position_5")
    private String hqPosition5;

    @Column(name = "branch_position_1")
    private String branchPosition1;

    @Column(name = "branch_position_2")
    private String branchPosition2;

    @Column(name = "branch_position_3")
    private String branchPosition3;

    @Column(name = "branch_position_4")
    private String branchPosition4;

    @Column(name = "branch_position_5")
    private String branchPosition5;

    @Column(name = "hq_dispatch_transport_fee_1")
    private Long hqDispatchTransportFee1;

    @Column(name = "hq_dispatch_transport_fee_2")
    private Long hqDispatchTransportFee2;

    @Column(name = "settlement_mail_zip")
    private String settlementMailZip;

    @Column(name = "settlement_mail_address")
    private String settlementMailAddress;

    @Column(name = "order_delivery_tel")
    private String orderDeliveryTel;

    @Column(name = "order_delivery_tel_2")
    private String orderDeliveryTel2;

    @Column(name = "member_trade_email")
    private String memberTradeEmail;

    @Column(name = "order_contact_email")
    private String orderContactEmail;

    @Column(name = "office_contact_email")
    private String officeContactEmail;

    @Column(name = "trade_stopped")
    private String tradeStopped;

    @Column(name = "order_func_control_date")
    private LocalDate orderFuncControlDate;

    @Column(name = "delivery_func_control_date")
    private LocalDate deliveryFuncControlDate;

    @Column(name = "hcp_town_url")
    private String hcpTownUrl;

    @Column(name = "recent_business_years")
    private String recentBusinessYears;

    @Column(name = "recent_employee_count")
    private Integer recentEmployeeCount;

    @Column(name = "recent_store_employee_count")
    private Integer recentStoreEmployeeCount;

    @Column(name = "recent_main_business")
    private String recentMainBusiness;

    @Column(name = "recent_fiscal_period_from")
    private LocalDate recentFiscalPeriodFrom;

    @Column(name = "recent_fiscal_period_to")
    private LocalDate recentFiscalPeriodTo;

    @Column(name = "recent_store_area")
    private String recentStoreArea;

    @Column(name = "recent_sales_ratio_fresh_flower")
    private Long recentSalesRatioFreshFlower;

    @Column(name = "recent_sales_ratio_potted_plant")
    private Long recentSalesRatioPottedPlant;

    @Column(name = "recent_sales_ratio_material")
    private Long recentSalesRatioMaterial;

    @Column(name = "recent_sales_ratio_other")
    private Long recentSalesRatioOther;

    @Column(name = "recent_business_ratio_storefront")
    private BigDecimal recentBusinessRatioStorefront;

    @Column(name = "recent_business_ratio_lesson")
    private BigDecimal recentBusinessRatioLesson;

    @Column(name = "recent_business_ratio_work")
    private BigDecimal recentBusinessRatioWork;

    @Column(name = "recent_business_ratio_other")
    private BigDecimal recentBusinessRatioOther;

    @Column(name = "recent_sales")
    private Long recentSales;

    @Column(name = "recent_purchase")
    private Long recentPurchase;

    @Column(name = "recent_operating_profit")
    private Long recentOperatingProfit;

    @Column(name = "recent_net_income")
    private Long recentNetIncome;

    @Column(name = "recent_delivery_vehicle_count")
    private Short recentDeliveryVehicleCount;

    @Column(name = "recent_store_location")
    private String recentStoreLocation;

    @Column(name = "recent_member_order_count_yearly")
    private Integer recentMemberOrderCountYearly;

    @Column(name = "recent_member_delivery_count_yearly")
    private Integer recentMemberDeliveryCountYearly;

    @Column(name = "recent_member_order_amount_yearly")
    private Long recentMemberOrderAmountYearly;

    @Column(name = "recent_member_order_amount_yearly_2")
    private Long recentMemberOrderAmountYearly2;

    @Column(name = "store_category")
    private String storeCategory;

    @Column(name = "latitude")
    private BigDecimal latitude;

    @Column(name = "longitude")
    private BigDecimal longitude;

    @Column(name = "hcp_town_status")
    private String hcpTownStatus;

    @Column(name = "create_date", nullable = false)
    private LocalDate createDate;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    @Column(name = "update_employee")
    private String updateEmployee;

    @Column(name = "member_type")
    private String memberType;

    @Column(name = "middle_code")
    private String middleCode;

    @Column(name = "corporation_flag")
    private String corporationFlag;

    @Column(name = "representative_name")
    private String representativeName;

    @Column(name = "representative_kana")
    private String representativeKana;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "address")
    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;

    public String getTradeCode() {
        return tradeCode;
    }

    public void setTradeCode(String tradeCode) {
        this.tradeCode = tradeCode;
    }

    public String getParentStoreCode() {
        return parentStoreCode;
    }

    public void setParentStoreCode(String parentStoreCode) {
        this.parentStoreCode = parentStoreCode;
    }

    public String getParentStoreName() {
        return parentStoreName;
    }

    public void setParentStoreName(String parentStoreName) {
        this.parentStoreName = parentStoreName;
    }

    public String getNewTradeCode() {
        return newTradeCode;
    }

    public void setNewTradeCode(String newTradeCode) {
        this.newTradeCode = newTradeCode;
    }

    public String getPrevTradeCode() {
        return prevTradeCode;
    }

    public void setPrevTradeCode(String prevTradeCode) {
        this.prevTradeCode = prevTradeCode;
    }

    public Short getMidCode() {
        return midCode;
    }

    public void setMidCode(Short midCode) {
        this.midCode = midCode;
    }

    public String getBlockCode() {
        return blockCode;
    }

    public void setBlockCode(String blockCode) {
        this.blockCode = blockCode;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public String getCorpAssocFlag() {
        return corpAssocFlag;
    }

    public void setCorpAssocFlag(String corpAssocFlag) {
        this.corpAssocFlag = corpAssocFlag;
    }

    public String getCooperativeFlag() {
        return cooperativeFlag;
    }

    public void setCooperativeFlag(String cooperativeFlag) {
        this.cooperativeFlag = cooperativeFlag;
    }

    public LocalDate getBranchSupplementPeriodFrom() {
        return branchSupplementPeriodFrom;
    }

    public void setBranchSupplementPeriodFrom(LocalDate branchSupplementPeriodFrom) {
        this.branchSupplementPeriodFrom = branchSupplementPeriodFrom;
    }

    public String getQualificationType() {
        return qualificationType;
    }

    public void setQualificationType(String qualificationType) {
        this.qualificationType = qualificationType;
    }

    public LocalDate getBranchSupplementPeriodTo() {
        return branchSupplementPeriodTo;
    }

    public void setBranchSupplementPeriodTo(LocalDate branchSupplementPeriodTo) {
        this.branchSupplementPeriodTo = branchSupplementPeriodTo;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreNameKana() {
        return storeNameKana;
    }

    public void setStoreNameKana(String storeNameKana) {
        this.storeNameKana = storeNameKana;
    }

    public String getStoreNameKanaShort() {
        return storeNameKanaShort;
    }

    public void setStoreNameKanaShort(String storeNameKanaShort) {
        this.storeNameKanaShort = storeNameKanaShort;
    }

    public String getStoreNameShort() {
        return storeNameShort;
    }

    public void setStoreNameShort(String storeNameShort) {
        this.storeNameShort = storeNameShort;
    }

    public Short getPrefCode() {
        return prefCode;
    }

    public void setPrefCode(Short prefCode) {
        this.prefCode = prefCode;
    }

    public Integer getCityCode() {
        return cityCode;
    }

    public void setCityCode(Integer cityCode) {
        this.cityCode = cityCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getAddrZip() {
        return addrZip;
    }

    public void setAddrZip(String addrZip) {
        this.addrZip = addrZip;
    }

    public String getAddrPref() {
        return addrPref;
    }

    public void setAddrPref(String addrPref) {
        this.addrPref = addrPref;
    }

    public String getAddrPrefKana() {
        return addrPrefKana;
    }

    public void setAddrPrefKana(String addrPrefKana) {
        this.addrPrefKana = addrPrefKana;
    }

    public String getAddrCity() {
        return addrCity;
    }

    public void setAddrCity(String addrCity) {
        this.addrCity = addrCity;
    }

    public String getAddrCityKana() {
        return addrCityKana;
    }

    public void setAddrCityKana(String addrCityKana) {
        this.addrCityKana = addrCityKana;
    }

    public String getAddrTown() {
        return addrTown;
    }

    public void setAddrTown(String addrTown) {
        this.addrTown = addrTown;
    }

    public String getAddrTownKana() {
        return addrTownKana;
    }

    public void setAddrTownKana(String addrTownKana) {
        this.addrTownKana = addrTownKana;
    }

    public String getAddrBlock() {
        return addrBlock;
    }

    public void setAddrBlock(String addrBlock) {
        this.addrBlock = addrBlock;
    }

    public String getAddrBlockKana() {
        return addrBlockKana;
    }

    public void setAddrBlockKana(String addrBlockKana) {
        this.addrBlockKana = addrBlockKana;
    }

    public String getAddrBuilding() {
        return addrBuilding;
    }

    public void setAddrBuilding(String addrBuilding) {
        this.addrBuilding = addrBuilding;
    }

    public String getAddrBuildingKana() {
        return addrBuildingKana;
    }

    public void setAddrBuildingKana(String addrBuildingKana) {
        this.addrBuildingKana = addrBuildingKana;
    }

    public String getAddrTel() {
        return addrTel;
    }

    public void setAddrTel(String addrTel) {
        this.addrTel = addrTel;
    }

    public String getAddrFax() {
        return addrFax;
    }

    public void setAddrFax(String addrFax) {
        this.addrFax = addrFax;
    }

    public String getMailZip() {
        return mailZip;
    }

    public void setMailZip(String mailZip) {
        this.mailZip = mailZip;
    }

    public String getMailAddress() {
        return mailAddress;
    }

    public void setMailAddress(String mailAddress) {
        this.mailAddress = mailAddress;
    }

    public String getMailTel() {
        return mailTel;
    }

    public void setMailTel(String mailTel) {
        this.mailTel = mailTel;
    }

    public String getBusinessHoursWeekday() {
        return businessHoursWeekday;
    }

    public void setBusinessHoursWeekday(String businessHoursWeekday) {
        this.businessHoursWeekday = businessHoursWeekday;
    }

    public String getBusinessHoursWeekdayNote() {
        return businessHoursWeekdayNote;
    }

    public void setBusinessHoursWeekdayNote(String businessHoursWeekdayNote) {
        this.businessHoursWeekdayNote = businessHoursWeekdayNote;
    }

    public String getBusinessHoursOther() {
        return businessHoursOther;
    }

    public void setBusinessHoursOther(String businessHoursOther) {
        this.businessHoursOther = businessHoursOther;
    }

    public String getBusinessHoursOtherNote() {
        return businessHoursOtherNote;
    }

    public void setBusinessHoursOtherNote(String businessHoursOtherNote) {
        this.businessHoursOtherNote = businessHoursOtherNote;
    }

    public String getRegularHoliday() {
        return regularHoliday;
    }

    public void setRegularHoliday(String regularHoliday) {
        this.regularHoliday = regularHoliday;
    }

    public String getHandlingItems() {
        return handlingItems;
    }

    public void setHandlingItems(String handlingItems) {
        this.handlingItems = handlingItems;
    }

    public LocalDate getClosureReceivedDate() {
        return closureReceivedDate;
    }

    public void setClosureReceivedDate(LocalDate closureReceivedDate) {
        this.closureReceivedDate = closureReceivedDate;
    }

    public LocalDate getClosureStartDate() {
        return closureStartDate;
    }

    public void setClosureStartDate(LocalDate closureStartDate) {
        this.closureStartDate = closureStartDate;
    }

    public LocalDate getClosureEndDate() {
        return closureEndDate;
    }

    public void setClosureEndDate(LocalDate closureEndDate) {
        this.closureEndDate = closureEndDate;
    }

    public String getClosureContact() {
        return closureContact;
    }

    public void setClosureContact(String closureContact) {
        this.closureContact = closureContact;
    }

    public String getClosureReason() {
        return closureReason;
    }

    public void setClosureReason(String closureReason) {
        this.closureReason = closureReason;
    }

    public String getClosureApprover() {
        return closureApprover;
    }

    public void setClosureApprover(String closureApprover) {
        this.closureApprover = closureApprover;
    }

    public String getDeliveryAreaStatus() {
        return deliveryAreaStatus;
    }

    public void setDeliveryAreaStatus(String deliveryAreaStatus) {
        this.deliveryAreaStatus = deliveryAreaStatus;
    }

    public String getFreeDeliveryArea1() {
        return freeDeliveryArea1;
    }

    public void setFreeDeliveryArea1(String freeDeliveryArea1) {
        this.freeDeliveryArea1 = freeDeliveryArea1;
    }

    public String getPaidDeliveryArea1() {
        return paidDeliveryArea1;
    }

    public void setPaidDeliveryArea1(String paidDeliveryArea1) {
        this.paidDeliveryArea1 = paidDeliveryArea1;
    }

    public String getFreeDeliveryArea2() {
        return freeDeliveryArea2;
    }

    public void setFreeDeliveryArea2(String freeDeliveryArea2) {
        this.freeDeliveryArea2 = freeDeliveryArea2;
    }

    public String getPaidDeliveryArea2() {
        return paidDeliveryArea2;
    }

    public void setPaidDeliveryArea2(String paidDeliveryArea2) {
        this.paidDeliveryArea2 = paidDeliveryArea2;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getAccountHolderKana() {
        return accountHolderKana;
    }

    public void setAccountHolderKana(String accountHolderKana) {
        this.accountHolderKana = accountHolderKana;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public void setAccountHolder(String accountHolder) {
        this.accountHolder = accountHolder;
    }

    public String getAccountHolderBirth() {
        return accountHolderBirth;
    }

    public void setAccountHolderBirth(String accountHolderBirth) {
        this.accountHolderBirth = accountHolderBirth;
    }

    public String getMgmtType() {
        return mgmtType;
    }

    public void setMgmtType(String mgmtType) {
        this.mgmtType = mgmtType;
    }

    public String getCorpLegalForm() {
        return corpLegalForm;
    }

    public void setCorpLegalForm(String corpLegalForm) {
        this.corpLegalForm = corpLegalForm;
    }

    public String getCorpName() {
        return corpName;
    }

    public void setCorpName(String corpName) {
        this.corpName = corpName;
    }

    public String getCorpLegalFormKana() {
        return corpLegalFormKana;
    }

    public void setCorpLegalFormKana(String corpLegalFormKana) {
        this.corpLegalFormKana = corpLegalFormKana;
    }

    public String getCorpNameKana() {
        return corpNameKana;
    }

    public void setCorpNameKana(String corpNameKana) {
        this.corpNameKana = corpNameKana;
    }

    public String getCorpZip() {
        return corpZip;
    }

    public void setCorpZip(String corpZip) {
        this.corpZip = corpZip;
    }

    public String getCorpPref() {
        return corpPref;
    }

    public void setCorpPref(String corpPref) {
        this.corpPref = corpPref;
    }

    public String getCorpPrefKana() {
        return corpPrefKana;
    }

    public void setCorpPrefKana(String corpPrefKana) {
        this.corpPrefKana = corpPrefKana;
    }

    public String getCorpCity() {
        return corpCity;
    }

    public void setCorpCity(String corpCity) {
        this.corpCity = corpCity;
    }

    public String getCorpCityKana() {
        return corpCityKana;
    }

    public void setCorpCityKana(String corpCityKana) {
        this.corpCityKana = corpCityKana;
    }

    public String getCorpTown() {
        return corpTown;
    }

    public void setCorpTown(String corpTown) {
        this.corpTown = corpTown;
    }

    public String getCorpTownKana() {
        return corpTownKana;
    }

    public void setCorpTownKana(String corpTownKana) {
        this.corpTownKana = corpTownKana;
    }

    public String getCorpBlock() {
        return corpBlock;
    }

    public void setCorpBlock(String corpBlock) {
        this.corpBlock = corpBlock;
    }

    public String getCorpBlockKana() {
        return corpBlockKana;
    }

    public void setCorpBlockKana(String corpBlockKana) {
        this.corpBlockKana = corpBlockKana;
    }

    public String getCorpBuilding() {
        return corpBuilding;
    }

    public void setCorpBuilding(String corpBuilding) {
        this.corpBuilding = corpBuilding;
    }

    public String getCorpBuildingKana() {
        return corpBuildingKana;
    }

    public void setCorpBuildingKana(String corpBuildingKana) {
        this.corpBuildingKana = corpBuildingKana;
    }

    public String getRepLastNameKana() {
        return repLastNameKana;
    }

    public void setRepLastNameKana(String repLastNameKana) {
        this.repLastNameKana = repLastNameKana;
    }

    public String getRepFirstNameKana() {
        return repFirstNameKana;
    }

    public void setRepFirstNameKana(String repFirstNameKana) {
        this.repFirstNameKana = repFirstNameKana;
    }

    public String getRepLastName() {
        return repLastName;
    }

    public void setRepLastName(String repLastName) {
        this.repLastName = repLastName;
    }

    public String getRepFirstName() {
        return repFirstName;
    }

    public void setRepFirstName(String repFirstName) {
        this.repFirstName = repFirstName;
    }

    public String getRepBirth() {
        return repBirth;
    }

    public void setRepBirth(String repBirth) {
        this.repBirth = repBirth;
    }

    public String getRepPosition() {
        return repPosition;
    }

    public void setRepPosition(String repPosition) {
        this.repPosition = repPosition;
    }

    public String getRepZip() {
        return repZip;
    }

    public void setRepZip(String repZip) {
        this.repZip = repZip;
    }

    public String getRepPref() {
        return repPref;
    }

    public void setRepPref(String repPref) {
        this.repPref = repPref;
    }

    public String getRepPrefKana() {
        return repPrefKana;
    }

    public void setRepPrefKana(String repPrefKana) {
        this.repPrefKana = repPrefKana;
    }

    public String getRepCity() {
        return repCity;
    }

    public void setRepCity(String repCity) {
        this.repCity = repCity;
    }

    public String getRepCityKana() {
        return repCityKana;
    }

    public void setRepCityKana(String repCityKana) {
        this.repCityKana = repCityKana;
    }

    public String getRepTown() {
        return repTown;
    }

    public void setRepTown(String repTown) {
        this.repTown = repTown;
    }

    public String getRepTownKana() {
        return repTownKana;
    }

    public void setRepTownKana(String repTownKana) {
        this.repTownKana = repTownKana;
    }

    public String getRepBlock() {
        return repBlock;
    }

    public void setRepBlock(String repBlock) {
        this.repBlock = repBlock;
    }

    public String getRepBlockKana() {
        return repBlockKana;
    }

    public void setRepBlockKana(String repBlockKana) {
        this.repBlockKana = repBlockKana;
    }

    public String getRepBuilding() {
        return repBuilding;
    }

    public void setRepBuilding(String repBuilding) {
        this.repBuilding = repBuilding;
    }

    public String getRepBuildingKana() {
        return repBuildingKana;
    }

    public void setRepBuildingKana(String repBuildingKana) {
        this.repBuildingKana = repBuildingKana;
    }

    public String getGuarantorName() {
        return guarantorName;
    }

    public void setGuarantorName(String guarantorName) {
        this.guarantorName = guarantorName;
    }

    public String getGuarantorZip() {
        return guarantorZip;
    }

    public void setGuarantorZip(String guarantorZip) {
        this.guarantorZip = guarantorZip;
    }

    public String getGuarantorAddress() {
        return guarantorAddress;
    }

    public void setGuarantorAddress(String guarantorAddress) {
        this.guarantorAddress = guarantorAddress;
    }

    public Long getCapitalYen() {
        return capitalYen;
    }

    public void setCapitalYen(Long capitalYen) {
        this.capitalYen = capitalYen;
    }

    public Integer getAppRegularEmployeeCount() {
        return appRegularEmployeeCount;
    }

    public void setAppRegularEmployeeCount(Integer appRegularEmployeeCount) {
        this.appRegularEmployeeCount = appRegularEmployeeCount;
    }

    public String getAppIndustry1() {
        return appIndustry1;
    }

    public void setAppIndustry1(String appIndustry1) {
        this.appIndustry1 = appIndustry1;
    }

    public BigDecimal getAppIndustry1Ratio() {
        return appIndustry1Ratio;
    }

    public void setAppIndustry1Ratio(BigDecimal appIndustry1Ratio) {
        this.appIndustry1Ratio = appIndustry1Ratio;
    }

    public String getAppIndustry2() {
        return appIndustry2;
    }

    public void setAppIndustry2(String appIndustry2) {
        this.appIndustry2 = appIndustry2;
    }

    public BigDecimal getAppIndustry2Ratio() {
        return appIndustry2Ratio;
    }

    public void setAppIndustry2Ratio(BigDecimal appIndustry2Ratio) {
        this.appIndustry2Ratio = appIndustry2Ratio;
    }

    public String getAppIndustry3() {
        return appIndustry3;
    }

    public void setAppIndustry3(String appIndustry3) {
        this.appIndustry3 = appIndustry3;
    }

    public BigDecimal getAppIndustry3Ratio() {
        return appIndustry3Ratio;
    }

    public void setAppIndustry3Ratio(BigDecimal appIndustry3Ratio) {
        this.appIndustry3Ratio = appIndustry3Ratio;
    }

    public String getOfficer1Position() {
        return officer1Position;
    }

    public void setOfficer1Position(String officer1Position) {
        this.officer1Position = officer1Position;
    }

    public String getOfficer1Name() {
        return officer1Name;
    }

    public void setOfficer1Name(String officer1Name) {
        this.officer1Name = officer1Name;
    }

    public String getOfficer2Position() {
        return officer2Position;
    }

    public void setOfficer2Position(String officer2Position) {
        this.officer2Position = officer2Position;
    }

    public String getOfficer2Name() {
        return officer2Name;
    }

    public void setOfficer2Name(String officer2Name) {
        this.officer2Name = officer2Name;
    }

    public String getNewCodeApplyDate() {
        return newCodeApplyDate;
    }

    public void setNewCodeApplyDate(String newCodeApplyDate) {
        this.newCodeApplyDate = newCodeApplyDate;
    }

    public LocalDate getCodeChangeNotifyDateStore() {
        return codeChangeNotifyDateStore;
    }

    public void setCodeChangeNotifyDateStore(LocalDate codeChangeNotifyDateStore) {
        this.codeChangeNotifyDateStore = codeChangeNotifyDateStore;
    }

    public LocalDate getCodeChangeNotifyDateBranch() {
        return codeChangeNotifyDateBranch;
    }

    public void setCodeChangeNotifyDateBranch(LocalDate codeChangeNotifyDateBranch) {
        this.codeChangeNotifyDateBranch = codeChangeNotifyDateBranch;
    }

    public LocalDate getCodeChangeNationwideNoticeDate() {
        return codeChangeNationwideNoticeDate;
    }

    public void setCodeChangeNationwideNoticeDate(LocalDate codeChangeNationwideNoticeDate) {
        this.codeChangeNationwideNoticeDate = codeChangeNationwideNoticeDate;
    }

    public String getCorpAssocWithdrawType() {
        return corpAssocWithdrawType;
    }

    public void setCorpAssocWithdrawType(String corpAssocWithdrawType) {
        this.corpAssocWithdrawType = corpAssocWithdrawType;
    }

    public LocalDate getCorpAssocWithdrawProcDate() {
        return corpAssocWithdrawProcDate;
    }

    public void setCorpAssocWithdrawProcDate(LocalDate corpAssocWithdrawProcDate) {
        this.corpAssocWithdrawProcDate = corpAssocWithdrawProcDate;
    }

    public LocalDate getCorpAssocWithdrawReceivedDate() {
        return corpAssocWithdrawReceivedDate;
    }

    public void setCorpAssocWithdrawReceivedDate(LocalDate corpAssocWithdrawReceivedDate) {
        this.corpAssocWithdrawReceivedDate = corpAssocWithdrawReceivedDate;
    }

    public LocalDate getCorpAssocWithdrawNotifyDate() {
        return corpAssocWithdrawNotifyDate;
    }

    public void setCorpAssocWithdrawNotifyDate(LocalDate corpAssocWithdrawNotifyDate) {
        this.corpAssocWithdrawNotifyDate = corpAssocWithdrawNotifyDate;
    }

    public LocalDate getCorpAssocWithdrawDate() {
        return corpAssocWithdrawDate;
    }

    public void setCorpAssocWithdrawDate(LocalDate corpAssocWithdrawDate) {
        this.corpAssocWithdrawDate = corpAssocWithdrawDate;
    }

    public String getCorpAssocWithdrawReason() {
        return corpAssocWithdrawReason;
    }

    public void setCorpAssocWithdrawReason(String corpAssocWithdrawReason) {
        this.corpAssocWithdrawReason = corpAssocWithdrawReason;
    }

    public String getCooperativeWithdrawType() {
        return cooperativeWithdrawType;
    }

    public void setCooperativeWithdrawType(String cooperativeWithdrawType) {
        this.cooperativeWithdrawType = cooperativeWithdrawType;
    }

    public LocalDate getCooperativeWithdrawProcDate() {
        return cooperativeWithdrawProcDate;
    }

    public void setCooperativeWithdrawProcDate(LocalDate cooperativeWithdrawProcDate) {
        this.cooperativeWithdrawProcDate = cooperativeWithdrawProcDate;
    }

    public LocalDate getCooperativeWithdrawReceivedDate() {
        return cooperativeWithdrawReceivedDate;
    }

    public void setCooperativeWithdrawReceivedDate(LocalDate cooperativeWithdrawReceivedDate) {
        this.cooperativeWithdrawReceivedDate = cooperativeWithdrawReceivedDate;
    }

    public LocalDate getCooperativeWithdrawNotifyDate() {
        return cooperativeWithdrawNotifyDate;
    }

    public void setCooperativeWithdrawNotifyDate(LocalDate cooperativeWithdrawNotifyDate) {
        this.cooperativeWithdrawNotifyDate = cooperativeWithdrawNotifyDate;
    }

    public LocalDate getCooperativeWithdrawDate() {
        return cooperativeWithdrawDate;
    }

    public void setCooperativeWithdrawDate(LocalDate cooperativeWithdrawDate) {
        this.cooperativeWithdrawDate = cooperativeWithdrawDate;
    }

    public String getCooperativeWithdrawReason() {
        return cooperativeWithdrawReason;
    }

    public void setCooperativeWithdrawReason(String cooperativeWithdrawReason) {
        this.cooperativeWithdrawReason = cooperativeWithdrawReason;
    }

    public LocalDate getBranchTradeStartDate() {
        return branchTradeStartDate;
    }

    public void setBranchTradeStartDate(LocalDate branchTradeStartDate) {
        this.branchTradeStartDate = branchTradeStartDate;
    }

    public String getBranchDeletedFlag() {
        return branchDeletedFlag;
    }

    public void setBranchDeletedFlag(String branchDeletedFlag) {
        this.branchDeletedFlag = branchDeletedFlag;
    }

    public LocalDate getBranchDeletedDate() {
        return branchDeletedDate;
    }

    public void setBranchDeletedDate(LocalDate branchDeletedDate) {
        this.branchDeletedDate = branchDeletedDate;
    }

    public String getBranchDeletedReason() {
        return branchDeletedReason;
    }

    public void setBranchDeletedReason(String branchDeletedReason) {
        this.branchDeletedReason = branchDeletedReason;
    }

    public String getReasonCategoryInput() {
        return reasonCategoryInput;
    }

    public void setReasonCategoryInput(String reasonCategoryInput) {
        this.reasonCategoryInput = reasonCategoryInput;
    }

    public String getTradeDirectoryStatus() {
        return tradeDirectoryStatus;
    }

    public void setTradeDirectoryStatus(String tradeDirectoryStatus) {
        this.tradeDirectoryStatus = tradeDirectoryStatus;
    }

    public String getOtherReturn() {
        return otherReturn;
    }

    public void setOtherReturn(String otherReturn) {
        this.otherReturn = otherReturn;
    }

    public String getCorpAssocWithdrawReasonType() {
        return corpAssocWithdrawReasonType;
    }

    public void setCorpAssocWithdrawReasonType(String corpAssocWithdrawReasonType) {
        this.corpAssocWithdrawReasonType = corpAssocWithdrawReasonType;
    }

    public String getCooperativeWithdrawReasonType() {
        return cooperativeWithdrawReasonType;
    }

    public void setCooperativeWithdrawReasonType(String cooperativeWithdrawReasonType) {
        this.cooperativeWithdrawReasonType = cooperativeWithdrawReasonType;
    }

    public String getApprovalNo() {
        return approvalNo;
    }

    public void setApprovalNo(String approvalNo) {
        this.approvalNo = approvalNo;
    }

    public LocalDate getApprovalDocIssueDate() {
        return approvalDocIssueDate;
    }

    public void setApprovalDocIssueDate(LocalDate approvalDocIssueDate) {
        this.approvalDocIssueDate = approvalDocIssueDate;
    }

    public LocalDate getApprovalApprovedDate() {
        return approvalApprovedDate;
    }

    public void setApprovalApprovedDate(LocalDate approvalApprovedDate) {
        this.approvalApprovedDate = approvalApprovedDate;
    }

    public LocalDate getContractDate() {
        return contractDate;
    }

    public void setContractDate(LocalDate contractDate) {
        this.contractDate = contractDate;
    }

    public LocalDate getBankTransferDate() {
        return bankTransferDate;
    }

    public void setBankTransferDate(LocalDate bankTransferDate) {
        this.bankTransferDate = bankTransferDate;
    }

    public LocalDate getEntryFeeInvoiceDate() {
        return entryFeeInvoiceDate;
    }

    public void setEntryFeeInvoiceDate(LocalDate entryFeeInvoiceDate) {
        this.entryFeeInvoiceDate = entryFeeInvoiceDate;
    }

    public LocalDate getContractReceivedDate() {
        return contractReceivedDate;
    }

    public void setContractReceivedDate(LocalDate contractReceivedDate) {
        this.contractReceivedDate = contractReceivedDate;
    }

    public LocalDate getBranchReportSendDate() {
        return branchReportSendDate;
    }

    public void setBranchReportSendDate(LocalDate branchReportSendDate) {
        this.branchReportSendDate = branchReportSendDate;
    }

    public LocalDate getOfficialSealRequestDate() {
        return officialSealRequestDate;
    }

    public void setOfficialSealRequestDate(LocalDate officialSealRequestDate) {
        this.officialSealRequestDate = officialSealRequestDate;
    }

    public LocalDate getEntryFeePaymentDate() {
        return entryFeePaymentDate;
    }

    public void setEntryFeePaymentDate(LocalDate entryFeePaymentDate) {
        this.entryFeePaymentDate = entryFeePaymentDate;
    }

    public LocalDate getAgencyToolContactDate() {
        return agencyToolContactDate;
    }

    public void setAgencyToolContactDate(LocalDate agencyToolContactDate) {
        this.agencyToolContactDate = agencyToolContactDate;
    }

    public LocalDate getPromotionAssocContactDate() {
        return promotionAssocContactDate;
    }

    public void setPromotionAssocContactDate(LocalDate promotionAssocContactDate) {
        this.promotionAssocContactDate = promotionAssocContactDate;
    }

    public String getParentCorpName() {
        return parentCorpName;
    }

    public void setParentCorpName(String parentCorpName) {
        this.parentCorpName = parentCorpName;
    }

    public Long getParentAnnualSalesYen() {
        return parentAnnualSalesYen;
    }

    public void setParentAnnualSalesYen(Long parentAnnualSalesYen) {
        this.parentAnnualSalesYen = parentAnnualSalesYen;
    }

    public String getParentFoundedDate() {
        return parentFoundedDate;
    }

    public void setParentFoundedDate(String parentFoundedDate) {
        this.parentFoundedDate = parentFoundedDate;
    }

    public String getParentBusinessYears() {
        return parentBusinessYears;
    }

    public void setParentBusinessYears(String parentBusinessYears) {
        this.parentBusinessYears = parentBusinessYears;
    }

    public Integer getParentStoreCount() {
        return parentStoreCount;
    }

    public void setParentStoreCount(Integer parentStoreCount) {
        this.parentStoreCount = parentStoreCount;
    }

    public Integer getParentEmployeeCount() {
        return parentEmployeeCount;
    }

    public void setParentEmployeeCount(Integer parentEmployeeCount) {
        this.parentEmployeeCount = parentEmployeeCount;
    }

    public String getParentMainBusiness() {
        return parentMainBusiness;
    }

    public void setParentMainBusiness(String parentMainBusiness) {
        this.parentMainBusiness = parentMainBusiness;
    }

    public Long getParentAnnualPurchase() {
        return parentAnnualPurchase;
    }

    public void setParentAnnualPurchase(Long parentAnnualPurchase) {
        this.parentAnnualPurchase = parentAnnualPurchase;
    }

    public Long getParentOperatingProfitYen() {
        return parentOperatingProfitYen;
    }

    public void setParentOperatingProfitYen(Long parentOperatingProfitYen) {
        this.parentOperatingProfitYen = parentOperatingProfitYen;
    }

    public Long getParentNetIncomeYen() {
        return parentNetIncomeYen;
    }

    public void setParentNetIncomeYen(Long parentNetIncomeYen) {
        this.parentNetIncomeYen = parentNetIncomeYen;
    }

    public LocalDate getParentFiscalPeriodFrom() {
        return parentFiscalPeriodFrom;
    }

    public void setParentFiscalPeriodFrom(LocalDate parentFiscalPeriodFrom) {
        this.parentFiscalPeriodFrom = parentFiscalPeriodFrom;
    }

    public LocalDate getParentFiscalPeriodTo() {
        return parentFiscalPeriodTo;
    }

    public void setParentFiscalPeriodTo(LocalDate parentFiscalPeriodTo) {
        this.parentFiscalPeriodTo = parentFiscalPeriodTo;
    }

    public Long getStoreAnnualSalesYen() {
        return storeAnnualSalesYen;
    }

    public void setStoreAnnualSalesYen(Long storeAnnualSalesYen) {
        this.storeAnnualSalesYen = storeAnnualSalesYen;
    }

    public LocalDate getStoreFoundedDate() {
        return storeFoundedDate;
    }

    public void setStoreFoundedDate(LocalDate storeFoundedDate) {
        this.storeFoundedDate = storeFoundedDate;
    }

    public String getStoreBusinessYears() {
        return storeBusinessYears;
    }

    public void setStoreBusinessYears(String storeBusinessYears) {
        this.storeBusinessYears = storeBusinessYears;
    }

    public Integer getStoreCount() {
        return storeCount;
    }

    public void setStoreCount(Integer storeCount) {
        this.storeCount = storeCount;
    }

    public Integer getStoreEmployeeCount() {
        return storeEmployeeCount;
    }

    public void setStoreEmployeeCount(Integer storeEmployeeCount) {
        this.storeEmployeeCount = storeEmployeeCount;
    }

    public String getStoreMainBusiness() {
        return storeMainBusiness;
    }

    public void setStoreMainBusiness(String storeMainBusiness) {
        this.storeMainBusiness = storeMainBusiness;
    }

    public Long getStoreAnnualPurchaseYen() {
        return storeAnnualPurchaseYen;
    }

    public void setStoreAnnualPurchaseYen(Long storeAnnualPurchaseYen) {
        this.storeAnnualPurchaseYen = storeAnnualPurchaseYen;
    }

    public Long getStoreOperatingProfitYen() {
        return storeOperatingProfitYen;
    }

    public void setStoreOperatingProfitYen(Long storeOperatingProfitYen) {
        this.storeOperatingProfitYen = storeOperatingProfitYen;
    }

    public Long getStoreNetIncomeYen() {
        return storeNetIncomeYen;
    }

    public void setStoreNetIncomeYen(Long storeNetIncomeYen) {
        this.storeNetIncomeYen = storeNetIncomeYen;
    }

    public LocalDate getStoreFiscalPeriodFrom() {
        return storeFiscalPeriodFrom;
    }

    public void setStoreFiscalPeriodFrom(LocalDate storeFiscalPeriodFrom) {
        this.storeFiscalPeriodFrom = storeFiscalPeriodFrom;
    }

    public LocalDate getStoreFiscalPeriodTo() {
        return storeFiscalPeriodTo;
    }

    public void setStoreFiscalPeriodTo(LocalDate storeFiscalPeriodTo) {
        this.storeFiscalPeriodTo = storeFiscalPeriodTo;
    }

    public Long getSalesRatioFreshFlower() {
        return salesRatioFreshFlower;
    }

    public void setSalesRatioFreshFlower(Long salesRatioFreshFlower) {
        this.salesRatioFreshFlower = salesRatioFreshFlower;
    }

    public Short getSalesRatioPottedPlant() {
        return salesRatioPottedPlant;
    }

    public void setSalesRatioPottedPlant(Short salesRatioPottedPlant) {
        this.salesRatioPottedPlant = salesRatioPottedPlant;
    }

    public Short getSalesRatioMaterial() {
        return salesRatioMaterial;
    }

    public void setSalesRatioMaterial(Short salesRatioMaterial) {
        this.salesRatioMaterial = salesRatioMaterial;
    }

    public Short getSalesRatioOther() {
        return salesRatioOther;
    }

    public void setSalesRatioOther(Short salesRatioOther) {
        this.salesRatioOther = salesRatioOther;
    }

    public BigDecimal getBusinessRatioStorefront() {
        return businessRatioStorefront;
    }

    public void setBusinessRatioStorefront(BigDecimal businessRatioStorefront) {
        this.businessRatioStorefront = businessRatioStorefront;
    }

    public Short getBusinessRatioLesson() {
        return businessRatioLesson;
    }

    public void setBusinessRatioLesson(Short businessRatioLesson) {
        this.businessRatioLesson = businessRatioLesson;
    }

    public Short getBusinessRatioWork() {
        return businessRatioWork;
    }

    public void setBusinessRatioWork(Short businessRatioWork) {
        this.businessRatioWork = businessRatioWork;
    }

    public Short getBusinessRatioOther() {
        return businessRatioOther;
    }

    public void setBusinessRatioOther(Short businessRatioOther) {
        this.businessRatioOther = businessRatioOther;
    }

    public BigDecimal getStoreArea() {
        return storeArea;
    }

    public void setStoreArea(BigDecimal storeArea) {
        this.storeArea = storeArea;
    }

    public Integer getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(Integer employeeCount) {
        this.employeeCount = employeeCount;
    }

    public Integer getEmployeeFamilyCount() {
        return employeeFamilyCount;
    }

    public void setEmployeeFamilyCount(Integer employeeFamilyCount) {
        this.employeeFamilyCount = employeeFamilyCount;
    }

    public Short getDeliveryVehicleCount() {
        return deliveryVehicleCount;
    }

    public void setDeliveryVehicleCount(Short deliveryVehicleCount) {
        this.deliveryVehicleCount = deliveryVehicleCount;
    }

    public String getMemberOrganization() {
        return memberOrganization;
    }

    public void setMemberOrganization(String memberOrganization) {
        this.memberOrganization = memberOrganization;
    }

    public String getFinancialStatementExists() {
        return financialStatementExists;
    }

    public void setFinancialStatementExists(String financialStatementExists) {
        this.financialStatementExists = financialStatementExists;
    }

    public String getMarketPurchaseCertExists() {
        return marketPurchaseCertExists;
    }

    public void setMarketPurchaseCertExists(String marketPurchaseCertExists) {
        this.marketPurchaseCertExists = marketPurchaseCertExists;
    }

    public String getStoreFloorPlanExists() {
        return storeFloorPlanExists;
    }

    public void setStoreFloorPlanExists(String storeFloorPlanExists) {
        this.storeFloorPlanExists = storeFloorPlanExists;
    }

    public String getStorePhotoExists() {
        return storePhotoExists;
    }

    public void setStorePhotoExists(String storePhotoExists) {
        this.storePhotoExists = storePhotoExists;
    }

    public String getNamePhotoExists() {
        return namePhotoExists;
    }

    public void setNamePhotoExists(String namePhotoExists) {
        this.namePhotoExists = namePhotoExists;
    }

    public String getBankAccountExists() {
        return bankAccountExists;
    }

    public void setBankAccountExists(String bankAccountExists) {
        this.bankAccountExists = bankAccountExists;
    }

    public String getBranchSecretary() {
        return branchSecretary;
    }

    public void setBranchSecretary(String branchSecretary) {
        this.branchSecretary = branchSecretary;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getSealCertExists() {
        return sealCertExists;
    }

    public void setSealCertExists(String sealCertExists) {
        this.sealCertExists = sealCertExists;
    }

    public String getResidentRecordExists() {
        return residentRecordExists;
    }

    public void setResidentRecordExists(String residentRecordExists) {
        this.residentRecordExists = residentRecordExists;
    }

    public LocalDate getOpeningDate() {
        return openingDate;
    }

    public void setOpeningDate(LocalDate openingDate) {
        this.openingDate = openingDate;
    }

    public LocalDate getApplicationReceivedDate() {
        return applicationReceivedDate;
    }

    public void setApplicationReceivedDate(LocalDate applicationReceivedDate) {
        this.applicationReceivedDate = applicationReceivedDate;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public void setTrainingDate(LocalDate trainingDate) {
        this.trainingDate = trainingDate;
    }

    public String getPreliminaryReviewResult() {
        return preliminaryReviewResult;
    }

    public void setPreliminaryReviewResult(String preliminaryReviewResult) {
        this.preliminaryReviewResult = preliminaryReviewResult;
    }

    public String getBoardReviewResult() {
        return boardReviewResult;
    }

    public void setBoardReviewResult(String boardReviewResult) {
        this.boardReviewResult = boardReviewResult;
    }

    public String getAgencyBranchResult() {
        return agencyBranchResult;
    }

    public void setAgencyBranchResult(String agencyBranchResult) {
        this.agencyBranchResult = agencyBranchResult;
    }

    public String getAgencyApprovalResult() {
        return agencyApprovalResult;
    }

    public void setAgencyApprovalResult(String agencyApprovalResult) {
        this.agencyApprovalResult = agencyApprovalResult;
    }

    public String getAgencyPaymentResult() {
        return agencyPaymentResult;
    }

    public void setAgencyPaymentResult(String agencyPaymentResult) {
        this.agencyPaymentResult = agencyPaymentResult;
    }

    public String getSealOriginalExists() {
        return sealOriginalExists;
    }

    public void setSealOriginalExists(String sealOriginalExists) {
        this.sealOriginalExists = sealOriginalExists;
    }

    public String getSealCopyExists() {
        return sealCopyExists;
    }

    public void setSealCopyExists(String sealCopyExists) {
        this.sealCopyExists = sealCopyExists;
    }

    public String getPositionDirector() {
        return positionDirector;
    }

    public void setPositionDirector(String positionDirector) {
        this.positionDirector = positionDirector;
    }

    public String getPositionAuditor() {
        return positionAuditor;
    }

    public void setPositionAuditor(String positionAuditor) {
        this.positionAuditor = positionAuditor;
    }

    public String getPositionDelegate() {
        return positionDelegate;
    }

    public void setPositionDelegate(String positionDelegate) {
        this.positionDelegate = positionDelegate;
    }

    public String getPositionBranchSecretary() {
        return positionBranchSecretary;
    }

    public void setPositionBranchSecretary(String positionBranchSecretary) {
        this.positionBranchSecretary = positionBranchSecretary;
    }

    public String getHqPosition1() {
        return hqPosition1;
    }

    public void setHqPosition1(String hqPosition1) {
        this.hqPosition1 = hqPosition1;
    }

    public String getHqPosition2() {
        return hqPosition2;
    }

    public void setHqPosition2(String hqPosition2) {
        this.hqPosition2 = hqPosition2;
    }

    public String getHqPosition3() {
        return hqPosition3;
    }

    public void setHqPosition3(String hqPosition3) {
        this.hqPosition3 = hqPosition3;
    }

    public String getHqPosition4() {
        return hqPosition4;
    }

    public void setHqPosition4(String hqPosition4) {
        this.hqPosition4 = hqPosition4;
    }

    public String getHqPosition5() {
        return hqPosition5;
    }

    public void setHqPosition5(String hqPosition5) {
        this.hqPosition5 = hqPosition5;
    }

    public String getBranchPosition1() {
        return branchPosition1;
    }

    public void setBranchPosition1(String branchPosition1) {
        this.branchPosition1 = branchPosition1;
    }

    public String getBranchPosition2() {
        return branchPosition2;
    }

    public void setBranchPosition2(String branchPosition2) {
        this.branchPosition2 = branchPosition2;
    }

    public String getBranchPosition3() {
        return branchPosition3;
    }

    public void setBranchPosition3(String branchPosition3) {
        this.branchPosition3 = branchPosition3;
    }

    public String getBranchPosition4() {
        return branchPosition4;
    }

    public void setBranchPosition4(String branchPosition4) {
        this.branchPosition4 = branchPosition4;
    }

    public String getBranchPosition5() {
        return branchPosition5;
    }

    public void setBranchPosition5(String branchPosition5) {
        this.branchPosition5 = branchPosition5;
    }

    public Long getHqDispatchTransportFee1() {
        return hqDispatchTransportFee1;
    }

    public void setHqDispatchTransportFee1(Long hqDispatchTransportFee1) {
        this.hqDispatchTransportFee1 = hqDispatchTransportFee1;
    }

    public Long getHqDispatchTransportFee2() {
        return hqDispatchTransportFee2;
    }

    public void setHqDispatchTransportFee2(Long hqDispatchTransportFee2) {
        this.hqDispatchTransportFee2 = hqDispatchTransportFee2;
    }

    public String getSettlementMailZip() {
        return settlementMailZip;
    }

    public void setSettlementMailZip(String settlementMailZip) {
        this.settlementMailZip = settlementMailZip;
    }

    public String getSettlementMailAddress() {
        return settlementMailAddress;
    }

    public void setSettlementMailAddress(String settlementMailAddress) {
        this.settlementMailAddress = settlementMailAddress;
    }

    public String getOrderDeliveryTel() {
        return orderDeliveryTel;
    }

    public void setOrderDeliveryTel(String orderDeliveryTel) {
        this.orderDeliveryTel = orderDeliveryTel;
    }

    public String getOrderDeliveryTel2() {
        return orderDeliveryTel2;
    }

    public void setOrderDeliveryTel2(String orderDeliveryTel2) {
        this.orderDeliveryTel2 = orderDeliveryTel2;
    }

    public String getMemberTradeEmail() {
        return memberTradeEmail;
    }

    public void setMemberTradeEmail(String memberTradeEmail) {
        this.memberTradeEmail = memberTradeEmail;
    }

    public String getOrderContactEmail() {
        return orderContactEmail;
    }

    public void setOrderContactEmail(String orderContactEmail) {
        this.orderContactEmail = orderContactEmail;
    }

    public String getOfficeContactEmail() {
        return officeContactEmail;
    }

    public void setOfficeContactEmail(String officeContactEmail) {
        this.officeContactEmail = officeContactEmail;
    }

    public String getTradeStopped() {
        return tradeStopped;
    }

    public void setTradeStopped(String tradeStopped) {
        this.tradeStopped = tradeStopped;
    }

    public LocalDate getOrderFuncControlDate() {
        return orderFuncControlDate;
    }

    public void setOrderFuncControlDate(LocalDate orderFuncControlDate) {
        this.orderFuncControlDate = orderFuncControlDate;
    }

    public LocalDate getDeliveryFuncControlDate() {
        return deliveryFuncControlDate;
    }

    public void setDeliveryFuncControlDate(LocalDate deliveryFuncControlDate) {
        this.deliveryFuncControlDate = deliveryFuncControlDate;
    }

    public String getHcpTownUrl() {
        return hcpTownUrl;
    }

    public void setHcpTownUrl(String hcpTownUrl) {
        this.hcpTownUrl = hcpTownUrl;
    }

    public String getRecentBusinessYears() {
        return recentBusinessYears;
    }

    public void setRecentBusinessYears(String recentBusinessYears) {
        this.recentBusinessYears = recentBusinessYears;
    }

    public Integer getRecentEmployeeCount() {
        return recentEmployeeCount;
    }

    public void setRecentEmployeeCount(Integer recentEmployeeCount) {
        this.recentEmployeeCount = recentEmployeeCount;
    }

    public Integer getRecentStoreEmployeeCount() {
        return recentStoreEmployeeCount;
    }

    public void setRecentStoreEmployeeCount(Integer recentStoreEmployeeCount) {
        this.recentStoreEmployeeCount = recentStoreEmployeeCount;
    }

    public String getRecentMainBusiness() {
        return recentMainBusiness;
    }

    public void setRecentMainBusiness(String recentMainBusiness) {
        this.recentMainBusiness = recentMainBusiness;
    }

    public LocalDate getRecentFiscalPeriodFrom() {
        return recentFiscalPeriodFrom;
    }

    public void setRecentFiscalPeriodFrom(LocalDate recentFiscalPeriodFrom) {
        this.recentFiscalPeriodFrom = recentFiscalPeriodFrom;
    }

    public LocalDate getRecentFiscalPeriodTo() {
        return recentFiscalPeriodTo;
    }

    public void setRecentFiscalPeriodTo(LocalDate recentFiscalPeriodTo) {
        this.recentFiscalPeriodTo = recentFiscalPeriodTo;
    }

    public String getRecentStoreArea() {
        return recentStoreArea;
    }

    public void setRecentStoreArea(String recentStoreArea) {
        this.recentStoreArea = recentStoreArea;
    }

    public Long getRecentSalesRatioFreshFlower() {
        return recentSalesRatioFreshFlower;
    }

    public void setRecentSalesRatioFreshFlower(Long recentSalesRatioFreshFlower) {
        this.recentSalesRatioFreshFlower = recentSalesRatioFreshFlower;
    }

    public Long getRecentSalesRatioPottedPlant() {
        return recentSalesRatioPottedPlant;
    }

    public void setRecentSalesRatioPottedPlant(Long recentSalesRatioPottedPlant) {
        this.recentSalesRatioPottedPlant = recentSalesRatioPottedPlant;
    }

    public Long getRecentSalesRatioMaterial() {
        return recentSalesRatioMaterial;
    }

    public void setRecentSalesRatioMaterial(Long recentSalesRatioMaterial) {
        this.recentSalesRatioMaterial = recentSalesRatioMaterial;
    }

    public Long getRecentSalesRatioOther() {
        return recentSalesRatioOther;
    }

    public void setRecentSalesRatioOther(Long recentSalesRatioOther) {
        this.recentSalesRatioOther = recentSalesRatioOther;
    }

    public BigDecimal getRecentBusinessRatioStorefront() {
        return recentBusinessRatioStorefront;
    }

    public void setRecentBusinessRatioStorefront(BigDecimal recentBusinessRatioStorefront) {
        this.recentBusinessRatioStorefront = recentBusinessRatioStorefront;
    }

    public BigDecimal getRecentBusinessRatioLesson() {
        return recentBusinessRatioLesson;
    }

    public void setRecentBusinessRatioLesson(BigDecimal recentBusinessRatioLesson) {
        this.recentBusinessRatioLesson = recentBusinessRatioLesson;
    }

    public BigDecimal getRecentBusinessRatioWork() {
        return recentBusinessRatioWork;
    }

    public void setRecentBusinessRatioWork(BigDecimal recentBusinessRatioWork) {
        this.recentBusinessRatioWork = recentBusinessRatioWork;
    }

    public BigDecimal getRecentBusinessRatioOther() {
        return recentBusinessRatioOther;
    }

    public void setRecentBusinessRatioOther(BigDecimal recentBusinessRatioOther) {
        this.recentBusinessRatioOther = recentBusinessRatioOther;
    }

    public Long getRecentSales() {
        return recentSales;
    }

    public void setRecentSales(Long recentSales) {
        this.recentSales = recentSales;
    }

    public Long getRecentPurchase() {
        return recentPurchase;
    }

    public void setRecentPurchase(Long recentPurchase) {
        this.recentPurchase = recentPurchase;
    }

    public Long getRecentOperatingProfit() {
        return recentOperatingProfit;
    }

    public void setRecentOperatingProfit(Long recentOperatingProfit) {
        this.recentOperatingProfit = recentOperatingProfit;
    }

    public Long getRecentNetIncome() {
        return recentNetIncome;
    }

    public void setRecentNetIncome(Long recentNetIncome) {
        this.recentNetIncome = recentNetIncome;
    }

    public Short getRecentDeliveryVehicleCount() {
        return recentDeliveryVehicleCount;
    }

    public void setRecentDeliveryVehicleCount(Short recentDeliveryVehicleCount) {
        this.recentDeliveryVehicleCount = recentDeliveryVehicleCount;
    }

    public String getRecentStoreLocation() {
        return recentStoreLocation;
    }

    public void setRecentStoreLocation(String recentStoreLocation) {
        this.recentStoreLocation = recentStoreLocation;
    }

    public Integer getRecentMemberOrderCountYearly() {
        return recentMemberOrderCountYearly;
    }

    public void setRecentMemberOrderCountYearly(Integer recentMemberOrderCountYearly) {
        this.recentMemberOrderCountYearly = recentMemberOrderCountYearly;
    }

    public Integer getRecentMemberDeliveryCountYearly() {
        return recentMemberDeliveryCountYearly;
    }

    public void setRecentMemberDeliveryCountYearly(Integer recentMemberDeliveryCountYearly) {
        this.recentMemberDeliveryCountYearly = recentMemberDeliveryCountYearly;
    }

    public Long getRecentMemberOrderAmountYearly() {
        return recentMemberOrderAmountYearly;
    }

    public void setRecentMemberOrderAmountYearly(Long recentMemberOrderAmountYearly) {
        this.recentMemberOrderAmountYearly = recentMemberOrderAmountYearly;
    }

    public Long getRecentMemberOrderAmountYearly2() {
        return recentMemberOrderAmountYearly2;
    }

    public void setRecentMemberOrderAmountYearly2(Long recentMemberOrderAmountYearly2) {
        this.recentMemberOrderAmountYearly2 = recentMemberOrderAmountYearly2;
    }

    public String getStoreCategory() {
        return storeCategory;
    }

    public void setStoreCategory(String storeCategory) {
        this.storeCategory = storeCategory;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getHcpTownStatus() {
        return hcpTownStatus;
    }

    public void setHcpTownStatus(String hcpTownStatus) {
        this.hcpTownStatus = hcpTownStatus;
    }

    public LocalDate getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDate createDate) {
        this.createDate = createDate;
    }

    public LocalDate getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDate updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getUpdateEmployee() {
        return updateEmployee;
    }

    public void setUpdateEmployee(String updateEmployee) {
        this.updateEmployee = updateEmployee;
    }

    public String getMemberType() {
        return memberType;
    }

    public void setMemberType(String memberType) {
        this.memberType = memberType;
    }

    public String getMiddleCode() {
        return middleCode;
    }

    public void setMiddleCode(String middleCode) {
        this.middleCode = middleCode;
    }

    public String getCorporationFlag() {
        return corporationFlag;
    }

    public void setCorporationFlag(String corporationFlag) {
        this.corporationFlag = corporationFlag;
    }

    public String getRepresentativeName() {
        return representativeName;
    }

    public void setRepresentativeName(String representativeName) {
        this.representativeName = representativeName;
    }

    public String getRepresentativeKana() {
        return representativeKana;
    }

    public void setRepresentativeKana(String representativeKana) {
        this.representativeKana = representativeKana;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
