package com.cupit.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * stera terminal経由のJCB売上明細。列構成は{@link JcbSalesDetail}と同一だが、
 * 取引コードはPAYGATE店舗コードマッピングではなくm_stera_terminal.jcb_merchant_noから
 * 解決する。m_import_batchは共用し、payment_type="stera JCB"で系統を分離する。
 */
@Entity
@Table(name = "m_stera_jcb_sales_detail")
public class SteraJcbSalesDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stera_jcb_sales_id")
    private int steraJcbSalesId;

    @Column(name = "trade_code", nullable = false)
    private String tradeCode;

    @Column(name = "batch_id", nullable = false)
    private int batchId;

    @Column(name = "store_name")
    private String storeName;

    @Column(name = "store_number")
    private String storeNumber;

    @Column(name = "card_company")
    private String cardCompany;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "card_name")
    private String cardName;

    @Column(name = "payment_type")
    private String paymentType;

    @Column(name = "sales_method")
    private String salesMethod;

    @Column(name = "sales_date")
    private String salesDate;

    @Column(name = "sales_count", nullable = false)
    private int salesCount;

    @Column(name = "sales_amount", nullable = false)
    private int salesAmount;

    @Column(name = "update_employee")
    private String updateEmployee;

    @Column(name = "create_date", nullable = false)
    private LocalDate createDate;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    public int getSteraJcbSalesId() {
        return steraJcbSalesId;
    }

    public void setSteraJcbSalesId(int steraJcbSalesId) {
        this.steraJcbSalesId = steraJcbSalesId;
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

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreNumber() {
        return storeNumber;
    }

    public void setStoreNumber(String storeNumber) {
        this.storeNumber = storeNumber;
    }

    public String getCardCompany() {
        return cardCompany;
    }

    public void setCardCompany(String cardCompany) {
        this.cardCompany = cardCompany;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getSalesMethod() {
        return salesMethod;
    }

    public void setSalesMethod(String salesMethod) {
        this.salesMethod = salesMethod;
    }

    public String getSalesDate() {
        return salesDate;
    }

    public void setSalesDate(String salesDate) {
        this.salesDate = salesDate;
    }

    public int getSalesCount() {
        return salesCount;
    }

    public void setSalesCount(int salesCount) {
        this.salesCount = salesCount;
    }

    public int getSalesAmount() {
        return salesAmount;
    }

    public void setSalesAmount(int salesAmount) {
        this.salesAmount = salesAmount;
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
