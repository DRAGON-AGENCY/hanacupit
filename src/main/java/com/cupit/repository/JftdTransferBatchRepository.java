package com.cupit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cupit.model.JftdTransferBatch;

public interface JftdTransferBatchRepository
        extends JpaRepository<JftdTransferBatch, Integer> {

    List<JftdTransferBatch> findAllByOrderByCreatedAtDesc();

}
