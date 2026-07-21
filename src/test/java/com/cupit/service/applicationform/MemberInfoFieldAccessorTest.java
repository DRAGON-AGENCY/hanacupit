package com.cupit.service.applicationform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.cupit.model.MemberInfo;

/**
 * {@link MemberInfoFieldAccessor} のテスト。既知の物理名からの値解決と、
 * 未知の物理名を渡した場合に{@link IllegalArgumentException}が送出されることを検証する。
 */
class MemberInfoFieldAccessorTest {

    @Test
    void getReturnsValueForKnownPhysicalName() {
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setStoreName("赤坂生花店");

        Object value = MemberInfoFieldAccessor.get(memberInfo, "store_name");

        assertThat(value).isEqualTo("赤坂生花店");
    }

    @Test
    void getThrowsForUnknownPhysicalName() {
        MemberInfo memberInfo = new MemberInfo();

        assertThatThrownBy(() -> MemberInfoFieldAccessor.get(memberInfo, "unknown_field"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unknown_field");
    }

}
