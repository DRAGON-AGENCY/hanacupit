package com.cupit.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * stera端末マスタ（m_stera_terminal）。stera terminal経由のJCB・stera code取込みで、
 * 取引コードの解決に使用する（JCB加盟店番号・端末識別番号→取引コード）。
 */
@Entity
@Table(name = "m_stera_terminal")
public class SteraTerminal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_no")
    private long recordNo;

    @Column(name = "trade_code", nullable = false)
    private String tradeCode;

    @Column(name = "terminal_id", nullable = false)
    private String terminalId;

    @Column(name = "jcb_merchant_no")
    private String jcbMerchantNo;

    @Column(name = "hana_cupid_mgmt_no_2", nullable = false)
    private String hanaCupidMgmtNo2;

    @Column(name = "branch_code", nullable = false)
    private String branchCode;

    @Column(name = "terminal_status", nullable = false)
    private String terminalStatus;

    @Column(name = "terminal_start_date", nullable = false)
    private LocalDate terminalStartDate;

    @Column(name = "terminal_end_date")
    private LocalDate terminalEndDate;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "updated_user_id", nullable = false)
    private String updatedUserId;

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

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getJcbMerchantNo() {
        return jcbMerchantNo;
    }

    public void setJcbMerchantNo(String jcbMerchantNo) {
        this.jcbMerchantNo = jcbMerchantNo;
    }

    public String getHanaCupidMgmtNo2() {
        return hanaCupidMgmtNo2;
    }

    public void setHanaCupidMgmtNo2(String hanaCupidMgmtNo2) {
        this.hanaCupidMgmtNo2 = hanaCupidMgmtNo2;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getTerminalStatus() {
        return terminalStatus;
    }

    public void setTerminalStatus(String terminalStatus) {
        this.terminalStatus = terminalStatus;
    }

    public LocalDate getTerminalStartDate() {
        return terminalStartDate;
    }

    public void setTerminalStartDate(LocalDate terminalStartDate) {
        this.terminalStartDate = terminalStartDate;
    }

    public LocalDate getTerminalEndDate() {
        return terminalEndDate;
    }

    public void setTerminalEndDate(LocalDate terminalEndDate) {
        this.terminalEndDate = terminalEndDate;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedUserId() {
        return updatedUserId;
    }

    public void setUpdatedUserId(String updatedUserId) {
        this.updatedUserId = updatedUserId;
    }
}
