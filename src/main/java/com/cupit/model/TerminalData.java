package com.cupit.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * TerminalDataを表すエンティティ。
 * m_terminal_data テーブルの 1 行に対応する。
 */
@Entity
@Table(name = "m_terminal_data")
public class TerminalData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_no")
    private long recordNo;

    @Column(name = "trade_code", nullable = false, length = 10)
    private String tradeCode;

    @Column(name = "application_category")
    private String applicationCategory;

    @Column(name = "applicant_type")
    private String applicantType;

    @Column(name = "application_or_cancellation_date")
    private LocalDate applicationOrCancellationDate;

    @Column(name = "service_start_desired_date")
    private LocalDate serviceStartDesiredDate;

    @Column(name = "service_end_date")
    private LocalDate serviceEndDate;

    @Column(name = "brand_name_english")
    private String brandNameEnglish;

    @Column(name = "representative_merchant_number")
    private String representativeMerchantNumber;

    @Column(name = "vm_merchant_number")
    private String vmMerchantNumber;

    @Column(name = "terminal_id")
    private String terminalId;

    @Column(name = "closing_date_1")
    private String closingDate1;

    @Column(name = "payment_date_1")
    private String paymentDate1;

    @Column(name = "closing_date_2")
    private String closingDate2;

    @Column(name = "payment_date_2")
    private String paymentDate2;

    @Column(name = "settlement_cycle")
    private String settlementCycle;

    @Column(name = "bank_code")
    private String bankCode;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_name_kana")
    private String bankNameKana;

    @Column(name = "branch_code")
    private String branchCode;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "branch_name_kana")
    private String branchNameKana;

    @Column(name = "account_type")
    private String accountType;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "contact_last_name")
    private String contactLastName;

    @Column(name = "contact_first_name")
    private String contactFirstName;

    @Column(name = "contact_last_name_kana")
    private String contactLastNameKana;

    @Column(name = "contact_first_name_kana")
    private String contactFirstNameKana;

    @Column(name = "merchant_type")
    private String merchantType;

    @Column(name = "franchise_flag")
    private String franchiseFlag;

    @Column(name = "paypay_fc_agreement_flag")
    private String paypayFcAgreementFlag;

    @Column(name = "store_count_applied")
    private Integer storeCountApplied;

    @Column(name = "terminal_type")
    private String terminalType;

    @Column(name = "corp_name_english")
    private String corpNameEnglish;

    @Column(name = "industry_category_major")
    private String industryCategoryMajor;

    @Column(name = "industry_category_minor")
    private String industryCategoryMinor;

    @Column(name = "corporate_number")
    private String corporateNumber;

    @Column(name = "rep_last_name_english")
    private String repLastNameEnglish;

    @Column(name = "rep_first_name_english")
    private String repFirstNameEnglish;

    @Column(name = "gender")
    private String gender;

    @Column(name = "rep_addr_pref_kana")
    private String repAddrPrefKana;

    @Column(name = "rep_addr_city_kana")
    private String repAddrCityKana;

    @Column(name = "rep_addr_town_kana")
    private String repAddrTownKana;

    @Column(name = "rep_addr_block_kana")
    private String repAddrBlockKana;

    @Column(name = "rep_addr_building_kana")
    private String repAddrBuildingKana;

    @Column(name = "door_to_door_sales_flag")
    private String doorToDoorSalesFlag;

    @Column(name = "continuous_service_flag")
    private String continuousServiceFlag;

    @Column(name = "telemarketing_sales_flag")
    private String telemarketingSalesFlag;

    @Column(name = "chain_sales_flag")
    private String chainSalesFlag;

    @Column(name = "business_opportunity_sales_flag")
    private String businessOpportunitySalesFlag;

    @Column(name = "prepaid_transaction_flag")
    private String prepaidTransactionFlag;

    @Column(name = "legal_violation_history_flag")
    private String legalViolationHistoryFlag;

    @Column(name = "fc_store_type")
    private String fcStoreType;

    @Column(name = "representative_store_flag")
    private String representativeStoreFlag;

    @Column(name = "store_industry_major")
    private String storeIndustryMajor;

    @Column(name = "store_industry_minor")
    private String storeIndustryMinor;

    @Column(name = "secondhand_dealer_license_number")
    private String secondhandDealerLicenseNumber;

    @Column(name = "store_name_english")
    private String storeNameEnglish;

    @Column(name = "map_listing_flag")
    private String mapListingFlag;

    @Column(name = "map_listing_desired_date_dpay_rakuten")
    private LocalDate mapListingDesiredDateDpayRakuten;

    @Column(name = "map_listing_desired_date_paypay_aupay")
    private LocalDate mapListingDesiredDatePaypayAupay;

    @Column(name = "store_image_listing_flag")
    private String storeImageListingFlag;

    @Column(name = "store_image_url")
    private String storeImageUrl;

    @Column(name = "store_introduction")
    private String storeIntroduction;

    @Column(name = "fee_rate_rakuten_pay")
    private BigDecimal feeRateRakutenPay;

    @Column(name = "fee_rate_line_pay")
    private BigDecimal feeRateLinePay;

    @Column(name = "fee_rate_paypay")
    private BigDecimal feeRatePaypay;

    @Column(name = "fee_rate_d_barai")
    private BigDecimal feeRateDBarai;

    @Column(name = "fee_rate_au_pay")
    private BigDecimal feeRateAuPay;

    @Column(name = "fee_rate_merpay")
    private BigDecimal feeRateMerpay;

    @Column(name = "fee_rate_yucho_pay")
    private BigDecimal feeRateYuchoPay;

    @Column(name = "fee_rate_aeon_pay")
    private BigDecimal feeRateAeonPay;

    @Column(name = "atokara_rate")
    private BigDecimal atokaraRate;

    @Column(name = "fee_rate_mdr_1")
    private BigDecimal feeRateMdr1;

    @Column(name = "fee_rate_mdr_3")
    private BigDecimal feeRateMdr3;

    @Column(name = "fee_rate_mdr_4")
    private BigDecimal feeRateMdr4;

    @Column(name = "fee_rate_installment_5")
    private BigDecimal feeRateInstallment5;

    @Column(name = "fee_rate_installment_6")
    private BigDecimal feeRateInstallment6;

    @Column(name = "fee_rate_installment_10")
    private BigDecimal feeRateInstallment10;

    @Column(name = "fee_rate_installment_12")
    private BigDecimal feeRateInstallment12;

    @Column(name = "fee_rate_installment_15")
    private BigDecimal feeRateInstallment15;

    @Column(name = "fee_rate_installment_18")
    private BigDecimal feeRateInstallment18;

    @Column(name = "fee_rate_installment_20")
    private BigDecimal feeRateInstallment20;

    @Column(name = "fee_rate_installment_24")
    private BigDecimal feeRateInstallment24;

    @Column(name = "fee_rate_installment_30")
    private BigDecimal feeRateInstallment30;

    @Column(name = "fee_rate_installment_36")
    private BigDecimal feeRateInstallment36;

    @Column(name = "fee_rate_wesmo")
    private BigDecimal feeRateWesmo;

    @Column(name = "fee_rate_bank_pay")
    private BigDecimal feeRateBankPay;

    @Column(name = "fee_rate_wechat")
    private BigDecimal feeRateWechat;

    @Column(name = "fee_rate_alipay")
    private BigDecimal feeRateAlipay;

    @Column(name = "fee_rate_unionpay_qr")
    private BigDecimal feeRateUnionpayQr;

    @Column(name = "change_notes")
    private String changeNotes;

    @Column(name = "smcc_department")
    private String smccDepartment;

    @Column(name = "smcc_contact_name")
    private String smccContactName;

    @Column(name = "smart_code_flag")
    private String smartCodeFlag;

    @Column(name = "mkp_flag")
    private String mkpFlag;

    @Column(name = "registered_date", nullable = false)
    private LocalDate registeredDate;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    @Column(name = "updated_by")
    private String updatedBy;

    public long getRecordNo() {
        return recordNo;
    }

    public void setRecordNo(long recordNo) {
        this.recordNo = recordNo;
    }

    public String getTradeCode() {
        return tradeCode;
    }

    public void setTradeCode(String tradeCode) {
        this.tradeCode = tradeCode;
    }

    public String getApplicationCategory() {
        return applicationCategory;
    }

    public void setApplicationCategory(String applicationCategory) {
        this.applicationCategory = applicationCategory;
    }

    public String getApplicantType() {
        return applicantType;
    }

    public void setApplicantType(String applicantType) {
        this.applicantType = applicantType;
    }

    public LocalDate getApplicationOrCancellationDate() {
        return applicationOrCancellationDate;
    }

    public void setApplicationOrCancellationDate(LocalDate applicationOrCancellationDate) {
        this.applicationOrCancellationDate = applicationOrCancellationDate;
    }

    public LocalDate getServiceStartDesiredDate() {
        return serviceStartDesiredDate;
    }

    public void setServiceStartDesiredDate(LocalDate serviceStartDesiredDate) {
        this.serviceStartDesiredDate = serviceStartDesiredDate;
    }

    public LocalDate getServiceEndDate() {
        return serviceEndDate;
    }

    public void setServiceEndDate(LocalDate serviceEndDate) {
        this.serviceEndDate = serviceEndDate;
    }

    public String getBrandNameEnglish() {
        return brandNameEnglish;
    }

    public void setBrandNameEnglish(String brandNameEnglish) {
        this.brandNameEnglish = brandNameEnglish;
    }

    public String getRepresentativeMerchantNumber() {
        return representativeMerchantNumber;
    }

    public void setRepresentativeMerchantNumber(String representativeMerchantNumber) {
        this.representativeMerchantNumber = representativeMerchantNumber;
    }

    public String getVmMerchantNumber() {
        return vmMerchantNumber;
    }

    public void setVmMerchantNumber(String vmMerchantNumber) {
        this.vmMerchantNumber = vmMerchantNumber;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getClosingDate1() {
        return closingDate1;
    }

    public void setClosingDate1(String closingDate1) {
        this.closingDate1 = closingDate1;
    }

    public String getPaymentDate1() {
        return paymentDate1;
    }

    public void setPaymentDate1(String paymentDate1) {
        this.paymentDate1 = paymentDate1;
    }

    public String getClosingDate2() {
        return closingDate2;
    }

    public void setClosingDate2(String closingDate2) {
        this.closingDate2 = closingDate2;
    }

    public String getPaymentDate2() {
        return paymentDate2;
    }

    public void setPaymentDate2(String paymentDate2) {
        this.paymentDate2 = paymentDate2;
    }

    public String getSettlementCycle() {
        return settlementCycle;
    }

    public void setSettlementCycle(String settlementCycle) {
        this.settlementCycle = settlementCycle;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankNameKana() {
        return bankNameKana;
    }

    public void setBankNameKana(String bankNameKana) {
        this.bankNameKana = bankNameKana;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBranchNameKana() {
        return branchNameKana;
    }

    public void setBranchNameKana(String branchNameKana) {
        this.branchNameKana = branchNameKana;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getContactLastName() {
        return contactLastName;
    }

    public void setContactLastName(String contactLastName) {
        this.contactLastName = contactLastName;
    }

    public String getContactFirstName() {
        return contactFirstName;
    }

    public void setContactFirstName(String contactFirstName) {
        this.contactFirstName = contactFirstName;
    }

    public String getContactLastNameKana() {
        return contactLastNameKana;
    }

    public void setContactLastNameKana(String contactLastNameKana) {
        this.contactLastNameKana = contactLastNameKana;
    }

    public String getContactFirstNameKana() {
        return contactFirstNameKana;
    }

    public void setContactFirstNameKana(String contactFirstNameKana) {
        this.contactFirstNameKana = contactFirstNameKana;
    }

    public String getMerchantType() {
        return merchantType;
    }

    public void setMerchantType(String merchantType) {
        this.merchantType = merchantType;
    }

    public String getFranchiseFlag() {
        return franchiseFlag;
    }

    public void setFranchiseFlag(String franchiseFlag) {
        this.franchiseFlag = franchiseFlag;
    }

    public String getPaypayFcAgreementFlag() {
        return paypayFcAgreementFlag;
    }

    public void setPaypayFcAgreementFlag(String paypayFcAgreementFlag) {
        this.paypayFcAgreementFlag = paypayFcAgreementFlag;
    }

    public Integer getStoreCountApplied() {
        return storeCountApplied;
    }

    public void setStoreCountApplied(Integer storeCountApplied) {
        this.storeCountApplied = storeCountApplied;
    }

    public String getTerminalType() {
        return terminalType;
    }

    public void setTerminalType(String terminalType) {
        this.terminalType = terminalType;
    }

    public String getCorpNameEnglish() {
        return corpNameEnglish;
    }

    public void setCorpNameEnglish(String corpNameEnglish) {
        this.corpNameEnglish = corpNameEnglish;
    }

    public String getIndustryCategoryMajor() {
        return industryCategoryMajor;
    }

    public void setIndustryCategoryMajor(String industryCategoryMajor) {
        this.industryCategoryMajor = industryCategoryMajor;
    }

    public String getIndustryCategoryMinor() {
        return industryCategoryMinor;
    }

    public void setIndustryCategoryMinor(String industryCategoryMinor) {
        this.industryCategoryMinor = industryCategoryMinor;
    }

    public String getCorporateNumber() {
        return corporateNumber;
    }

    public void setCorporateNumber(String corporateNumber) {
        this.corporateNumber = corporateNumber;
    }

    public String getRepLastNameEnglish() {
        return repLastNameEnglish;
    }

    public void setRepLastNameEnglish(String repLastNameEnglish) {
        this.repLastNameEnglish = repLastNameEnglish;
    }

    public String getRepFirstNameEnglish() {
        return repFirstNameEnglish;
    }

    public void setRepFirstNameEnglish(String repFirstNameEnglish) {
        this.repFirstNameEnglish = repFirstNameEnglish;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getRepAddrPrefKana() {
        return repAddrPrefKana;
    }

    public void setRepAddrPrefKana(String repAddrPrefKana) {
        this.repAddrPrefKana = repAddrPrefKana;
    }

    public String getRepAddrCityKana() {
        return repAddrCityKana;
    }

    public void setRepAddrCityKana(String repAddrCityKana) {
        this.repAddrCityKana = repAddrCityKana;
    }

    public String getRepAddrTownKana() {
        return repAddrTownKana;
    }

    public void setRepAddrTownKana(String repAddrTownKana) {
        this.repAddrTownKana = repAddrTownKana;
    }

    public String getRepAddrBlockKana() {
        return repAddrBlockKana;
    }

    public void setRepAddrBlockKana(String repAddrBlockKana) {
        this.repAddrBlockKana = repAddrBlockKana;
    }

    public String getRepAddrBuildingKana() {
        return repAddrBuildingKana;
    }

    public void setRepAddrBuildingKana(String repAddrBuildingKana) {
        this.repAddrBuildingKana = repAddrBuildingKana;
    }

    public String getDoorToDoorSalesFlag() {
        return doorToDoorSalesFlag;
    }

    public void setDoorToDoorSalesFlag(String doorToDoorSalesFlag) {
        this.doorToDoorSalesFlag = doorToDoorSalesFlag;
    }

    public String getContinuousServiceFlag() {
        return continuousServiceFlag;
    }

    public void setContinuousServiceFlag(String continuousServiceFlag) {
        this.continuousServiceFlag = continuousServiceFlag;
    }

    public String getTelemarketingSalesFlag() {
        return telemarketingSalesFlag;
    }

    public void setTelemarketingSalesFlag(String telemarketingSalesFlag) {
        this.telemarketingSalesFlag = telemarketingSalesFlag;
    }

    public String getChainSalesFlag() {
        return chainSalesFlag;
    }

    public void setChainSalesFlag(String chainSalesFlag) {
        this.chainSalesFlag = chainSalesFlag;
    }

    public String getBusinessOpportunitySalesFlag() {
        return businessOpportunitySalesFlag;
    }

    public void setBusinessOpportunitySalesFlag(String businessOpportunitySalesFlag) {
        this.businessOpportunitySalesFlag = businessOpportunitySalesFlag;
    }

    public String getPrepaidTransactionFlag() {
        return prepaidTransactionFlag;
    }

    public void setPrepaidTransactionFlag(String prepaidTransactionFlag) {
        this.prepaidTransactionFlag = prepaidTransactionFlag;
    }

    public String getLegalViolationHistoryFlag() {
        return legalViolationHistoryFlag;
    }

    public void setLegalViolationHistoryFlag(String legalViolationHistoryFlag) {
        this.legalViolationHistoryFlag = legalViolationHistoryFlag;
    }

    public String getFcStoreType() {
        return fcStoreType;
    }

    public void setFcStoreType(String fcStoreType) {
        this.fcStoreType = fcStoreType;
    }

    public String getRepresentativeStoreFlag() {
        return representativeStoreFlag;
    }

    public void setRepresentativeStoreFlag(String representativeStoreFlag) {
        this.representativeStoreFlag = representativeStoreFlag;
    }

    public String getStoreIndustryMajor() {
        return storeIndustryMajor;
    }

    public void setStoreIndustryMajor(String storeIndustryMajor) {
        this.storeIndustryMajor = storeIndustryMajor;
    }

    public String getStoreIndustryMinor() {
        return storeIndustryMinor;
    }

    public void setStoreIndustryMinor(String storeIndustryMinor) {
        this.storeIndustryMinor = storeIndustryMinor;
    }

    public String getSecondhandDealerLicenseNumber() {
        return secondhandDealerLicenseNumber;
    }

    public void setSecondhandDealerLicenseNumber(String secondhandDealerLicenseNumber) {
        this.secondhandDealerLicenseNumber = secondhandDealerLicenseNumber;
    }

    public String getStoreNameEnglish() {
        return storeNameEnglish;
    }

    public void setStoreNameEnglish(String storeNameEnglish) {
        this.storeNameEnglish = storeNameEnglish;
    }

    public String getMapListingFlag() {
        return mapListingFlag;
    }

    public void setMapListingFlag(String mapListingFlag) {
        this.mapListingFlag = mapListingFlag;
    }

    public LocalDate getMapListingDesiredDateDpayRakuten() {
        return mapListingDesiredDateDpayRakuten;
    }

    public void setMapListingDesiredDateDpayRakuten(LocalDate mapListingDesiredDateDpayRakuten) {
        this.mapListingDesiredDateDpayRakuten = mapListingDesiredDateDpayRakuten;
    }

    public LocalDate getMapListingDesiredDatePaypayAupay() {
        return mapListingDesiredDatePaypayAupay;
    }

    public void setMapListingDesiredDatePaypayAupay(LocalDate mapListingDesiredDatePaypayAupay) {
        this.mapListingDesiredDatePaypayAupay = mapListingDesiredDatePaypayAupay;
    }

    public String getStoreImageListingFlag() {
        return storeImageListingFlag;
    }

    public void setStoreImageListingFlag(String storeImageListingFlag) {
        this.storeImageListingFlag = storeImageListingFlag;
    }

    public String getStoreImageUrl() {
        return storeImageUrl;
    }

    public void setStoreImageUrl(String storeImageUrl) {
        this.storeImageUrl = storeImageUrl;
    }

    public String getStoreIntroduction() {
        return storeIntroduction;
    }

    public void setStoreIntroduction(String storeIntroduction) {
        this.storeIntroduction = storeIntroduction;
    }

    public BigDecimal getFeeRateRakutenPay() {
        return feeRateRakutenPay;
    }

    public void setFeeRateRakutenPay(BigDecimal feeRateRakutenPay) {
        this.feeRateRakutenPay = feeRateRakutenPay;
    }

    public BigDecimal getFeeRateLinePay() {
        return feeRateLinePay;
    }

    public void setFeeRateLinePay(BigDecimal feeRateLinePay) {
        this.feeRateLinePay = feeRateLinePay;
    }

    public BigDecimal getFeeRatePaypay() {
        return feeRatePaypay;
    }

    public void setFeeRatePaypay(BigDecimal feeRatePaypay) {
        this.feeRatePaypay = feeRatePaypay;
    }

    public BigDecimal getFeeRateDBarai() {
        return feeRateDBarai;
    }

    public void setFeeRateDBarai(BigDecimal feeRateDBarai) {
        this.feeRateDBarai = feeRateDBarai;
    }

    public BigDecimal getFeeRateAuPay() {
        return feeRateAuPay;
    }

    public void setFeeRateAuPay(BigDecimal feeRateAuPay) {
        this.feeRateAuPay = feeRateAuPay;
    }

    public BigDecimal getFeeRateMerpay() {
        return feeRateMerpay;
    }

    public void setFeeRateMerpay(BigDecimal feeRateMerpay) {
        this.feeRateMerpay = feeRateMerpay;
    }

    public BigDecimal getFeeRateYuchoPay() {
        return feeRateYuchoPay;
    }

    public void setFeeRateYuchoPay(BigDecimal feeRateYuchoPay) {
        this.feeRateYuchoPay = feeRateYuchoPay;
    }

    public BigDecimal getFeeRateAeonPay() {
        return feeRateAeonPay;
    }

    public void setFeeRateAeonPay(BigDecimal feeRateAeonPay) {
        this.feeRateAeonPay = feeRateAeonPay;
    }

    public BigDecimal getAtokaraRate() {
        return atokaraRate;
    }

    public void setAtokaraRate(BigDecimal atokaraRate) {
        this.atokaraRate = atokaraRate;
    }

    public BigDecimal getFeeRateMdr1() {
        return feeRateMdr1;
    }

    public void setFeeRateMdr1(BigDecimal feeRateMdr1) {
        this.feeRateMdr1 = feeRateMdr1;
    }

    public BigDecimal getFeeRateMdr3() {
        return feeRateMdr3;
    }

    public void setFeeRateMdr3(BigDecimal feeRateMdr3) {
        this.feeRateMdr3 = feeRateMdr3;
    }

    public BigDecimal getFeeRateMdr4() {
        return feeRateMdr4;
    }

    public void setFeeRateMdr4(BigDecimal feeRateMdr4) {
        this.feeRateMdr4 = feeRateMdr4;
    }

    public BigDecimal getFeeRateInstallment5() {
        return feeRateInstallment5;
    }

    public void setFeeRateInstallment5(BigDecimal feeRateInstallment5) {
        this.feeRateInstallment5 = feeRateInstallment5;
    }

    public BigDecimal getFeeRateInstallment6() {
        return feeRateInstallment6;
    }

    public void setFeeRateInstallment6(BigDecimal feeRateInstallment6) {
        this.feeRateInstallment6 = feeRateInstallment6;
    }

    public BigDecimal getFeeRateInstallment10() {
        return feeRateInstallment10;
    }

    public void setFeeRateInstallment10(BigDecimal feeRateInstallment10) {
        this.feeRateInstallment10 = feeRateInstallment10;
    }

    public BigDecimal getFeeRateInstallment12() {
        return feeRateInstallment12;
    }

    public void setFeeRateInstallment12(BigDecimal feeRateInstallment12) {
        this.feeRateInstallment12 = feeRateInstallment12;
    }

    public BigDecimal getFeeRateInstallment15() {
        return feeRateInstallment15;
    }

    public void setFeeRateInstallment15(BigDecimal feeRateInstallment15) {
        this.feeRateInstallment15 = feeRateInstallment15;
    }

    public BigDecimal getFeeRateInstallment18() {
        return feeRateInstallment18;
    }

    public void setFeeRateInstallment18(BigDecimal feeRateInstallment18) {
        this.feeRateInstallment18 = feeRateInstallment18;
    }

    public BigDecimal getFeeRateInstallment20() {
        return feeRateInstallment20;
    }

    public void setFeeRateInstallment20(BigDecimal feeRateInstallment20) {
        this.feeRateInstallment20 = feeRateInstallment20;
    }

    public BigDecimal getFeeRateInstallment24() {
        return feeRateInstallment24;
    }

    public void setFeeRateInstallment24(BigDecimal feeRateInstallment24) {
        this.feeRateInstallment24 = feeRateInstallment24;
    }

    public BigDecimal getFeeRateInstallment30() {
        return feeRateInstallment30;
    }

    public void setFeeRateInstallment30(BigDecimal feeRateInstallment30) {
        this.feeRateInstallment30 = feeRateInstallment30;
    }

    public BigDecimal getFeeRateInstallment36() {
        return feeRateInstallment36;
    }

    public void setFeeRateInstallment36(BigDecimal feeRateInstallment36) {
        this.feeRateInstallment36 = feeRateInstallment36;
    }

    public BigDecimal getFeeRateWesmo() {
        return feeRateWesmo;
    }

    public void setFeeRateWesmo(BigDecimal feeRateWesmo) {
        this.feeRateWesmo = feeRateWesmo;
    }

    public BigDecimal getFeeRateBankPay() {
        return feeRateBankPay;
    }

    public void setFeeRateBankPay(BigDecimal feeRateBankPay) {
        this.feeRateBankPay = feeRateBankPay;
    }

    public BigDecimal getFeeRateWechat() {
        return feeRateWechat;
    }

    public void setFeeRateWechat(BigDecimal feeRateWechat) {
        this.feeRateWechat = feeRateWechat;
    }

    public BigDecimal getFeeRateAlipay() {
        return feeRateAlipay;
    }

    public void setFeeRateAlipay(BigDecimal feeRateAlipay) {
        this.feeRateAlipay = feeRateAlipay;
    }

    public BigDecimal getFeeRateUnionpayQr() {
        return feeRateUnionpayQr;
    }

    public void setFeeRateUnionpayQr(BigDecimal feeRateUnionpayQr) {
        this.feeRateUnionpayQr = feeRateUnionpayQr;
    }

    public String getChangeNotes() {
        return changeNotes;
    }

    public void setChangeNotes(String changeNotes) {
        this.changeNotes = changeNotes;
    }

    public String getSmccDepartment() {
        return smccDepartment;
    }

    public void setSmccDepartment(String smccDepartment) {
        this.smccDepartment = smccDepartment;
    }

    public String getSmccContactName() {
        return smccContactName;
    }

    public void setSmccContactName(String smccContactName) {
        this.smccContactName = smccContactName;
    }

    public String getSmartCodeFlag() {
        return smartCodeFlag;
    }

    public void setSmartCodeFlag(String smartCodeFlag) {
        this.smartCodeFlag = smartCodeFlag;
    }

    public String getMkpFlag() {
        return mkpFlag;
    }

    public void setMkpFlag(String mkpFlag) {
        this.mkpFlag = mkpFlag;
    }

    public LocalDate getRegisteredDate() {
        return registeredDate;
    }

    public void setRegisteredDate(LocalDate registeredDate) {
        this.registeredDate = registeredDate;
    }

    public LocalDate getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDate updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

}
