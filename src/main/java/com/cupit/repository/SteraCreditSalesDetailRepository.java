package com.cupit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cupit.model.SteraCreditSalesDetail;

public interface SteraCreditSalesDetailRepository extends JpaRepository<SteraCreditSalesDetail, Integer> {

    void deleteByBatchId(int batchId);

}
