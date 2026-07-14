package com.cupit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cupit.model.RakutenPayTransaction;

public interface RakutenPayTransactionRepository extends JpaRepository<RakutenPayTransaction, Integer> {

    @Query("SELECT d.tradeCode AS tradeCode, d.batchId AS batchId, SUM(d.totalAmount) AS totalAmount "
            + "FROM RakutenPayTransaction d WHERE d.batchId IN :batchIds "
            + "GROUP BY d.tradeCode, d.batchId")
    List<RakutenPayAggregate> sumByTradeCode(@Param("batchIds") List<Integer> batchIds);

    void deleteByBatchId(int batchId);

    interface RakutenPayAggregate {

        String getTradeCode();

        Integer getBatchId();

        Long getTotalAmount();

    }

}
