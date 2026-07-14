package com.cupit.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "m_import_batch")
public class ImportBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_id")
    private int batchId;

    @Column(name = "payment_type", nullable = false)
    private String paymentType;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "imported_at", nullable = false)
    private LocalDateTime importedAt;

    @Column(name = "record_count")
    private Integer recordCount;

    @Column(name = "error_count")
    private Integer errorCount;

    @Column(name = "transfer_batch_id")
    private Integer transferBatchId;

    @Column(name = "file_hash")
    private String fileHash;

    @Column(name = "lookup_keys")
    private String lookupKeys;

    @Column(name = "update_employee")
    private String updateEmployee;

    @Column(name = "create_date", nullable = false)
    private LocalDate createDate;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    @Column(name = "cutoff_date")
    private LocalDate cutoffDate;

    public int getBatchId() {
        return batchId;
    }

    public void setBatchId(int batchId) {
        this.batchId = batchId;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public LocalDateTime getImportedAt() {
        return importedAt;
    }

    public void setImportedAt(LocalDateTime importedAt) {
        this.importedAt = importedAt;
    }

    public Integer getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Integer recordCount) {
        this.recordCount = recordCount;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }

    public Integer getTransferBatchId() {
        return transferBatchId;
    }

    public void setTransferBatchId(Integer transferBatchId) {
        this.transferBatchId = transferBatchId;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getLookupKeys() {
        return lookupKeys;
    }

    public void setLookupKeys(String lookupKeys) {
        this.lookupKeys = lookupKeys;
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

    public LocalDate getCutoffDate() {
        return cutoffDate;
    }

    public void setCutoffDate(LocalDate cutoffDate) {
        this.cutoffDate = cutoffDate;
    }
}
