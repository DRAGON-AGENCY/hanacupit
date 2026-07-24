package com.cupit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.cupit.model.SteraTerminal;

/**
 * {@link SteraTerminalRepository#findByTradeCodeOrderByRecordNoAsc(String)} が、
 * 店舗・端末・SMCC加盟店番号情報照会画面（Stera端末情報タブ）向けに、実際に登録した
 * 複数件をrecord_no昇順で読み出せることを、モック化しない実リポジトリ・実DB接続で検証する。
 * {@code @DataJpaTest}は各テストメソッドをトランザクションで囲み終了後に自動ロールバック
 * するため、開発DBへ永続的なデータは残らない。
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SteraTerminalRepositoryDbIntegrationTest {

    private static final String TEST_TRADE_CODE = "99-995";

    @Autowired
    private SteraTerminalRepository steraTerminalRepository;

    @Test
    void findByTradeCodeOrderByRecordNoAscReturnsRowsInRecordNoOrder() {
        SteraTerminal firstSaved = newTerminal("DBTEST0000001");
        steraTerminalRepository.saveAndFlush(firstSaved);
        SteraTerminal secondSaved = newTerminal("DBTEST0000002");
        steraTerminalRepository.saveAndFlush(secondSaved);

        List<SteraTerminal> reloaded =
                steraTerminalRepository.findByTradeCodeOrderByRecordNoAsc(TEST_TRADE_CODE);

        assertThat(reloaded).hasSize(2);
        assertThat(reloaded.get(0).getRecordNo()).isEqualTo(firstSaved.getRecordNo());
        assertThat(reloaded.get(1).getRecordNo()).isEqualTo(secondSaved.getRecordNo());
        assertThat(reloaded.get(0).getRecordNo()).isLessThan(reloaded.get(1).getRecordNo());
    }

    private SteraTerminal newTerminal(String terminalId) {
        SteraTerminal terminal = new SteraTerminal();
        terminal.setTradeCode(TEST_TRADE_CODE);
        terminal.setTerminalId(terminalId);
        terminal.setBranchCode("01-001000");
        terminal.setTerminalStatus("利用中");
        terminal.setTerminalStartDate(LocalDate.of(2020, 1, 1));
        OffsetDateTime now = OffsetDateTime.now();
        terminal.setCreatedAt(now);
        terminal.setUpdatedAt(now);
        terminal.setUpdatedUserId("dbtest");
        return terminal;
    }

}
