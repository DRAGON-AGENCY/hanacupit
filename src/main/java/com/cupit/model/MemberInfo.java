package com.cupit.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 会員情報を表すエンティティ。
 * member_info テーブルの 1 行に対応する。
 */
@Entity
@Table(name = "m_member_info")
public class MemberInfo {

    @Id
    @Column(name = "trade_code")
    private String tradeCode;

    @Column(name = "store_name")
    private String storeName;

    @Column(name = "store_name_kana")
    private String storeNameKana;

    @Column(name = "member_type")
    private String memberType;

    @Column(name = "parent_store_code")
    private String parentStoreCode;

    @Column(name = "parent_store_name")
    private String parentStoreName;

    @Column(name = "new_trade_code")
    private String newTradeCode;

    @Column(name = "prev_trade_code")
    private String prevTradeCode;

    @Column(name = "middle_code")
    private String middleCode;

    @Column(name = "block_code")
    private String blockCode;

    @Column(name = "join_date")
    private String joinDate;

    @Column(name = "corporation_flag")
    private String corporationFlag;

    @Column(name = "cooperative_flag")
    private String cooperativeFlag;

    @Column(name = "representative_name")
    private String representativeName;

    @Column(name = "representative_kana")
    private String representativeKana;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "address")
    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "create_date", nullable = false)
    private LocalDate createDate;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    @Column(name = "update_employee")
    private String updateEmployee;

    public String getTradeCode() {
        return tradeCode;
    }

    public void setTradeCode(String tradeCode) {
        this.tradeCode = tradeCode;
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

    public String getMemberType() {
        return memberType;
    }

    public void setMemberType(String memberType) {
        this.memberType = memberType;
    }

    public String getParentStoreCode() {
        return parentStoreCode;
    }

    public void setParentStoreCode(String parentStoreCode) {
        this.parentStoreCode = parentStoreCode;
    }

    public String getParentStoreName() {
        return parentStoreName;
    }

    public void setParentStoreName(String parentStoreName) {
        this.parentStoreName = parentStoreName;
    }

    public String getNewTradeCode() {
        return newTradeCode;
    }

    public void setNewTradeCode(String newTradeCode) {
        this.newTradeCode = newTradeCode;
    }

    public String getPrevTradeCode() {
        return prevTradeCode;
    }

    public void setPrevTradeCode(String prevTradeCode) {
        this.prevTradeCode = prevTradeCode;
    }

    public String getMiddleCode() {
        return middleCode;
    }

    public void setMiddleCode(String middleCode) {
        this.middleCode = middleCode;
    }

    public String getBlockCode() {
        return blockCode;
    }

    public void setBlockCode(String blockCode) {
        this.blockCode = blockCode;
    }

    public String getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(String joinDate) {
        this.joinDate = joinDate;
    }

    public String getCorporationFlag() {
        return corporationFlag;
    }

    public void setCorporationFlag(String corporationFlag) {
        this.corporationFlag = corporationFlag;
    }

    public String getCooperativeFlag() {
        return cooperativeFlag;
    }

    public void setCooperativeFlag(String cooperativeFlag) {
        this.cooperativeFlag = cooperativeFlag;
    }

    public String getRepresentativeName() {
        return representativeName;
    }

    public void setRepresentativeName(String representativeName) {
        this.representativeName = representativeName;
    }

    public String getRepresentativeKana() {
        return representativeKana;
    }

    public void setRepresentativeKana(String representativeKana) {
        this.representativeKana = representativeKana;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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

    public String getUpdateEmployee() {
        return updateEmployee;
    }

    public void setUpdateEmployee(String updateEmployee) {
        this.updateEmployee = updateEmployee;
    }

    @Override
    public String toString() {
        return "MemberInfo{"
                + "tradeCode=" + tradeCode
                + ", storeName=" + storeName
                + ", storeNameKana=" + storeNameKana
                + ", memberType=" + memberType
                + ", parentStoreCode=" + parentStoreCode
                + ", parentStoreName=" + parentStoreName
                + ", newTradeCode=" + newTradeCode
                + ", prevTradeCode=" + prevTradeCode
                + ", middleCode=" + middleCode
                + ", blockCode=" + blockCode
                + ", joinDate=" + joinDate
                + ", corporationFlag=" + corporationFlag
                + ", cooperativeFlag=" + cooperativeFlag
                + ", representativeName=" + representativeName
                + ", representativeKana=" + representativeKana
                + ", postalCode=" + postalCode
                + ", address=" + address
                + ", phoneNumber=" + phoneNumber
                + "}";
    }
}
