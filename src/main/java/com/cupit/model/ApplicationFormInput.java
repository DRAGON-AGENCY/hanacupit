package com.cupit.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 各決済会社所定申込フォーム作成のINPUTファイル（230列）1行分を表すクラス。
 * data1（サンプル_010_stera申込み）由来の56項目と、m_member_infoから取得・加工しても
 * 設定できない項目（JCB・SMCC申込書・SMCC店舗情報一覧向け）を保持する。
 * m_member_info・m_paygate_store_mappingと組み合わせて各決済会社所定フォーマットの
 * Excelを生成するための一時的な入力データであり、DBへの永続化は行わない
 * （JPAエンティティではないプレーンなクラス）。
 */
public class ApplicationFormInput {

    private String recordNumber;
    private String readerSerialNo;
    private String terminalId;
    private String tradeCode;
    private String paygateContinuationStatus;
    private String memberType;
    private String storeName;
    private String storeNameKana;
    private String storeNameAlphabet;
    private String storeZip;
    private String storeAddress;
    private String storeAddressKana;
    private String storeTel;
    private String storeEmail;
    private String storeHomepageUrl;
    private String individualOrCorporateType;
    private String corpNumber;
    private String corpName;
    private String corpNameKana;
    private String corpNameAlphabet;
    private String corpZip;
    private String corpAddress;
    private String corpAddressKana;
    private String corpTel;
    private String establishmentDate;
    private String repFullName;
    private String repFullNameKana;
    private String repZip;
    private String repAddress;
    private String repAddressKana;
    private String repTel;
    private String repBirthDate;
    private String bankName;
    private String branchName;
    private String accountNumber;
    private String accountHolderKana;
    private String jcbUsageFlag;
    private String annualSales;
    private String accountType;
    private String status;
    private String workerName;
    private String contactFullName;
    private String contactTel;
    private String storeAddressPref;
    private String contactLastName;
    private String contactFirstName;
    private String corpAddressPref;
    private String repLastName;
    private String repFirstName;
    private String repLastNameKana;
    private String repFirstNameKana;
    private String repAddressPref;
    private String dPointUsageFlag;
    private String repLastNameAlphabet;
    private String repFirstNameAlphabet;
    private String repGender;
    private String doorToDoorSalesFlag;
    private String telemarketingSalesFlag;
    private String chainSalesFlag;
    private String businessOpportunitySalesFlag;
    private String continuousServiceFlag;
    private String prepaidTransactionFlag;
    private String legalViolationHistoryFlag;
    private String cardDataRetentionStatus;
    private String pciDssComplianceStatus;
    private String nonRetentionPlannedMonth;
    private String pciDssCompliancePlannedMonth;
    private String terminalIcStatus;
    private String terminalIcPlannedMonth;
    private String acquirerUniqueKey;
    private String classification;
    private String contractSource;
    private String giftContractFlag;
    private String edyContractFlag;
    private String applicantType;
    private LocalDate serviceStartDesiredDate;
    private LocalDate serviceEndDate;
    private String vmMerchantNumber;
    private String closingDate1;
    private String paymentDate1;
    private String closingDate2;
    private String paymentDate2;
    private String settlementCycle;
    private String bankCode;
    private String bankNameKana;
    private String branchCode;
    private String branchNameKana;
    private String contactLastNameKana;
    private String contactFirstNameKana;
    private String merchantType;
    private String franchiseFlag;
    private String paypayFcAgreementFlag;
    private String terminalType;
    private String industryCategoryMajor;
    private String industryCategoryMinor;
    private String storeIndustryMajor;
    private String storeIndustryMinor;
    private String repAddrPrefKana;
    private String repAddrCityKana;
    private String repAddrTownKana;
    private String repAddrBlockKana;
    private String repAddrBuildingKana;
    private String fcStoreType;
    private String secondhandDealerLicenseNumber;
    private String mapListingFlag;
    private LocalDate mapListingDesiredDateDpayRakuten;
    private LocalDate mapListingDesiredDatePaypayAupay;
    private String storeImageListingFlag;
    private String storeImageUrl;
    private String storeIntroduction;
    private BigDecimal feeRateRakutenPay;
    private BigDecimal feeRateLinePay;
    private BigDecimal feeRatePaypay;
    private BigDecimal feeRateDBarai;
    private BigDecimal feeRateAuPay;
    private BigDecimal feeRateMerpay;
    private BigDecimal feeRateYuchoPay;
    private BigDecimal feeRateAeonPay;
    private BigDecimal atokaraRate;
    private BigDecimal feeRateMdr1;
    private BigDecimal feeRateMdr3;
    private BigDecimal feeRateMdr4;
    private BigDecimal feeRateInstallment5;
    private BigDecimal feeRateInstallment6;
    private BigDecimal feeRateInstallment10;
    private BigDecimal feeRateInstallment12;
    private BigDecimal feeRateInstallment15;
    private BigDecimal feeRateInstallment18;
    private BigDecimal feeRateInstallment20;
    private BigDecimal feeRateInstallment24;
    private BigDecimal feeRateInstallment30;
    private BigDecimal feeRateInstallment36;
    private BigDecimal feeRateWesmo;
    private BigDecimal feeRateBankPay;
    private BigDecimal feeRateWechat;
    private BigDecimal feeRateAlipay;
    private BigDecimal feeRateUnionpayQr;
    private String changeNotes;
    private String smccDepartment;
    private String smccContactName;
    private String smartCodeFlag;
    private String mkpFlag;
    private Integer terminalCount;
    private String lineType;
    private String posConnectionFlag;
    private String posMakerName;
    private String posVendorContactName;
    private String posVendorContactTel;
    private String smartCodeConnectionFlag;
    private String dPointMerchantCode;
    private String dPointStoreCode;
    private String dPointBranchCode;
    private String visaMasterMerchantNumber;
    private String nanacoMerchantNumber;
    private String idMerchantNumber;
    private String transitMerchantNumber;
    private String unionpayMerchantNumber;
    private String waonMerchantNumber;
    private String edyMerchantNumber;
    private String nfcMerchantNumber;
    private String transitOperator;
    private String edyId;
    private String steraTerminalNumber1;
    private String steraTerminalNumber2;
    private String steraTerminalNumber3;
    private String steraTerminalNumber4;
    private String steraTerminalNumber5;
    private BigDecimal feeRateBrandRakutenPay;
    private BigDecimal feeRateBrandLinePay;
    private BigDecimal feeRateBrandPaypay;
    private BigDecimal feeRateBrandDBarai;
    private BigDecimal feeRateBrandAuPay;
    private BigDecimal feeRateBrandMerpay;
    private BigDecimal feeRateBrandYuchoPay;
    private BigDecimal feeRateBrandAeonPay;
    private BigDecimal atokaraWholesaleRate;
    private BigDecimal merchantInstallmentFee1;
    private BigDecimal merchantInstallmentFee3;
    private BigDecimal merchantInstallmentFee4;
    private BigDecimal merchantInstallmentFee5;
    private BigDecimal merchantInstallmentFee6;
    private BigDecimal merchantInstallmentFee10;
    private BigDecimal merchantInstallmentFee12;
    private BigDecimal merchantInstallmentFee15;
    private BigDecimal merchantInstallmentFee18;
    private BigDecimal merchantInstallmentFee20;
    private BigDecimal merchantInstallmentFee24;
    private BigDecimal merchantInstallmentFee30;
    private BigDecimal merchantInstallmentFee36;
    private BigDecimal feeRateBrandWechat;
    private BigDecimal feeRateBrandAlipay;
    private BigDecimal feeRateBrandWesmo;
    private BigDecimal atokaraCustomerRate;
    private BigDecimal customerInstallmentFee1;
    private BigDecimal customerInstallmentFee3;
    private BigDecimal customerInstallmentFee4;
    private BigDecimal customerInstallmentFee5;
    private BigDecimal customerInstallmentFee6;
    private BigDecimal customerInstallmentFee10;
    private BigDecimal customerInstallmentFee12;
    private BigDecimal customerInstallmentFee15;
    private BigDecimal customerInstallmentFee18;
    private BigDecimal customerInstallmentFee20;
    private BigDecimal customerInstallmentFee24;
    private BigDecimal customerInstallmentFee30;
    private BigDecimal customerInstallmentFee36;
    private String costShareFlagRakutenPay;
    private String costShareFlagLinePay;
    private String costShareFlagPaypay;
    private String costShareFlagDBarai;
    private String costShareFlagAuPay;
    private String costShareFlagMerpay;
    private String costShareFlagYuchoPay;
    private String costShareFlagAeonPay;
    private String costShareFlagWesmo;
    private String unionpayQrMerchantNumber;
    private String awMerchantNumber;
    private String dBaraiIpid;
    private String alipayPid;
    private String unionpayQrMid;
    private String relocationRepresentativeMerchantNumber;
    private String relocationPlatformMerchantNumber;
    private String cancelAndNewRepresentativeMerchantNumber;
    private String cancelAndNewPlatformMerchantNumber;
    private Integer cafisArchTerminalCount;
    private BigDecimal quoCardPayMerchantRateNss;
    private BigDecimal quoCardPayBrandRateNss;
    private String bankPayNssTid;
    private BigDecimal feeRateJcoinPayMerchant;
    private BigDecimal feeRateJcoinPayBrand;

    public String getRecordNumber() {
        return recordNumber;
    }

    public void setRecordNumber(String recordNumber) {
        this.recordNumber = recordNumber;
    }
    public String getReaderSerialNo() {
        return readerSerialNo;
    }

    public void setReaderSerialNo(String readerSerialNo) {
        this.readerSerialNo = readerSerialNo;
    }
    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }
    public String getTradeCode() {
        return tradeCode;
    }

    public void setTradeCode(String tradeCode) {
        this.tradeCode = tradeCode;
    }
    public String getPaygateContinuationStatus() {
        return paygateContinuationStatus;
    }

    public void setPaygateContinuationStatus(String paygateContinuationStatus) {
        this.paygateContinuationStatus = paygateContinuationStatus;
    }
    public String getMemberType() {
        return memberType;
    }

    public void setMemberType(String memberType) {
        this.memberType = memberType;
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
    public String getStoreNameAlphabet() {
        return storeNameAlphabet;
    }

    public void setStoreNameAlphabet(String storeNameAlphabet) {
        this.storeNameAlphabet = storeNameAlphabet;
    }
    public String getStoreZip() {
        return storeZip;
    }

    public void setStoreZip(String storeZip) {
        this.storeZip = storeZip;
    }
    public String getStoreAddress() {
        return storeAddress;
    }

    public void setStoreAddress(String storeAddress) {
        this.storeAddress = storeAddress;
    }
    public String getStoreAddressKana() {
        return storeAddressKana;
    }

    public void setStoreAddressKana(String storeAddressKana) {
        this.storeAddressKana = storeAddressKana;
    }
    public String getStoreTel() {
        return storeTel;
    }

    public void setStoreTel(String storeTel) {
        this.storeTel = storeTel;
    }
    public String getStoreEmail() {
        return storeEmail;
    }

    public void setStoreEmail(String storeEmail) {
        this.storeEmail = storeEmail;
    }
    public String getStoreHomepageUrl() {
        return storeHomepageUrl;
    }

    public void setStoreHomepageUrl(String storeHomepageUrl) {
        this.storeHomepageUrl = storeHomepageUrl;
    }
    public String getIndividualOrCorporateType() {
        return individualOrCorporateType;
    }

    public void setIndividualOrCorporateType(String individualOrCorporateType) {
        this.individualOrCorporateType = individualOrCorporateType;
    }
    public String getCorpNumber() {
        return corpNumber;
    }

    public void setCorpNumber(String corpNumber) {
        this.corpNumber = corpNumber;
    }
    public String getCorpName() {
        return corpName;
    }

    public void setCorpName(String corpName) {
        this.corpName = corpName;
    }
    public String getCorpNameKana() {
        return corpNameKana;
    }

    public void setCorpNameKana(String corpNameKana) {
        this.corpNameKana = corpNameKana;
    }
    public String getCorpNameAlphabet() {
        return corpNameAlphabet;
    }

    public void setCorpNameAlphabet(String corpNameAlphabet) {
        this.corpNameAlphabet = corpNameAlphabet;
    }
    public String getCorpZip() {
        return corpZip;
    }

    public void setCorpZip(String corpZip) {
        this.corpZip = corpZip;
    }
    public String getCorpAddress() {
        return corpAddress;
    }

    public void setCorpAddress(String corpAddress) {
        this.corpAddress = corpAddress;
    }
    public String getCorpAddressKana() {
        return corpAddressKana;
    }

    public void setCorpAddressKana(String corpAddressKana) {
        this.corpAddressKana = corpAddressKana;
    }
    public String getCorpTel() {
        return corpTel;
    }

    public void setCorpTel(String corpTel) {
        this.corpTel = corpTel;
    }
    public String getEstablishmentDate() {
        return establishmentDate;
    }

    public void setEstablishmentDate(String establishmentDate) {
        this.establishmentDate = establishmentDate;
    }
    public String getRepFullName() {
        return repFullName;
    }

    public void setRepFullName(String repFullName) {
        this.repFullName = repFullName;
    }
    public String getRepFullNameKana() {
        return repFullNameKana;
    }

    public void setRepFullNameKana(String repFullNameKana) {
        this.repFullNameKana = repFullNameKana;
    }
    public String getRepZip() {
        return repZip;
    }

    public void setRepZip(String repZip) {
        this.repZip = repZip;
    }
    public String getRepAddress() {
        return repAddress;
    }

    public void setRepAddress(String repAddress) {
        this.repAddress = repAddress;
    }
    public String getRepAddressKana() {
        return repAddressKana;
    }

    public void setRepAddressKana(String repAddressKana) {
        this.repAddressKana = repAddressKana;
    }
    public String getRepTel() {
        return repTel;
    }

    public void setRepTel(String repTel) {
        this.repTel = repTel;
    }
    public String getRepBirthDate() {
        return repBirthDate;
    }

    public void setRepBirthDate(String repBirthDate) {
        this.repBirthDate = repBirthDate;
    }
    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    public String getAccountHolderKana() {
        return accountHolderKana;
    }

    public void setAccountHolderKana(String accountHolderKana) {
        this.accountHolderKana = accountHolderKana;
    }
    public String getJcbUsageFlag() {
        return jcbUsageFlag;
    }

    public void setJcbUsageFlag(String jcbUsageFlag) {
        this.jcbUsageFlag = jcbUsageFlag;
    }
    public String getAnnualSales() {
        return annualSales;
    }

    public void setAnnualSales(String annualSales) {
        this.annualSales = annualSales;
    }
    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }
    public String getContactFullName() {
        return contactFullName;
    }

    public void setContactFullName(String contactFullName) {
        this.contactFullName = contactFullName;
    }
    public String getContactTel() {
        return contactTel;
    }

    public void setContactTel(String contactTel) {
        this.contactTel = contactTel;
    }
    public String getStoreAddressPref() {
        return storeAddressPref;
    }

    public void setStoreAddressPref(String storeAddressPref) {
        this.storeAddressPref = storeAddressPref;
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
    public String getCorpAddressPref() {
        return corpAddressPref;
    }

    public void setCorpAddressPref(String corpAddressPref) {
        this.corpAddressPref = corpAddressPref;
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
    public String getRepAddressPref() {
        return repAddressPref;
    }

    public void setRepAddressPref(String repAddressPref) {
        this.repAddressPref = repAddressPref;
    }
    public String getDPointUsageFlag() {
        return dPointUsageFlag;
    }

    public void setDPointUsageFlag(String dPointUsageFlag) {
        this.dPointUsageFlag = dPointUsageFlag;
    }
    public String getRepLastNameAlphabet() {
        return repLastNameAlphabet;
    }

    public void setRepLastNameAlphabet(String repLastNameAlphabet) {
        this.repLastNameAlphabet = repLastNameAlphabet;
    }
    public String getRepFirstNameAlphabet() {
        return repFirstNameAlphabet;
    }

    public void setRepFirstNameAlphabet(String repFirstNameAlphabet) {
        this.repFirstNameAlphabet = repFirstNameAlphabet;
    }
    public String getRepGender() {
        return repGender;
    }

    public void setRepGender(String repGender) {
        this.repGender = repGender;
    }
    public String getDoorToDoorSalesFlag() {
        return doorToDoorSalesFlag;
    }

    public void setDoorToDoorSalesFlag(String doorToDoorSalesFlag) {
        this.doorToDoorSalesFlag = doorToDoorSalesFlag;
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
    public String getContinuousServiceFlag() {
        return continuousServiceFlag;
    }

    public void setContinuousServiceFlag(String continuousServiceFlag) {
        this.continuousServiceFlag = continuousServiceFlag;
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
    public String getCardDataRetentionStatus() {
        return cardDataRetentionStatus;
    }

    public void setCardDataRetentionStatus(String cardDataRetentionStatus) {
        this.cardDataRetentionStatus = cardDataRetentionStatus;
    }
    public String getPciDssComplianceStatus() {
        return pciDssComplianceStatus;
    }

    public void setPciDssComplianceStatus(String pciDssComplianceStatus) {
        this.pciDssComplianceStatus = pciDssComplianceStatus;
    }
    public String getNonRetentionPlannedMonth() {
        return nonRetentionPlannedMonth;
    }

    public void setNonRetentionPlannedMonth(String nonRetentionPlannedMonth) {
        this.nonRetentionPlannedMonth = nonRetentionPlannedMonth;
    }
    public String getPciDssCompliancePlannedMonth() {
        return pciDssCompliancePlannedMonth;
    }

    public void setPciDssCompliancePlannedMonth(String pciDssCompliancePlannedMonth) {
        this.pciDssCompliancePlannedMonth = pciDssCompliancePlannedMonth;
    }
    public String getTerminalIcStatus() {
        return terminalIcStatus;
    }

    public void setTerminalIcStatus(String terminalIcStatus) {
        this.terminalIcStatus = terminalIcStatus;
    }
    public String getTerminalIcPlannedMonth() {
        return terminalIcPlannedMonth;
    }

    public void setTerminalIcPlannedMonth(String terminalIcPlannedMonth) {
        this.terminalIcPlannedMonth = terminalIcPlannedMonth;
    }
    public String getAcquirerUniqueKey() {
        return acquirerUniqueKey;
    }

    public void setAcquirerUniqueKey(String acquirerUniqueKey) {
        this.acquirerUniqueKey = acquirerUniqueKey;
    }
    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }
    public String getContractSource() {
        return contractSource;
    }

    public void setContractSource(String contractSource) {
        this.contractSource = contractSource;
    }
    public String getGiftContractFlag() {
        return giftContractFlag;
    }

    public void setGiftContractFlag(String giftContractFlag) {
        this.giftContractFlag = giftContractFlag;
    }
    public String getEdyContractFlag() {
        return edyContractFlag;
    }

    public void setEdyContractFlag(String edyContractFlag) {
        this.edyContractFlag = edyContractFlag;
    }
    public String getApplicantType() {
        return applicantType;
    }

    public void setApplicantType(String applicantType) {
        this.applicantType = applicantType;
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
    public String getVmMerchantNumber() {
        return vmMerchantNumber;
    }

    public void setVmMerchantNumber(String vmMerchantNumber) {
        this.vmMerchantNumber = vmMerchantNumber;
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
    public String getBranchNameKana() {
        return branchNameKana;
    }

    public void setBranchNameKana(String branchNameKana) {
        this.branchNameKana = branchNameKana;
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
    public String getTerminalType() {
        return terminalType;
    }

    public void setTerminalType(String terminalType) {
        this.terminalType = terminalType;
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
    public String getFcStoreType() {
        return fcStoreType;
    }

    public void setFcStoreType(String fcStoreType) {
        this.fcStoreType = fcStoreType;
    }
    public String getSecondhandDealerLicenseNumber() {
        return secondhandDealerLicenseNumber;
    }

    public void setSecondhandDealerLicenseNumber(String secondhandDealerLicenseNumber) {
        this.secondhandDealerLicenseNumber = secondhandDealerLicenseNumber;
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
    public Integer getTerminalCount() {
        return terminalCount;
    }

    public void setTerminalCount(Integer terminalCount) {
        this.terminalCount = terminalCount;
    }
    public String getLineType() {
        return lineType;
    }

    public void setLineType(String lineType) {
        this.lineType = lineType;
    }
    public String getPosConnectionFlag() {
        return posConnectionFlag;
    }

    public void setPosConnectionFlag(String posConnectionFlag) {
        this.posConnectionFlag = posConnectionFlag;
    }
    public String getPosMakerName() {
        return posMakerName;
    }

    public void setPosMakerName(String posMakerName) {
        this.posMakerName = posMakerName;
    }
    public String getPosVendorContactName() {
        return posVendorContactName;
    }

    public void setPosVendorContactName(String posVendorContactName) {
        this.posVendorContactName = posVendorContactName;
    }
    public String getPosVendorContactTel() {
        return posVendorContactTel;
    }

    public void setPosVendorContactTel(String posVendorContactTel) {
        this.posVendorContactTel = posVendorContactTel;
    }
    public String getSmartCodeConnectionFlag() {
        return smartCodeConnectionFlag;
    }

    public void setSmartCodeConnectionFlag(String smartCodeConnectionFlag) {
        this.smartCodeConnectionFlag = smartCodeConnectionFlag;
    }
    public String getDPointMerchantCode() {
        return dPointMerchantCode;
    }

    public void setDPointMerchantCode(String dPointMerchantCode) {
        this.dPointMerchantCode = dPointMerchantCode;
    }
    public String getDPointStoreCode() {
        return dPointStoreCode;
    }

    public void setDPointStoreCode(String dPointStoreCode) {
        this.dPointStoreCode = dPointStoreCode;
    }
    public String getDPointBranchCode() {
        return dPointBranchCode;
    }

    public void setDPointBranchCode(String dPointBranchCode) {
        this.dPointBranchCode = dPointBranchCode;
    }
    public String getVisaMasterMerchantNumber() {
        return visaMasterMerchantNumber;
    }

    public void setVisaMasterMerchantNumber(String visaMasterMerchantNumber) {
        this.visaMasterMerchantNumber = visaMasterMerchantNumber;
    }
    public String getNanacoMerchantNumber() {
        return nanacoMerchantNumber;
    }

    public void setNanacoMerchantNumber(String nanacoMerchantNumber) {
        this.nanacoMerchantNumber = nanacoMerchantNumber;
    }
    public String getIdMerchantNumber() {
        return idMerchantNumber;
    }

    public void setIdMerchantNumber(String idMerchantNumber) {
        this.idMerchantNumber = idMerchantNumber;
    }
    public String getTransitMerchantNumber() {
        return transitMerchantNumber;
    }

    public void setTransitMerchantNumber(String transitMerchantNumber) {
        this.transitMerchantNumber = transitMerchantNumber;
    }
    public String getUnionpayMerchantNumber() {
        return unionpayMerchantNumber;
    }

    public void setUnionpayMerchantNumber(String unionpayMerchantNumber) {
        this.unionpayMerchantNumber = unionpayMerchantNumber;
    }
    public String getWaonMerchantNumber() {
        return waonMerchantNumber;
    }

    public void setWaonMerchantNumber(String waonMerchantNumber) {
        this.waonMerchantNumber = waonMerchantNumber;
    }
    public String getEdyMerchantNumber() {
        return edyMerchantNumber;
    }

    public void setEdyMerchantNumber(String edyMerchantNumber) {
        this.edyMerchantNumber = edyMerchantNumber;
    }
    public String getNfcMerchantNumber() {
        return nfcMerchantNumber;
    }

    public void setNfcMerchantNumber(String nfcMerchantNumber) {
        this.nfcMerchantNumber = nfcMerchantNumber;
    }
    public String getTransitOperator() {
        return transitOperator;
    }

    public void setTransitOperator(String transitOperator) {
        this.transitOperator = transitOperator;
    }
    public String getEdyId() {
        return edyId;
    }

    public void setEdyId(String edyId) {
        this.edyId = edyId;
    }
    public String getSteraTerminalNumber1() {
        return steraTerminalNumber1;
    }

    public void setSteraTerminalNumber1(String steraTerminalNumber1) {
        this.steraTerminalNumber1 = steraTerminalNumber1;
    }
    public String getSteraTerminalNumber2() {
        return steraTerminalNumber2;
    }

    public void setSteraTerminalNumber2(String steraTerminalNumber2) {
        this.steraTerminalNumber2 = steraTerminalNumber2;
    }
    public String getSteraTerminalNumber3() {
        return steraTerminalNumber3;
    }

    public void setSteraTerminalNumber3(String steraTerminalNumber3) {
        this.steraTerminalNumber3 = steraTerminalNumber3;
    }
    public String getSteraTerminalNumber4() {
        return steraTerminalNumber4;
    }

    public void setSteraTerminalNumber4(String steraTerminalNumber4) {
        this.steraTerminalNumber4 = steraTerminalNumber4;
    }
    public String getSteraTerminalNumber5() {
        return steraTerminalNumber5;
    }

    public void setSteraTerminalNumber5(String steraTerminalNumber5) {
        this.steraTerminalNumber5 = steraTerminalNumber5;
    }
    public BigDecimal getFeeRateBrandRakutenPay() {
        return feeRateBrandRakutenPay;
    }

    public void setFeeRateBrandRakutenPay(BigDecimal feeRateBrandRakutenPay) {
        this.feeRateBrandRakutenPay = feeRateBrandRakutenPay;
    }
    public BigDecimal getFeeRateBrandLinePay() {
        return feeRateBrandLinePay;
    }

    public void setFeeRateBrandLinePay(BigDecimal feeRateBrandLinePay) {
        this.feeRateBrandLinePay = feeRateBrandLinePay;
    }
    public BigDecimal getFeeRateBrandPaypay() {
        return feeRateBrandPaypay;
    }

    public void setFeeRateBrandPaypay(BigDecimal feeRateBrandPaypay) {
        this.feeRateBrandPaypay = feeRateBrandPaypay;
    }
    public BigDecimal getFeeRateBrandDBarai() {
        return feeRateBrandDBarai;
    }

    public void setFeeRateBrandDBarai(BigDecimal feeRateBrandDBarai) {
        this.feeRateBrandDBarai = feeRateBrandDBarai;
    }
    public BigDecimal getFeeRateBrandAuPay() {
        return feeRateBrandAuPay;
    }

    public void setFeeRateBrandAuPay(BigDecimal feeRateBrandAuPay) {
        this.feeRateBrandAuPay = feeRateBrandAuPay;
    }
    public BigDecimal getFeeRateBrandMerpay() {
        return feeRateBrandMerpay;
    }

    public void setFeeRateBrandMerpay(BigDecimal feeRateBrandMerpay) {
        this.feeRateBrandMerpay = feeRateBrandMerpay;
    }
    public BigDecimal getFeeRateBrandYuchoPay() {
        return feeRateBrandYuchoPay;
    }

    public void setFeeRateBrandYuchoPay(BigDecimal feeRateBrandYuchoPay) {
        this.feeRateBrandYuchoPay = feeRateBrandYuchoPay;
    }
    public BigDecimal getFeeRateBrandAeonPay() {
        return feeRateBrandAeonPay;
    }

    public void setFeeRateBrandAeonPay(BigDecimal feeRateBrandAeonPay) {
        this.feeRateBrandAeonPay = feeRateBrandAeonPay;
    }
    public BigDecimal getAtokaraWholesaleRate() {
        return atokaraWholesaleRate;
    }

    public void setAtokaraWholesaleRate(BigDecimal atokaraWholesaleRate) {
        this.atokaraWholesaleRate = atokaraWholesaleRate;
    }
    public BigDecimal getMerchantInstallmentFee1() {
        return merchantInstallmentFee1;
    }

    public void setMerchantInstallmentFee1(BigDecimal merchantInstallmentFee1) {
        this.merchantInstallmentFee1 = merchantInstallmentFee1;
    }
    public BigDecimal getMerchantInstallmentFee3() {
        return merchantInstallmentFee3;
    }

    public void setMerchantInstallmentFee3(BigDecimal merchantInstallmentFee3) {
        this.merchantInstallmentFee3 = merchantInstallmentFee3;
    }
    public BigDecimal getMerchantInstallmentFee4() {
        return merchantInstallmentFee4;
    }

    public void setMerchantInstallmentFee4(BigDecimal merchantInstallmentFee4) {
        this.merchantInstallmentFee4 = merchantInstallmentFee4;
    }
    public BigDecimal getMerchantInstallmentFee5() {
        return merchantInstallmentFee5;
    }

    public void setMerchantInstallmentFee5(BigDecimal merchantInstallmentFee5) {
        this.merchantInstallmentFee5 = merchantInstallmentFee5;
    }
    public BigDecimal getMerchantInstallmentFee6() {
        return merchantInstallmentFee6;
    }

    public void setMerchantInstallmentFee6(BigDecimal merchantInstallmentFee6) {
        this.merchantInstallmentFee6 = merchantInstallmentFee6;
    }
    public BigDecimal getMerchantInstallmentFee10() {
        return merchantInstallmentFee10;
    }

    public void setMerchantInstallmentFee10(BigDecimal merchantInstallmentFee10) {
        this.merchantInstallmentFee10 = merchantInstallmentFee10;
    }
    public BigDecimal getMerchantInstallmentFee12() {
        return merchantInstallmentFee12;
    }

    public void setMerchantInstallmentFee12(BigDecimal merchantInstallmentFee12) {
        this.merchantInstallmentFee12 = merchantInstallmentFee12;
    }
    public BigDecimal getMerchantInstallmentFee15() {
        return merchantInstallmentFee15;
    }

    public void setMerchantInstallmentFee15(BigDecimal merchantInstallmentFee15) {
        this.merchantInstallmentFee15 = merchantInstallmentFee15;
    }
    public BigDecimal getMerchantInstallmentFee18() {
        return merchantInstallmentFee18;
    }

    public void setMerchantInstallmentFee18(BigDecimal merchantInstallmentFee18) {
        this.merchantInstallmentFee18 = merchantInstallmentFee18;
    }
    public BigDecimal getMerchantInstallmentFee20() {
        return merchantInstallmentFee20;
    }

    public void setMerchantInstallmentFee20(BigDecimal merchantInstallmentFee20) {
        this.merchantInstallmentFee20 = merchantInstallmentFee20;
    }
    public BigDecimal getMerchantInstallmentFee24() {
        return merchantInstallmentFee24;
    }

    public void setMerchantInstallmentFee24(BigDecimal merchantInstallmentFee24) {
        this.merchantInstallmentFee24 = merchantInstallmentFee24;
    }
    public BigDecimal getMerchantInstallmentFee30() {
        return merchantInstallmentFee30;
    }

    public void setMerchantInstallmentFee30(BigDecimal merchantInstallmentFee30) {
        this.merchantInstallmentFee30 = merchantInstallmentFee30;
    }
    public BigDecimal getMerchantInstallmentFee36() {
        return merchantInstallmentFee36;
    }

    public void setMerchantInstallmentFee36(BigDecimal merchantInstallmentFee36) {
        this.merchantInstallmentFee36 = merchantInstallmentFee36;
    }
    public BigDecimal getFeeRateBrandWechat() {
        return feeRateBrandWechat;
    }

    public void setFeeRateBrandWechat(BigDecimal feeRateBrandWechat) {
        this.feeRateBrandWechat = feeRateBrandWechat;
    }
    public BigDecimal getFeeRateBrandAlipay() {
        return feeRateBrandAlipay;
    }

    public void setFeeRateBrandAlipay(BigDecimal feeRateBrandAlipay) {
        this.feeRateBrandAlipay = feeRateBrandAlipay;
    }
    public BigDecimal getFeeRateBrandWesmo() {
        return feeRateBrandWesmo;
    }

    public void setFeeRateBrandWesmo(BigDecimal feeRateBrandWesmo) {
        this.feeRateBrandWesmo = feeRateBrandWesmo;
    }
    public BigDecimal getAtokaraCustomerRate() {
        return atokaraCustomerRate;
    }

    public void setAtokaraCustomerRate(BigDecimal atokaraCustomerRate) {
        this.atokaraCustomerRate = atokaraCustomerRate;
    }
    public BigDecimal getCustomerInstallmentFee1() {
        return customerInstallmentFee1;
    }

    public void setCustomerInstallmentFee1(BigDecimal customerInstallmentFee1) {
        this.customerInstallmentFee1 = customerInstallmentFee1;
    }
    public BigDecimal getCustomerInstallmentFee3() {
        return customerInstallmentFee3;
    }

    public void setCustomerInstallmentFee3(BigDecimal customerInstallmentFee3) {
        this.customerInstallmentFee3 = customerInstallmentFee3;
    }
    public BigDecimal getCustomerInstallmentFee4() {
        return customerInstallmentFee4;
    }

    public void setCustomerInstallmentFee4(BigDecimal customerInstallmentFee4) {
        this.customerInstallmentFee4 = customerInstallmentFee4;
    }
    public BigDecimal getCustomerInstallmentFee5() {
        return customerInstallmentFee5;
    }

    public void setCustomerInstallmentFee5(BigDecimal customerInstallmentFee5) {
        this.customerInstallmentFee5 = customerInstallmentFee5;
    }
    public BigDecimal getCustomerInstallmentFee6() {
        return customerInstallmentFee6;
    }

    public void setCustomerInstallmentFee6(BigDecimal customerInstallmentFee6) {
        this.customerInstallmentFee6 = customerInstallmentFee6;
    }
    public BigDecimal getCustomerInstallmentFee10() {
        return customerInstallmentFee10;
    }

    public void setCustomerInstallmentFee10(BigDecimal customerInstallmentFee10) {
        this.customerInstallmentFee10 = customerInstallmentFee10;
    }
    public BigDecimal getCustomerInstallmentFee12() {
        return customerInstallmentFee12;
    }

    public void setCustomerInstallmentFee12(BigDecimal customerInstallmentFee12) {
        this.customerInstallmentFee12 = customerInstallmentFee12;
    }
    public BigDecimal getCustomerInstallmentFee15() {
        return customerInstallmentFee15;
    }

    public void setCustomerInstallmentFee15(BigDecimal customerInstallmentFee15) {
        this.customerInstallmentFee15 = customerInstallmentFee15;
    }
    public BigDecimal getCustomerInstallmentFee18() {
        return customerInstallmentFee18;
    }

    public void setCustomerInstallmentFee18(BigDecimal customerInstallmentFee18) {
        this.customerInstallmentFee18 = customerInstallmentFee18;
    }
    public BigDecimal getCustomerInstallmentFee20() {
        return customerInstallmentFee20;
    }

    public void setCustomerInstallmentFee20(BigDecimal customerInstallmentFee20) {
        this.customerInstallmentFee20 = customerInstallmentFee20;
    }
    public BigDecimal getCustomerInstallmentFee24() {
        return customerInstallmentFee24;
    }

    public void setCustomerInstallmentFee24(BigDecimal customerInstallmentFee24) {
        this.customerInstallmentFee24 = customerInstallmentFee24;
    }
    public BigDecimal getCustomerInstallmentFee30() {
        return customerInstallmentFee30;
    }

    public void setCustomerInstallmentFee30(BigDecimal customerInstallmentFee30) {
        this.customerInstallmentFee30 = customerInstallmentFee30;
    }
    public BigDecimal getCustomerInstallmentFee36() {
        return customerInstallmentFee36;
    }

    public void setCustomerInstallmentFee36(BigDecimal customerInstallmentFee36) {
        this.customerInstallmentFee36 = customerInstallmentFee36;
    }
    public String getCostShareFlagRakutenPay() {
        return costShareFlagRakutenPay;
    }

    public void setCostShareFlagRakutenPay(String costShareFlagRakutenPay) {
        this.costShareFlagRakutenPay = costShareFlagRakutenPay;
    }
    public String getCostShareFlagLinePay() {
        return costShareFlagLinePay;
    }

    public void setCostShareFlagLinePay(String costShareFlagLinePay) {
        this.costShareFlagLinePay = costShareFlagLinePay;
    }
    public String getCostShareFlagPaypay() {
        return costShareFlagPaypay;
    }

    public void setCostShareFlagPaypay(String costShareFlagPaypay) {
        this.costShareFlagPaypay = costShareFlagPaypay;
    }
    public String getCostShareFlagDBarai() {
        return costShareFlagDBarai;
    }

    public void setCostShareFlagDBarai(String costShareFlagDBarai) {
        this.costShareFlagDBarai = costShareFlagDBarai;
    }
    public String getCostShareFlagAuPay() {
        return costShareFlagAuPay;
    }

    public void setCostShareFlagAuPay(String costShareFlagAuPay) {
        this.costShareFlagAuPay = costShareFlagAuPay;
    }
    public String getCostShareFlagMerpay() {
        return costShareFlagMerpay;
    }

    public void setCostShareFlagMerpay(String costShareFlagMerpay) {
        this.costShareFlagMerpay = costShareFlagMerpay;
    }
    public String getCostShareFlagYuchoPay() {
        return costShareFlagYuchoPay;
    }

    public void setCostShareFlagYuchoPay(String costShareFlagYuchoPay) {
        this.costShareFlagYuchoPay = costShareFlagYuchoPay;
    }
    public String getCostShareFlagAeonPay() {
        return costShareFlagAeonPay;
    }

    public void setCostShareFlagAeonPay(String costShareFlagAeonPay) {
        this.costShareFlagAeonPay = costShareFlagAeonPay;
    }
    public String getCostShareFlagWesmo() {
        return costShareFlagWesmo;
    }

    public void setCostShareFlagWesmo(String costShareFlagWesmo) {
        this.costShareFlagWesmo = costShareFlagWesmo;
    }
    public String getUnionpayQrMerchantNumber() {
        return unionpayQrMerchantNumber;
    }

    public void setUnionpayQrMerchantNumber(String unionpayQrMerchantNumber) {
        this.unionpayQrMerchantNumber = unionpayQrMerchantNumber;
    }
    public String getAwMerchantNumber() {
        return awMerchantNumber;
    }

    public void setAwMerchantNumber(String awMerchantNumber) {
        this.awMerchantNumber = awMerchantNumber;
    }
    public String getDBaraiIpid() {
        return dBaraiIpid;
    }

    public void setDBaraiIpid(String dBaraiIpid) {
        this.dBaraiIpid = dBaraiIpid;
    }
    public String getAlipayPid() {
        return alipayPid;
    }

    public void setAlipayPid(String alipayPid) {
        this.alipayPid = alipayPid;
    }
    public String getUnionpayQrMid() {
        return unionpayQrMid;
    }

    public void setUnionpayQrMid(String unionpayQrMid) {
        this.unionpayQrMid = unionpayQrMid;
    }
    public String getRelocationRepresentativeMerchantNumber() {
        return relocationRepresentativeMerchantNumber;
    }

    public void setRelocationRepresentativeMerchantNumber(String relocationRepresentativeMerchantNumber) {
        this.relocationRepresentativeMerchantNumber = relocationRepresentativeMerchantNumber;
    }
    public String getRelocationPlatformMerchantNumber() {
        return relocationPlatformMerchantNumber;
    }

    public void setRelocationPlatformMerchantNumber(String relocationPlatformMerchantNumber) {
        this.relocationPlatformMerchantNumber = relocationPlatformMerchantNumber;
    }
    public String getCancelAndNewRepresentativeMerchantNumber() {
        return cancelAndNewRepresentativeMerchantNumber;
    }

    public void setCancelAndNewRepresentativeMerchantNumber(String cancelAndNewRepresentativeMerchantNumber) {
        this.cancelAndNewRepresentativeMerchantNumber = cancelAndNewRepresentativeMerchantNumber;
    }
    public String getCancelAndNewPlatformMerchantNumber() {
        return cancelAndNewPlatformMerchantNumber;
    }

    public void setCancelAndNewPlatformMerchantNumber(String cancelAndNewPlatformMerchantNumber) {
        this.cancelAndNewPlatformMerchantNumber = cancelAndNewPlatformMerchantNumber;
    }
    public Integer getCafisArchTerminalCount() {
        return cafisArchTerminalCount;
    }

    public void setCafisArchTerminalCount(Integer cafisArchTerminalCount) {
        this.cafisArchTerminalCount = cafisArchTerminalCount;
    }
    public BigDecimal getQuoCardPayMerchantRateNss() {
        return quoCardPayMerchantRateNss;
    }

    public void setQuoCardPayMerchantRateNss(BigDecimal quoCardPayMerchantRateNss) {
        this.quoCardPayMerchantRateNss = quoCardPayMerchantRateNss;
    }
    public BigDecimal getQuoCardPayBrandRateNss() {
        return quoCardPayBrandRateNss;
    }

    public void setQuoCardPayBrandRateNss(BigDecimal quoCardPayBrandRateNss) {
        this.quoCardPayBrandRateNss = quoCardPayBrandRateNss;
    }
    public String getBankPayNssTid() {
        return bankPayNssTid;
    }

    public void setBankPayNssTid(String bankPayNssTid) {
        this.bankPayNssTid = bankPayNssTid;
    }
    public BigDecimal getFeeRateJcoinPayMerchant() {
        return feeRateJcoinPayMerchant;
    }

    public void setFeeRateJcoinPayMerchant(BigDecimal feeRateJcoinPayMerchant) {
        this.feeRateJcoinPayMerchant = feeRateJcoinPayMerchant;
    }
    public BigDecimal getFeeRateJcoinPayBrand() {
        return feeRateJcoinPayBrand;
    }

    public void setFeeRateJcoinPayBrand(BigDecimal feeRateJcoinPayBrand) {
        this.feeRateJcoinPayBrand = feeRateJcoinPayBrand;
    }

}
