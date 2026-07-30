package com.cupit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cupit.model.SettlementFeeRate;

/**
 * 手数料率マスタ (m_settlement_fee_rate) の永続化を担うリポジトリ。
 */
public interface SettlementFeeRateRepository
        extends JpaRepository<SettlementFeeRate, Integer> {

    Optional<SettlementFeeRate> findByPaymentCompanyAndCardBrand(
            String paymentCompany, String cardBrand);

    /**
     * 全手数料率を手数料率 ID の昇順で取得する。
     *
     * @return 手数料率の一覧
     */
    List<SettlementFeeRate> findAllByOrderByFeeRateIdAsc();

    /**
     * 指定した決済会社・カードブランドの組み合わせが存在するかどうかを返す。
     *
     * @param paymentCompany 決済会社
     * @param cardBrand カードブランド
     * @return 存在する場合は true
     */
    boolean existsByPaymentCompanyAndCardBrand(
            String paymentCompany, String cardBrand);

    /**
     * 指定した手数料率 ID 以外で、指定した決済会社・カードブランドの組み合わせが
     * 存在するかどうかを返す。編集時に自分自身を除外して重複を判定するために使用する。
     *
     * @param paymentCompany 決済会社
     * @param cardBrand カードブランド
     * @param feeRateId 除外する手数料率 ID
     * @return 存在する場合は true
     */
    boolean existsByPaymentCompanyAndCardBrandAndFeeRateIdNot(
            String paymentCompany, String cardBrand, int feeRateId);

}
