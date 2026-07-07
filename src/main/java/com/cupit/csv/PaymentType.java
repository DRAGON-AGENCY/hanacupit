package com.cupit.csv;

/**
 * 決済種類と対応するファイル拡張子を保持する列挙型。
 */
public enum PaymentType {

    JCB("JCB", "csv"),
    SUMAREJO("スマレジ", "csv"),
    NETSTARS("ネットスターズ", "xlsx"),
    RAKUTENPAY("楽天ペイ", "xlsx"),
    JUSHIN_SBI("住信SBI", "dat");

    private final String displayName;
    private final String expectedExtension;

    PaymentType(String displayName, String expectedExtension) {
        this.displayName = displayName;
        this.expectedExtension = expectedExtension;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getExpectedExtension() {
        return expectedExtension;
    }

    public static PaymentType fromDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("決済種類が未指定です。");
        }
        for (PaymentType type : values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("不明な決済種類です: " + displayName);
    }
}
