package com.cupit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cupit.model.SteraJcbSalesDetail;

public interface SteraJcbSalesDetailRepository extends JpaRepository<SteraJcbSalesDetail, Integer> {

    void deleteByBatchId(int batchId);

}
