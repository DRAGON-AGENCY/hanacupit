package com.cupit.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cupit.model.SteraStore;

public interface SteraStoreRepository extends JpaRepository<SteraStore, Long> {

    Optional<SteraStore> findByTradeCode(String tradeCode);

    List<SteraStore> findByTradeCodeIn(Collection<String> tradeCodes);

    List<SteraStore> findAllByOrderByTradeCodeAsc();

}
