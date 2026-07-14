package com.cupit.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * その他統合振込明細（m_stera_transfer_detail）。確定時点の計算結果に加え、
 * m_stera_storeの口座情報を確定時点のスナップショットとして保持する
 * （確定後にm_stera_storeの内容が変わっても振込内容が変わらないようにするため）。
 */
@Entity
@Table(name = "m_stera_transfer_detail")
public class SteraTransferDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transfer_detail_id")
    private int transferDetailId;

    @Column(name = "transfer_batch_id", nullable = false)
    private int transferBatchId;

    @Column(name = "trade_code", nullable = false)
    private String tradeCode;

    @Column(name = "gross_amount", nullable = false)
    private int grossAmount;

    @Column(name = "acquirer_fee", nullable = false)
    private int acquirerFee;

    @Column(name = "company_fee", nullable = false)
    private int companyFee;

    @Column(name = "transfer_fee", nullable = false)
    private int transferFee;

    @Column(name = "net_amount", nullable = false)
    private int netAmount;

    @Column(name = "bank_code", nullable = false)
    private String bankCode;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "bank_branch_code", nullable = false)
    private String bankBranchCode;

    @Column(name = "branch_name", nullable = false)
    private String branchName;

    @Column(name = "account_type", nullable = false)
    private String accountType;

    @Column(name = "account_no", nullable = false)
    private String accountNo;

    @Column(name = "account_holder_kana", nullable = false)
    private String accountHolderKana;

    @Column(name = "update_employee")
    private String updateEmployee;

    @Column(name = "create_date", nullable = false)
    private LocalDate createDate;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    public int getTransferDetailId() {
        return transferDetailId;
    }

    public void setTransferDetailId(int transferDetailId) {
        this.transferDetailId = transferDetailId;
    }

    public int getTransferBatchId() {
        return transferBatchId;
    }

    public void setTransferBatchId(int transferBatchId) {
        this.transferBatchId = transferBatchId;
    }

    public String getTradeCode() {
        return tradeCode;
    }

    public void setTradeCode(String tradeCode) {
        this.tradeCode = tradeCode;
    }

    public int getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(int grossAmount) {
        this.grossAmount = grossAmount;
    }

    public int getAcquirerFee() {
        return acquirerFee;
    }

    public void setAcquirerFee(int acquirerFee) {
        this.acquirerFee = acquirerFee;
    }

    public int getCompanyFee() {
        return companyFee;
    }

    public void setCompanyFee(int companyFee) {
        this.companyFee = companyFee;
    }

    public int getTransferFee() {
        return transferFee;
    }

    public void setTransferFee(int transferFee) {
        this.transferFee = transferFee;
    }

    public int getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(int netAmount) {
        this.netAmount = netAmount;
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

    public String getBankBranchCode() {
        return bankBranchCode;
    }

    public void setBankBranchCode(String bankBranchCode) {
        this.bankBranchCode = bankBranchCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getAccountHolderKana() {
        return accountHolderKana;
    }

    public void setAccountHolderKana(String accountHolderKana) {
        this.accountHolderKana = accountHolderKana;
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
