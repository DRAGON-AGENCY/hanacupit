package com.cupit.service;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.cupit.exception.MemberInfoNotFoundException;
import com.cupit.model.MemberInfo;
import com.cupit.repository.MemberInfoRepository;

/**
 * 会員情報の取得を担うサービス。
 * 取引コードを指定して会員情報をデータベースから取得する。
 */
@Service
public class MemberInfoService {

    private static final String DEFAULT_TRADE_CODE = "01-001";
    private static final String MESSAGE_NOT_FOUND = "会員情報が見つかりません: ";

    private final MemberInfoRepository memberInfoRepository;

    public MemberInfoService(MemberInfoRepository memberInfoRepository) {
        this.memberInfoRepository = memberInfoRepository;
    }

    /**
     * 取引コードを指定して会員情報を取得する。
     * 取引コードが未指定の場合は既定の取引コードを使用する。
     *
     * @param tradeCode 取引コード
     * @return 取得した会員情報
     * @throws MemberInfoNotFoundException 該当する会員情報が存在しない場合
     */
    public MemberInfo findByTradeCode(String tradeCode) {
        String resolvedTradeCode = resolveTradeCode(tradeCode);
        return memberInfoRepository.findById(resolvedTradeCode)
                .orElseThrow(() -> new MemberInfoNotFoundException(
                        MESSAGE_NOT_FOUND + resolvedTradeCode));
    }

    private @NonNull String resolveTradeCode(String tradeCode) {
        if (tradeCode == null || tradeCode.isBlank()) {
            return DEFAULT_TRADE_CODE;
        }
        return tradeCode;
    }
}
