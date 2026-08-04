package com.cupit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cupit.model.TransferFeeRate;

/**
 * 振込手数料マスタ (m_transfer_fee_rate) の永続化を担うリポジトリ。
 */
public interface TransferFeeRateRepository
        extends JpaRepository<TransferFeeRate, Integer> {

    Optional<TransferFeeRate> findByBankCode(String bankCode);

    /**
     * 全振込手数料を振込手数料 ID の昇順で取得する。
     *
     * @return 振込手数料の一覧
     */
    List<TransferFeeRate> findAllByOrderByTransferFeeIdAsc();

    /**
     * 指定した銀行コードが存在するかどうかを返す。
     *
     * @param bankCode 銀行コード
     * @return 存在する場合は true
     */
    boolean existsByBankCode(String bankCode);

    /**
     * 指定した振込手数料 ID 以外で、指定した銀行コードが存在するかどうかを返す。
     * 編集時に自分自身を除外して重複を判定するために使用する。
     *
     * @param bankCode 銀行コード
     * @param transferFeeId 除外する振込手数料 ID
     * @return 存在する場合は true
     */
    boolean existsByBankCodeAndTransferFeeIdNot(String bankCode, int transferFeeId);

}
