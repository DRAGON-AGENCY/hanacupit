package com.cupit.service.applicationform;

import java.util.Map;

import com.cupit.model.ApplicationFormInput;
import com.cupit.model.MemberInfo;
import com.cupit.model.PaygateStoreMapping;

/**
 * 出力Excel1行分の値解決に必要な情報をまとめたコンテキスト。
 * INPUT行・m_member_info・m_paygate_store_mapping（存在する場合）・
 * システム側で算出した値（新規/変更/解約フラグ等）を保持する。
 */
public class ApplicationFormRowContext {

    private final ApplicationFormInput input;
    private final MemberInfo memberInfo;
    private final PaygateStoreMapping paygateStoreMapping;
    private final Map<String, String> derivedValues;
    private final int rowSequence;

    public ApplicationFormRowContext(
            ApplicationFormInput input, MemberInfo memberInfo,
            PaygateStoreMapping paygateStoreMapping, Map<String, String> derivedValues,
            int rowSequence) {
        this.input = input;
        this.memberInfo = memberInfo;
        this.paygateStoreMapping = paygateStoreMapping;
        this.derivedValues = derivedValues;
        this.rowSequence = rowSequence;
    }

    public ApplicationFormInput getInput() {
        return input;
    }

    public MemberInfo getMemberInfo() {
        return memberInfo;
    }

    public PaygateStoreMapping getPaygateStoreMapping() {
        return paygateStoreMapping;
    }

    public Map<String, String> getDerivedValues() {
        return derivedValues;
    }

    public int getRowSequence() {
        return rowSequence;
    }

}
