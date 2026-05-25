package com.cupit.service;

import org.springframework.stereotype.Service;

import com.cupit.model.MemberInfo;

@Service
public class MemberInfoService {

    private static final String DEFAULT_TRANSACTION_CODE = "01-001";
    private static final String DEFAULT_STORE_NAME = "赤坂生花店";
    private static final String DEFAULT_STORE_NAME_KANA = "アカサカセイカテン";
    private static final String DEFAULT_MEMBER_TYPE = "正会員";
    private static final String DEFAULT_PARENT_STORE_CODE = "01-001";
    private static final String DEFAULT_PARENT_STORE_NAME = "赤坂生花店";
    private static final String EMPTY_MARK = "—";
    private static final String DEFAULT_BLOCK_CODE = "資格区分";
    private static final String DEFAULT_JOIN_DATE = "1960/08/16";
    private static final String DEFAULT_CORPORATION_FLAG = "社団法人";
    private static final String DEFAULT_COOPERATIVE_FLAG = "協同組合";
    private static final String DEFAULT_REPRESENTATIVE_NAME = "山田 太郎";
    private static final String DEFAULT_REPRESENTATIVE_KANA = "ヤマダ タロウ";
    private static final String DEFAULT_POSTAL_CODE = "107-0052";
    private static final String DEFAULT_ADDRESS = "東京都港区赤坂1-2-3";
    private static final String DEFAULT_PHONE_NUMBER = "03-1234-5678";

    public MemberInfo findByTransactionCode(String transactionCode) {
        String resolvedTransactionCode = resolveTransactionCode(transactionCode);
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setTransactionCode(resolvedTransactionCode);
        memberInfo.setStoreName(DEFAULT_STORE_NAME);
        memberInfo.setStoreNameKana(DEFAULT_STORE_NAME_KANA);
        memberInfo.setMemberType(DEFAULT_MEMBER_TYPE);
        memberInfo.setParentStoreCode(DEFAULT_PARENT_STORE_CODE);
        memberInfo.setParentStoreName(DEFAULT_PARENT_STORE_NAME);
        memberInfo.setNewTransactionCode(EMPTY_MARK);
        memberInfo.setPrevTransactionCode(EMPTY_MARK);
        memberInfo.setMiddleCode(EMPTY_MARK);
        memberInfo.setBlockCode(DEFAULT_BLOCK_CODE);
        memberInfo.setJoinDate(DEFAULT_JOIN_DATE);
        memberInfo.setCorporationFlag(DEFAULT_CORPORATION_FLAG);
        memberInfo.setCooperativeFlag(DEFAULT_COOPERATIVE_FLAG);
        memberInfo.setRepresentativeName(DEFAULT_REPRESENTATIVE_NAME);
        memberInfo.setRepresentativeKana(DEFAULT_REPRESENTATIVE_KANA);
        memberInfo.setPostalCode(DEFAULT_POSTAL_CODE);
        memberInfo.setAddress(DEFAULT_ADDRESS);
        memberInfo.setPhoneNumber(DEFAULT_PHONE_NUMBER);
        return memberInfo;
    }

    private String resolveTransactionCode(String transactionCode) {
        if (transactionCode == null || transactionCode.isBlank()) {
            return DEFAULT_TRANSACTION_CODE;
        }
        return transactionCode;
    }
}
