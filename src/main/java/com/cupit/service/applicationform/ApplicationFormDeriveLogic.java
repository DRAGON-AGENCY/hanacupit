package com.cupit.service.applicationform;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.cupit.model.ApplicationFormInput;

/**
 * INPUT行の値やm_paygate_store_mappingの既存有無から、ユーザー入力を求めず
 * システム側で自動算出できる項目（新規/変更/解約フラグ等）を計算する。
 * 「1：新規加盟店」シナリオを主対象とし（[[hanacupit-payment-company-format-check]]と
 * 同じスコープ方針）、既存契約がある場合は「変更」系の値を返すに留める
 * （解約・ブランド追加/削除等の細分類は自動判定せず、必要なら生成後のExcelで
 * 担当者が手動修正する）。
 */
@Component
public class ApplicationFormDeriveLogic {

    private static final DateTimeFormatter FMT_SLASH = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    public Map<String, String> compute(ApplicationFormInput input, boolean existingContract) {
        Map<String, String> values = new HashMap<>();
        String today = LocalDate.now().format(FMT_SLASH);
        boolean cancelIntent = isCancelIntent(input);

        values.put("NEW_CHANGE_CANCEL_FLAG_JCB", jcbFlag(existingContract, cancelIntent));
        values.put("NEW_CHANGE_CANCEL_FLAG_SMCC", smccFlag(existingContract, cancelIntent));
        values.put("SYSTEM_DATE_SLASH", today);
        values.put("EXISTING_CONTRACT_FLAG", existingContract ? "有" : "無");
        values.put("CANCEL_INTENTION", cancelIntent ? "解約意思あり" : "");
        values.put("CANCEL_STATUS", trimToEmpty(input.getStatus()));
        values.put("DEFAULT_STORE_COUNT_1", "1");
        values.put("DEFAULT_REPRESENTATIVE_STORE_FLAG", "有");
        return values;
    }

    private boolean isCancelIntent(ApplicationFormInput input) {
        String v = input.getPaygateContinuationStatus();
        return v != null && v.contains("停止");
    }

    private String jcbFlag(boolean existingContract, boolean cancelIntent) {
        if (cancelIntent) {
            return "解約";
        }
        return existingContract ? "変更" : "新規";
    }

    private String smccFlag(boolean existingContract, boolean cancelIntent) {
        if (cancelIntent) {
            return "5：解約";
        }
        return existingContract ? "4：加盟店情報変更" : "1：新規加盟店";
    }

    private String trimToEmpty(String s) {
        return s == null ? "" : s.trim();
    }

}
