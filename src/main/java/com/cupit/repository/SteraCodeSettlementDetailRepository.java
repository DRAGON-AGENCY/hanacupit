package com.cupit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cupit.model.SteraCodeSettlementDetail;

public interface SteraCodeSettlementDetailRepository extends JpaRepository<SteraCodeSettlementDetail, Integer> {

    void deleteByBatchId(int batchId);

    /**
     * その他統合振込CSV作成の手数料計算単位（取引コード×ブランド）で決済金額を合計する。
     * stera codeはQR決済のため支払回数の概念が無く、stera JCB・steraクレジットと異なり
     * ブランドのみでGROUP BYすればよい。
     */
    @Query("SELECT d.tradeCode AS tradeCode, d.brand AS brand, "
            + "SUM(d.settlementAmount) AS totalSettlementAmount "
            + "FROM SteraCodeSettlementDetail d WHERE d.batchId IN :batchIds "
            + "GROUP BY d.tradeCode, d.brand")
    List<SteraCodeGroupAggregate> sumByTradeCodeAndBrand(@Param("batchIds") List<Integer> batchIds);

    interface SteraCodeGroupAggregate {

        String getTradeCode();

        String getBrand();

        Long getTotalSettlementAmount();

    }

    /**
     * stera terminal精算情報照会(SMCC)画面用。取引コード単位に合算する
     * {@link #sumByTradeCodeAndBrand}と異なり、terminal_id（端末識別番号）単位の明細を
     * そのまま表示するための集計。このテーブル自体はstore_name（店舗名）を持たないため、
     * 画面側でm_stera_store（取引コード単位の店舗マスタ）から別途解決する。
     */
    @Query("SELECT d.tradeCode AS tradeCode, d.terminalId AS terminalId, d.brand AS brand, "
            + "d.batchId AS batchId, SUM(d.settlementAmount) AS totalSettlementAmount "
            + "FROM SteraCodeSettlementDetail d WHERE d.batchId IN :batchIds "
            + "GROUP BY d.tradeCode, d.terminalId, d.brand, d.batchId")
    List<SteraCodeStoreGroupAggregate> sumByTerminalAndBrand(@Param("batchIds") List<Integer> batchIds);

    interface SteraCodeStoreGroupAggregate {

        String getTradeCode();

        String getTerminalId();

        String getBrand();

        Integer getBatchId();

        Long getTotalSettlementAmount();

    }

}
