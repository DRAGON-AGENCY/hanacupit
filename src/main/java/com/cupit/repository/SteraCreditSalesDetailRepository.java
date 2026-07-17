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

    /**
     * stera terminal精算情報照会(SMCC)画面用。取引コード単位に合算する
     * {@link #sumByTradeCodeCardBrandAndTransactionType}と異なり、merchant_id
     * （利用加盟店番号）単位の明細をそのまま表示するための集計。実データ上、
     * 1取引コードに複数のmerchant_idが存在し、merchant_idごとにstore_name（屋号）も
     * 異なる（読み取り機の種類ごとに別のmerchant_idが割り当てられているため）。
     */
    @Query("SELECT d.tradeCode AS tradeCode, d.merchantId AS merchantId, d.storeName AS storeName, "
            + "d.cardBrand AS cardBrand, d.transactionType AS transactionType, d.batchId AS batchId, "
            + "SUM(d.billingAmount) AS totalBillingAmount "
            + "FROM SteraCreditSalesDetail d WHERE d.batchId IN :batchIds "
            + "GROUP BY d.tradeCode, d.merchantId, d.storeName, d.cardBrand, d.transactionType, d.batchId")
    List<SteraCreditStoreGroupAggregate> sumByMerchantCardBrandAndTransactionType(
            @Param("batchIds") List<Integer> batchIds);

    interface SteraCreditStoreGroupAggregate {

        String getTradeCode();

        String getMerchantId();

        String getStoreName();

        String getCardBrand();

        String getTransactionType();

        Integer getBatchId();

        Long getTotalBillingAmount();

    }

}
