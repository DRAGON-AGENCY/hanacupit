package com.cupit.service.applicationform;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.cupit.model.PaygateStoreMapping;

/**
 * PaygateStoreMappingの物理名(スネークケース) -> ゲッターの対応表。
 * 各決済会社所定申込フォーム作成のフィールドマッピング（データ駆動）が、
 * 物理名の文字列だけから値を解決できるようにするための自動生成クラス。
 */
public final class PaygateStoreMappingFieldAccessor {

    private static final Map<String, Function<PaygateStoreMapping, Object>> MAP = new HashMap<>();

    static {
        MAP.put("jcb_merchant_no", PaygateStoreMapping::getJcbMerchantNo);
        MAP.put("trade_code", PaygateStoreMapping::getTradeCode);
        MAP.put("terminal_id", PaygateStoreMapping::getTerminalId);
    }

    private PaygateStoreMappingFieldAccessor() {
    }

    public static Object get(PaygateStoreMapping source, String physicalName) {
        Function<PaygateStoreMapping, Object> getter = MAP.get(physicalName);
        if (getter == null) {
            throw new IllegalArgumentException(
                    "PaygateStoreMappingFieldAccessor: 未知の物理名です: " + physicalName);
        }
        return getter.apply(source);
    }

}
