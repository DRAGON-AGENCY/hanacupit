package com.cupit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cupit.model.SmccMerchantNo;

public interface SmccMerchantNoRepository extends JpaRepository<SmccMerchantNo, Long> {

    List<SmccMerchantNo> findByMerchantNo(String merchantNo);

}
