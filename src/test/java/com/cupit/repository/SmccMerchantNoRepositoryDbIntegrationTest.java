package com.cupit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.cupit.model.SmccMerchantNo;

/**
 * {@link SmccMerchantNoRepository#findByTradeCodeOrderByRecordNoAsc(String)} が、
 * 店舗・端末・SMCC加盟店番号情報照会画面（SMCC加盟店番号情報タブ）向けに、実際に登録した
 * 複数件をrecord_no昇順で読み出せることを、モック化しない実リポジトリ・実DB接続で検証する。
 * {@code @DataJpaTest}は各テストメソッドをトランザクションで囲み終了後に自動ロールバック
 * するため、開発DBへ永続的なデータは残らない。
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SmccMerchantNoRepositoryDbIntegrationTest {

    private static final String TEST_TRADE_CODE = "99-996";

    @Autowired
    private SmccMerchantNoRepository smccMerchantNoRepository;

    @Test
    void findByTradeCodeOrderByRecordNoAscReturnsRowsInRecordNoOrder() {
        SmccMerchantNo firstSaved = newMerchantNo("DBTEST01");
        smccMerchantNoRepository.saveAndFlush(firstSaved);
        SmccMerchantNo secondSaved = newMerchantNo("DBTEST02");
        smccMerchantNoRepository.saveAndFlush(secondSaved);

        List<SmccMerchantNo> reloaded =
                smccMerchantNoRepository.findByTradeCodeOrderByRecordNoAsc(TEST_TRADE_CODE);

        assertThat(reloaded).hasSize(2);
        assertThat(reloaded.get(0).getRecordNo()).isEqualTo(firstSaved.getRecordNo());
        assertThat(reloaded.get(1).getRecordNo()).isEqualTo(secondSaved.getRecordNo());
        assertThat(reloaded.get(0).getRecordNo()).isLessThan(reloaded.get(1).getRecordNo());
    }

    private SmccMerchantNo newMerchantNo(String merchantNo) {
        SmccMerchantNo merchant = new SmccMerchantNo();
        merchant.setTradeCode(TEST_TRADE_CODE);
        merchant.setMerchantNo(merchantNo);
        merchant.setType("クレジット");
        merchant.setBranchCode("01-001000");
        OffsetDateTime now = OffsetDateTime.now();
        merchant.setCreatedAt(now);
        merchant.setUpdatedAt(now);
        merchant.setUpdatedUserId("dbtest");
        return merchant;
    }

}
