package com.cupit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.cupit.dto.MemberListItem;
import com.cupit.model.MemberInfo;

/**
 * 会員情報の永続化を担うリポジトリ。
 * 主キーは取引コード (trade_code) を表す String 型とする。
 */
@Repository
public interface MemberInfoRepository extends JpaRepository<MemberInfo, String> {

    List<MemberInfo> findAllByOrderByTradeCodeAsc();

    /**
     * 「加盟店一覧」画面向けに、一覧表示に必要な列だけを射影して取得する。
     * {@code m_member_info}は266列を持つため、一覧の全行についてエンティティの
     * 全列を取得することを避け、必要な8列だけをJPQLコンストラクタ式で取得する。
     */
    @Query("SELECT new com.cupit.dto.MemberListItem("
            + "m.tradeCode, m.storeNameKana, m.storeName, "
            + "m.addrCity, m.addrTown, m.addrBlock, m.addrBuilding, m.qualificationType) "
            + "FROM MemberInfo m ORDER BY m.tradeCode ASC")
    List<MemberListItem> findAllForList();
}
