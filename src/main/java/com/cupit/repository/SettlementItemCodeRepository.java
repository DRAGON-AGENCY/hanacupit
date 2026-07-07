package com.cupit.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cupit.model.SettlementItemCode;

public interface SettlementItemCodeRepository
        extends JpaRepository<SettlementItemCode, Integer> {

    Optional<SettlementItemCode> findByPaymentCompanyAndCardBrandAndAmountType(
            String paymentCompany, String cardBrand, String amountType);

    Optional<SettlementItemCode> findByItemCode(String itemCode);

}
