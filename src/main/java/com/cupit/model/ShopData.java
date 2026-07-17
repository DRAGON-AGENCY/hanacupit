package com.cupit.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * ShopDataを表すエンティティ。
 * m_shop_data テーブルの 1 行に対応する。
 */
@Entity
@Table(name = "m_shop_data")
public class ShopData {

    @Id
    @Column(name = "trade_code")
    private String tradeCode;

    @Column(name = "application_type_flag")
    private String applicationTypeFlag;

    @Column(name = "store_name_alphabet")
    private String storeNameAlphabet;

    @Column(name = "rep_address_kana")
    private String repAddressKana;

    @Column(name = "jcb_merchant_number")
    private String jcbMerchantNumber;

    @Column(name = "corporate_number")
    private String corporateNumber;

    @Column(name = "door_to_door_sales_flag")
    private String doorToDoorSalesFlag;

    @Column(name = "telemarketing_sales_flag")
    private String telemarketingSalesFlag;

    @Column(name = "chain_sales_flag")
    private String chainSalesFlag;

    @Column(name = "business_opportunity_sales_flag")
    private String businessOpportunitySalesFlag;

    @Column(name = "continuous_service_flag")
    private String continuousServiceFlag;

    @Column(name = "card_data_retention_status")
    private String cardDataRetentionStatus;

    @Column(name = "pci_dss_compliance_status")
    private String pciDssComplianceStatus;

    @Column(name = "non_retention_planned_month")
    private String nonRetentionPlannedMonth;

    @Column(name = "pci_dss_compliance_planned_month")
    private String pciDssCompliancePlannedMonth;

    @Column(name = "terminal_ic_status")
    private String terminalIcStatus;

    @Column(name = "terminal_ic_planned_month")
    private String terminalIcPlannedMonth;

    @Column(name = "acquirer_unique_key")
    private String acquirerUniqueKey;

    @Column(name = "stera_terminal_id")
    private String steraTerminalId;

    @Column(name = "linkage_date")
    private LocalDate linkageDate;

    @Column(name = "existing_contract_flag")
    private String existingContractFlag;

    @Column(name = "classification")
    private String classification;

    @Column(name = "contract_source")
    private String contractSource;

    @Column(name = "gift_contract_flag")
    private String giftContractFlag;

    @Column(name = "edy_contract_flag")
    private String edyContractFlag;

    @Column(name = "cancellation_confirmation")
    private String cancellationConfirmation;

    @Column(name = "cancellation_process_status")
    private String cancellationProcessStatus;

    @Column(name = "registered_date", nullable = false)
    private LocalDate registeredDate;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    @Column(name = "updated_by")
    private String updatedBy;

    public String getTradeCode() {
        return tradeCode;
    }

    public void setTradeCode(String tradeCode) {
        this.tradeCode = tradeCode;
    }

    public String getApplicationTypeFlag() {
        return applicationTypeFlag;
    }

    public void setApplicationTypeFlag(String applicationTypeFlag) {
        this.applicationTypeFlag = applicationTypeFlag;
    }

    public String getStoreNameAlphabet() {
        return storeNameAlphabet;
    }

    public void setStoreNameAlphabet(String storeNameAlphabet) {
        this.storeNameAlphabet = storeNameAlphabet;
    }

    public String getRepAddressKana() {
        return repAddressKana;
    }

    public void setRepAddressKana(String repAddressKana) {
        this.repAddressKana = repAddressKana;
    }

    public String getJcbMerchantNumber() {
        return jcbMerchantNumber;
    }

    public void setJcbMerchantNumber(String jcbMerchantNumber) {
        this.jcbMerchantNumber = jcbMerchantNumber;
    }

    public String getCorporateNumber() {
        return corporateNumber;
    }

    public void setCorporateNumber(String corporateNumber) {
        this.corporateNumber = corporateNumber;
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

    public String getSteraTerminalId() {
        return steraTerminalId;
    }

    public void setSteraTerminalId(String steraTerminalId) {
        this.steraTerminalId = steraTerminalId;
    }

    public LocalDate getLinkageDate() {
        return linkageDate;
    }

    public void setLinkageDate(LocalDate linkageDate) {
        this.linkageDate = linkageDate;
    }

    public String getExistingContractFlag() {
        return existingContractFlag;
    }

    public void setExistingContractFlag(String existingContractFlag) {
        this.existingContractFlag = existingContractFlag;
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

    public String getCancellationConfirmation() {
        return cancellationConfirmation;
    }

    public void setCancellationConfirmation(String cancellationConfirmation) {
        this.cancellationConfirmation = cancellationConfirmation;
    }

    public String getCancellationProcessStatus() {
        return cancellationProcessStatus;
    }

    public void setCancellationProcessStatus(String cancellationProcessStatus) {
        this.cancellationProcessStatus = cancellationProcessStatus;
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
