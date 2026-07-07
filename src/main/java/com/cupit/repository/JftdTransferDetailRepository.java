package com.cupit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cupit.model.JftdTransferDetail;

public interface JftdTransferDetailRepository
        extends JpaRepository<JftdTransferDetail, Integer> {

    List<JftdTransferDetail> findByTransferBatchId(int transferBatchId);

}
