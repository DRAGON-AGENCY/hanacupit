package com.cupit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cupit.model.MemberInfo;

/**
 * 会員情報の永続化を担うリポジトリ。
 * 主キーは取引コード (trade_code) を表す String 型とする。
 */
@Repository
public interface MemberInfoRepository extends JpaRepository<MemberInfo, String> {

    List<MemberInfo> findAllByOrderByTradeCodeAsc();
}
