package com.cupit.service.applicationform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.cupit.model.ApplicationFormInput;

/**
 * {@link ApplicationFormInputFieldAccessor} のテスト。既知の物理名からの値解決と、
 * 未知の物理名を渡した場合に{@link IllegalArgumentException}が送出されることを検証する。
 */
class ApplicationFormInputFieldAccessorTest {

    @Test
    void getReturnsValueForKnownPhysicalName() {
        ApplicationFormInput input = new ApplicationFormInput();
        input.setStoreNameAlphabet("FLOWER SHOP YAZAKI");

        Object value = ApplicationFormInputFieldAccessor.get(input, "store_name_alphabet");

        assertThat(value).isEqualTo("FLOWER SHOP YAZAKI");
    }

    @Test
    void getThrowsForUnknownPhysicalName() {
        ApplicationFormInput input = new ApplicationFormInput();

        assertThatThrownBy(() -> ApplicationFormInputFieldAccessor.get(input, "unknown_field"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unknown_field");
    }

}
