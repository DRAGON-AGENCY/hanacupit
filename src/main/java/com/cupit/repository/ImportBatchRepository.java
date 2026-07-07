package com.cupit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cupit.model.ImportBatch;

public interface ImportBatchRepository extends JpaRepository<ImportBatch, Integer> {

    List<ImportBatch> findByPaymentTypeAndTransferBatchIdIsNull(String paymentType);

}
