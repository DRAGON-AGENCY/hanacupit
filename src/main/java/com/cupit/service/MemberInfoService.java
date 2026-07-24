package com.cupit.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cupit.dto.MemberListItem;
import com.cupit.exception.MemberInfoNotFoundException;
import com.cupit.model.MemberInfo;
import com.cupit.repository.MemberInfoRepository;

/**
 * 会員情報の取得を担うサービス。
 * 取引コードを指定して会員情報をデータベースから取得する。
 */
@Service
public class MemberInfoService {

    private static final String MESSAGE_NOT_FOUND = "会員情報が見つかりません: ";

    private final MemberInfoRepository memberInfoRepository;

    public MemberInfoService(MemberInfoRepository memberInfoRepository) {
        this.memberInfoRepository = memberInfoRepository;
    }

    /**
     * 取引コードを指定して会員情報を取得する。
     * 取引コードが未入力（null／空文字）の場合も既定の取引コードへ読み替えず、
     * そのまま検索する（該当する会員情報が存在しないため例外となる）。
     *
     * @param tradeCode 取引コード
     * @return 取得した会員情報
     * @throws MemberInfoNotFoundException 該当する会員情報が存在しない場合
     */
    public MemberInfo findByTradeCode(String tradeCode) {
        String lookupTradeCode = tradeCode != null ? tradeCode : "";
        return memberInfoRepository.findById(lookupTradeCode)
                .orElseThrow(() -> new MemberInfoNotFoundException(
                        MESSAGE_NOT_FOUND + lookupTradeCode));
    }

    /**
     * 「加盟店一覧」画面向けに、取引コード順の一覧を取得する。
     *
     * @return 一覧表示用DTOのリスト
     */
    public List<MemberListItem> findAllForList() {
        return memberInfoRepository.findAllForList();
    }
}
