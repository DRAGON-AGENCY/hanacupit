package com.cupit.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "m_paygate_store_mapping")
public class PaygateStoreMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paygate_mapping_id")
    private int paygateMappingId;

    @Column(name = "trade_code", nullable = false, length = 10)
    private String tradeCode;

    @Column(name = "store_name", length = 100)
    private String storeName;

    @Column(name = "member_type", length = 10)
    private String memberType;

    @Column(name = "terminal_id", length = 13)
    private String terminalId;

    @Column(name = "reader_serial_no", length = 20)
    private String readerSerialNo;

    @Column(name = "sbi_merchant_id", length = 20)
    private String sbiMerchantId;

    @Column(name = "netstar_store_code", length = 20)
    private String netstarStoreCode;

    @Column(name = "jcb_merchant_no", length = 14)
    private String jcbMerchantNo;

    @Column(name = "dnp_mgmt_no", length = 20)
    private String dnpMgmtNo;

    @Column(name = "rpay_store_code", length = 20)
    private String rpayStoreCode;

    @Column(name = "terminal_status", length = 10)
    private String terminalStatus;

    @Column(name = "usage_intention", length = 10)
    private String usageIntention;

    @Column(name = "usage_intention_updated")
    private LocalDate usageIntentionUpdated;

    @Column(name = "create_date", nullable = false)
    private LocalDate createDate;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    @Column(name = "update_employee", length = 50)
    private String updateEmployee;

    public int getPaygateMappingId() {
        return paygateMappingId;
    }

    public void setPaygateMappingId(int paygateMappingId) {
        this.paygateMappingId = paygateMappingId;
    }

    public String getTradeCode() {
        return tradeCode;
    }

    public void setTradeCode(String tradeCode) {
        this.tradeCode = tradeCode;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getMemberType() {
        return memberType;
    }

    public void setMemberType(String memberType) {
        this.memberType = memberType;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getReaderSerialNo() {
        return readerSerialNo;
    }

    public void setReaderSerialNo(String readerSerialNo) {
        this.readerSerialNo = readerSerialNo;
    }

    public String getSbiMerchantId() {
        return sbiMerchantId;
    }

    public void setSbiMerchantId(String sbiMerchantId) {
        this.sbiMerchantId = sbiMerchantId;
    }

    public String getNetstarStoreCode() {
        return netstarStoreCode;
    }

    public void setNetstarStoreCode(String netstarStoreCode) {
        this.netstarStoreCode = netstarStoreCode;
    }

    public String getJcbMerchantNo() {
        return jcbMerchantNo;
    }

    public void setJcbMerchantNo(String jcbMerchantNo) {
        this.jcbMerchantNo = jcbMerchantNo;
    }

    public String getDnpMgmtNo() {
        return dnpMgmtNo;
    }

    public void setDnpMgmtNo(String dnpMgmtNo) {
        this.dnpMgmtNo = dnpMgmtNo;
    }

    public String getRpayStoreCode() {
        return rpayStoreCode;
    }

    public void setRpayStoreCode(String rpayStoreCode) {
        this.rpayStoreCode = rpayStoreCode;
    }

    public String getTerminalStatus() {
        return terminalStatus;
    }

    public void setTerminalStatus(String terminalStatus) {
        this.terminalStatus = terminalStatus;
    }

    public String getUsageIntention() {
        return usageIntention;
    }

    public void setUsageIntention(String usageIntention) {
        this.usageIntention = usageIntention;
    }

    public LocalDate getUsageIntentionUpdated() {
        return usageIntentionUpdated;
    }

    public void setUsageIntentionUpdated(LocalDate usageIntentionUpdated) {
        this.usageIntentionUpdated = usageIntentionUpdated;
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

    @Override
    public String toString() {
        return "PaygateStoreMapping{"
                + "paygateMappingId=" + paygateMappingId
                + ", tradeCode='" + tradeCode + '\''
                + ", storeName='" + storeName + '\''
                + ", terminalId='" + terminalId + '\''
                + '}';
    }

}
