package com.cupit.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cupit.dto.SettlementFeeRateRequest;
import com.cupit.dto.SettlementFeeRateResponse;
import com.cupit.model.SettlementFeeRate;
import com.cupit.repository.SettlementFeeRateRepository;
import com.cupit.service.settlement.FeeCalcModel;

/**
 * 手数料率マスタ (m_settlement_fee_rate) の取得・登録・更新・削除を担うサービス。
 * JFTD精算・stera terminal精算の各計算処理が参照するマスタのメンテナンスを提供する。
 */
@Service
public class SettlementFeeRateService {

    private static final String MODE_NEW = "new";

    private static final int MAX_PAYMENT_COMPANY_LENGTH = 30;
    private static final int MAX_CARD_BRAND_LENGTH = 30;

    // NUMERIC(6,5) の範囲 (整数部1桁・小数部5桁) に収まる非負の数値のみ許可する
    private static final Pattern RATE_PATTERN =
            Pattern.compile("^[0-9](\\.[0-9]{1,5})?$");

    private static final String MESSAGE_INVALID_INPUT =
            "入力内容が正しくありません。";
    private static final String MESSAGE_PAYMENT_COMPANY_REQUIRED =
            "決済会社を入力してください。";
    private static final String MESSAGE_PAYMENT_COMPANY_LENGTH =
            "決済会社は30文字以内で入力してください。";
    private static final String MESSAGE_CARD_BRAND_REQUIRED =
            "カードブランドを入力してください。";
    private static final String MESSAGE_CARD_BRAND_LENGTH =
            "カードブランドは30文字以内で入力してください。";
    private static final String MESSAGE_CALC_MODEL_INVALID =
            "計算モデルが正しくありません。";
    private static final String MESSAGE_ACQUIRER_FEE_RATE_FORMAT =
            "仕入手数料率は0以上10未満の数値で、小数点以下5桁までで入力してください。";
    private static final String MESSAGE_OUR_FEE_RATE_BASE_REQUIRED =
            "当社手数料率 (本体) を入力してください。";
    private static final String MESSAGE_OUR_FEE_RATE_BASE_FORMAT =
            "当社手数料率 (本体) は0以上10未満の数値で、小数点以下5桁までで入力してください。";
    private static final String MESSAGE_OUR_FEE_RATE_TAX_FORMAT =
            "当社手数料率 (消費税) は0以上10未満の数値で、小数点以下5桁までで入力してください。";
    private static final String MESSAGE_DUPLICATED =
            "同じ決済会社・カードブランドの組み合わせが既に登録されています。";
    private static final String MESSAGE_FEE_RATE_NOT_FOUND =
            "対象の手数料率が見つかりません。";

    private final SettlementFeeRateRepository settlementFeeRateRepository;

    public SettlementFeeRateService(
            SettlementFeeRateRepository settlementFeeRateRepository) {
        this.settlementFeeRateRepository = settlementFeeRateRepository;
    }

    /**
     * 全手数料率を手数料率 ID の昇順で取得する。
     *
     * @return 手数料率の一覧
     */
    public List<SettlementFeeRate> findAllFeeRates() {
        return settlementFeeRateRepository.findAllByOrderByFeeRateIdAsc();
    }

    /**
     * 手数料率 ID を指定して手数料率を 1 件取得する。
     *
     * @param feeRateId 手数料率 ID
     * @return 該当する手数料率。存在しない場合は null
     */
    public SettlementFeeRate findById(int feeRateId) {
        return settlementFeeRateRepository.findById(feeRateId).orElse(null);
    }

    /**
     * 手数料率を登録または更新する。
     * 入力値を検査し、問題があれば失敗結果とメッセージを返す。
     *
     * @param request 画面から送信された手数料率情報
     * @param loginUserId 操作中のログインユーザの user_id (更新者として記録する)
     * @return 処理結果
     */
    @Transactional
    public SettlementFeeRateResponse saveFeeRate(
            SettlementFeeRateRequest request, String loginUserId) {
        if (request == null) {
            return new SettlementFeeRateResponse(false, MESSAGE_INVALID_INPUT);
        }

        String paymentCompany = trimToEmpty(request.getPaymentCompany());
        String cardBrand = trimToEmpty(request.getCardBrand());
        String calcModel = trimToEmpty(request.getCalcModel());
        String acquirerFeeRateText = trimToEmpty(request.getAcquirerFeeRate());
        String ourFeeRateBaseText = trimToEmpty(request.getOurFeeRateBase());
        String ourFeeRateTaxText = trimToEmpty(request.getOurFeeRateTax());

        String validationMessage = validateInput(
                paymentCompany, cardBrand, calcModel,
                acquirerFeeRateText, ourFeeRateBaseText, ourFeeRateTaxText);
        if (validationMessage != null) {
            return new SettlementFeeRateResponse(false, validationMessage);
        }

        BigDecimal acquirerFeeRate = toBigDecimalOrNull(acquirerFeeRateText);
        BigDecimal ourFeeRateBase = new BigDecimal(ourFeeRateBaseText);
        BigDecimal ourFeeRateTax = toBigDecimalOrNull(ourFeeRateTaxText);

        boolean isNewMode = MODE_NEW.equals(request.getMode());
        if (isNewMode) {
            return createFeeRate(
                    paymentCompany, cardBrand, calcModel,
                    acquirerFeeRate, ourFeeRateBase, ourFeeRateTax, loginUserId);
        }
        return updateFeeRate(
                request.getFeeRateId(), paymentCompany, cardBrand, calcModel,
                acquirerFeeRate, ourFeeRateBase, ourFeeRateTax, loginUserId);
    }

    /**
     * 手数料率 ID を指定して手数料率を削除する。
     *
     * @param feeRateId 手数料率 ID
     * @return 処理結果
     */
    @Transactional
    public SettlementFeeRateResponse deleteFeeRate(int feeRateId) {
        if (!settlementFeeRateRepository.existsById(feeRateId)) {
            return new SettlementFeeRateResponse(false, MESSAGE_FEE_RATE_NOT_FOUND);
        }
        settlementFeeRateRepository.deleteById(feeRateId);
        return new SettlementFeeRateResponse(true, null);
    }

    /**
     * 新規手数料率を登録する。決済会社・カードブランドの組み合わせの重複を検査する。
     */
    private SettlementFeeRateResponse createFeeRate(
            String paymentCompany, String cardBrand, String calcModel,
            BigDecimal acquirerFeeRate, BigDecimal ourFeeRateBase,
            BigDecimal ourFeeRateTax, String loginUserId) {
        if (settlementFeeRateRepository.existsByPaymentCompanyAndCardBrand(
                paymentCompany, cardBrand)) {
            return new SettlementFeeRateResponse(false, MESSAGE_DUPLICATED);
        }

        SettlementFeeRate feeRate = new SettlementFeeRate();
        feeRate.setPaymentCompany(paymentCompany);
        feeRate.setCardBrand(cardBrand);
        feeRate.setCalcModel(calcModel);
        feeRate.setAcquirerFeeRate(acquirerFeeRate);
        feeRate.setOurFeeRateBase(ourFeeRateBase);
        feeRate.setOurFeeRateTax(ourFeeRateTax);
        feeRate.setUpdateEmployee(loginUserId);

        LocalDate today = LocalDate.now();
        feeRate.setCreateDate(today);
        feeRate.setUpdatedDate(today);

        settlementFeeRateRepository.save(feeRate);
        return new SettlementFeeRateResponse(true, null);
    }

    /**
     * 既存手数料率を更新する。
     */
    private SettlementFeeRateResponse updateFeeRate(
            int feeRateId, String paymentCompany, String cardBrand,
            String calcModel, BigDecimal acquirerFeeRate,
            BigDecimal ourFeeRateBase, BigDecimal ourFeeRateTax,
            String loginUserId) {
        SettlementFeeRate feeRate =
                settlementFeeRateRepository.findById(feeRateId).orElse(null);
        if (feeRate == null) {
            return new SettlementFeeRateResponse(false, MESSAGE_FEE_RATE_NOT_FOUND);
        }
        if (settlementFeeRateRepository
                .existsByPaymentCompanyAndCardBrandAndFeeRateIdNot(
                        paymentCompany, cardBrand, feeRateId)) {
            return new SettlementFeeRateResponse(false, MESSAGE_DUPLICATED);
        }

        feeRate.setPaymentCompany(paymentCompany);
        feeRate.setCardBrand(cardBrand);
        feeRate.setCalcModel(calcModel);
        feeRate.setAcquirerFeeRate(acquirerFeeRate);
        feeRate.setOurFeeRateBase(ourFeeRateBase);
        feeRate.setOurFeeRateTax(ourFeeRateTax);
        feeRate.setUpdateEmployee(loginUserId);
        feeRate.setUpdatedDate(LocalDate.now());

        settlementFeeRateRepository.save(feeRate);
        return new SettlementFeeRateResponse(true, null);
    }

    /**
     * 入力値の必須・形式を検査する。
     *
     * @return 問題があればエラーメッセージ。問題が無ければ null
     */
    private String validateInput(
            String paymentCompany, String cardBrand, String calcModel,
            String acquirerFeeRateText, String ourFeeRateBaseText,
            String ourFeeRateTaxText) {
        if (paymentCompany.isEmpty()) {
            return MESSAGE_PAYMENT_COMPANY_REQUIRED;
        }
        if (paymentCompany.length() > MAX_PAYMENT_COMPANY_LENGTH) {
            return MESSAGE_PAYMENT_COMPANY_LENGTH;
        }
        if (cardBrand.isEmpty()) {
            return MESSAGE_CARD_BRAND_REQUIRED;
        }
        if (cardBrand.length() > MAX_CARD_BRAND_LENGTH) {
            return MESSAGE_CARD_BRAND_LENGTH;
        }
        if (!isValidCalcModel(calcModel)) {
            return MESSAGE_CALC_MODEL_INVALID;
        }
        // 仕入手数料率は任意項目のため、入力された場合のみ形式を検査する
        if (!acquirerFeeRateText.isEmpty()
                && !RATE_PATTERN.matcher(acquirerFeeRateText).matches()) {
            return MESSAGE_ACQUIRER_FEE_RATE_FORMAT;
        }
        if (ourFeeRateBaseText.isEmpty()) {
            return MESSAGE_OUR_FEE_RATE_BASE_REQUIRED;
        }
        if (!RATE_PATTERN.matcher(ourFeeRateBaseText).matches()) {
            return MESSAGE_OUR_FEE_RATE_BASE_FORMAT;
        }
        // 当社手数料率(消費税)は任意項目のため、入力された場合のみ形式を検査する
        if (!ourFeeRateTaxText.isEmpty()
                && !RATE_PATTERN.matcher(ourFeeRateTaxText).matches()) {
            return MESSAGE_OUR_FEE_RATE_TAX_FORMAT;
        }
        return null;
    }

    /**
     * 計算モデルが許可された値 (STRAIGHT/PURCHASE_COLLECT/SBI_RESIDUAL) かどうかを判定する。
     *
     * @param calcModel 計算モデル
     * @return 許可された値の場合は true
     */
    private boolean isValidCalcModel(String calcModel) {
        try {
            FeeCalcModel.valueOf(calcModel);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 文字列を BigDecimal に変換する。空文字の場合は null を返す。
     *
     * @param value 対象の文字列
     * @return 変換後の値。空文字の場合は null
     */
    private BigDecimal toBigDecimalOrNull(String value) {
        if (value.isEmpty()) {
            return null;
        }
        return new BigDecimal(value);
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
