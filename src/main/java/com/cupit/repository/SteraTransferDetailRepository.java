package com.cupit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cupit.model.SteraTransferDetail;

public interface SteraTransferDetailRepository
        extends JpaRepository<SteraTransferDetail, Integer> {

    List<SteraTransferDetail> findByTransferBatchId(int transferBatchId);

}
