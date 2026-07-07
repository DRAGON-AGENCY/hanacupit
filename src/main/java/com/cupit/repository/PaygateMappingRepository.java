package com.cupit.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cupit.model.PaygateStoreMapping;

public interface PaygateMappingRepository
        extends JpaRepository<PaygateStoreMapping, Integer> {

    List<PaygateStoreMapping> findAllByOrderByTradeCodeAscTerminalIdAsc();

    List<PaygateStoreMapping> findByTradeCodeOrderByTerminalId(String tradeCode);

    void deleteByTradeCodeIn(Collection<String> tradeCodes);

    Optional<PaygateStoreMapping> findFirstByJcbMerchantNo(String jcbMerchantNo);

    Optional<PaygateStoreMapping> findFirstByTerminalId(String terminalId);

    Optional<PaygateStoreMapping> findFirstByNetstarStoreCode(String netstarStoreCode);

    Optional<PaygateStoreMapping> findFirstByRpayStoreCode(String rpayStoreCode);

    Optional<PaygateStoreMapping> findFirstBySbiMerchantId(String sbiMerchantId);

}
