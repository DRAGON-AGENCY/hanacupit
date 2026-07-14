package com.cupit.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HalfWidthKanaConverterTest {

    @Test
    void convertsPlainKatakana() {
        assertThat(HalfWidthKanaConverter.toHalfWidth("カタカナ")).isEqualTo("ｶﾀｶﾅ");
    }

    @Test
    void convertsVoicedAndSemiVoicedSoundsToTwoCharacters() {
        assertThat(HalfWidthKanaConverter.toHalfWidth("ガパ")).isEqualTo("ｶﾞﾊﾟ");
    }

    @Test
    void convertsLongVowelMark() {
        assertThat(HalfWidthKanaConverter.toHalfWidth("ハナキューピット"))
                .isEqualTo("ﾊﾅｷｭｰﾋﾟｯﾄ");
    }

    @Test
    void leavesAsciiAndAlreadyHalfWidthCharactersUnchanged() {
        assertThat(HalfWidthKanaConverter.toHalfWidth("ABC123ｶﾀｶﾅ")).isEqualTo("ABC123ｶﾀｶﾅ");
    }

    @Test
    void returnsNullForNullInput() {
        assertThat(HalfWidthKanaConverter.toHalfWidth(null)).isNull();
    }

}
