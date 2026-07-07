package com.cupit.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "m_visa_master_store_header")
public class VisaMasterStoreHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_header_id")
    private int storeHeaderId;

    @Column(name = "trade_code", nullable = false)
    private String tradeCode;

    @Column(name = "batch_id", nullable = false)
    private int batchId;

    @Column(name = "file_created_date")
    private LocalDate fileCreatedDate;

    @Column(name = "sales_summary_date")
    private LocalDate salesSummaryDate;

    @Column(name = "parent_merchant_id")
    private String parentMerchantId;

    @Column(name = "parent_merchant_name")
    private String parentMerchantName;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "merchant_name")
    private String merchantName;

    @Column(name = "transfer_date")
    private LocalDate transferDate;

    @Column(name = "total_sales_count", nullable = false)
    private int totalSalesCount;

    @Column(name = "total_sales_amount", nullable = false)
    private int totalSalesAmount;

    @Column(name = "total_fee_amount_1", nullable = false)
    private int totalFeeAmount1;

    @Column(name = "total_payment_amount_1", nullable = false)
    private int totalPaymentAmount1;

    @Column(name = "update_employee")
    private String updateEmployee;

    @Column(name = "create_date", nullable = false)
    private LocalDate createDate;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    public int getStoreHeaderId() {
        return storeHeaderId;
    }

    public void setStoreHeaderId(int storeHeaderId) {
        this.storeHeaderId = storeHeaderId;
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

    public LocalDate getFileCreatedDate() {
        return fileCreatedDate;
    }

    public void setFileCreatedDate(LocalDate fileCreatedDate) {
        this.fileCreatedDate = fileCreatedDate;
    }

    public LocalDate getSalesSummaryDate() {
        return salesSummaryDate;
    }

    public void setSalesSummaryDate(LocalDate salesSummaryDate) {
        this.salesSummaryDate = salesSummaryDate;
    }

    public String getParentMerchantId() {
        return parentMerchantId;
    }

    public void setParentMerchantId(String parentMerchantId) {
        this.parentMerchantId = parentMerchantId;
    }

    public String getParentMerchantName() {
        return parentMerchantName;
    }

    public void setParentMerchantName(String parentMerchantName) {
        this.parentMerchantName = parentMerchantName;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public LocalDate getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(LocalDate transferDate) {
        this.transferDate = transferDate;
    }

    public int getTotalSalesCount() {
        return totalSalesCount;
    }

    public void setTotalSalesCount(int totalSalesCount) {
        this.totalSalesCount = totalSalesCount;
    }

    public int getTotalSalesAmount() {
        return totalSalesAmount;
    }

    public void setTotalSalesAmount(int totalSalesAmount) {
        this.totalSalesAmount = totalSalesAmount;
    }

    public int getTotalFeeAmount1() {
        return totalFeeAmount1;
    }

    public void setTotalFeeAmount1(int totalFeeAmount1) {
        this.totalFeeAmount1 = totalFeeAmount1;
    }

    public int getTotalPaymentAmount1() {
        return totalPaymentAmount1;
    }

    public void setTotalPaymentAmount1(int totalPaymentAmount1) {
        this.totalPaymentAmount1 = totalPaymentAmount1;
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
