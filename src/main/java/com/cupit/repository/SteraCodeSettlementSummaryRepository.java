package com.cupit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cupit.model.SteraCodeSettlementSummary;

public interface SteraCodeSettlementSummaryRepository extends JpaRepository<SteraCodeSettlementSummary, Integer> {

    void deleteByBatchId(int batchId);

}
