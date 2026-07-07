package com.cupit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cupit.model.JftdTransferBatch;

public interface JftdTransferBatchRepository
        extends JpaRepository<JftdTransferBatch, Integer> {
}
