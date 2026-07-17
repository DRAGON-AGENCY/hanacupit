package com.cupit.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cupit.model.TerminalData;

/**
 * 端末データ（SMCC加盟店申込書の m_member_info 不可項目）の永続化を担うリポジトリ。
 * 1取引コードに複数行を許容するため、主キーは合成キー (record_no) とする。
 */
@Repository
public interface TerminalDataRepository extends JpaRepository<TerminalData, Long> {

    List<TerminalData> findAllByOrderByTradeCodeAsc();

    void deleteByTradeCodeIn(Collection<String> tradeCodes);
}
