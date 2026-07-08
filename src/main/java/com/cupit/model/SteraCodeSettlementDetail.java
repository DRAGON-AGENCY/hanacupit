package com.cupit.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * stera code精算明細CSV由来の個別取引明細。ブランド（楽天ペイ・PayPay等）ごとの
 * 明細ブロック末尾にある小計行はこのテーブルには含まず、
 * {@link SteraCodeSettlementSummary}へ格納する（住信SBIの区分1/区分2と同じ設計）。
 * 取引コードはm_stera_terminal.terminal_idから解決する。
 */
@Entity
@Table(name = "m_stera_code_settlement_detail")
public class SteraCodeSettlementDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stera_code_settlement_id")
    private int steraCodeSettlementId;

    @Column(name = "trade_code", nullable = false)
    private String tradeCode;

    @Column(name = "batch_id", nullable = false)
    private int batchId;

    @Column(name = "brand", nullable = false)
    private String brand;

    @Column(name = "terminal_id", nullable = false)
    private String terminalId;

    @Column(name = "slip_number", nullable = false)
    private String slipNumber;

    @Column(name = "settlement_date", nullable = false)
    private String settlementDate;

    @Column(name = "settlement_time", nullable = false)
    private String settlementTime;

    @Column(name = "sales_return_flag", nullable = false)
    private int salesReturnFlag;

    @Column(name = "settlement_amount", nullable = false)
    private int settlementAmount;

    @Column(name = "sub_wallet_name")
    private String subWalletName;

    @Column(name = "update_employee")
    private String updateEmployee;

    @Column(name = "create_date", nullable = false)
    private LocalDate createDate;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    public int getSteraCodeSettlementId() {
        return steraCodeSettlementId;
    }

    public void setSteraCodeSettlementId(int steraCodeSettlementId) {
        this.steraCodeSettlementId = steraCodeSettlementId;
    }

    public String getTradeCode() {
        return tradeCode;
    }

    public void setTradeCode(String tradeCode) {
        this.tradeCode = tradeCode;
    }

    public int getBatchId() {
        return batchId;
    }

    public void setBatchId(int batchId) {
        this.batchId = batchId;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getSlipNumber() {
        return slipNumber;
    }

    public void setSlipNumber(String slipNumber) {
        this.slipNumber = slipNumber;
    }

    public String getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(String settlementDate) {
        this.settlementDate = settlementDate;
    }

    public String getSettlementTime() {
        return settlementTime;
    }

    public void setSettlementTime(String settlementTime) {
        this.settlementTime = settlementTime;
    }

    public int getSalesReturnFlag() {
        return salesReturnFlag;
    }

    public void setSalesReturnFlag(int salesReturnFlag) {
        this.salesReturnFlag = salesReturnFlag;
    }

    public int getSettlementAmount() {
        return settlementAmount;
    }

    public void setSettlementAmount(int settlementAmount) {
        this.settlementAmount = settlementAmount;
    }

    public String getSubWalletName() {
        return subWalletName;
    }

    public void setSubWalletName(String subWalletName) {
        this.subWalletName = subWalletName;
    }

    public String getUpdateEmployee() {
        return updateEmployee;
    }

    public void setUpdateEmployee(String updateEmployee) {
        this.updateEmployee = updateEmployee;
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
}
