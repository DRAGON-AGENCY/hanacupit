package com.cupit.service.applicationform;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.cupit.model.ApplicationFormInput;
import com.cupit.model.MemberInfo;
import com.cupit.model.PaygateStoreMapping;
import com.cupit.service.applicationform.ApplicationFormFieldMapping.SourceType;
import com.cupit.service.applicationform.ApplicationFormFieldMapping.Transform;

/**
 * {@link ApplicationFormFieldResolver} のテスト。各決済会社所定申込フォーム作成の
 * 出力Excel1セルの値解決ロジック（MEMBER_INFO/INPUT/PAYGATE/DERIVEの参照解決と
 * 各種変換）を検証する。
 */
class ApplicationFormFieldResolverTest {

    private final ApplicationFormFieldResolver resolver = new ApplicationFormFieldResolver();

    @Test
    void resolvesDirectMemberInfoValue() {
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setStoreName("フラワーショップやざき");
        ApplicationFormRowContext ctx = context(new ApplicationFormInput(), memberInfo, null);

        String value = resolver.resolve(mapping(SourceType.MEMBER_INFO,
                List.of("store_name"), Transform.DIRECT), ctx);

        assertThat(value).isEqualTo("フラワーショップやざき");
    }

    @Test
    void returnsNullWhenMemberInfoMissing() {
        ApplicationFormRowContext ctx = context(new ApplicationFormInput(), null, null);

        String value = resolver.resolve(mapping(SourceType.MEMBER_INFO,
                List.of("store_name"), Transform.DIRECT), ctx);

        assertThat(value).isNull();
    }

    @Test
    void resolvesDirectInputValue() {
        ApplicationFormInput input = new ApplicationFormInput();
        input.setStoreNameAlphabet("FLOWER SHOP YAZAKI");
        ApplicationFormRowContext ctx = context(input, null, null);

        String value = resolver.resolve(mapping(SourceType.INPUT,
                List.of("store_name_alphabet"), Transform.DIRECT), ctx);

        assertThat(value).isEqualTo("FLOWER SHOP YAZAKI");
    }

    @Test
    void resolvesPaygateValue() {
        PaygateStoreMapping paygate = new PaygateStoreMapping();
        paygate.setJcbMerchantNo("1234567890");
        ApplicationFormRowContext ctx = context(new ApplicationFormInput(), null, paygate);

        String value = resolver.resolve(mapping(SourceType.PAYGATE,
                List.of("jcb_merchant_no"), Transform.DIRECT), ctx);

        assertThat(value).isEqualTo("1234567890");
    }

    @Test
    void resolvesDerivedValue() {
        ApplicationFormRowContext ctx = new ApplicationFormRowContext(
                new ApplicationFormInput(), null, null,
                Map.of("SYSTEM_DATE_SLASH", "2026/07/17"), 1);

        String value = resolver.resolve(mapping(SourceType.DERIVE,
                List.of("SYSTEM_DATE_SLASH"), Transform.DIRECT), ctx);

        assertThat(value).isEqualTo("2026/07/17");
    }

    @Test
    void returnsNullForManualAndProtected() {
        ApplicationFormRowContext ctx = context(new ApplicationFormInput(), null, null);

        assertThat(resolver.resolve(mapping(SourceType.MANUAL, List.of(), Transform.NONE), ctx))
                .isNull();
        assertThat(resolver.resolve(mapping(SourceType.PROTECTED, List.of(), Transform.NONE), ctx))
                .isNull();
    }

    @Test
    void concatenatesAddressComponentsWithoutSeparator() {
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setAddrPref("神奈川県");
        memberInfo.setAddrCity("鎌倉市");
        memberInfo.setAddrTown("梶原");
        memberInfo.setAddrBlock("1-20-7");
        ApplicationFormRowContext ctx = context(new ApplicationFormInput(), memberInfo, null);

        String value = resolver.resolve(mapping(SourceType.MEMBER_INFO,
                List.of("addr_pref", "addr_city", "addr_town", "addr_block", "addr_building"),
                Transform.CONCAT_ADDRESS), ctx);

        assertThat(value).isEqualTo("神奈川県鎌倉市梶原1-20-7");
    }

    @Test
    void concatenatesNameWithSpace() {
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setRepLastName("花");
        memberInfo.setRepFirstName("花子");
        ApplicationFormRowContext ctx = context(new ApplicationFormInput(), memberInfo, null);

        String value = resolver.resolve(mapping(SourceType.MEMBER_INFO,
                List.of("rep_last_name", "rep_first_name"), Transform.CONCAT_NAME), ctx);

        assertThat(value).isEqualTo("花 花子");
    }

    @Test
    void stripsHyphenFromZip() {
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setCorpZip("070-0032");
        ApplicationFormRowContext ctx = context(new ApplicationFormInput(), memberInfo, null);

        String value = resolver.resolve(mapping(SourceType.MEMBER_INFO,
                List.of("corp_zip"), Transform.HYPHEN_STRIP), ctx);

        assertThat(value).isEqualTo("0700032");
    }

    @Test
    void dividesAmountBy10000() {
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setStoreAnnualSalesYen(30_000_000L);
        ApplicationFormRowContext ctx = context(new ApplicationFormInput(), memberInfo, null);

        String value = resolver.resolve(mapping(SourceType.MEMBER_INFO,
                List.of("store_annual_sales_yen"), Transform.DIVIDE_10000), ctx);

        assertThat(value).isEqualTo("3000");
    }

    @Test
    void convertsMgmtTypeToJcbCode() {
        MemberInfo corp = new MemberInfo();
        corp.setMgmtType("株式会社");
        MemberInfo individual = new MemberInfo();
        individual.setMgmtType("個人");

        String corpValue = resolver.resolve(
                mapping(SourceType.MEMBER_INFO, List.of("mgmt_type"), Transform.CODE_MGMT_TYPE),
                context(new ApplicationFormInput(), corp, null));
        String individualValue = resolver.resolve(
                mapping(SourceType.MEMBER_INFO, List.of("mgmt_type"), Transform.CODE_MGMT_TYPE),
                context(new ApplicationFormInput(), individual, null));

        assertThat(corpValue).isEqualTo("01");
        assertThat(individualValue).isEqualTo("02");
    }

    @Test
    void truncatesKanaTo23CharsWhenShortNameMissing() {
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setStoreNameKana("アイウエオカキクケコサシスセソタチツテトナニヌネノ");
        ApplicationFormRowContext ctx = context(new ApplicationFormInput(), memberInfo, null);

        String value = resolver.resolve(mapping(SourceType.MEMBER_INFO,
                List.of("store_name_kana_short", "store_name_kana"),
                Transform.TRUNCATE23_WITH_FALLBACK), ctx);

        assertThat(value).hasSize(23);
        assertThat(value).isEqualTo("アイウエオカキクケコサシスセソタチツテトナニヌ");
    }

    @Test
    void usesShortNameWhenPresentForTruncate23() {
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setStoreNameKanaShort("ハナキュ");
        memberInfo.setStoreNameKana("アイウエオカキクケコサシスセソタチツテトナニヌネノ");
        ApplicationFormRowContext ctx = context(new ApplicationFormInput(), memberInfo, null);

        String value = resolver.resolve(mapping(SourceType.MEMBER_INFO,
                List.of("store_name_kana_short", "store_name_kana"),
                Transform.TRUNCATE23_WITH_FALLBACK), ctx);

        assertThat(value).isEqualTo("ハナキュ");
    }

    @Test
    void resolvesRowSequenceForSystemSource() {
        ApplicationFormRowContext ctx = new ApplicationFormRowContext(
                new ApplicationFormInput(), null, null, Map.of(), 3);

        String value = resolver.resolve(mapping(SourceType.SYSTEM,
                List.of("ROW_SEQUENCE"), Transform.DIRECT), ctx);

        assertThat(value).isEqualTo("3");
    }

    @Test
    void returnsNullForSystemSkipTag() {
        ApplicationFormRowContext ctx = context(new ApplicationFormInput(), null, null);

        String value = resolver.resolve(mapping(SourceType.SYSTEM,
                List.of("SKIP"), Transform.DIRECT), ctx);

        assertThat(value).isNull();
    }

    @Test
    void convertsMgmtTypeToSmccNumericCode() {
        MemberInfo corp = new MemberInfo();
        corp.setMgmtType("株式会社");
        MemberInfo individual = new MemberInfo();
        individual.setMgmtType("個人");

        String corpValue = resolver.resolve(
                mapping(SourceType.MEMBER_INFO, List.of("mgmt_type"), Transform.CODE_MGMT_TYPE_NUMERIC),
                context(new ApplicationFormInput(), corp, null));
        String individualValue = resolver.resolve(
                mapping(SourceType.MEMBER_INFO, List.of("mgmt_type"), Transform.CODE_MGMT_TYPE_NUMERIC),
                context(new ApplicationFormInput(), individual, null));

        assertThat(corpValue).isEqualTo("1");
        assertThat(individualValue).isEqualTo("2");
    }

    @Test
    void convertsDate8ToSlash() {
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setRepBirth("19600816");
        ApplicationFormRowContext ctx = context(new ApplicationFormInput(), memberInfo, null);

        String value = resolver.resolve(mapping(SourceType.MEMBER_INFO,
                List.of("rep_birth"), Transform.DATE_8_TO_SLASH), ctx);

        assertThat(value).isEqualTo("1960/08/16");
    }

    @Test
    void returnsRawValueWhenDate8ToSlashFailsToParse() {
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setRepBirth("not-a-date");
        ApplicationFormRowContext ctx = context(new ApplicationFormInput(), memberInfo, null);

        String value = resolver.resolve(mapping(SourceType.MEMBER_INFO,
                List.of("rep_birth"), Transform.DATE_8_TO_SLASH), ctx);

        assertThat(value).isEqualTo("not-a-date");
    }

    @Test
    void concatenatesIndustriesWithJapaneseComma() {
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setAppIndustry1("生花小売業");
        memberInfo.setAppIndustry2("");
        memberInfo.setAppIndustry3("宅配サービス業");
        ApplicationFormRowContext ctx = context(new ApplicationFormInput(), memberInfo, null);

        String value = resolver.resolve(mapping(SourceType.MEMBER_INFO,
                List.of("app_industry_1", "app_industry_2", "app_industry_3"),
                Transform.CONCAT_INDUSTRY), ctx);

        assertThat(value).isEqualTo("生花小売業、宅配サービス業");
    }

    @Test
    void prefersCorpNameOverStoreNameForCorpOrStoreName() {
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setCorpName("有限会社赤坂生花店");
        memberInfo.setStoreName("赤坂生花店");
        ApplicationFormRowContext ctx = context(new ApplicationFormInput(), memberInfo, null);

        String value = resolver.resolve(mapping(SourceType.MEMBER_INFO,
                List.of("corp_name", "store_name"), Transform.CORP_OR_STORE_NAME), ctx);

        assertThat(value).isEqualTo("有限会社赤坂生花店");
    }

    @Test
    void fallsBackToStoreNameWhenCorpNameBlankForCorpOrStoreName() {
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setCorpName("");
        memberInfo.setStoreName("赤坂生花店");
        ApplicationFormRowContext ctx = context(new ApplicationFormInput(), memberInfo, null);

        String value = resolver.resolve(mapping(SourceType.MEMBER_INFO,
                List.of("corp_name", "store_name"), Transform.CORP_OR_STORE_NAME), ctx);

        assertThat(value).isEqualTo("赤坂生花店");
    }

    @Test
    void prefersCorpZipOverStoreZipForZipCorpOrStore() {
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setCorpZip("070-0032");
        memberInfo.setAddrZip("1234567");
        ApplicationFormRowContext ctx = context(new ApplicationFormInput(), memberInfo, null);

        String value = resolver.resolve(mapping(SourceType.MEMBER_INFO,
                List.of("corp_zip", "addr_zip"), Transform.ZIP_CORP_OR_STORE), ctx);

        assertThat(value).isEqualTo("0700032");
    }

    @Test
    void fallsBackToStoreZipWhenCorpZipBlankForZipCorpOrStore() {
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setCorpZip("");
        memberInfo.setAddrZip("1234567");
        ApplicationFormRowContext ctx = context(new ApplicationFormInput(), memberInfo, null);

        String value = resolver.resolve(mapping(SourceType.MEMBER_INFO,
                List.of("corp_zip", "addr_zip"), Transform.ZIP_CORP_OR_STORE), ctx);

        assertThat(value).isEqualTo("1234567");
    }

    @Test
    void prefersParentFoundedDateOverStoreFoundedDate8() {
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setStoreFoundedDate(LocalDate.of(1960, 8, 16));
        memberInfo.setParentFoundedDate("19550101");
        ApplicationFormRowContext ctx = context(new ApplicationFormInput(), memberInfo, null);

        String value = resolver.resolve(mapping(SourceType.MEMBER_INFO,
                List.of("store_founded_date", "parent_founded_date"),
                Transform.STORE_FOUNDED_DATE_8), ctx);

        assertThat(value).isEqualTo("19550101");
    }

    @Test
    void convertsStoreFoundedDateSlashTo8WhenParentBlank() {
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setStoreFoundedDate(LocalDate.of(1960, 8, 16));
        memberInfo.setParentFoundedDate("");
        ApplicationFormRowContext ctx = context(new ApplicationFormInput(), memberInfo, null);

        String value = resolver.resolve(mapping(SourceType.MEMBER_INFO,
                List.of("store_founded_date", "parent_founded_date"),
                Transform.STORE_FOUNDED_DATE_8), ctx);

        assertThat(value).isEqualTo("19600816");
    }

    private ApplicationFormFieldMapping mapping(
            SourceType sourceType, List<String> source, Transform transform) {
        return new ApplicationFormFieldMapping(2, sourceType, source, transform);
    }

    private ApplicationFormRowContext context(
            ApplicationFormInput input, MemberInfo memberInfo, PaygateStoreMapping paygate) {
        return new ApplicationFormRowContext(input, memberInfo, paygate, Map.of(), 1);
    }

}
