package com.cupit.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "m_transfer_fee_rate")
public class TransferFeeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transfer_fee_id")
    private int transferFeeId;

    @Column(name = "bank_code", nullable = false)
    private String bankCode;

    @Column(name = "transfer_fee", nullable = false)
    private int transferFee;

    @Column(name = "note")
    private String note;

    @Column(name = "update_employee")
    private String updateEmployee;

    @Column(name = "create_date", nullable = false)
    private LocalDate createDate;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    public int getTransferFeeId() {
        return transferFeeId;
    }

    public void setTransferFeeId(int transferFeeId) {
        this.transferFeeId = transferFeeId;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public int getTransferFee() {
        return transferFee;
    }

    public void setTransferFee(int transferFee) {
        this.transferFee = transferFee;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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

    @Override
    public String toString() {
        return "TransferFeeRate{"
                + "transferFeeId=" + transferFeeId
                + ", bankCode=" + bankCode
                + ", transferFee=" + transferFee
                + "}";
    }
}
