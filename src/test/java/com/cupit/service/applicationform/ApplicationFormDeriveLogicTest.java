package com.cupit.service.applicationform;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.cupit.model.ApplicationFormInput;

/**
 * {@link ApplicationFormDeriveLogic} のテスト。新規/変更/解約フラグ自体はINPUT列
 * （jcb_application_classification・smcc_application_classification）を直接使う方式に
 * 変更されたため（自動算出は廃止）、このクラスが引き続き自動算出する既存契約有無・
 * 解約意思・システム日付・既定値等のみを検証する。
 */
class ApplicationFormDeriveLogicTest {

    private static final DateTimeFormatter FMT_SLASH = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final ApplicationFormDeriveLogic deriveLogic = new ApplicationFormDeriveLogic();

    @Test
    void existingContractFlagIsMuWhenNoExistingContract() {
        ApplicationFormInput input = new ApplicationFormInput();
        input.setPaygateContinuationStatus("利用を継続する");

        Map<String, String> values = deriveLogic.compute(input, false);

        assertThat(values.get("EXISTING_CONTRACT_FLAG")).isEqualTo("無");
        assertThat(values.get("CANCEL_INTENTION")).isEmpty();
    }

    @Test
    void existingContractFlagIsAriWhenExistingContract() {
        ApplicationFormInput input = new ApplicationFormInput();
        input.setPaygateContinuationStatus("利用を継続する");

        Map<String, String> values = deriveLogic.compute(input, true);

        assertThat(values.get("EXISTING_CONTRACT_FLAG")).isEqualTo("有");
    }

    @Test
    void cancelIntentionIsDetectedWhenPaygateContinuationStatusIndicatesStop() {
        ApplicationFormInput input = new ApplicationFormInput();
        input.setPaygateContinuationStatus("利用を停止する");

        Map<String, String> values = deriveLogic.compute(input, true);

        assertThat(values.get("CANCEL_INTENTION")).isEqualTo("解約意思あり");
    }

    @Test
    void setsSystemDateToToday() {
        ApplicationFormInput input = new ApplicationFormInput();

        Map<String, String> values = deriveLogic.compute(input, false);

        assertThat(values.get("SYSTEM_DATE_SLASH")).isEqualTo(LocalDate.now().format(FMT_SLASH));
    }

    @Test
    void setsDefaultStoreCountAndRepresentativeStoreFlag() {
        ApplicationFormInput input = new ApplicationFormInput();

        Map<String, String> values = deriveLogic.compute(input, false);

        assertThat(values.get("DEFAULT_STORE_COUNT_1")).isEqualTo("1");
        assertThat(values.get("DEFAULT_REPRESENTATIVE_STORE_FLAG")).isEqualTo("有");
    }

}
