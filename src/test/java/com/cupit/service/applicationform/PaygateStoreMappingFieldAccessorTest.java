package com.cupit.service.applicationform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.cupit.model.PaygateStoreMapping;

/**
 * {@link PaygateStoreMappingFieldAccessor} のテスト。既知の物理名からの値解決と、
 * 未知の物理名を渡した場合に{@link IllegalArgumentException}が送出されることを検証する。
 */
class PaygateStoreMappingFieldAccessorTest {

    @Test
    void getReturnsValueForKnownPhysicalName() {
        PaygateStoreMapping paygate = new PaygateStoreMapping();
        paygate.setJcbMerchantNo("1234567890");

        Object value = PaygateStoreMappingFieldAccessor.get(paygate, "jcb_merchant_no");

        assertThat(value).isEqualTo("1234567890");
    }

    @Test
    void getThrowsForUnknownPhysicalName() {
        PaygateStoreMapping paygate = new PaygateStoreMapping();

        assertThatThrownBy(() -> PaygateStoreMappingFieldAccessor.get(paygate, "unknown_field"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unknown_field");
    }

}
