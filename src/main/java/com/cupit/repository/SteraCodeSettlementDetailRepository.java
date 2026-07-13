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

}
