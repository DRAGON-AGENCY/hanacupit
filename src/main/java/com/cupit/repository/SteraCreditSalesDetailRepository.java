package com.cupit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cupit.model.SteraCreditSalesDetail;

public interface SteraCreditSalesDetailRepository extends JpaRepository<SteraCreditSalesDetail, Integer> {

    void deleteByBatchId(int batchId);

    /**
     * その他統合振込CSV作成の手数料計算単位（取引コード×カードブランド×取扱区分）で
     * 請求金額を合計する。取扱区分（1回払/2回払等）ごとに手数料を丸めてから合算しないと
     * 実データと1円ズレるため、この粒度でのGROUP BYが必須。
     */
    @Query("SELECT d.tradeCode AS tradeCode, d.cardBrand AS cardBrand, d.transactionType AS transactionType, "
            + "SUM(d.billingAmount) AS totalBillingAmount "
            + "FROM SteraCreditSalesDetail d WHERE d.batchId IN :batchIds "
            + "GROUP BY d.tradeCode, d.cardBrand, d.transactionType")
    List<SteraCreditGroupAggregate> sumByTradeCodeCardBrandAndTransactionType(
            @Param("batchIds") List<Integer> batchIds);

    interface SteraCreditGroupAggregate {

        String getTradeCode();

        String getCardBrand();

        String getTransactionType();

        Long getTotalBillingAmount();

    }

}
