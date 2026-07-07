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
@Table(name = "m_settlement_fee_rate")
public class SettlementFeeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fee_rate_id")
    private int feeRateId;

    @Column(name = "payment_company", nullable = false)
    private String paymentCompany;

    @Column(name = "card_brand", nullable = false)
    private String cardBrand;

    @Column(name = "calc_model", nullable = false)
    private String calcModel;

    @Column(name = "acquirer_fee_rate", precision = 6, scale = 5)
    private BigDecimal acquirerFeeRate;

    @Column(name = "our_fee_rate_base", nullable = false, precision = 6, scale = 5)
    private BigDecimal ourFeeRateBase;

    @Column(name = "our_fee_rate_tax", precision = 6, scale = 5)
    private BigDecimal ourFeeRateTax;

    @Column(name = "update_employee")
    private String updateEmployee;

    @Column(name = "create_date", nullable = false)
    private LocalDate createDate;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    public int getFeeRateId() {
        return feeRateId;
    }

    public void setFeeRateId(int feeRateId) {
        this.feeRateId = feeRateId;
    }

    public String getPaymentCompany() {
        return paymentCompany;
    }

    public void setPaymentCompany(String paymentCompany) {
        this.paymentCompany = paymentCompany;
    }

    public String getCardBrand() {
        return cardBrand;
    }

    public void setCardBrand(String cardBrand) {
        this.cardBrand = cardBrand;
    }

    public String getCalcModel() {
        return calcModel;
    }

    public void setCalcModel(String calcModel) {
        this.calcModel = calcModel;
    }

    public BigDecimal getAcquirerFeeRate() {
        return acquirerFeeRate;
    }

    public void setAcquirerFeeRate(BigDecimal acquirerFeeRate) {
        this.acquirerFeeRate = acquirerFeeRate;
    }

    public BigDecimal getOurFeeRateBase() {
        return ourFeeRateBase;
    }

    public void setOurFeeRateBase(BigDecimal ourFeeRateBase) {
        this.ourFeeRateBase = ourFeeRateBase;
    }

    public BigDecimal getOurFeeRateTax() {
        return ourFeeRateTax;
    }

    public void setOurFeeRateTax(BigDecimal ourFeeRateTax) {
        this.ourFeeRateTax = ourFeeRateTax;
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
