package com.cupit.service.applicationform;

import java.util.List;

/**
 * 出力Excelの1セルをどこから埋めるかを表すマッピング1件分。
 * 各決済会社所定申込フォーム作成の3種類（JCB／SMCC加盟店申込書／SMCC店舗情報一覧）の
 * テンプレート実列に対して、値の取得元（sourceType／source）と変換方法（transform）を持つ。
 */
public class ApplicationFormFieldMapping {

    public enum SourceType {
        MEMBER_INFO,
        INPUT,
        PAYGATE,
        DERIVE,
        SYSTEM,
        MANUAL,
        PROTECTED,
    }

    public enum Transform {
        DIRECT,
        CONCAT_ADDRESS,
        CONCAT_ADDRESS_KANA,
        CONCAT_NAME,
        CONCAT_NAME_KANA,
        CONCAT_CORP_NAME,
        CONCAT_CORP_NAME_KANA,
        CONCAT_CORP_ADDRESS,
        CONCAT_CORP_ADDRESS_KANA,
        CONCAT_REP_ADDRESS,
        HYPHEN_STRIP,
        DIVIDE_10000,
        DATE_8_TO_SLASH,
        CODE_MGMT_TYPE,
        CODE_MGMT_TYPE_NUMERIC,
        TRUNCATE23_WITH_FALLBACK,
        CONCAT_INDUSTRY,
        CORP_OR_STORE_NAME,
        ZIP_CORP_OR_STORE,
        STORE_FOUNDED_DATE_8,
        NONE,
        MANUAL,
    }

    private final int excelCol;
    private final SourceType sourceType;
    private final List<String> source;
    private final Transform transform;

    public ApplicationFormFieldMapping(
            int excelCol, SourceType sourceType, List<String> source, Transform transform) {
        this.excelCol = excelCol;
        this.sourceType = sourceType;
        this.source = source;
        this.transform = transform;
    }

    public int getExcelCol() {
        return excelCol;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public List<String> getSource() {
        return source;
    }

    public Transform getTransform() {
        return transform;
    }

}
