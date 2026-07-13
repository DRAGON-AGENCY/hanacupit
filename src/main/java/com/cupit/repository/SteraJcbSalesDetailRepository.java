package com.cupit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cupit.model.SteraJcbSalesDetail;

public interface SteraJcbSalesDetailRepository extends JpaRepository<SteraJcbSalesDetail, Integer> {

    void deleteByBatchId(int batchId);

    /**
     * その他統合振込CSV作成の手数料計算単位（取引コード×お取扱カード名×支払区分）で
     * 売上金額を合計する。実データ検証の結果、同じカードブランドでも支払回数
     * （1回払/2回払等）ごとに手数料を丸めてから合算しないと実データと1円ズレるため、
     * この粒度でのGROUP BYが必須。
     */
    @Query("SELECT d.tradeCode AS tradeCode, d.cardName AS cardName, d.paymentMethod AS paymentMethod, "
            + "SUM(d.salesAmount) AS totalSalesAmount "
            + "FROM SteraJcbSalesDetail d WHERE d.batchId IN :batchIds "
            + "GROUP BY d.tradeCode, d.cardName, d.paymentMethod")
    List<SteraJcbGroupAggregate> sumByTradeCodeCardNameAndPaymentMethod(
            @Param("batchIds") List<Integer> batchIds);

    interface SteraJcbGroupAggregate {

        String getTradeCode();

        String getCardName();

        String getPaymentMethod();

        Long getTotalSalesAmount();

    }

}
