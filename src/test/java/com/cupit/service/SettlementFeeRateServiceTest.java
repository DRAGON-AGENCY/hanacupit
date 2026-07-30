package com.cupit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cupit.dto.SettlementFeeRateRequest;
import com.cupit.dto.SettlementFeeRateResponse;
import com.cupit.model.SettlementFeeRate;
import com.cupit.repository.SettlementFeeRateRepository;

/**
 * SettlementFeeRateService のテスト。
 * リポジトリをモック化し、決済会社・カードブランドの重複検査と
 * 手数料率の入力検査を検証する。
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class SettlementFeeRateServiceTest {

    private static final String LOGIN_USER_ID = "user001";
    private static final int EXISTING_FEE_RATE_ID = 1;

    private static final String MESSAGE_PAYMENT_COMPANY_REQUIRED =
            "決済会社を入力してください。";
    private static final String MESSAGE_CARD_BRAND_REQUIRED =
            "カードブランドを入力してください。";
    private static final String MESSAGE_CALC_MODEL_INVALID =
            "計算モデルが正しくありません。";
    private static final String MESSAGE_OUR_FEE_RATE_BASE_REQUIRED =
            "当社手数料率 (本体) を入力してください。";
    private static final String MESSAGE_OUR_FEE_RATE_BASE_FORMAT =
            "当社手数料率 (本体) は0以上10未満の数値で、小数点以下5桁までで入力してください。";
    private static final String MESSAGE_ACQUIRER_FEE_RATE_FORMAT =
            "仕入手数料率は0以上10未満の数値で、小数点以下5桁までで入力してください。";
    private static final String MESSAGE_DUPLICATED =
            "同じ決済会社・カードブランドの組み合わせが既に登録されています。";
    private static final String MESSAGE_FEE_RATE_NOT_FOUND =
            "対象の手数料率が見つかりません。";

    @Mock
    private SettlementFeeRateRepository settlementFeeRateRepository;

    private SettlementFeeRateService settlementFeeRateService;

    @BeforeEach
    void setUp() {
        settlementFeeRateService =
                new SettlementFeeRateService(settlementFeeRateRepository);
    }

    private SettlementFeeRateRequest createValidRequest(String mode) {
        SettlementFeeRateRequest request = new SettlementFeeRateRequest();
        request.setMode(mode);
        request.setFeeRateId(EXISTING_FEE_RATE_ID);
        request.setPaymentCompany("JCB");
        request.setCardBrand("【ＪＣＢカード】");
        request.setCalcModel("STRAIGHT");
        request.setAcquirerFeeRate("0.0275");
        request.setOurFeeRateBase("0.0018");
        request.setOurFeeRateTax("0.0002");
        return request;
    }

    private SettlementFeeRate createExistingFeeRate() {
        SettlementFeeRate feeRate = new SettlementFeeRate();
        feeRate.setFeeRateId(EXISTING_FEE_RATE_ID);
        feeRate.setPaymentCompany("JCB");
        feeRate.setCardBrand("【ＪＣＢカード】");
        feeRate.setCalcModel("STRAIGHT");
        feeRate.setAcquirerFeeRate(new BigDecimal("0.0275"));
        feeRate.setOurFeeRateBase(new BigDecimal("0.0018"));
        feeRate.setOurFeeRateTax(new BigDecimal("0.0002"));
        return feeRate;
    }

    @Test
    void saveFeeRateCreatesWhenInputIsValid() {
        when(settlementFeeRateRepository
                .existsByPaymentCompanyAndCardBrand("JCB", "【ＪＣＢカード】"))
                .thenReturn(false);

        SettlementFeeRateResponse response = settlementFeeRateService.saveFeeRate(
                createValidRequest("new"), LOGIN_USER_ID);

        assertThat(response.isSuccess()).isTrue();
        verify(settlementFeeRateRepository).save(any());
    }

    @Test
    void saveFeeRateRejectsDuplicatedCombinationOnCreate() {
        when(settlementFeeRateRepository
                .existsByPaymentCompanyAndCardBrand("JCB", "【ＪＣＢカード】"))
                .thenReturn(true);

        SettlementFeeRateResponse response = settlementFeeRateService.saveFeeRate(
                createValidRequest("new"), LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_DUPLICATED);
        verify(settlementFeeRateRepository, never()).save(any());
    }

    @Test
    void saveFeeRateRejectsBlankPaymentCompany() {
        SettlementFeeRateRequest request = createValidRequest("new");
        request.setPaymentCompany("  ");

        SettlementFeeRateResponse response =
                settlementFeeRateService.saveFeeRate(request, LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_PAYMENT_COMPANY_REQUIRED);
        verify(settlementFeeRateRepository, never()).save(any());
    }

    @Test
    void saveFeeRateRejectsBlankCardBrand() {
        SettlementFeeRateRequest request = createValidRequest("new");
        request.setCardBrand("");

        SettlementFeeRateResponse response =
                settlementFeeRateService.saveFeeRate(request, LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_CARD_BRAND_REQUIRED);
    }

    @Test
    void saveFeeRateRejectsInvalidCalcModel() {
        SettlementFeeRateRequest request = createValidRequest("new");
        request.setCalcModel("UNKNOWN_MODEL");

        SettlementFeeRateResponse response =
                settlementFeeRateService.saveFeeRate(request, LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_CALC_MODEL_INVALID);
    }

    @Test
    void saveFeeRateRejectsBlankOurFeeRateBase() {
        SettlementFeeRateRequest request = createValidRequest("new");
        request.setOurFeeRateBase("");

        SettlementFeeRateResponse response =
                settlementFeeRateService.saveFeeRate(request, LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_OUR_FEE_RATE_BASE_REQUIRED);
    }

    @Test
    void saveFeeRateRejectsOurFeeRateBaseOutOfRange() {
        SettlementFeeRateRequest request = createValidRequest("new");
        request.setOurFeeRateBase("12.34567");

        SettlementFeeRateResponse response =
                settlementFeeRateService.saveFeeRate(request, LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_OUR_FEE_RATE_BASE_FORMAT);
    }

    @Test
    void saveFeeRateRejectsOurFeeRateBaseWithTooManyDecimals() {
        SettlementFeeRateRequest request = createValidRequest("new");
        request.setOurFeeRateBase("0.123456");

        SettlementFeeRateResponse response =
                settlementFeeRateService.saveFeeRate(request, LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_OUR_FEE_RATE_BASE_FORMAT);
    }

    @Test
    void saveFeeRateRejectsInvalidAcquirerFeeRateWhenProvided() {
        SettlementFeeRateRequest request = createValidRequest("new");
        request.setAcquirerFeeRate("abc");

        SettlementFeeRateResponse response =
                settlementFeeRateService.saveFeeRate(request, LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_ACQUIRER_FEE_RATE_FORMAT);
    }

    @Test
    void saveFeeRateAllowsBlankAcquirerFeeRateAndTax() {
        SettlementFeeRateRequest request = createValidRequest("new");
        request.setAcquirerFeeRate("");
        request.setOurFeeRateTax("");
        when(settlementFeeRateRepository
                .existsByPaymentCompanyAndCardBrand("JCB", "【ＪＣＢカード】"))
                .thenReturn(false);

        SettlementFeeRateResponse response =
                settlementFeeRateService.saveFeeRate(request, LOGIN_USER_ID);

        assertThat(response.isSuccess()).isTrue();
        verify(settlementFeeRateRepository).save(any());
    }

    @Test
    void saveFeeRateUpdatesExistingFeeRate() {
        SettlementFeeRate feeRate = createExistingFeeRate();
        when(settlementFeeRateRepository.findById(EXISTING_FEE_RATE_ID))
                .thenReturn(Optional.of(feeRate));
        when(settlementFeeRateRepository
                .existsByPaymentCompanyAndCardBrandAndFeeRateIdNot(
                        "JCB", "【ＪＣＢカード】", EXISTING_FEE_RATE_ID))
                .thenReturn(false);

        SettlementFeeRateRequest request = createValidRequest("edit");
        request.setOurFeeRateBase("0.0025");

        SettlementFeeRateResponse response =
                settlementFeeRateService.saveFeeRate(request, LOGIN_USER_ID);

        assertThat(response.isSuccess()).isTrue();
        assertThat(feeRate.getOurFeeRateBase())
                .isEqualByComparingTo(new BigDecimal("0.0025"));
        assertThat(feeRate.getUpdateEmployee()).isEqualTo(LOGIN_USER_ID);
    }

    @Test
    void saveFeeRateRejectsDuplicatedCombinationOnUpdate() {
        SettlementFeeRate feeRate = createExistingFeeRate();
        when(settlementFeeRateRepository.findById(EXISTING_FEE_RATE_ID))
                .thenReturn(Optional.of(feeRate));
        when(settlementFeeRateRepository
                .existsByPaymentCompanyAndCardBrandAndFeeRateIdNot(
                        "JCB", "【ＪＣＢカード】", EXISTING_FEE_RATE_ID))
                .thenReturn(true);

        SettlementFeeRateResponse response = settlementFeeRateService.saveFeeRate(
                createValidRequest("edit"), LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_DUPLICATED);
        verify(settlementFeeRateRepository, never()).save(any());
    }

    @Test
    void saveFeeRateRejectsUpdateWhenFeeRateNotFound() {
        when(settlementFeeRateRepository.findById(EXISTING_FEE_RATE_ID))
                .thenReturn(Optional.empty());

        SettlementFeeRateResponse response = settlementFeeRateService.saveFeeRate(
                createValidRequest("edit"), LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_FEE_RATE_NOT_FOUND);
    }

    @Test
    void deleteFeeRateRemovesExistingFeeRate() {
        when(settlementFeeRateRepository.existsById(EXISTING_FEE_RATE_ID))
                .thenReturn(true);

        SettlementFeeRateResponse response =
                settlementFeeRateService.deleteFeeRate(EXISTING_FEE_RATE_ID);

        assertThat(response.isSuccess()).isTrue();
        verify(settlementFeeRateRepository).deleteById(EXISTING_FEE_RATE_ID);
    }

    @Test
    void deleteFeeRateRejectsWhenNotFound() {
        when(settlementFeeRateRepository.existsById(EXISTING_FEE_RATE_ID))
                .thenReturn(false);

        SettlementFeeRateResponse response =
                settlementFeeRateService.deleteFeeRate(EXISTING_FEE_RATE_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_FEE_RATE_NOT_FOUND);
        verify(settlementFeeRateRepository, never()).deleteById(any());
    }
}
