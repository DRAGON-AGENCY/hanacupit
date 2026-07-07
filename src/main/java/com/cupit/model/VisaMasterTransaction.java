package com.cupit.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "m_visa_master_transaction")
public class VisaMasterTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private int transactionId;

    @Column(name = "trade_code", nullable = false)
    private String tradeCode;

    @Column(name = "batch_id", nullable = false)
    private int batchId;

    @Column(name = "header_id")
    private Integer headerId;

    @Column(name = "parent_merchant_id")
    private String parentMerchantId;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "transaction_no")
    private String transactionNo;

    @Column(name = "sales_date")
    private LocalDate salesDate;

    @Column(name = "card_number_masked")
    private String cardNumberMasked;

    @Column(name = "brand_type")
    private String brandType;

    @Column(name = "payment_type_code")
    private String paymentTypeCode;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "fee_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal feeRate;

    @Column(name = "sales_amount", nullable = false)
    private int salesAmount;

    @Column(name = "fee_amount_1", nullable = false)
    private int feeAmount1;

    @Column(name = "deferred_amount", nullable = false)
    private int deferredAmount;

    @Column(name = "deferred_fee", nullable = false)
    private int deferredFee;

    @Column(name = "transfer_deferred_amount", nullable = false)
    private int transferDeferredAmount;

    @Column(name = "transfer_deferred_fee", nullable = false)
    private int transferDeferredFee;

    @Column(name = "payable_sales_amount", nullable = false)
    private int payableSalesAmount;

    @Column(name = "payable_fee_amount", nullable = false)
    private int payableFeeAmount;

    @Column(name = "payment_amount_1", nullable = false)
    private int paymentAmount1;

    @Column(name = "deferred_balance", nullable = false)
    private int deferredBalance;

    @Column(name = "update_employee")
    private String updateEmployee;

    @Column(name = "create_date", nullable = false)
    private LocalDate createDate;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
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

    public Integer getHeaderId() {
        return headerId;
    }

    public void setHeaderId(Integer headerId) {
        this.headerId = headerId;
    }

    public String getParentMerchantId() {
        return parentMerchantId;
    }

    public void setParentMerchantId(String parentMerchantId) {
        this.parentMerchantId = parentMerchantId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getTransactionNo() {
        return transactionNo;
    }

    public void setTransactionNo(String transactionNo) {
        this.transactionNo = transactionNo;
    }

    public LocalDate getSalesDate() {
        return salesDate;
    }

    public void setSalesDate(LocalDate salesDate) {
        this.salesDate = salesDate;
    }

    public String getCardNumberMasked() {
        return cardNumberMasked;
    }

    public void setCardNumberMasked(String cardNumberMasked) {
        this.cardNumberMasked = cardNumberMasked;
    }

    public String getBrandType() {
        return brandType;
    }

    public void setBrandType(String brandType) {
        this.brandType = brandType;
    }

    public String getPaymentTypeCode() {
        return paymentTypeCode;
    }

    public void setPaymentTypeCode(String paymentTypeCode) {
        this.paymentTypeCode = paymentTypeCode;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getFeeRate() {
        return feeRate;
    }

    public void setFeeRate(BigDecimal feeRate) {
        this.feeRate = feeRate;
    }

    public int getSalesAmount() {
        return salesAmount;
    }

    public void setSalesAmount(int salesAmount) {
        this.salesAmount = salesAmount;
    }

    public int getFeeAmount1() {
        return feeAmount1;
    }

    public void setFeeAmount1(int feeAmount1) {
        this.feeAmount1 = feeAmount1;
    }

    public int getDeferredAmount() {
        return deferredAmount;
    }

    public void setDeferredAmount(int deferredAmount) {
        this.deferredAmount = deferredAmount;
    }

    public int getDeferredFee() {
        return deferredFee;
    }

    public void setDeferredFee(int deferredFee) {
        this.deferredFee = deferredFee;
    }

    public int getTransferDeferredAmount() {
        return transferDeferredAmount;
    }

    public void setTransferDeferredAmount(int transferDeferredAmount) {
        this.transferDeferredAmount = transferDeferredAmount;
    }

    public int getTransferDeferredFee() {
        return transferDeferredFee;
    }

    public void setTransferDeferredFee(int transferDeferredFee) {
        this.transferDeferredFee = transferDeferredFee;
    }

    public int getPayableSalesAmount() {
        return payableSalesAmount;
    }

    public void setPayableSalesAmount(int payableSalesAmount) {
        this.payableSalesAmount = payableSalesAmount;
    }

    public int getPayableFeeAmount() {
        return payableFeeAmount;
    }

    public void setPayableFeeAmount(int payableFeeAmount) {
        this.payableFeeAmount = payableFeeAmount;
    }

    public int getPaymentAmount1() {
        return paymentAmount1;
    }

    public void setPaymentAmount1(int paymentAmount1) {
        this.paymentAmount1 = paymentAmount1;
    }

    public int getDeferredBalance() {
        return deferredBalance;
    }

    public void setDeferredBalance(int deferredBalance) {
        this.deferredBalance = deferredBalance;
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
