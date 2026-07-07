package com.cupit.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "m_terminal_monthly_fee")
public class TerminalMonthlyFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "terminal_fee_id")
    private int terminalFeeId;

    @Column(name = "trade_code", nullable = false)
    private String tradeCode;

    @Column(name = "batch_id", nullable = false)
    private int batchId;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "billing_month")
    private String billingMonth;

    @Column(name = "billing_no")
    private String billingNo;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "store_name")
    private String storeName;

    @Column(name = "terminal_id")
    private String terminalId;

    @Column(name = "unit_price", nullable = false)
    private int unitPrice;

    @Column(name = "qty_credit", nullable = false)
    private int qtyCredit;

    @Column(name = "qty_qr", nullable = false)
    private int qtyQr;

    @Column(name = "qty_ic_transportation", nullable = false)
    private int qtyIcTransportation;

    @Column(name = "qty_ic_id", nullable = false)
    private int qtyIcId;

    @Column(name = "qty_ic_waon", nullable = false)
    private int qtyIcWaon;

    @Column(name = "qty_ic_nanaco", nullable = false)
    private int qtyIcNanaco;

    @Column(name = "qty_ic_edyrakuten", nullable = false)
    private int qtyIcEdyrakuten;

    @Column(name = "qty_ic_quicpay", nullable = false)
    private int qtyIcQuicpay;

    @Column(name = "qty_sim", nullable = false)
    private int qtySim;

    @Column(name = "tx_count_credit", nullable = false)
    private int txCountCredit;

    @Column(name = "tx_count_qr", nullable = false)
    private int txCountQr;

    @Column(name = "tx_count_ic", nullable = false)
    private int txCountIc;

    @Column(name = "tx_count_total", nullable = false)
    private int txCountTotal;

    @Column(name = "amount_credit", nullable = false)
    private int amountCredit;

    @Column(name = "amount_qr", nullable = false)
    private int amountQr;

    @Column(name = "amount_ic", nullable = false)
    private int amountIc;

    @Column(name = "amount_total", nullable = false)
    private int amountTotal;

    @Column(name = "update_employee")
    private String updateEmployee;

    @Column(name = "create_date", nullable = false)
    private LocalDate createDate;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    public int getTerminalFeeId() { return terminalFeeId; }
    public void setTerminalFeeId(int terminalFeeId) { this.terminalFeeId = terminalFeeId; }
    public String getTradeCode() { return tradeCode; }
    public void setTradeCode(String tradeCode) { this.tradeCode = tradeCode; }
    public int getBatchId() { return batchId; }
    public void setBatchId(int batchId) { this.batchId = batchId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getBillingMonth() { return billingMonth; }
    public void setBillingMonth(String billingMonth) { this.billingMonth = billingMonth; }
    public String getBillingNo() { return billingNo; }
    public void setBillingNo(String billingNo) { this.billingNo = billingNo; }
    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public String getTerminalId() { return terminalId; }
    public void setTerminalId(String terminalId) { this.terminalId = terminalId; }
    public int getUnitPrice() { return unitPrice; }
    public void setUnitPrice(int unitPrice) { this.unitPrice = unitPrice; }
    public int getQtyCredit() { return qtyCredit; }
    public void setQtyCredit(int qtyCredit) { this.qtyCredit = qtyCredit; }
    public int getQtyQr() { return qtyQr; }
    public void setQtyQr(int qtyQr) { this.qtyQr = qtyQr; }
    public int getQtyIcTransportation() { return qtyIcTransportation; }
    public void setQtyIcTransportation(int qtyIcTransportation) { this.qtyIcTransportation = qtyIcTransportation; }
    public int getQtyIcId() { return qtyIcId; }
    public void setQtyIcId(int qtyIcId) { this.qtyIcId = qtyIcId; }
    public int getQtyIcWaon() { return qtyIcWaon; }
    public void setQtyIcWaon(int qtyIcWaon) { this.qtyIcWaon = qtyIcWaon; }
    public int getQtyIcNanaco() { return qtyIcNanaco; }
    public void setQtyIcNanaco(int qtyIcNanaco) { this.qtyIcNanaco = qtyIcNanaco; }
    public int getQtyIcEdyrakuten() { return qtyIcEdyrakuten; }
    public void setQtyIcEdyrakuten(int qtyIcEdyrakuten) { this.qtyIcEdyrakuten = qtyIcEdyrakuten; }
    public int getQtyIcQuicpay() { return qtyIcQuicpay; }
    public void setQtyIcQuicpay(int qtyIcQuicpay) { this.qtyIcQuicpay = qtyIcQuicpay; }
    public int getQtySim() { return qtySim; }
    public void setQtySim(int qtySim) { this.qtySim = qtySim; }
    public int getTxCountCredit() { return txCountCredit; }
    public void setTxCountCredit(int txCountCredit) { this.txCountCredit = txCountCredit; }
    public int getTxCountQr() { return txCountQr; }
    public void setTxCountQr(int txCountQr) { this.txCountQr = txCountQr; }
    public int getTxCountIc() { return txCountIc; }
    public void setTxCountIc(int txCountIc) { this.txCountIc = txCountIc; }
    public int getTxCountTotal() { return txCountTotal; }
    public void setTxCountTotal(int txCountTotal) { this.txCountTotal = txCountTotal; }
    public int getAmountCredit() { return amountCredit; }
    public void setAmountCredit(int amountCredit) { this.amountCredit = amountCredit; }
    public int getAmountQr() { return amountQr; }
    public void setAmountQr(int amountQr) { this.amountQr = amountQr; }
    public int getAmountIc() { return amountIc; }
    public void setAmountIc(int amountIc) { this.amountIc = amountIc; }
    public int getAmountTotal() { return amountTotal; }
    public void setAmountTotal(int amountTotal) { this.amountTotal = amountTotal; }
    public String getUpdateEmployee() { return updateEmployee; }
    public void setUpdateEmployee(String updateEmployee) { this.updateEmployee = updateEmployee; }
    public LocalDate getCreateDate() { return createDate; }
    public void setCreateDate(LocalDate createDate) { this.createDate = createDate; }
    public LocalDate getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDate updatedDate) { this.updatedDate = updatedDate; }
}
