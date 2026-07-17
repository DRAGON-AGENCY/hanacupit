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

    /**
     * stera terminal精算情報照会(JCB)画面用。取引コード単位に合算する
     * {@link #sumByTradeCodeCardNameAndPaymentMethod}と異なり、店舗（支店コード・店舗名）
     * 単位の明細をそのまま表示するための集計。1取引コードに複数の支店（store_number）が
     * 存在する運用があり、支店ごとに店舗名も異なるため、m_stera_store（取引コード単位＝
     * 口座単位のマスタ）ではなくこのテーブル自身が持つstore_number・store_nameを使う。
     * card_name（お取扱カード名）・payment_method（お支払方法）に加え、payment_type
     * （支払区分＝1回払い/2回払い/リボ払い/分割払い）も別次元として保持する
     * （課題表項番28参照：payment_methodとpayment_typeは全く別の列であり、
     * このテーブルはこの2つを混同しない）。
     */
    @Query("SELECT d.tradeCode AS tradeCode, d.storeNumber AS storeNumber, d.storeName AS storeName, "
            + "d.cardName AS cardName, d.paymentMethod AS paymentMethod, d.paymentType AS paymentType, "
            + "d.batchId AS batchId, SUM(d.salesAmount) AS totalSalesAmount "
            + "FROM SteraJcbSalesDetail d WHERE d.batchId IN :batchIds "
            + "GROUP BY d.tradeCode, d.storeNumber, d.storeName, d.cardName, d.paymentMethod, "
            + "d.paymentType, d.batchId")
    List<SteraJcbStoreGroupAggregate> sumByStoreCardNameAndPaymentMethod(
            @Param("batchIds") List<Integer> batchIds);

    interface SteraJcbStoreGroupAggregate {

        String getTradeCode();

        String getStoreNumber();

        String getStoreName();

        String getCardName();

        String getPaymentMethod();

        String getPaymentType();

        Integer getBatchId();

        Long getTotalSalesAmount();

    }

}
