package com.cupit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cupit.model.SteraCodeSettlementDetail;

public interface SteraCodeSettlementDetailRepository extends JpaRepository<SteraCodeSettlementDetail, Integer> {

    void deleteByBatchId(int batchId);

}
