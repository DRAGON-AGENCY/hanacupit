package com.cupit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cupit.model.SettlementItemCode;

/**
 * 項目コードマスタ (m_settlement_item_code) の永続化を担うリポジトリ。
 */
public interface SettlementItemCodeRepository
        extends JpaRepository<SettlementItemCode, Integer> {

    Optional<SettlementItemCode> findByPaymentCompanyAndCardBrandAndAmountType(
            String paymentCompany, String cardBrand, String amountType);

    Optional<SettlementItemCode> findByItemCode(String itemCode);

    /**
     * 全項目コードを項目コードIDの昇順で取得する。
     *
     * @return 項目コードの一覧
     */
    List<SettlementItemCode> findAllByOrderByItemCodeIdAsc();

    /**
     * 指定した決済会社・カードブランド・金額種別の組み合わせが存在するかどうかを返す。
     *
     * @param paymentCompany 決済会社
     * @param cardBrand カードブランド
     * @param amountType 金額種別
     * @return 存在する場合は true
     */
    boolean existsByPaymentCompanyAndCardBrandAndAmountType(
            String paymentCompany, String cardBrand, String amountType);

    /**
     * 指定した項目コードID以外で、指定した決済会社・カードブランド・金額種別の
     * 組み合わせが存在するかどうかを返す。編集時に自分自身を除外して重複を判定する
     * ために使用する。
     *
     * @param paymentCompany 決済会社
     * @param cardBrand カードブランド
     * @param amountType 金額種別
     * @param itemCodeId 除外する項目コードID
     * @return 存在する場合は true
     */
    boolean existsByPaymentCompanyAndCardBrandAndAmountTypeAndItemCodeIdNot(
            String paymentCompany, String cardBrand, String amountType, int itemCodeId);

    /**
     * 指定した項目コードが存在するかどうかを返す。
     * 帳票出力(JftdReportDataService)が項目コードをキーに逆引きするため、
     * DB制約には無いが項目コード自体の一意性もアプリ側で保証する必要がある。
     *
     * @param itemCode 項目コード
     * @return 存在する場合は true
     */
    boolean existsByItemCode(String itemCode);

    /**
     * 指定した項目コードID以外で、指定した項目コードが存在するかどうかを返す。
     *
     * @param itemCode 項目コード
     * @param itemCodeId 除外する項目コードID
     * @return 存在する場合は true
     */
    boolean existsByItemCodeAndItemCodeIdNot(String itemCode, int itemCodeId);

}
