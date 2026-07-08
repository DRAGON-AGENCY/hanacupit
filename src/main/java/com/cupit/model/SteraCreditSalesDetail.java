package com.cupit.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * stera terminal経由のクレジット・電子マネー売上件別明細CSV由来の明細。
 * 取引コードはm_smcc_merchant_no.merchant_no（ファイル列：利用加盟店番号）から解決する。
 */
@Entity
@Table(name = "m_stera_credit_sales_detail")
public class SteraCreditSalesDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stera_credit_sales_id")
    private int steraCreditSalesId;

    @Column(name = "trade_code", nullable = false)
    private String tradeCode;

    @Column(name = "batch_id", nullable = false)
    private int batchId;

    @Column(name = "merchant_id", nullable = false)
    private String merchantId;

    @Column(name = "sent_date", nullable = false)
    private String sentDate;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType;

    @Column(name = "transaction_type2")
    private String transactionType2;

    @Column(name = "card_number_masked")
    private String cardNumberMasked;

    @Column(name = "transaction_date", nullable = false)
    private String transactionDate;

    @Column(name = "amount_sign", nullable = false)
    private String amountSign;

    @Column(name = "billing_amount", nullable = false)
    private int billingAmount;

    @Column(name = "original_amount", nullable = false)
    private int originalAmount;

    @Column(name = "approval_number", nullable = false)
    private String approvalNumber;

    @Column(name = "terminal_id", nullable = false)
    private String terminalId;

    @Column(name = "change_data_flag")
    private String changeDataFlag;

    @Column(name = "store_name", nullable = false)
    private String storeName;

    @Column(name = "card_brand", nullable = false)
    private String cardBrand;

    @Column(name = "terminal_sequence_no")
    private String terminalSequenceNo;

    @Column(name = "summary_count")
    private String summaryCount;

    @Column(name = "reader_writer_id")
    private String readerWriterId;

    @Column(name = "representative_merchant_id", nullable = false)
    private String representativeMerchantId;

    @Column(name = "update_employee")
    private String updateEmployee;

    @Column(name = "create_date", nullable = false)
    private LocalDate createDate;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    public int getSteraCreditSalesId() {
        return steraCreditSalesId;
    }

    public void setSteraCreditSalesId(int steraCreditSalesId) {
        this.steraCreditSalesId = steraCreditSalesId;
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

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getSentDate() {
        return sentDate;
    }

    public void setSentDate(String sentDate) {
        this.sentDate = sentDate;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getTransactionType2() {
        return transactionType2;
    }

    public void setTransactionType2(String transactionType2) {
        this.transactionType2 = transactionType2;
    }

    public String getCardNumberMasked() {
        return cardNumberMasked;
    }

    public void setCardNumberMasked(String cardNumberMasked) {
        this.cardNumberMasked = cardNumberMasked;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getAmountSign() {
        return amountSign;
    }

    public void setAmountSign(String amountSign) {
        this.amountSign = amountSign;
    }

    public int getBillingAmount() {
        return billingAmount;
    }

    public void setBillingAmount(int billingAmount) {
        this.billingAmount = billingAmount;
    }

    public int getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(int originalAmount) {
        this.originalAmount = originalAmount;
    }

    public String getApprovalNumber() {
        return approvalNumber;
    }

    public void setApprovalNumber(String approvalNumber) {
        this.approvalNumber = approvalNumber;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getChangeDataFlag() {
        return changeDataFlag;
    }

    public void setChangeDataFlag(String changeDataFlag) {
        this.changeDataFlag = changeDataFlag;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getCardBrand() {
        return cardBrand;
    }

    public void setCardBrand(String cardBrand) {
        this.cardBrand = cardBrand;
    }

    public String getTerminalSequenceNo() {
        return terminalSequenceNo;
    }

    public void setTerminalSequenceNo(String terminalSequenceNo) {
        this.terminalSequenceNo = terminalSequenceNo;
    }

    public String getSummaryCount() {
        return summaryCount;
    }

    public void setSummaryCount(String summaryCount) {
        this.summaryCount = summaryCount;
    }

    public String getReaderWriterId() {
        return readerWriterId;
    }

    public void setReaderWriterId(String readerWriterId) {
        this.readerWriterId = readerWriterId;
    }

    public String getRepresentativeMerchantId() {
        return representativeMerchantId;
    }

    public void setRepresentativeMerchantId(String representativeMerchantId) {
        this.representativeMerchantId = representativeMerchantId;
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
