package com.cupit.service.applicationform;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.stereotype.Component;

import com.cupit.model.MemberInfo;
import com.cupit.model.PaygateStoreMapping;
import com.cupit.service.applicationform.ApplicationFormFieldMapping.SourceType;
import com.cupit.service.applicationform.ApplicationFormFieldMapping.Transform;

/**
 * {@link ApplicationFormFieldMapping}1件と{@link ApplicationFormRowContext}から、
 * 出力Excelのセルに書き込む文字列値を1つ解決する。
 * 各決済会社所定申込フォーム作成の3種類の出力（JCB／SMCC加盟店申込書／SMCC店舗情報一覧）で
 * 共通して使うため、変換ロジックはここに集約する。
 */
@Component
public class ApplicationFormFieldResolver {

    private static final DateTimeFormatter FMT_SLASH = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter FMT_8 = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * セルに書き込む値を解決する。PROTECTED／SYSTEM(SKIP)／MANUALは常にnullを返す
     * （PROTECTEDは保護セルのため書き込み禁止、MANUALは申請担当者の手入力に委ねる）。
     */
    public String resolve(ApplicationFormFieldMapping mapping, ApplicationFormRowContext ctx) {
        SourceType type = mapping.getSourceType();
        if (type == SourceType.PROTECTED || type == SourceType.MANUAL) {
            return null;
        }
        if (type == SourceType.SYSTEM) {
            return resolveSystem(mapping, ctx);
        }
        if (type == SourceType.DERIVE) {
            return ctx.getDerivedValues().get(mapping.getSource().get(0));
        }
        List<String> values = mapping.getSource().stream()
                .map(physicalName -> resolveRaw(type, physicalName, ctx))
                .toList();
        return applyTransform(mapping.getTransform(), values);
    }

    private String resolveSystem(ApplicationFormFieldMapping mapping, ApplicationFormRowContext ctx) {
        String tag = mapping.getSource().get(0);
        if ("ROW_SEQUENCE".equals(tag)) {
            return String.valueOf(ctx.getRowSequence());
        }
        return null; // SKIP（重複チェック等、システム側の数式・機能に委ねるセル）
    }

    private String resolveRaw(SourceType type, String physicalName, ApplicationFormRowContext ctx) {
        Object value = switch (type) {
            case MEMBER_INFO -> resolveMemberInfo(physicalName, ctx.getMemberInfo());
            case INPUT -> ApplicationFormInputFieldAccessor.get(ctx.getInput(), physicalName);
            case PAYGATE -> resolvePaygate(physicalName, ctx.getPaygateStoreMapping());
            default -> null;
        };
        return toDisplayString(value);
    }

    private Object resolveMemberInfo(String physicalName, MemberInfo memberInfo) {
        return memberInfo == null ? null : MemberInfoFieldAccessor.get(memberInfo, physicalName);
    }

    private Object resolvePaygate(String physicalName, PaygateStoreMapping paygate) {
        return paygate == null ? null
                : PaygateStoreMappingFieldAccessor.get(paygate, physicalName);
    }

    private String toDisplayString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate date) {
            return date.format(FMT_SLASH);
        }
        return value.toString();
    }

    private String applyTransform(Transform transform, List<String> values) {
        return switch (transform) {
            case DIRECT, NONE, MANUAL -> values.isEmpty() ? null : values.get(0);
            case CONCAT_ADDRESS, CONCAT_ADDRESS_KANA, CONCAT_CORP_ADDRESS,
                    CONCAT_CORP_ADDRESS_KANA, CONCAT_REP_ADDRESS -> concatAddress(values);
            case CONCAT_NAME, CONCAT_NAME_KANA -> concatWithSpace(values);
            case CONCAT_CORP_NAME, CONCAT_CORP_NAME_KANA -> concatPlain(values);
            case HYPHEN_STRIP -> hyphenStrip(firstOrNull(values));
            case DIVIDE_10000 -> divide10000(firstOrNull(values));
            case DATE_8_TO_SLASH -> date8ToSlash(firstOrNull(values));
            case CODE_MGMT_TYPE -> codeMgmtType(firstOrNull(values));
            case CODE_MGMT_TYPE_NUMERIC -> codeMgmtTypeNumeric(firstOrNull(values));
            case TRUNCATE23_WITH_FALLBACK -> truncate23WithFallback(values);
            case CONCAT_INDUSTRY -> concatIndustry(values);
            case CORP_OR_STORE_NAME -> firstNonBlank(values);
            case ZIP_CORP_OR_STORE -> zipCorpOrStore(values);
            case STORE_FOUNDED_DATE_8 -> storeFoundedDate8(values);
        };
    }

    private String firstOrNull(List<String> values) {
        return values.isEmpty() ? null : values.get(0);
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String concatAddress(List<String> values) {
        StringBuilder sb = new StringBuilder();
        for (String v : values) {
            if (!isBlank(v)) {
                sb.append(v.trim());
            }
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    private String concatWithSpace(List<String> values) {
        StringBuilder sb = new StringBuilder();
        for (String v : values) {
            if (!isBlank(v)) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(v.trim());
            }
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    private String concatPlain(List<String> values) {
        StringBuilder sb = new StringBuilder();
        for (String v : values) {
            if (!isBlank(v)) {
                sb.append(v.trim());
            }
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    private String hyphenStrip(String value) {
        return isBlank(value) ? null : value.replace("-", "").trim();
    }

    private String divide10000(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            java.math.BigDecimal amount = new java.math.BigDecimal(value.trim());
            return amount.divide(java.math.BigDecimal.valueOf(10_000))
                    .stripTrailingZeros().toPlainString();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String date8ToSlash(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), FMT_8).format(FMT_SLASH);
        } catch (DateTimeParseException e) {
            return value.trim();
        }
    }

    private String codeMgmtType(String value) {
        if (isBlank(value)) {
            return null;
        }
        return value.contains("個人") ? "02" : "01";
    }

    private String codeMgmtTypeNumeric(String value) {
        if (isBlank(value)) {
            return null;
        }
        return value.contains("個人") ? "2" : "1";
    }

    private String truncate23WithFallback(List<String> values) {
        String shortName = values.size() > 0 ? values.get(0) : null;
        String fullName = values.size() > 1 ? values.get(1) : null;
        if (!isBlank(shortName)) {
            return shortName.trim();
        }
        if (isBlank(fullName)) {
            return null;
        }
        String trimmed = fullName.trim();
        return trimmed.length() <= 23 ? trimmed : trimmed.substring(0, 23);
    }

    private String concatIndustry(List<String> values) {
        StringBuilder sb = new StringBuilder();
        for (String v : values) {
            if (!isBlank(v)) {
                if (sb.length() > 0) {
                    sb.append('、');
                }
                sb.append(v.trim());
            }
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    private String firstNonBlank(List<String> values) {
        for (String v : values) {
            if (!isBlank(v)) {
                return v.trim();
            }
        }
        return null;
    }

    private String zipCorpOrStore(List<String> values) {
        String corpZip = values.size() > 0 ? values.get(0) : null;
        String storeZip = values.size() > 1 ? values.get(1) : null;
        if (!isBlank(corpZip)) {
            return corpZip.replace("-", "").trim();
        }
        return isBlank(storeZip) ? null : storeZip.trim();
    }

    private String storeFoundedDate8(List<String> values) {
        String storeFounded = values.size() > 0 ? values.get(0) : null;
        String parentFounded = values.size() > 1 ? values.get(1) : null;
        if (!isBlank(parentFounded)) {
            return parentFounded.trim();
        }
        if (isBlank(storeFounded)) {
            return null;
        }
        try {
            return LocalDate.parse(storeFounded.trim(), FMT_SLASH).format(FMT_8);
        } catch (DateTimeParseException e) {
            return storeFounded.trim();
        }
    }

}
