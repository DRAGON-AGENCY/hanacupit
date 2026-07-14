package com.cupit.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * stera店舗マスタ（m_stera_store）。振込先口座情報を保持し、その他精算データ作成
 * （stera terminal取込み）の取引コード解決後の口座突合、およびその他統合振込CSV作成の
 * 確定処理で参照する。
 */
@Entity
@Table(name = "m_stera_store")
public class SteraStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_no")
    private long recordNo;

    @Column(name = "trade_code", nullable = false)
    private String tradeCode;

    @Column(name = "transit_company", nullable = false)
    private String transitCompany;

    @Column(name = "edy_id", nullable = false)
    private String edyId;

    @Column(name = "d_point_merchant_code")
    private String dPointMerchantCode;

    @Column(name = "d_point_store_code")
    private String dPointStoreCode;

    @Column(name = "d_point_branch_code")
    private String dPointBranchCode;

    @Column(name = "branch_code", nullable = false)
    private String branchCode;

    @Column(name = "member_type")
    private String memberType;

    @Column(name = "store_name", nullable = false)
    private String storeName;

    @Column(name = "store_name_kana", nullable = false)
    private String storeNameKana;

    @Column(name = "store_name_en", nullable = false)
    private String storeNameEn;

    @Column(name = "store_zip", nullable = false)
    private String storeZip;

    @Column(name = "store_address", nullable = false)
    private String storeAddress;

    @Column(name = "store_address_kana", nullable = false)
    private String storeAddressKana;

    @Column(name = "store_tel", nullable = false)
    private String storeTel;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "latitude")
    private BigDecimal latitude;

    @Column(name = "longitude")
    private BigDecimal longitude;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "bank_code", nullable = false)
    private String bankCode;

    @Column(name = "branch_name", nullable = false)
    private String branchName;

    @Column(name = "bank_branch_code", nullable = false)
    private String bankBranchCode;

    @Column(name = "account_type", nullable = false)
    private String accountType;

    @Column(name = "account_no", nullable = false)
    private String accountNo;

    @Column(name = "account_holder_kana", nullable = false)
    private String accountHolderKana;

    @Column(name = "jcb_status", nullable = false)
    private String jcbStatus;

    @Column(name = "jcb_start_date")
    private LocalDate jcbStartDate;

    @Column(name = "d_point_status", nullable = false)
    private String dPointStatus;

    @Column(name = "d_point_start_date")
    private LocalDate dPointStartDate;

    @Column(name = "remarks")
    private String remarks;

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

    public String getTransitCompany() {
        return transitCompany;
    }

    public void setTransitCompany(String transitCompany) {
        this.transitCompany = transitCompany;
    }

    public String getEdyId() {
        return edyId;
    }

    public void setEdyId(String edyId) {
        this.edyId = edyId;
    }

    public String getDPointMerchantCode() {
        return dPointMerchantCode;
    }

    public void setDPointMerchantCode(String dPointMerchantCode) {
        this.dPointMerchantCode = dPointMerchantCode;
    }

    public String getDPointStoreCode() {
        return dPointStoreCode;
    }

    public void setDPointStoreCode(String dPointStoreCode) {
        this.dPointStoreCode = dPointStoreCode;
    }

    public String getDPointBranchCode() {
        return dPointBranchCode;
    }

    public void setDPointBranchCode(String dPointBranchCode) {
        this.dPointBranchCode = dPointBranchCode;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getMemberType() {
        return memberType;
    }

    public void setMemberType(String memberType) {
        this.memberType = memberType;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreNameKana() {
        return storeNameKana;
    }

    public void setStoreNameKana(String storeNameKana) {
        this.storeNameKana = storeNameKana;
    }

    public String getStoreNameEn() {
        return storeNameEn;
    }

    public void setStoreNameEn(String storeNameEn) {
        this.storeNameEn = storeNameEn;
    }

    public String getStoreZip() {
        return storeZip;
    }

    public void setStoreZip(String storeZip) {
        this.storeZip = storeZip;
    }

    public String getStoreAddress() {
        return storeAddress;
    }

    public void setStoreAddress(String storeAddress) {
        this.storeAddress = storeAddress;
    }

    public String getStoreAddressKana() {
        return storeAddressKana;
    }

    public void setStoreAddressKana(String storeAddressKana) {
        this.storeAddressKana = storeAddressKana;
    }

    public String getStoreTel() {
        return storeTel;
    }

    public void setStoreTel(String storeTel) {
        this.storeTel = storeTel;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBankBranchCode() {
        return bankBranchCode;
    }

    public void setBankBranchCode(String bankBranchCode) {
        this.bankBranchCode = bankBranchCode;
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

    public String getJcbStatus() {
        return jcbStatus;
    }

    public void setJcbStatus(String jcbStatus) {
        this.jcbStatus = jcbStatus;
    }

    public LocalDate getJcbStartDate() {
        return jcbStartDate;
    }

    public void setJcbStartDate(LocalDate jcbStartDate) {
        this.jcbStartDate = jcbStartDate;
    }

    public String getDPointStatus() {
        return dPointStatus;
    }

    public void setDPointStatus(String dPointStatus) {
        this.dPointStatus = dPointStatus;
    }

    public LocalDate getDPointStartDate() {
        return dPointStartDate;
    }

    public void setDPointStartDate(LocalDate dPointStartDate) {
        this.dPointStartDate = dPointStartDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
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
