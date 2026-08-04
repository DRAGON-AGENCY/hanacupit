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

import com.cupit.dto.TransferFeeRateRequest;
import com.cupit.dto.TransferFeeRateResponse;
import com.cupit.model.TransferFeeRate;
import com.cupit.repository.TransferFeeRateRepository;

/**
 * TransferFeeRateService のテスト。
 * リポジトリをモック化し、銀行コードの重複検査・入力検査・既定値行（DEFAULT）の
 * 削除／銀行コード変更禁止を検証する。
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class TransferFeeRateServiceTest {

    private static final String LOGIN_USER_ID = "user001";
    private static final int EXISTING_TRANSFER_FEE_ID = 1;

    private static final String MESSAGE_BANK_CODE_REQUIRED =
            "銀行コードを入力してください。";
    private static final String MESSAGE_BANK_CODE_FORMAT =
            "銀行コードは数字4桁で入力してください（既定値の行を除く）。";
    private static final String MESSAGE_TRANSFER_FEE_REQUIRED =
            "振込手数料を入力してください。";
    private static final String MESSAGE_TRANSFER_FEE_FORMAT =
            "振込手数料は0以上999999以下の整数で入力してください。";
    private static final String MESSAGE_DUPLICATED =
            "同じ銀行コードが既に登録されています。";
    private static final String MESSAGE_TRANSFER_FEE_NOT_FOUND =
            "対象の振込手数料が見つかりません。";
    private static final String MESSAGE_DEFAULT_BANK_CODE_NOT_DELETABLE =
            "既定の振込手数料（DEFAULT）は削除できません。";
    private static final String MESSAGE_DEFAULT_BANK_CODE_NOT_CHANGEABLE =
            "既定の振込手数料（DEFAULT）の銀行コードは変更できません。";

    @Mock
    private TransferFeeRateRepository transferFeeRateRepository;

    private TransferFeeRateService transferFeeRateService;

    @BeforeEach
    void setUp() {
        transferFeeRateService = new TransferFeeRateService(transferFeeRateRepository);
    }

    private TransferFeeRateRequest createValidRequest(String mode) {
        TransferFeeRateRequest request = new TransferFeeRateRequest();
        request.setMode(mode);
        request.setTransferFeeId(EXISTING_TRANSFER_FEE_ID);
        request.setBankCode("0310");
        request.setTransferFee("0");
        request.setNote("ＧＭＯあおぞらネット銀行");
        return request;
    }

    private TransferFeeRate createExistingTransferFeeRate(String bankCode) {
        TransferFeeRate transferFeeRate = new TransferFeeRate();
        transferFeeRate.setTransferFeeId(EXISTING_TRANSFER_FEE_ID);
        transferFeeRate.setBankCode(bankCode);
        transferFeeRate.setTransferFee(0);
        return transferFeeRate;
    }

    @Test
    void saveTransferFeeRateCreatesWhenInputIsValid() {
        when(transferFeeRateRepository.existsByBankCode("0310")).thenReturn(false);

        TransferFeeRateResponse response = transferFeeRateService.saveTransferFeeRate(
                createValidRequest("new"), LOGIN_USER_ID);

        assertThat(response.isSuccess()).isTrue();
        verify(transferFeeRateRepository).save(any());
    }

    @Test
    void saveTransferFeeRateAllowsDefaultBankCodeOnCreate() {
        TransferFeeRateRequest request = createValidRequest("new");
        request.setBankCode("DEFAULT");
        request.setTransferFee("129");
        when(transferFeeRateRepository.existsByBankCode("DEFAULT")).thenReturn(false);

        TransferFeeRateResponse response =
                transferFeeRateService.saveTransferFeeRate(request, LOGIN_USER_ID);

        assertThat(response.isSuccess()).isTrue();
        verify(transferFeeRateRepository).save(any());
    }

    @Test
    void saveTransferFeeRateRejectsDuplicatedBankCodeOnCreate() {
        when(transferFeeRateRepository.existsByBankCode("0310")).thenReturn(true);

        TransferFeeRateResponse response = transferFeeRateService.saveTransferFeeRate(
                createValidRequest("new"), LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_DUPLICATED);
        verify(transferFeeRateRepository, never()).save(any());
    }

    @Test
    void saveTransferFeeRateRejectsBlankBankCode() {
        TransferFeeRateRequest request = createValidRequest("new");
        request.setBankCode("  ");

        TransferFeeRateResponse response =
                transferFeeRateService.saveTransferFeeRate(request, LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_BANK_CODE_REQUIRED);
    }

    @Test
    void saveTransferFeeRateRejectsNonFourDigitBankCode() {
        TransferFeeRateRequest request = createValidRequest("new");
        request.setBankCode("31");

        TransferFeeRateResponse response =
                transferFeeRateService.saveTransferFeeRate(request, LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_BANK_CODE_FORMAT);
    }

    @Test
    void saveTransferFeeRateRejectsBlankTransferFee() {
        TransferFeeRateRequest request = createValidRequest("new");
        request.setTransferFee("");

        TransferFeeRateResponse response =
                transferFeeRateService.saveTransferFeeRate(request, LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_TRANSFER_FEE_REQUIRED);
    }

    @Test
    void saveTransferFeeRateRejectsNonNumericTransferFee() {
        TransferFeeRateRequest request = createValidRequest("new");
        request.setTransferFee("abc");

        TransferFeeRateResponse response =
                transferFeeRateService.saveTransferFeeRate(request, LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_TRANSFER_FEE_FORMAT);
    }

    @Test
    void saveTransferFeeRateRejectsNegativeTransferFee() {
        TransferFeeRateRequest request = createValidRequest("new");
        request.setTransferFee("-1");

        TransferFeeRateResponse response =
                transferFeeRateService.saveTransferFeeRate(request, LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_TRANSFER_FEE_FORMAT);
    }

    @Test
    void saveTransferFeeRateUpdatesExistingTransferFeeRate() {
        TransferFeeRate transferFeeRate = createExistingTransferFeeRate("0310");
        when(transferFeeRateRepository.findById(EXISTING_TRANSFER_FEE_ID))
                .thenReturn(Optional.of(transferFeeRate));
        when(transferFeeRateRepository
                .existsByBankCodeAndTransferFeeIdNot("0310", EXISTING_TRANSFER_FEE_ID))
                .thenReturn(false);

        TransferFeeRateRequest request = createValidRequest("edit");
        request.setTransferFee("100");

        TransferFeeRateResponse response =
                transferFeeRateService.saveTransferFeeRate(request, LOGIN_USER_ID);

        assertThat(response.isSuccess()).isTrue();
        assertThat(transferFeeRate.getTransferFee()).isEqualTo(100);
        assertThat(transferFeeRate.getUpdateEmployee()).isEqualTo(LOGIN_USER_ID);
    }

    @Test
    void saveTransferFeeRateRejectsDuplicatedBankCodeOnUpdate() {
        TransferFeeRate transferFeeRate = createExistingTransferFeeRate("0310");
        when(transferFeeRateRepository.findById(EXISTING_TRANSFER_FEE_ID))
                .thenReturn(Optional.of(transferFeeRate));
        when(transferFeeRateRepository
                .existsByBankCodeAndTransferFeeIdNot("0310", EXISTING_TRANSFER_FEE_ID))
                .thenReturn(true);

        TransferFeeRateResponse response = transferFeeRateService.saveTransferFeeRate(
                createValidRequest("edit"), LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_DUPLICATED);
        verify(transferFeeRateRepository, never()).save(any());
    }

    @Test
    void saveTransferFeeRateRejectsUpdateWhenTransferFeeRateNotFound() {
        when(transferFeeRateRepository.findById(EXISTING_TRANSFER_FEE_ID))
                .thenReturn(Optional.empty());

        TransferFeeRateResponse response = transferFeeRateService.saveTransferFeeRate(
                createValidRequest("edit"), LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_TRANSFER_FEE_NOT_FOUND);
    }

    @Test
    void saveTransferFeeRateRejectsChangingDefaultBankCode() {
        TransferFeeRate transferFeeRate = createExistingTransferFeeRate("DEFAULT");
        when(transferFeeRateRepository.findById(EXISTING_TRANSFER_FEE_ID))
                .thenReturn(Optional.of(transferFeeRate));

        TransferFeeRateRequest request = createValidRequest("edit");
        request.setBankCode("0310");

        TransferFeeRateResponse response =
                transferFeeRateService.saveTransferFeeRate(request, LOGIN_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_DEFAULT_BANK_CODE_NOT_CHANGEABLE);
        verify(transferFeeRateRepository, never()).save(any());
    }

    @Test
    void saveTransferFeeRateAllowsUpdatingDefaultRowFeeAmount() {
        TransferFeeRate transferFeeRate = createExistingTransferFeeRate("DEFAULT");
        when(transferFeeRateRepository.findById(EXISTING_TRANSFER_FEE_ID))
                .thenReturn(Optional.of(transferFeeRate));
        when(transferFeeRateRepository
                .existsByBankCodeAndTransferFeeIdNot("DEFAULT", EXISTING_TRANSFER_FEE_ID))
                .thenReturn(false);

        TransferFeeRateRequest request = createValidRequest("edit");
        request.setBankCode("DEFAULT");
        request.setTransferFee("150");

        TransferFeeRateResponse response =
                transferFeeRateService.saveTransferFeeRate(request, LOGIN_USER_ID);

        assertThat(response.isSuccess()).isTrue();
        assertThat(transferFeeRate.getTransferFee()).isEqualTo(150);
    }

    @Test
    void deleteTransferFeeRateRemovesExistingTransferFeeRate() {
        TransferFeeRate transferFeeRate = createExistingTransferFeeRate("0310");
        when(transferFeeRateRepository.findById(EXISTING_TRANSFER_FEE_ID))
                .thenReturn(Optional.of(transferFeeRate));

        TransferFeeRateResponse response =
                transferFeeRateService.deleteTransferFeeRate(EXISTING_TRANSFER_FEE_ID);

        assertThat(response.isSuccess()).isTrue();
        verify(transferFeeRateRepository).deleteById(EXISTING_TRANSFER_FEE_ID);
    }

    @Test
    void deleteTransferFeeRateRejectsWhenNotFound() {
        when(transferFeeRateRepository.findById(EXISTING_TRANSFER_FEE_ID))
                .thenReturn(Optional.empty());

        TransferFeeRateResponse response =
                transferFeeRateService.deleteTransferFeeRate(EXISTING_TRANSFER_FEE_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_TRANSFER_FEE_NOT_FOUND);
        verify(transferFeeRateRepository, never()).deleteById(any());
    }

    @Test
    void deleteTransferFeeRateRejectsDefaultRow() {
        TransferFeeRate transferFeeRate = createExistingTransferFeeRate("DEFAULT");
        when(transferFeeRateRepository.findById(EXISTING_TRANSFER_FEE_ID))
                .thenReturn(Optional.of(transferFeeRate));

        TransferFeeRateResponse response =
                transferFeeRateService.deleteTransferFeeRate(EXISTING_TRANSFER_FEE_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_DEFAULT_BANK_CODE_NOT_DELETABLE);
        verify(transferFeeRateRepository, never()).deleteById(any());
    }
}
