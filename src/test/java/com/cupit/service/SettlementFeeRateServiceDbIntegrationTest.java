package com.cupit.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.cupit.dto.SettlementFeeRateRequest;
import com.cupit.dto.SettlementFeeRateResponse;
import com.cupit.model.SettlementFeeRate;
import com.cupit.repository.SettlementFeeRateRepository;

/**
 * {@link SettlementFeeRateService} の登録・更新・削除が、実際に
 * m_settlement_fee_rate テーブルへ反映されることを、モック化しない実リポジトリ・
 * 実DB接続で検証する。{@link SettlementFeeRateServiceTest}はRepositoryをモック化して
 * おり「保存したJavaオブジェクトの値が正しいか」までしか保証しないため、実際の
 * INSERT/UPDATE/DELETE・カラムマッピング（BigDecimalの小数点以下5桁の精度含む）が
 * 正しく行われることをここで別途確認する。{@code @DataJpaTest}は各テストメソッドを
 * トランザクションで囲み終了後に自動ロールバックするため、開発DBへ永続的なデータは
 * 残らない。既存の実データ（JCB・ネットスターズ等）と衝突しないよう、決済会社・
 * カードブランドにはテスト専用の値を使用する。
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SettlementFeeRateServiceDbIntegrationTest {

    private static final String TEST_PAYMENT_COMPANY = "テスト決済会社";
    private static final String TEST_CARD_BRAND = "テストブランド";
    private static final String LOGIN_USER_ID = "user001";

    @Autowired
    private SettlementFeeRateRepository settlementFeeRateRepository;

    @Test
    void saveFeeRateCreatePersistsToRealDatabase() {
        SettlementFeeRateService service =
                new SettlementFeeRateService(settlementFeeRateRepository);

        SettlementFeeRateRequest request = new SettlementFeeRateRequest();
        request.setMode("new");
        request.setPaymentCompany(TEST_PAYMENT_COMPANY);
        request.setCardBrand(TEST_CARD_BRAND);
        request.setCalcModel("STRAIGHT");
        request.setAcquirerFeeRate("0.02750");
        request.setOurFeeRateBase("0.00180");
        request.setOurFeeRateTax("0.00020");

        SettlementFeeRateResponse response = service.saveFeeRate(request, LOGIN_USER_ID);
        assertThat(response.isSuccess()).isTrue();

        Optional<SettlementFeeRate> reloaded = settlementFeeRateRepository
                .findByPaymentCompanyAndCardBrand(TEST_PAYMENT_COMPANY, TEST_CARD_BRAND);
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getCalcModel()).isEqualTo("STRAIGHT");
        assertThat(reloaded.get().getAcquirerFeeRate())
                .isEqualByComparingTo(new BigDecimal("0.02750"));
        assertThat(reloaded.get().getOurFeeRateBase())
                .isEqualByComparingTo(new BigDecimal("0.00180"));
        assertThat(reloaded.get().getOurFeeRateTax())
                .isEqualByComparingTo(new BigDecimal("0.00020"));
        assertThat(reloaded.get().getUpdateEmployee()).isEqualTo(LOGIN_USER_ID);
    }

    @Test
    void saveFeeRateUpdatePersistsToRealDatabase() {
        SettlementFeeRate feeRate = new SettlementFeeRate();
        feeRate.setPaymentCompany(TEST_PAYMENT_COMPANY);
        feeRate.setCardBrand(TEST_CARD_BRAND);
        feeRate.setCalcModel("STRAIGHT");
        feeRate.setOurFeeRateBase(new BigDecimal("0.00180"));
        feeRate.setCreateDate(java.time.LocalDate.now());
        settlementFeeRateRepository.saveAndFlush(feeRate);

        SettlementFeeRateService service =
                new SettlementFeeRateService(settlementFeeRateRepository);

        SettlementFeeRateRequest request = new SettlementFeeRateRequest();
        request.setMode("edit");
        request.setFeeRateId(feeRate.getFeeRateId());
        request.setPaymentCompany(TEST_PAYMENT_COMPANY);
        request.setCardBrand(TEST_CARD_BRAND);
        request.setCalcModel("PURCHASE_COLLECT");
        request.setOurFeeRateBase("0.00250");

        SettlementFeeRateResponse response = service.saveFeeRate(request, LOGIN_USER_ID);
        assertThat(response.isSuccess()).isTrue();

        SettlementFeeRate reloaded = settlementFeeRateRepository
                .findById(feeRate.getFeeRateId()).orElseThrow();
        assertThat(reloaded.getCalcModel()).isEqualTo("PURCHASE_COLLECT");
        assertThat(reloaded.getOurFeeRateBase())
                .isEqualByComparingTo(new BigDecimal("0.00250"));
        assertThat(reloaded.getUpdateEmployee()).isEqualTo(LOGIN_USER_ID);
    }

    @Test
    void deleteFeeRateRemovesRowFromRealDatabase() {
        SettlementFeeRate feeRate = new SettlementFeeRate();
        feeRate.setPaymentCompany(TEST_PAYMENT_COMPANY);
        feeRate.setCardBrand(TEST_CARD_BRAND);
        feeRate.setCalcModel("STRAIGHT");
        feeRate.setOurFeeRateBase(new BigDecimal("0.00180"));
        feeRate.setCreateDate(java.time.LocalDate.now());
        settlementFeeRateRepository.saveAndFlush(feeRate);
        int feeRateId = feeRate.getFeeRateId();

        SettlementFeeRateService service =
                new SettlementFeeRateService(settlementFeeRateRepository);

        SettlementFeeRateResponse response = service.deleteFeeRate(feeRateId);
        assertThat(response.isSuccess()).isTrue();

        assertThat(settlementFeeRateRepository.existsById(feeRateId)).isFalse();
    }

}
