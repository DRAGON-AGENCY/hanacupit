package com.cupit.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cupit.model.SteraTerminal;

public interface SteraTerminalRepository extends JpaRepository<SteraTerminal, Long> {

    List<SteraTerminal> findByJcbMerchantNo(String jcbMerchantNo);

    List<SteraTerminal> findByTerminalId(String terminalId);

    List<SteraTerminal> findAllByOrderByTradeCodeAsc();

    List<SteraTerminal> findByTradeCodeOrderByRecordNoAsc(String tradeCode);

    void deleteByTradeCodeIn(Collection<String> tradeCodes);

}
