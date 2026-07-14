package com.cupit.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "m_jftd_transfer_detail")
public class JftdTransferDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transfer_detail_id")
    private int transferDetailId;

    @Column(name = "transfer_batch_id", nullable = false)
    private int transferBatchId;

    @Column(name = "import_batch_id", nullable = false)
    private int importBatchId;

    @Column(name = "trade_code", nullable = false)
    private String tradeCode;

    @Column(name = "item_code", nullable = false)
    private String itemCode;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "gross_amount", nullable = false)
    private int grossAmount;

    @Column(name = "acquirer_fee_tax_free", nullable = false)
    private int acquirerFeeTaxFree;

    @Column(name = "acquirer_fee_base", nullable = false)
    private int acquirerFeeBase;

    @Column(name = "acquirer_fee_tax", nullable = false)
    private int acquirerFeeTax;

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

    public int getImportBatchId() {
        return importBatchId;
    }

    public void setImportBatchId(int importBatchId) {
        this.importBatchId = importBatchId;
    }

    public String getTradeCode() {
        return tradeCode;
    }

    public void setTradeCode(String tradeCode) {
        this.tradeCode = tradeCode;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(int grossAmount) {
        this.grossAmount = grossAmount;
    }

    public int getAcquirerFeeTaxFree() {
        return acquirerFeeTaxFree;
    }

    public void setAcquirerFeeTaxFree(int acquirerFeeTaxFree) {
        this.acquirerFeeTaxFree = acquirerFeeTaxFree;
    }

    public int getAcquirerFeeBase() {
        return acquirerFeeBase;
    }

    public void setAcquirerFeeBase(int acquirerFeeBase) {
        this.acquirerFeeBase = acquirerFeeBase;
    }

    public int getAcquirerFeeTax() {
        return acquirerFeeTax;
    }

    public void setAcquirerFeeTax(int acquirerFeeTax) {
        this.acquirerFeeTax = acquirerFeeTax;
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
