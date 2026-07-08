package com.cupit.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JFTD統合振込CSV作成・帳票出力の支払明細書に印字する会社情報・振込先情報。
 * company_info_id=1固定の1行のみを想定した設定マスタ。
 */
@Entity
@Table(name = "m_jftd_report_company_info")
public class JftdReportCompanyInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_info_id")
    private int companyInfoId;

    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    @Column(name = "recipient_zip")
    private String recipientZip;

    @Column(name = "recipient_address")
    private String recipientAddress;

    @Column(name = "recipient_invoice_no")
    private String recipientInvoiceNo;

    @Column(name = "sender_name", nullable = false)
    private String senderName;

    @Column(name = "sender_zip")
    private String senderZip;

    @Column(name = "sender_address")
    private String senderAddress;

    @Column(name = "sender_invoice_no")
    private String senderInvoiceNo;

    @Column(name = "sender_tel")
    private String senderTel;

    @Column(name = "sender_fax")
    private String senderFax;

    @Column(name = "sender_contact")
    private String senderContact;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_branch_name")
    private String bankBranchName;

    @Column(name = "bank_account_type")
    private String bankAccountType;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "bank_account_holder_kana")
    private String bankAccountHolderKana;

    @Column(name = "update_employee")
    private String updateEmployee;

    @Column(name = "create_date", nullable = false)
    private LocalDate createDate;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    public int getCompanyInfoId() {
        return companyInfoId;
    }

    public void setCompanyInfoId(int companyInfoId) {
        this.companyInfoId = companyInfoId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientZip() {
        return recipientZip;
    }

    public void setRecipientZip(String recipientZip) {
        this.recipientZip = recipientZip;
    }

    public String getRecipientAddress() {
        return recipientAddress;
    }

    public void setRecipientAddress(String recipientAddress) {
        this.recipientAddress = recipientAddress;
    }

    public String getRecipientInvoiceNo() {
        return recipientInvoiceNo;
    }

    public void setRecipientInvoiceNo(String recipientInvoiceNo) {
        this.recipientInvoiceNo = recipientInvoiceNo;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderZip() {
        return senderZip;
    }

    public void setSenderZip(String senderZip) {
        this.senderZip = senderZip;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public String getSenderInvoiceNo() {
        return senderInvoiceNo;
    }

    public void setSenderInvoiceNo(String senderInvoiceNo) {
        this.senderInvoiceNo = senderInvoiceNo;
    }

    public String getSenderTel() {
        return senderTel;
    }

    public void setSenderTel(String senderTel) {
        this.senderTel = senderTel;
    }

    public String getSenderFax() {
        return senderFax;
    }

    public void setSenderFax(String senderFax) {
        this.senderFax = senderFax;
    }

    public String getSenderContact() {
        return senderContact;
    }

    public void setSenderContact(String senderContact) {
        this.senderContact = senderContact;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankBranchName() {
        return bankBranchName;
    }

    public void setBankBranchName(String bankBranchName) {
        this.bankBranchName = bankBranchName;
    }

    public String getBankAccountType() {
        return bankAccountType;
    }

    public void setBankAccountType(String bankAccountType) {
        this.bankAccountType = bankAccountType;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getBankAccountHolderKana() {
        return bankAccountHolderKana;
    }

    public void setBankAccountHolderKana(String bankAccountHolderKana) {
        this.bankAccountHolderKana = bankAccountHolderKana;
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
