package com.cupit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cupit.model.TerminalMonthlyFee;

public interface TerminalMonthlyFeeRepository extends JpaRepository<TerminalMonthlyFee, Integer> {

    /**
     * 端末月額利用料は決済金額（amount_total）とは無関係で、単価(unit_price)に基づく
     * 端末レンタルの定額料金である（11_月額利用料_端末.xlsxの生データで確認済み）。
     * 単価は700円（標準）と1800円（高単価端末）の2種類が存在し、いずれも基本料700円は
     * 共通で、1800円端末のみ差額1100円が別項目（調整）として加算される。
     * そのためtrade_code・unit_price単位で端末数を集計する。
     */
    @Query("SELECT d.tradeCode AS tradeCode, d.unitPrice AS unitPrice, COUNT(d) AS terminalCount "
            + "FROM TerminalMonthlyFee d WHERE d.batchId IN :batchIds "
            + "GROUP BY d.tradeCode, d.unitPrice")
    List<TerminalFeeAggregate> sumByTradeCodeAndUnitPrice(@Param("batchIds") List<Integer> batchIds);

    interface TerminalFeeAggregate {

        String getTradeCode();

        Integer getUnitPrice();

        Long getTerminalCount();

    }

}
