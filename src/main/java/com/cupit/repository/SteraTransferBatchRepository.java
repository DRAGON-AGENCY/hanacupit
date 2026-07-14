package com.cupit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cupit.model.SteraTransferBatch;

public interface SteraTransferBatchRepository
        extends JpaRepository<SteraTransferBatch, Integer> {

    List<SteraTransferBatch> findAllByOrderByCreatedAtDesc();

}
