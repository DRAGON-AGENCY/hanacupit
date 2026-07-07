package com.cupit.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cupit.model.SettlementFeeRate;

public interface SettlementFeeRateRepository
        extends JpaRepository<SettlementFeeRate, Integer> {

    Optional<SettlementFeeRate> findByPaymentCompanyAndCardBrand(
            String paymentCompany, String cardBrand);

}
