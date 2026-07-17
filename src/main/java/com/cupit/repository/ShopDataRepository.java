package com.cupit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cupit.model.ShopData;

/**
 * 店舗データ（JCB申込フォーマットの m_member_info 不可項目）の永続化を担うリポジトリ。
 * 主キーは取引コード (trade_code) を表す String 型とする。
 */
@Repository
public interface ShopDataRepository extends JpaRepository<ShopData, String> {

    List<ShopData> findAllByOrderByTradeCodeAsc();
}
