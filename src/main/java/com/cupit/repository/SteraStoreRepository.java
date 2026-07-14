package com.cupit.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cupit.model.SteraStore;

public interface SteraStoreRepository extends JpaRepository<SteraStore, Long> {

    Optional<SteraStore> findByTradeCode(String tradeCode);

}
