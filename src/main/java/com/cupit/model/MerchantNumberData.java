package com.cupit.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * MerchantNumberDataを表すエンティティ。
 * m_merchant_number_data テーブルの 1 行に対応する。
 */
@Entity
@Table(name = "m_merchant_number_data")
public class MerchantNumberData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_no")
    private long recordNo;

    @Column(name = "trade_code", nullable = false, length = 10)
    private String tradeCode;

    @Column(name = "terminal_count")
    private Integer terminalCount;

    @Column(name = "line_type")
    private String lineType;

    @Column(name = "store_contact_name")
    private String storeContactName;

    @Column(name = "store_contact_tel")
    private String storeContactTel;

    @Column(name = "pos_connection_flag")
    private String posConnectionFlag;

    @Column(name = "pos_maker_name")
    private String posMakerName;

    @Column(name = "pos_vendor_contact_name")
    private String posVendorContactName;

    @Column(name = "pos_vendor_contact_tel")
    private String posVendorContactTel;

    @Column(name = "d_point_enabled_flag")
    private String dPointEnabledFlag;

    @Column(name = "d_point_merchant_code")
    private String dPointMerchantCode;

    @Column(name = "d_point_store_code")
    private String dPointStoreCode;

    @Column(name = "d_point_branch_code")
    private String dPointBranchCode;

    @Column(name = "visa_master_merchant_number")
    private String visaMasterMerchantNumber;

    @Column(name = "nanaco_merchant_number")
    private String nanacoMerchantNumber;

    @Column(name = "id_merchant_number")
    private String idMerchantNumber;

    @Column(name = "transit_merchant_number")
    private String transitMerchantNumber;

    @Column(name = "unionpay_merchant_number")
    private String unionpayMerchantNumber;

    @Column(name = "waon_merchant_number")
    private String waonMerchantNumber;

    @Column(name = "edy_merchant_number")
    private String edyMerchantNumber;

    @Column(name = "nfc_merchant_number")
    private String nfcMerchantNumber;

    @Column(name = "transit_operator")
    private String transitOperator;

    @Column(name = "edy_id")
    private String edyId;

    @Column(name = "stera_terminal_number")
    private String steraTerminalNumber;

    @Column(name = "jcb_connection_flag")
    private String jcbConnectionFlag;

    @Column(name = "smart_code_connection_flag")
    private String smartCodeConnectionFlag;

    @Column(name = "registered_date", nullable = false)
    private LocalDate registeredDate;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    @Column(name = "updated_by")
    private String updatedBy;

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

    public Integer getTerminalCount() {
        return terminalCount;
    }

    public void setTerminalCount(Integer terminalCount) {
        this.terminalCount = terminalCount;
    }

    public String getLineType() {
        return lineType;
    }

    public void setLineType(String lineType) {
        this.lineType = lineType;
    }

    public String getStoreContactName() {
        return storeContactName;
    }

    public void setStoreContactName(String storeContactName) {
        this.storeContactName = storeContactName;
    }

    public String getStoreContactTel() {
        return storeContactTel;
    }

    public void setStoreContactTel(String storeContactTel) {
        this.storeContactTel = storeContactTel;
    }

    public String getPosConnectionFlag() {
        return posConnectionFlag;
    }

    public void setPosConnectionFlag(String posConnectionFlag) {
        this.posConnectionFlag = posConnectionFlag;
    }

    public String getPosMakerName() {
        return posMakerName;
    }

    public void setPosMakerName(String posMakerName) {
        this.posMakerName = posMakerName;
    }

    public String getPosVendorContactName() {
        return posVendorContactName;
    }

    public void setPosVendorContactName(String posVendorContactName) {
        this.posVendorContactName = posVendorContactName;
    }

    public String getPosVendorContactTel() {
        return posVendorContactTel;
    }

    public void setPosVendorContactTel(String posVendorContactTel) {
        this.posVendorContactTel = posVendorContactTel;
    }

    public String getDPointEnabledFlag() {
        return dPointEnabledFlag;
    }

    public void setDPointEnabledFlag(String dPointEnabledFlag) {
        this.dPointEnabledFlag = dPointEnabledFlag;
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

    public String getVisaMasterMerchantNumber() {
        return visaMasterMerchantNumber;
    }

    public void setVisaMasterMerchantNumber(String visaMasterMerchantNumber) {
        this.visaMasterMerchantNumber = visaMasterMerchantNumber;
    }

    public String getNanacoMerchantNumber() {
        return nanacoMerchantNumber;
    }

    public void setNanacoMerchantNumber(String nanacoMerchantNumber) {
        this.nanacoMerchantNumber = nanacoMerchantNumber;
    }

    public String getIdMerchantNumber() {
        return idMerchantNumber;
    }

    public void setIdMerchantNumber(String idMerchantNumber) {
        this.idMerchantNumber = idMerchantNumber;
    }

    public String getTransitMerchantNumber() {
        return transitMerchantNumber;
    }

    public void setTransitMerchantNumber(String transitMerchantNumber) {
        this.transitMerchantNumber = transitMerchantNumber;
    }

    public String getUnionpayMerchantNumber() {
        return unionpayMerchantNumber;
    }

    public void setUnionpayMerchantNumber(String unionpayMerchantNumber) {
        this.unionpayMerchantNumber = unionpayMerchantNumber;
    }

    public String getWaonMerchantNumber() {
        return waonMerchantNumber;
    }

    public void setWaonMerchantNumber(String waonMerchantNumber) {
        this.waonMerchantNumber = waonMerchantNumber;
    }

    public String getEdyMerchantNumber() {
        return edyMerchantNumber;
    }

    public void setEdyMerchantNumber(String edyMerchantNumber) {
        this.edyMerchantNumber = edyMerchantNumber;
    }

    public String getNfcMerchantNumber() {
        return nfcMerchantNumber;
    }

    public void setNfcMerchantNumber(String nfcMerchantNumber) {
        this.nfcMerchantNumber = nfcMerchantNumber;
    }

    public String getTransitOperator() {
        return transitOperator;
    }

    public void setTransitOperator(String transitOperator) {
        this.transitOperator = transitOperator;
    }

    public String getEdyId() {
        return edyId;
    }

    public void setEdyId(String edyId) {
        this.edyId = edyId;
    }

    public String getSteraTerminalNumber() {
        return steraTerminalNumber;
    }

    public void setSteraTerminalNumber(String steraTerminalNumber) {
        this.steraTerminalNumber = steraTerminalNumber;
    }

    public String getJcbConnectionFlag() {
        return jcbConnectionFlag;
    }

    public void setJcbConnectionFlag(String jcbConnectionFlag) {
        this.jcbConnectionFlag = jcbConnectionFlag;
    }

    public String getSmartCodeConnectionFlag() {
        return smartCodeConnectionFlag;
    }

    public void setSmartCodeConnectionFlag(String smartCodeConnectionFlag) {
        this.smartCodeConnectionFlag = smartCodeConnectionFlag;
    }

    public LocalDate getRegisteredDate() {
        return registeredDate;
    }

    public void setRegisteredDate(LocalDate registeredDate) {
        this.registeredDate = registeredDate;
    }

    public LocalDate getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDate updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

}
