package com.cupit.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cupit.model.SmccMerchantNo;

public interface SmccMerchantNoRepository extends JpaRepository<SmccMerchantNo, Long> {

    List<SmccMerchantNo> findByMerchantNo(String merchantNo);

    List<SmccMerchantNo> findAllByOrderByTradeCodeAsc();

    List<SmccMerchantNo> findByTradeCodeOrderByRecordNoAsc(String tradeCode);

    void deleteByTradeCodeIn(Collection<String> tradeCodes);

}
