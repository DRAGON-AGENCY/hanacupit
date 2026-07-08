package com.cupit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cupit.model.VisaMasterTransaction;

public interface VisaMasterTransactionRepository extends JpaRepository<VisaMasterTransaction, Integer> {

    /**
     * 決済手数料①は明細行ごとに実データ側で計算済みの値（fee_amount_1）をそのまま合計する。
     * 売上金額×手数料率(fee_rate)を自前で再計算すると、明細行数が多い店舗（実データでは
     * 300件超）で数円〜十数円の丸め誤差が生じ実データと一致しないことを検証済みのため、
     * 必ずfee_amount_1列の値を使うこと（fee_rateは実績値の参考表示以上の意味を持たない）。
     */
    @Query("SELECT d.tradeCode AS tradeCode, SUM(d.salesAmount) AS totalSalesAmount, "
            + "SUM(d.feeAmount1) AS totalFeeAmount1 "
            + "FROM VisaMasterTransaction d WHERE d.batchId IN :batchIds "
            + "GROUP BY d.tradeCode")
    List<VisaMasterAggregate> sumByTradeCode(@Param("batchIds") List<Integer> batchIds);

    void deleteByBatchId(int batchId);

    interface VisaMasterAggregate {

        String getTradeCode();

        Long getTotalSalesAmount();

        Long getTotalFeeAmount1();

    }

}
