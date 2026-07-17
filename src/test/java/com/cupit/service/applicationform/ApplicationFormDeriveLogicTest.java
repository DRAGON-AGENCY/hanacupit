package com.cupit.service.applicationform;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.cupit.model.ApplicationFormInput;

/**
 * {@link ApplicationFormDeriveLogic} のテスト。新規/変更/解約フラグの自動算出
 * （既存契約有無とPAYGATEの継続利用の値による判定）、システム日付、既定値等を検証する。
 */
class ApplicationFormDeriveLogicTest {

    private static final DateTimeFormatter FMT_SLASH = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final ApplicationFormDeriveLogic deriveLogic = new ApplicationFormDeriveLogic();

    @Test
    void newApplicationWhenNoExistingContractAndNoCancelIntent() {
        ApplicationFormInput input = new ApplicationFormInput();
        input.setPaygateContinuationStatus("利用を継続する");

        Map<String, String> values = deriveLogic.compute(input, false);

        assertThat(values.get("NEW_CHANGE_CANCEL_FLAG_JCB")).isEqualTo("新規");
        assertThat(values.get("NEW_CHANGE_CANCEL_FLAG_SMCC")).isEqualTo("1：新規加盟店");
        assertThat(values.get("EXISTING_CONTRACT_FLAG")).isEqualTo("無");
        assertThat(values.get("CANCEL_INTENTION")).isEmpty();
    }

    @Test
    void changeApplicationWhenExistingContractAndNoCancelIntent() {
        ApplicationFormInput input = new ApplicationFormInput();
        input.setPaygateContinuationStatus("利用を継続する");

        Map<String, String> values = deriveLogic.compute(input, true);

        assertThat(values.get("NEW_CHANGE_CANCEL_FLAG_JCB")).isEqualTo("変更");
        assertThat(values.get("NEW_CHANGE_CANCEL_FLAG_SMCC")).isEqualTo("4：加盟店情報変更");
        assertThat(values.get("EXISTING_CONTRACT_FLAG")).isEqualTo("有");
    }

    @Test
    void cancelApplicationWhenPaygateContinuationStatusIndicatesStop() {
        ApplicationFormInput input = new ApplicationFormInput();
        input.setPaygateContinuationStatus("利用を停止する");

        Map<String, String> values = deriveLogic.compute(input, true);

        assertThat(values.get("NEW_CHANGE_CANCEL_FLAG_JCB")).isEqualTo("解約");
        assertThat(values.get("NEW_CHANGE_CANCEL_FLAG_SMCC")).isEqualTo("5：解約");
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

    @Test
    void passesThroughStatusForCancelStatus() {
        ApplicationFormInput input = new ApplicationFormInput();
        input.setStatus("未処理");

        Map<String, String> values = deriveLogic.compute(input, false);

        assertThat(values.get("CANCEL_STATUS")).isEqualTo("未処理");
    }

}
