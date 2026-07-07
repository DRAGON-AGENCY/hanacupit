package com.cupit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cupit.model.JcbSalesDetail;

public interface JcbSalesDetailRepository extends JpaRepository<JcbSalesDetail, Integer> {

    @Query("SELECT d.tradeCode AS tradeCode, d.cardName AS cardName, "
            + "SUM(d.salesCount) AS totalSalesCount, SUM(d.salesAmount) AS totalSalesAmount "
            + "FROM JcbSalesDetail d WHERE d.batchId IN :batchIds "
            + "GROUP BY d.tradeCode, d.cardName")
    List<JcbBrandAggregate> sumByTradeCodeAndCardName(@Param("batchIds") List<Integer> batchIds);

    interface JcbBrandAggregate {

        String getTradeCode();

        String getCardName();

        Long getTotalSalesCount();

        Long getTotalSalesAmount();

    }

}
