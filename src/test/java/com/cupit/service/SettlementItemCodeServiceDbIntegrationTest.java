package com.cupit.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.cupit.dto.SettlementItemCodeRequest;
import com.cupit.dto.SettlementItemCodeResponse;
import com.cupit.model.SettlementItemCode;
import com.cupit.repository.SettlementItemCodeRepository;

/**
 * {@link SettlementItemCodeService} の登録・更新・削除が、実際に
 * m_settlement_item_code テーブルへ反映されることを、モック化しない実リポジトリ・
 * 実DB接続で検証する。{@link SettlementItemCodeServiceTest}はRepositoryをモック化して
 * おり「保存したJavaオブジェクトの値が正しいか」までしか保証しないため、実際の
 * INSERT/UPDATE/DELETE・カラムマッピングが正しく行われることをここで別途確認する。
 * {@code @DataJpaTest}は各テストメソッドをトランザクションで囲み終了後に自動
 * ロールバックするため、開発DBへ永続的なデータは残らない。既存の実データ
 * （JCB・ネットスターズ等）と衝突しないよう、決済会社・カードブランド・項目コードには
 * テスト専用の値を使用する。
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SettlementItemCodeServiceDbIntegrationTest {

    private static final String TEST_PAYMENT_COMPANY = "テスト決済会社";
    private static final String TEST_CARD_BRAND = "テストブランド";
    private static final String TEST_ITEM_CODE = "9990001";
    private static final String LOGIN_USER_ID = "user001";

    @Autowired
    private SettlementItemCodeRepository settlementItemCodeRepository;

    @Test
    void saveItemCodeCreatePersistsToRealDatabase() {
        SettlementItemCodeService service =
                new SettlementItemCodeService(settlementItemCodeRepository);

        SettlementItemCodeRequest request = new SettlementItemCodeRequest();
        request.setMode("new");
        request.setPaymentCompany(TEST_PAYMENT_COMPANY);
        request.setCardBrand(TEST_CARD_BRAND);
        request.setAmountType("PAYMENT");
        request.setItemCode(TEST_ITEM_CODE);

        SettlementItemCodeResponse response = service.saveItemCode(request, LOGIN_USER_ID);
        assertThat(response.isSuccess()).isTrue();

        Optional<SettlementItemCode> reloaded = settlementItemCodeRepository
                .findByPaymentCompanyAndCardBrandAndAmountType(
                        TEST_PAYMENT_COMPANY, TEST_CARD_BRAND, "PAYMENT");
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getItemCode()).isEqualTo(TEST_ITEM_CODE);
        assertThat(reloaded.get().getUpdateEmployee()).isEqualTo(LOGIN_USER_ID);
    }

    @Test
    void saveItemCodeUpdatePersistsToRealDatabase() {
        SettlementItemCode itemCode = new SettlementItemCode();
        itemCode.setPaymentCompany(TEST_PAYMENT_COMPANY);
        itemCode.setCardBrand(TEST_CARD_BRAND);
        itemCode.setAmountType("PAYMENT");
        itemCode.setItemCode(TEST_ITEM_CODE);
        itemCode.setCreateDate(java.time.LocalDate.now());
        settlementItemCodeRepository.saveAndFlush(itemCode);

        SettlementItemCodeService service =
                new SettlementItemCodeService(settlementItemCodeRepository);

        SettlementItemCodeRequest request = new SettlementItemCodeRequest();
        request.setMode("edit");
        request.setItemCodeId(itemCode.getItemCodeId());
        request.setPaymentCompany(TEST_PAYMENT_COMPANY);
        request.setCardBrand(TEST_CARD_BRAND);
        request.setAmountType("FEE_BASE");
        request.setItemCode("9990002");

        SettlementItemCodeResponse response = service.saveItemCode(request, LOGIN_USER_ID);
        assertThat(response.isSuccess()).isTrue();

        SettlementItemCode reloaded = settlementItemCodeRepository
                .findById(itemCode.getItemCodeId()).orElseThrow();
        assertThat(reloaded.getAmountType()).isEqualTo("FEE_BASE");
        assertThat(reloaded.getItemCode()).isEqualTo("9990002");
        assertThat(reloaded.getUpdateEmployee()).isEqualTo(LOGIN_USER_ID);
    }

    @Test
    void deleteItemCodeRemovesRowFromRealDatabase() {
        SettlementItemCode itemCode = new SettlementItemCode();
        itemCode.setPaymentCompany(TEST_PAYMENT_COMPANY);
        itemCode.setCardBrand(TEST_CARD_BRAND);
        itemCode.setAmountType("PAYMENT");
        itemCode.setItemCode(TEST_ITEM_CODE);
        itemCode.setCreateDate(java.time.LocalDate.now());
        settlementItemCodeRepository.saveAndFlush(itemCode);
        int itemCodeId = itemCode.getItemCodeId();

        SettlementItemCodeService service =
                new SettlementItemCodeService(settlementItemCodeRepository);

        SettlementItemCodeResponse response = service.deleteItemCode(itemCodeId);
        assertThat(response.isSuccess()).isTrue();

        assertThat(settlementItemCodeRepository.existsById(itemCodeId)).isFalse();
    }

}
