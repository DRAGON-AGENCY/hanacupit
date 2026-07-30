package com.cupit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cupit.dto.SettlementItemCodeRequest;
import com.cupit.dto.SettlementItemCodeResponse;
import com.cupit.model.SettlementItemCode;
import com.cupit.repository.SettlementItemCodeRepository;

/**
 * SettlementItemCodeService のテスト。
 * リポジトリをモック化し、決済会社・カードブランド・金額種別の重複検査、
 * 項目コード自体の重複検査、入力検査を検証する。
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class SettlementItemCodeServiceTest {

    private static final String LOGIN_USER_ID = "user001";
    private static final int EXISTING_ITEM_CODE_ID = 1;

    private static final String MESSAGE_PAYMENT_COMPANY_REQUIRED =
            "決済会社を入力してください。";
    private static final String MESSAGE_CARD_BRAND_REQUIRED =
            "カードブランドを入力してください。";
    private static final String MESSAGE_AMOUNT_TYPE_INVALID =
            "金額種別が正しくありません。";
    private static final String MESSAGE_ITEM_CODE_REQUIRED =
            "項目コードを入力してください。";
    private static final String MESSAGE_ITEM_CODE_FORMAT =
            "項目コードは半角数字10桁以内で入力してください。";
    private static final String MESSAGE_DUPLICATED_COMBINATION =
            "同じ決済会社・カードブランド・金額種別の組み合わせが既に登録されています。";
    private static final String MESSAGE_DUPLICATED_ITEM_CODE =
            "同じ項目コードが既に別の行で使用されています。";
    private static final String MESSAGE_ITEM_CODE_NOT_FOUND =
            "対象の項目コードが見つかりません。";

    @Mock
    private SettlementItemCodeRepository settlementItemCodeRepository;

    private SettlementItemCodeService settlementItemCodeService;

    @BeforeEach
    void setUp() {
        settlementItemCodeService =
                new SettlementItemCodeService(settlementItemCodeRepository);
    }

    private SettlementItemCodeRequest createValidRequest(String mode) {
        SettlementItemCodeRequest request = new SettlementItemCodeRequest();
        request.setMode(mode);
        request.setItemCodeId(EXISTING_ITEM_CODE_ID);
        request.setPaymentCompany("JCB");
        request.setCardBrand("【ＪＣＢカード】");
        request.setAmountType("PAYMENT");
        request.setItemCode("3300024");
        return request;
    }

    private SettlementItemCode createExistingItemCode() {
        SettlementItemCode itemCode = new SettlementItemCode();
        itemCode.setItemCodeId(EXISTING_ITEM_CODE_ID);
        itemCode.setPaymentCompany("JCB");
        itemCode.setCardBrand("【ＪＣＢカード】");
        itemCode.setAmountType("PAYMENT");
        itemCode.setItemCode("3300024");
        return itemCode;
    }

    @Test
    void saveItemCodeCreatesWhenInputIsValid() {
        when(settlementItemCodeRepository
                .existsByPaymentCompanyAndCardBrandAndAmountType(
                        "JCB", "【ＪＣＢカード】", "PAYMENT"))
                .thenReturn(false);
        when(settlementItemCodeRepository.existsByItemCode("3300024"))
                .thenReturn(false);

        SettlementItemCodeResponse response = settlementItemCodeService.saveItemCode(
                createValidRequest("new"), LOGIN_USER_ID);

        assertThat(response.isSuccess()).isTrue();
        verify(settlementItemCodeRepository).save(any());
    }

    @Test
    void saveItemCodeRejectsDuplicatedCombinationOnCreate() {
        when(settlementItemCodeRepository
                .existsByPaymentCompanyAndCardBrandAndAmountType(
                        "JCB", "【ＪＣＢカード】", "PAYMENT"))
                .thenReturn(true);

        SettlementItemCodeResponse response = settlementItemCodeService.saveItemCode(
                createValidRequest("new"), LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_DUPLICATED_COMBINATION);
        verify(settlementItemCodeRepository, never()).save(any());
    }

    @Test
    void saveItemCodeRejectsDuplicatedItemCodeOnCreate() {
        when(settlementItemCodeRepository
                .existsByPaymentCompanyAndCardBrandAndAmountType(
                        "JCB", "【ＪＣＢカード】", "PAYMENT"))
                .thenReturn(false);
        when(settlementItemCodeRepository.existsByItemCode("3300024"))
                .thenReturn(true);

        SettlementItemCodeResponse response = settlementItemCodeService.saveItemCode(
                createValidRequest("new"), LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_DUPLICATED_ITEM_CODE);
        verify(settlementItemCodeRepository, never()).save(any());
    }

    @Test
    void saveItemCodeRejectsBlankPaymentCompany() {
        SettlementItemCodeRequest request = createValidRequest("new");
        request.setPaymentCompany("  ");

        SettlementItemCodeResponse response =
                settlementItemCodeService.saveItemCode(request, LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_PAYMENT_COMPANY_REQUIRED);
        verify(settlementItemCodeRepository, never()).save(any());
    }

    @Test
    void saveItemCodeRejectsBlankCardBrand() {
        SettlementItemCodeRequest request = createValidRequest("new");
        request.setCardBrand("");

        SettlementItemCodeResponse response =
                settlementItemCodeService.saveItemCode(request, LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_CARD_BRAND_REQUIRED);
    }

    @Test
    void saveItemCodeRejectsInvalidAmountType() {
        SettlementItemCodeRequest request = createValidRequest("new");
        request.setAmountType("UNKNOWN_TYPE");

        SettlementItemCodeResponse response =
                settlementItemCodeService.saveItemCode(request, LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_AMOUNT_TYPE_INVALID);
    }

    @Test
    void saveItemCodeRejectsBlankItemCode() {
        SettlementItemCodeRequest request = createValidRequest("new");
        request.setItemCode("");

        SettlementItemCodeResponse response =
                settlementItemCodeService.saveItemCode(request, LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_ITEM_CODE_REQUIRED);
    }

    @Test
    void saveItemCodeRejectsItemCodeWithNonDigitCharacters() {
        SettlementItemCodeRequest request = createValidRequest("new");
        request.setItemCode("330A024");

        SettlementItemCodeResponse response =
                settlementItemCodeService.saveItemCode(request, LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_ITEM_CODE_FORMAT);
    }

    @Test
    void saveItemCodeRejectsItemCodeLongerThanTenDigits() {
        SettlementItemCodeRequest request = createValidRequest("new");
        request.setItemCode("12345678901");

        SettlementItemCodeResponse response =
                settlementItemCodeService.saveItemCode(request, LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_ITEM_CODE_FORMAT);
    }

    @Test
    void saveItemCodeUpdatesExistingItemCode() {
        SettlementItemCode itemCode = createExistingItemCode();
        when(settlementItemCodeRepository.findById(EXISTING_ITEM_CODE_ID))
                .thenReturn(Optional.of(itemCode));
        when(settlementItemCodeRepository
                .existsByPaymentCompanyAndCardBrandAndAmountTypeAndItemCodeIdNot(
                        "JCB", "【ＪＣＢカード】", "PAYMENT", EXISTING_ITEM_CODE_ID))
                .thenReturn(false);
        when(settlementItemCodeRepository
                .existsByItemCodeAndItemCodeIdNot("3300099", EXISTING_ITEM_CODE_ID))
                .thenReturn(false);

        SettlementItemCodeRequest request = createValidRequest("edit");
        request.setItemCode("3300099");

        SettlementItemCodeResponse response =
                settlementItemCodeService.saveItemCode(request, LOGIN_USER_ID);

        assertThat(response.isSuccess()).isTrue();
        assertThat(itemCode.getItemCode()).isEqualTo("3300099");
        assertThat(itemCode.getUpdateEmployee()).isEqualTo(LOGIN_USER_ID);
    }

    @Test
    void saveItemCodeRejectsDuplicatedCombinationOnUpdate() {
        SettlementItemCode itemCode = createExistingItemCode();
        when(settlementItemCodeRepository.findById(EXISTING_ITEM_CODE_ID))
                .thenReturn(Optional.of(itemCode));
        when(settlementItemCodeRepository
                .existsByPaymentCompanyAndCardBrandAndAmountTypeAndItemCodeIdNot(
                        "JCB", "【ＪＣＢカード】", "PAYMENT", EXISTING_ITEM_CODE_ID))
                .thenReturn(true);

        SettlementItemCodeResponse response = settlementItemCodeService.saveItemCode(
                createValidRequest("edit"), LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_DUPLICATED_COMBINATION);
        verify(settlementItemCodeRepository, never()).save(any());
    }

    @Test
    void saveItemCodeRejectsUpdateWhenItemCodeNotFound() {
        when(settlementItemCodeRepository.findById(EXISTING_ITEM_CODE_ID))
                .thenReturn(Optional.empty());

        SettlementItemCodeResponse response = settlementItemCodeService.saveItemCode(
                createValidRequest("edit"), LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_ITEM_CODE_NOT_FOUND);
    }

    @Test
    void deleteItemCodeRemovesExistingItemCode() {
        when(settlementItemCodeRepository.existsById(EXISTING_ITEM_CODE_ID))
                .thenReturn(true);

        SettlementItemCodeResponse response =
                settlementItemCodeService.deleteItemCode(EXISTING_ITEM_CODE_ID);

        assertThat(response.isSuccess()).isTrue();
        verify(settlementItemCodeRepository).deleteById(EXISTING_ITEM_CODE_ID);
    }

    @Test
    void deleteItemCodeRejectsWhenNotFound() {
        when(settlementItemCodeRepository.existsById(EXISTING_ITEM_CODE_ID))
                .thenReturn(false);

        SettlementItemCodeResponse response =
                settlementItemCodeService.deleteItemCode(EXISTING_ITEM_CODE_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_ITEM_CODE_NOT_FOUND);
        verify(settlementItemCodeRepository, never()).deleteById(any());
    }
}
