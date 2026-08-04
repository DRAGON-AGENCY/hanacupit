package com.cupit.service;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cupit.dto.TransferFeeRateRequest;
import com.cupit.dto.TransferFeeRateResponse;
import com.cupit.model.TransferFeeRate;
import com.cupit.repository.TransferFeeRateRepository;

/**
 * 振込手数料マスタ (m_transfer_fee_rate) の取得・登録・更新・削除を担うサービス。
 * その他統合振込CSV作成（{@link SteraTransferCalculationService}）が参照するマスタの
 * メンテナンスを提供する。
 */
@Service
public class TransferFeeRateService {

    /** 該当する銀行コードの行が無い場合に使う既定値の特別行。削除・改名を禁止する。 */
    public static final String DEFAULT_BANK_CODE = "DEFAULT";

    private static final String MODE_NEW = "new";

    private static final int MAX_NOTE_LENGTH = 100;

    private static final Pattern BANK_CODE_PATTERN = Pattern.compile("^[0-9]{4}$");
    private static final Pattern TRANSFER_FEE_PATTERN = Pattern.compile("^[0-9]{1,6}$");

    private static final String MESSAGE_INVALID_INPUT =
            "入力内容が正しくありません。";
    private static final String MESSAGE_BANK_CODE_REQUIRED =
            "銀行コードを入力してください。";
    private static final String MESSAGE_BANK_CODE_FORMAT =
            "銀行コードは数字4桁で入力してください（既定値の行を除く）。";
    private static final String MESSAGE_TRANSFER_FEE_REQUIRED =
            "振込手数料を入力してください。";
    private static final String MESSAGE_TRANSFER_FEE_FORMAT =
            "振込手数料は0以上999999以下の整数で入力してください。";
    private static final String MESSAGE_NOTE_LENGTH =
            "備考は100文字以内で入力してください。";
    private static final String MESSAGE_DUPLICATED =
            "同じ銀行コードが既に登録されています。";
    private static final String MESSAGE_TRANSFER_FEE_NOT_FOUND =
            "対象の振込手数料が見つかりません。";
    private static final String MESSAGE_DEFAULT_BANK_CODE_NOT_DELETABLE =
            "既定の振込手数料（DEFAULT）は削除できません。";
    private static final String MESSAGE_DEFAULT_BANK_CODE_NOT_CHANGEABLE =
            "既定の振込手数料（DEFAULT）の銀行コードは変更できません。";

    private final TransferFeeRateRepository transferFeeRateRepository;

    public TransferFeeRateService(TransferFeeRateRepository transferFeeRateRepository) {
        this.transferFeeRateRepository = transferFeeRateRepository;
    }

    /**
     * 全振込手数料を振込手数料 ID の昇順で取得する。
     *
     * @return 振込手数料の一覧
     */
    public List<TransferFeeRate> findAllTransferFeeRates() {
        return transferFeeRateRepository.findAllByOrderByTransferFeeIdAsc();
    }

    /**
     * 振込手数料 ID を指定して振込手数料を 1 件取得する。
     *
     * @param transferFeeId 振込手数料 ID
     * @return 該当する振込手数料。存在しない場合は null
     */
    public TransferFeeRate findById(int transferFeeId) {
        return transferFeeRateRepository.findById(transferFeeId).orElse(null);
    }

    /**
     * 振込手数料を登録または更新する。入力値を検査し、問題があれば失敗結果と
     * メッセージを返す。既定値の行（bank_code='DEFAULT'）の銀行コードは
     * 変更できない。
     *
     * @param request 画面から送信された振込手数料情報
     * @param loginUserId 操作中のログインユーザの user_id (更新者として記録する)
     * @return 処理結果
     */
    @Transactional
    public TransferFeeRateResponse saveTransferFeeRate(
            TransferFeeRateRequest request, String loginUserId) {
        if (request == null) {
            return new TransferFeeRateResponse(false, MESSAGE_INVALID_INPUT);
        }

        String bankCode = trimToEmpty(request.getBankCode());
        String transferFeeText = trimToEmpty(request.getTransferFee());
        String note = trimToEmpty(request.getNote());

        String validationMessage = validateInput(bankCode, transferFeeText, note);
        if (validationMessage != null) {
            return new TransferFeeRateResponse(false, validationMessage);
        }

        int transferFee = Integer.parseInt(transferFeeText);
        String noteOrNull = note.isEmpty() ? null : note;

        boolean isNewMode = MODE_NEW.equals(request.getMode());
        if (isNewMode) {
            return createTransferFeeRate(bankCode, transferFee, noteOrNull, loginUserId);
        }
        return updateTransferFeeRate(
                request.getTransferFeeId(), bankCode, transferFee, noteOrNull, loginUserId);
    }

    /**
     * 振込手数料 ID を指定して振込手数料を削除する。既定値の行（DEFAULT）は
     * 計算処理が必ず参照するため削除できない。
     *
     * @param transferFeeId 振込手数料 ID
     * @return 処理結果
     */
    @Transactional
    public TransferFeeRateResponse deleteTransferFeeRate(int transferFeeId) {
        TransferFeeRate transferFeeRate =
                transferFeeRateRepository.findById(transferFeeId).orElse(null);
        if (transferFeeRate == null) {
            return new TransferFeeRateResponse(false, MESSAGE_TRANSFER_FEE_NOT_FOUND);
        }
        if (DEFAULT_BANK_CODE.equals(transferFeeRate.getBankCode())) {
            return new TransferFeeRateResponse(false, MESSAGE_DEFAULT_BANK_CODE_NOT_DELETABLE);
        }
        transferFeeRateRepository.deleteById(transferFeeId);
        return new TransferFeeRateResponse(true, null);
    }

    /**
     * 新規振込手数料を登録する。銀行コードの重複を検査する。
     */
    private TransferFeeRateResponse createTransferFeeRate(
            String bankCode, int transferFee, String note, String loginUserId) {
        if (transferFeeRateRepository.existsByBankCode(bankCode)) {
            return new TransferFeeRateResponse(false, MESSAGE_DUPLICATED);
        }

        TransferFeeRate transferFeeRate = new TransferFeeRate();
        transferFeeRate.setBankCode(bankCode);
        transferFeeRate.setTransferFee(transferFee);
        transferFeeRate.setNote(note);
        transferFeeRate.setUpdateEmployee(loginUserId);

        LocalDate today = LocalDate.now();
        transferFeeRate.setCreateDate(today);
        transferFeeRate.setUpdatedDate(today);

        transferFeeRateRepository.save(transferFeeRate);
        return new TransferFeeRateResponse(true, null);
    }

    /**
     * 既存振込手数料を更新する。既定値の行（DEFAULT）は銀行コードの変更を禁止する。
     */
    private TransferFeeRateResponse updateTransferFeeRate(
            int transferFeeId, String bankCode, int transferFee, String note,
            String loginUserId) {
        TransferFeeRate transferFeeRate =
                transferFeeRateRepository.findById(transferFeeId).orElse(null);
        if (transferFeeRate == null) {
            return new TransferFeeRateResponse(false, MESSAGE_TRANSFER_FEE_NOT_FOUND);
        }
        if (DEFAULT_BANK_CODE.equals(transferFeeRate.getBankCode())
                && !DEFAULT_BANK_CODE.equals(bankCode)) {
            return new TransferFeeRateResponse(false, MESSAGE_DEFAULT_BANK_CODE_NOT_CHANGEABLE);
        }
        if (transferFeeRateRepository.existsByBankCodeAndTransferFeeIdNot(
                bankCode, transferFeeId)) {
            return new TransferFeeRateResponse(false, MESSAGE_DUPLICATED);
        }

        transferFeeRate.setBankCode(bankCode);
        transferFeeRate.setTransferFee(transferFee);
        transferFeeRate.setNote(note);
        transferFeeRate.setUpdateEmployee(loginUserId);
        transferFeeRate.setUpdatedDate(LocalDate.now());

        transferFeeRateRepository.save(transferFeeRate);
        return new TransferFeeRateResponse(true, null);
    }

    /**
     * 入力値の必須・形式を検査する。
     *
     * @return 問題があればエラーメッセージ。問題が無ければ null
     */
    private String validateInput(String bankCode, String transferFeeText, String note) {
        if (bankCode.isEmpty()) {
            return MESSAGE_BANK_CODE_REQUIRED;
        }
        if (!DEFAULT_BANK_CODE.equals(bankCode) && !BANK_CODE_PATTERN.matcher(bankCode).matches()) {
            return MESSAGE_BANK_CODE_FORMAT;
        }
        if (transferFeeText.isEmpty()) {
            return MESSAGE_TRANSFER_FEE_REQUIRED;
        }
        if (!TRANSFER_FEE_PATTERN.matcher(transferFeeText).matches()) {
            return MESSAGE_TRANSFER_FEE_FORMAT;
        }
        if (note.length() > MAX_NOTE_LENGTH) {
            return MESSAGE_NOTE_LENGTH;
        }
        return null;
    }

    /**
     * 文字列の前後の空白を除去する。null の場合は空文字を返す。
     *
     * @param value 対象の文字列
     * @return 前後の空白を除去した文字列
     */
    private String trimToEmpty(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
