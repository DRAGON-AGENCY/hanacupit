package com.cupit.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * stera code精算明細CSV内、ブランドごとの明細ブロック末尾にある小計行（突合検証用）。
 * 住信SBIファイルの区分1（{@link VisaMasterStoreHeader}）と同様の位置づけで、
 * 個別取引（{@link SteraCodeSettlementDetail}）とは別テーブルで保持する。
 */
@Entity
@Table(name = "m_stera_code_settlement_summary")
public class SteraCodeSettlementSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stera_code_summary_id")
    private int steraCodeSummaryId;

    @Column(name = "batch_id", nullable = false)
    private int batchId;

    @Column(name = "brand", nullable = false)
    private String brand;

    @Column(name = "transaction_count", nullable = false)
    private int transactionCount;

    @Column(name = "settlement_amount", nullable = false)
    private int settlementAmount;

    @Column(name = "fee_amount", nullable = false)
    private int feeAmount;

    @Column(name = "net_amount", nullable = false)
    private int netAmount;

    @Column(name = "update_employee")
    private String updateEmployee;

    @Column(name = "create_date", nullable = false)
    private LocalDate createDate;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    public int getSteraCodeSummaryId() {
        return steraCodeSummaryId;
    }

    public void setSteraCodeSummaryId(int steraCodeSummaryId) {
        this.steraCodeSummaryId = steraCodeSummaryId;
    }

    public int getBatchId() {
        return batchId;
    }

    public void setBatchId(int batchId) {
        this.batchId = batchId;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }

    public int getSettlementAmount() {
        return settlementAmount;
    }

    public void setSettlementAmount(int settlementAmount) {
        this.settlementAmount = settlementAmount;
    }

    public int getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(int feeAmount) {
        this.feeAmount = feeAmount;
    }

    public int getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(int netAmount) {
        this.netAmount = netAmount;
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
