package com.cupit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.cupit.model.SettlementFeeRate;

/**
 * stera terminal精算情報照会(JCB/SMCC)・その他統合振込CSV作成（確定処理）が参照する
 * 手数料率マスタ（payment_company='stera terminal', card_brand='共通'）が、
 * ハードコード定数ではなく実際にDBへ登録済みであり、値が仕様どおり
 * （仕入手数料2.75%・当社手数料0.2%）であることを、モック化しない実リポジトリ・
 * 実DB接続で検証する。schema.sqlおよび07_テーブル作成sqlのシード行と一致しない場合は
 * このテストが失敗する。
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SettlementFeeRateRepositoryDbIntegrationTest {

    @Autowired
    private SettlementFeeRateRepository settlementFeeRateRepository;

    @Test
    void findByPaymentCompanyAndCardBrandReturnsSteraTerminalRateFromRealDb() {
        Optional<SettlementFeeRate> found = settlementFeeRateRepository
                .findByPaymentCompanyAndCardBrand("stera terminal", "共通");

        assertThat(found).isPresent();
        SettlementFeeRate rate = found.get();
        assertThat(rate.getAcquirerFeeRate()).isEqualByComparingTo(new BigDecimal("0.0275"));
        assertThat(rate.getOurFeeRateBase()).isEqualByComparingTo(new BigDecimal("0.002"));
    }

}
