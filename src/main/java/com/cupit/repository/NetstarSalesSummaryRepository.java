package com.cupit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cupit.model.NetstarSalesSummary;

public interface NetstarSalesSummaryRepository extends JpaRepository<NetstarSalesSummary, Integer> {

    List<NetstarSalesSummary> findByBatchIdIn(List<Integer> batchIds);

}
