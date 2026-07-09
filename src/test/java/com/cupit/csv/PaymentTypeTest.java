package com.cupit.csv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * {@link PaymentType} のテスト。stera terminal 系の3決済種類（stera JCB・stera code・
 * steraクレジット）の表示名解決と、未指定・不明値の異常系を検証する。
 */
class PaymentTypeTest {

    @Test
    void fromDisplayNameResolvesSteraJcb() {
        assertThat(PaymentType.fromDisplayName("stera JCB")).isEqualTo(PaymentType.STERA_JCB);
    }

    @Test
    void fromDisplayNameResolvesSteraCode() {
        assertThat(PaymentType.fromDisplayName("stera code")).isEqualTo(PaymentType.STERA_CODE);
    }

    @Test
    void fromDisplayNameResolvesSteraCredit() {
        assertThat(PaymentType.fromDisplayName("steraクレジット"))
                .isEqualTo(PaymentType.STERA_CREDIT);
    }

    @Test
    void fromDisplayNameThrowsForUnknownName() {
        assertThatThrownBy(() -> PaymentType.fromDisplayName("stera クレジット"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不明な決済種類");
    }

    @Test
    void fromDisplayNameThrowsForNull() {
        assertThatThrownBy(() -> PaymentType.fromDisplayName(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("決済種類が未指定");
    }

    @Test
    void fromDisplayNameThrowsForBlank() {
        assertThatThrownBy(() -> PaymentType.fromDisplayName("  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("決済種類が未指定");
    }
}
