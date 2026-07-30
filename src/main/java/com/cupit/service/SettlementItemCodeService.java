package com.cupit.service;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cupit.dto.SettlementItemCodeRequest;
import com.cupit.dto.SettlementItemCodeResponse;
import com.cupit.model.SettlementItemCode;
import com.cupit.repository.SettlementItemCodeRepository;

/**
 * 項目コードマスタ (m_settlement_item_code) の取得・登録・更新・削除を担うサービス。
 * JFTD統合振込CSV作成・帳票出力の各処理が参照するマスタのメンテナンスを提供する。
 */
@Service
public class SettlementItemCodeService {

    private static final String MODE_NEW = "new";

    private static final int MAX_PAYMENT_COMPANY_LENGTH = 30;
    private static final int MAX_CARD_BRAND_LENGTH = 30;
    private static final int MAX_ITEM_CODE_LENGTH = 10;

    private static final String AMOUNT_TYPE_PAYMENT = "PAYMENT";
    private static final String AMOUNT_TYPE_FEE_BASE = "FEE_BASE";
    private static final String AMOUNT_TYPE_FEE_TAX = "FEE_TAX";

    // 項目コードは半角数字のみ、VARCHAR(10)の範囲で入力する
    private static final Pattern ITEM_CODE_PATTERN = Pattern.compile("^[0-9]{1,10}$");

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

    private final SettlementItemCodeRepository settlementItemCodeRepository;

    public SettlementItemCodeService(
            SettlementItemCodeRepository settlementItemCodeRepository) {
        this.settlementItemCodeRepository = settlementItemCodeRepository;
    }

    /**
     * 全項目コードを項目コードIDの昇順で取得する。
     *
     * @return 項目コードの一覧
     */
    public List<SettlementItemCode> findAllItemCodes() {
        return settlementItemCodeRepository.findAllByOrderByItemCodeIdAsc();
    }

    /**
     * 項目コードIDを指定して項目コードを 1 件取得する。
     *
     * @param itemCodeId 項目コードID
     * @return 該当する項目コード。存在しない場合は null
     */
    public SettlementItemCode findById(int itemCodeId) {
        return settlementItemCodeRepository.findById(itemCodeId).orElse(null);
    }

    /**
     * 項目コードを登録または更新する。
     * 入力値を検査し、問題があれば失敗結果とメッセージを返す。
     *
     * @param request 画面から送信された項目コード情報
     * @param loginUserId 操作中のログインユーザの user_id (更新者として記録する)
     * @return 処理結果
     */
    @Transactional
    public SettlementItemCodeResponse saveItemCode(
            SettlementItemCodeRequest request, String loginUserId) {
        if (request == null) {
            return new SettlementItemCodeResponse(false, MESSAGE_INVALID_INPUT);
        }

        String paymentCompany = trimToEmpty(request.getPaymentCompany());
        String cardBrand = trimToEmpty(request.getCardBrand());
        String amountType = trimToEmpty(request.getAmountType());
        String itemCode = trimToEmpty(request.getItemCode());

        String validationMessage = validateInput(
                paymentCompany, cardBrand, amountType, itemCode);
        if (validationMessage != null) {
            return new SettlementItemCodeResponse(false, validationMessage);
        }

        boolean isNewMode = MODE_NEW.equals(request.getMode());
        if (isNewMode) {
            return createItemCode(
                    paymentCompany, cardBrand, amountType, itemCode, loginUserId);
        }
        return updateItemCode(
                request.getItemCodeId(), paymentCompany, cardBrand,
                amountType, itemCode, loginUserId);
    }

    /**
     * 項目コードIDを指定して項目コードを削除する。
     *
     * @param itemCodeId 項目コードID
     * @return 処理結果
     */
    @Transactional
    public SettlementItemCodeResponse deleteItemCode(int itemCodeId) {
        if (!settlementItemCodeRepository.existsById(itemCodeId)) {
            return new SettlementItemCodeResponse(false, MESSAGE_ITEM_CODE_NOT_FOUND);
        }
        settlementItemCodeRepository.deleteById(itemCodeId);
        return new SettlementItemCodeResponse(true, null);
    }

    /**
     * 新規項目コードを登録する。
     * 決済会社・カードブランド・金額種別の組み合わせと、項目コード自体の重複を検査する。
     */
    private SettlementItemCodeResponse createItemCode(
            String paymentCompany, String cardBrand, String amountType,
            String itemCode, String loginUserId) {
        if (settlementItemCodeRepository.existsByPaymentCompanyAndCardBrandAndAmountType(
                paymentCompany, cardBrand, amountType)) {
            return new SettlementItemCodeResponse(false, MESSAGE_DUPLICATED_COMBINATION);
        }
        if (settlementItemCodeRepository.existsByItemCode(itemCode)) {
            return new SettlementItemCodeResponse(false, MESSAGE_DUPLICATED_ITEM_CODE);
        }

        SettlementItemCode entity = new SettlementItemCode();
        entity.setPaymentCompany(paymentCompany);
        entity.setCardBrand(cardBrand);
        entity.setAmountType(amountType);
        entity.setItemCode(itemCode);
        entity.setUpdateEmployee(loginUserId);

        LocalDate today = LocalDate.now();
        entity.setCreateDate(today);
        entity.setUpdatedDate(today);

        settlementItemCodeRepository.save(entity);
        return new SettlementItemCodeResponse(true, null);
    }

    /**
     * 既存項目コードを更新する。
     */
    private SettlementItemCodeResponse updateItemCode(
            int itemCodeId, String paymentCompany, String cardBrand,
            String amountType, String itemCode, String loginUserId) {
        SettlementItemCode entity =
                settlementItemCodeRepository.findById(itemCodeId).orElse(null);
        if (entity == null) {
            return new SettlementItemCodeResponse(false, MESSAGE_ITEM_CODE_NOT_FOUND);
        }
        if (settlementItemCodeRepository
                .existsByPaymentCompanyAndCardBrandAndAmountTypeAndItemCodeIdNot(
                        paymentCompany, cardBrand, amountType, itemCodeId)) {
            return new SettlementItemCodeResponse(false, MESSAGE_DUPLICATED_COMBINATION);
        }
        if (settlementItemCodeRepository
                .existsByItemCodeAndItemCodeIdNot(itemCode, itemCodeId)) {
            return new SettlementItemCodeResponse(false, MESSAGE_DUPLICATED_ITEM_CODE);
        }

        entity.setPaymentCompany(paymentCompany);
        entity.setCardBrand(cardBrand);
        entity.setAmountType(amountType);
        entity.setItemCode(itemCode);
        entity.setUpdateEmployee(loginUserId);
        entity.setUpdatedDate(LocalDate.now());

        settlementItemCodeRepository.save(entity);
        return new SettlementItemCodeResponse(true, null);
    }

    /**
     * 入力値の必須・形式を検査する。
     *
     * @return 問題があればエラーメッセージ。問題が無ければ null
     */
    private String validateInput(
            String paymentCompany, String cardBrand, String amountType,
            String itemCode) {
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
        if (!isValidAmountType(amountType)) {
            return MESSAGE_AMOUNT_TYPE_INVALID;
        }
        if (itemCode.isEmpty()) {
            return MESSAGE_ITEM_CODE_REQUIRED;
        }
        if (itemCode.length() > MAX_ITEM_CODE_LENGTH
                || !ITEM_CODE_PATTERN.matcher(itemCode).matches()) {
            return MESSAGE_ITEM_CODE_FORMAT;
        }
        return null;
    }

    /**
     * 金額種別が許可された値 (PAYMENT/FEE_BASE/FEE_TAX) かどうかを判定する。
     *
     * @param amountType 金額種別
     * @return 許可された値の場合は true
     */
    private boolean isValidAmountType(String amountType) {
        return AMOUNT_TYPE_PAYMENT.equals(amountType)
                || AMOUNT_TYPE_FEE_BASE.equals(amountType)
                || AMOUNT_TYPE_FEE_TAX.equals(amountType);
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
