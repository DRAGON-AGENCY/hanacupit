package com.cupit.csv.validator;

import java.util.List;

import org.springframework.stereotype.Component;

import com.cupit.csv.CsvValidationError;

/**
 * 加盟会員店マスターデータの各項目が、各決済会社所定申込フォーム（JCB・SMCC）の
 * 入力要件を満たしているかを検証する。将来の「各決済会社所定申込フォーム作成」機能で
 * このデータを転記する際にエラーにならないよう、登録・更新の入口で事前に検知する。
 *
 * 対象は新規申込シナリオ（JCB「01：新規出店」・SMCC steracode「1：新規加盟店」）に
 * 限定し、機械的に判定できるルール（必須・最大/固定文字数）のみを対象とする。
 * 都道府県から入力必須・ハイフン2つ以上必要 等の自由記述の業務ルールは対象外。
 * 決済会社間で最大文字数が異なる項目は、最も厳しい（小さい）桁数を採用する
 * （それを満たせば全社の申込フォームへ転記可能なため）。半角/全角の要求が
 * 資料間で矛盾する項目（店名カナ等）は文字幅チェックを行わない。
 *
 * m_member_infoに対応する列が存在しない項目（店舗名アルファベット・口座情報一式・
 * 法人番号・法人設立年月日・法人電話番号・代表者電話番号・カード情報保持状況等の
 * 決済契約固有情報）は、機械的にチェック不可能なため対象外。
 *
 * 法人所在地（本社所在地）はSMCC steracode仕様書「企業情報」セクションに列単位の
 * 桁数定義があるため、所在地（店舗住所）と同様に必須・桁数チェックを行う
 * （建物名・建物名カナのみ任意）。代表者住所も同セクション「代表者情報」に列単位の
 * 定義があるが、SMCC上は任意項目のため桁数チェックのみとし、必須化はしない
 * （JCBは個人事業主の場合に条件付必須だが、m_member_info.mgmt_typeは
 * 「有限」「株式会社」等の自由記述で、個人/法人の判定には
 * ApplicationFormFieldResolver#codeMgmtTypeと同じ「値に"個人"を含むか」という
 * ヒューリスティックが必要になり、このチェッカーでは条件分岐を持たせない方針とした）。
 * 経営区分（mgmt_type）はDB列がVARCHAR(20)のため、桁数超過によるDB例外を防ぐ目的で
 * 任意項目として桁数チェックのみ行う（決済会社資料に直接対応する項目ではない）。
 */
@Component
public class PaymentCompanyFormatChecker {

    private static final int IDX_MGMT_TYPE = 57;
    private static final int IDX_STORE_NAME = 13;
    private static final int IDX_STORE_NAME_KANA = 14;
    private static final int IDX_ADDR_ZIP = 20;
    private static final int IDX_ADDR_PREF = 21;
    private static final int IDX_ADDR_PREF_KANA = 22;
    private static final int IDX_ADDR_CITY = 23;
    private static final int IDX_ADDR_CITY_KANA = 24;
    private static final int IDX_ADDR_TOWN = 25;
    private static final int IDX_ADDR_TOWN_KANA = 26;
    private static final int IDX_ADDR_BLOCK = 27;
    private static final int IDX_ADDR_BLOCK_KANA = 28;
    private static final int IDX_ADDR_BUILDING = 29;
    private static final int IDX_ADDR_BUILDING_KANA = 30;
    private static final int IDX_ADDR_TEL = 31;
    private static final int IDX_CORP_NAME = 59;
    private static final int IDX_CORP_NAME_KANA = 61;
    private static final int IDX_CORP_ZIP = 62;
    private static final int IDX_CORP_PREF = 63;
    private static final int IDX_CORP_PREF_KANA = 64;
    private static final int IDX_CORP_CITY = 65;
    private static final int IDX_CORP_CITY_KANA = 66;
    private static final int IDX_CORP_TOWN = 67;
    private static final int IDX_CORP_TOWN_KANA = 68;
    private static final int IDX_CORP_BLOCK = 69;
    private static final int IDX_CORP_BLOCK_KANA = 70;
    private static final int IDX_CORP_BUILDING = 71;
    private static final int IDX_CORP_BUILDING_KANA = 72;
    private static final int IDX_REP_LAST_NAME_KANA = 73;
    private static final int IDX_REP_FIRST_NAME_KANA = 74;
    private static final int IDX_REP_LAST_NAME = 75;
    private static final int IDX_REP_FIRST_NAME = 76;
    private static final int IDX_REP_BIRTH = 77;
    private static final int IDX_REP_ZIP = 79;
    private static final int IDX_REP_PREF = 80;
    private static final int IDX_REP_PREF_KANA = 81;
    private static final int IDX_REP_CITY = 82;
    private static final int IDX_REP_CITY_KANA = 83;
    private static final int IDX_REP_TOWN = 84;
    private static final int IDX_REP_TOWN_KANA = 85;
    private static final int IDX_REP_BLOCK = 86;
    private static final int IDX_REP_BLOCK_KANA = 87;
    private static final int IDX_REP_BUILDING = 88;
    private static final int IDX_REP_BUILDING_KANA = 89;
    private static final int IDX_HANDLING_ITEMS = 41;

    /**
     * 1データ行分の項目を検証し、違反があれば errors に追加する。
     *
     * @param fields CSVの1行分のフィールド（255列）
     * @param rowNum 行番号（エラーメッセージ用）
     * @param errors 検出したエラーを追加する先
     */
    public void check(List<String> fields, int rowNum, List<CsvValidationError> errors) {
        checkOptionalMaxLength(fields, rowNum, errors, IDX_MGMT_TYPE, "経営区分", 20,
                "システムの登録上限（20桁）");
        checkRequiredMaxLength(fields, rowNum, errors, IDX_STORE_NAME, "店舗名", 16,
                "JCB仕様書 最大20桁 / SMCC steracode仕様書 最大16桁 / SMCC店舗情報一覧 最大20桁");
        checkRequiredMaxLength(fields, rowNum, errors, IDX_STORE_NAME_KANA, "店名カナ", 23,
                "JCB仕様書 最大30桁 / SMCC steracode仕様書 最大23桁");
        checkFixedLength(fields, rowNum, errors, IDX_ADDR_ZIP, "所在地郵便番号", 7,
                "JCB・SMCC steracode・SMCC店舗情報一覧 いずれも7桁固定（ハイフン無し）が仕様");
        checkRequiredMaxLength(fields, rowNum, errors, IDX_ADDR_PREF, "所在地都道府県", 4,
                "SMCC steracode仕様書 最大4桁");
        checkRequiredMaxLength(fields, rowNum, errors, IDX_ADDR_CITY, "所在地市区町村", 20,
                "SMCC steracode仕様書 最大20桁");
        checkRequiredMaxLength(fields, rowNum, errors, IDX_ADDR_TOWN, "所在地町名", 30,
                "SMCC steracode仕様書 最大30桁");
        checkRequiredMaxLength(fields, rowNum, errors, IDX_ADDR_BLOCK, "所在地丁目・番・番地・号", 30,
                "SMCC steracode仕様書 最大30桁");
        checkOptionalMaxLength(fields, rowNum, errors, IDX_ADDR_BUILDING, "所在地建物名・部屋番号", 30,
                "SMCC steracode仕様書 最大30桁");
        checkRequiredMaxLength(fields, rowNum, errors, IDX_ADDR_PREF_KANA, "所在地都道府県カナ", 10,
                "SMCC steracode仕様書 最大10桁");
        checkRequiredMaxLength(fields, rowNum, errors, IDX_ADDR_CITY_KANA, "所在地市区町村カナ", 20,
                "SMCC steracode仕様書 最大20桁");
        checkRequiredMaxLength(fields, rowNum, errors, IDX_ADDR_TOWN_KANA, "所在地町名カナ", 30,
                "SMCC steracode仕様書 最大30桁");
        checkRequiredMaxLength(fields, rowNum, errors, IDX_ADDR_BLOCK_KANA, "所在地丁目・番・番地・号カナ", 30,
                "SMCC steracode仕様書 最大30桁");
        checkOptionalMaxLength(fields, rowNum, errors, IDX_ADDR_BUILDING_KANA, "所在地建物名・部屋番号カナ", 50,
                "SMCC steracode仕様書 最大50桁");
        checkRequiredMaxLength(fields, rowNum, errors, IDX_ADDR_TEL, "所在地電話番号", 13,
                "SMCC steracode仕様書 最大13桁");
        checkPairedMaxLength(fields, rowNum, errors,
                IDX_CORP_NAME, "法人名", 20, IDX_CORP_NAME_KANA, "法人名カナ", 30,
                "JCB仕様書「会社名」最大20桁・「会社名（カナ）」最大30桁（セットで入力必須）");
        checkFixedLength(fields, rowNum, errors, IDX_CORP_ZIP, "法人郵便番号", 7,
                "SMCC steracode仕様書「企業情報」郵便番号 7桁固定必須 / JCB仕様書「本社所在地（郵便番号）」7桁固定必須");
        checkRequiredMaxLength(fields, rowNum, errors, IDX_CORP_PREF, "法人所在地都道府県", 4,
                "SMCC steracode仕様書「企業情報」住所：都道府県 最大4桁必須");
        checkRequiredMaxLength(fields, rowNum, errors, IDX_CORP_CITY, "法人所在地市区町村", 20,
                "SMCC steracode仕様書「企業情報」住所：市区町村 最大20桁必須");
        checkRequiredMaxLength(fields, rowNum, errors, IDX_CORP_TOWN, "法人所在地町名", 30,
                "SMCC steracode仕様書「企業情報」住所：町名 最大30桁必須");
        checkRequiredMaxLength(fields, rowNum, errors, IDX_CORP_BLOCK, "法人所在地丁目・番・番地・号", 30,
                "SMCC steracode仕様書「企業情報」住所：丁目・番・番地・号 最大30桁必須");
        checkOptionalMaxLength(fields, rowNum, errors, IDX_CORP_BUILDING, "法人所在地建物名・部屋番号", 30,
                "SMCC steracode仕様書「企業情報」住所：建物名・部屋番号 最大30桁任意");
        checkRequiredMaxLength(fields, rowNum, errors, IDX_CORP_PREF_KANA, "法人所在地都道府県カナ", 10,
                "SMCC steracode仕様書「企業情報」住所：都道府県（カナ） 最大10桁必須");
        checkRequiredMaxLength(fields, rowNum, errors, IDX_CORP_CITY_KANA, "法人所在地市区町村カナ", 30,
                "SMCC steracode仕様書「企業情報」住所：市区町村（カナ） 最大30桁必須");
        checkRequiredMaxLength(fields, rowNum, errors, IDX_CORP_TOWN_KANA, "法人所在地町名カナ", 30,
                "SMCC steracode仕様書「企業情報」住所：町名（カナ） 最大30桁必須");
        checkRequiredMaxLength(fields, rowNum, errors, IDX_CORP_BLOCK_KANA, "法人所在地丁目・番・番地・号カナ", 30,
                "SMCC steracode仕様書「企業情報」住所：丁目・番・番地・号（カナ） 最大30桁必須");
        checkOptionalMaxLength(fields, rowNum, errors, IDX_CORP_BUILDING_KANA, "法人所在地建物名・部屋番号カナ", 50,
                "SMCC steracode仕様書「企業情報」住所：建物名・部屋番号（カナ） 最大50桁任意");
        checkRequiredMaxLength(fields, rowNum, errors, IDX_REP_LAST_NAME, "代表者姓", 10,
                "JCB仕様書「代表者氏名」最大20桁（姓・名で折半）");
        checkRequiredMaxLength(fields, rowNum, errors, IDX_REP_FIRST_NAME, "代表者名", 10,
                "JCB仕様書「代表者氏名」最大20桁（姓・名で折半）");
        checkRequiredMaxLength(fields, rowNum, errors, IDX_REP_LAST_NAME_KANA, "代表者姓カナ", 15,
                "JCB仕様書「代表者氏名（カナ）」最大30桁（姓・名で折半）");
        checkRequiredMaxLength(fields, rowNum, errors, IDX_REP_FIRST_NAME_KANA, "代表者名カナ", 15,
                "JCB仕様書「代表者氏名（カナ）」最大30桁（姓・名で折半）");
        checkFixedLength(fields, rowNum, errors, IDX_REP_BIRTH, "代表者生年月日", 8,
                "JCB・SMCC steracode いずれもYYYYMMDD形式8桁固定が仕様");
        checkOptionalFixedLength(fields, rowNum, errors, IDX_REP_ZIP, "代表者郵便番号", 7,
                "SMCC steracode仕様書「代表者情報」郵便番号 任意・7桁固定");
        checkOptionalMaxLength(fields, rowNum, errors, IDX_REP_PREF, "代表者住所：都道府県", 4,
                "SMCC steracode仕様書「代表者情報」住所：都道府県 最大4桁任意");
        checkOptionalMaxLength(fields, rowNum, errors, IDX_REP_CITY, "代表者住所：市区町村", 20,
                "SMCC steracode仕様書「代表者情報」住所：市区町村 最大20桁任意");
        checkOptionalMaxLength(fields, rowNum, errors, IDX_REP_TOWN, "代表者住所：町名", 30,
                "SMCC steracode仕様書「代表者情報」住所：町名 最大30桁任意");
        checkOptionalMaxLength(fields, rowNum, errors, IDX_REP_BLOCK, "代表者住所：丁目・番・番地・号", 30,
                "SMCC steracode仕様書「代表者情報」住所：丁目・番・番地・号 最大30桁任意");
        checkOptionalMaxLength(fields, rowNum, errors, IDX_REP_BUILDING, "代表者住所：建物名・部屋番号", 30,
                "SMCC steracode仕様書「代表者情報」住所：建物名・部屋番号 最大30桁任意");
        checkOptionalMaxLength(fields, rowNum, errors, IDX_REP_PREF_KANA, "代表者住所：都道府県カナ", 10,
                "SMCC steracode仕様書「代表者情報」住所：都道府県（カナ） 最大10桁任意");
        checkOptionalMaxLength(fields, rowNum, errors, IDX_REP_CITY_KANA, "代表者住所：市区町村カナ", 30,
                "SMCC steracode仕様書「代表者情報」住所：市区町村（カナ） 最大30桁任意");
        checkOptionalMaxLength(fields, rowNum, errors, IDX_REP_TOWN_KANA, "代表者住所：町名カナ", 30,
                "SMCC steracode仕様書「代表者情報」住所：町名（カナ） 最大30桁任意");
        checkOptionalMaxLength(fields, rowNum, errors, IDX_REP_BLOCK_KANA, "代表者住所：丁目・番・番地・号カナ", 30,
                "SMCC steracode仕様書「代表者情報」住所：丁目・番・番地・号（カナ） 最大30桁任意");
        checkOptionalMaxLength(fields, rowNum, errors, IDX_REP_BUILDING_KANA, "代表者住所：建物名・部屋番号カナ", 50,
                "SMCC steracode仕様書「代表者情報」住所：建物名・部屋番号（カナ） 最大50桁任意");
        checkRequiredMaxLength(fields, rowNum, errors, IDX_HANDLING_ITEMS, "取扱品目", 30,
                "JCB仕様書「取扱商品」が必須項目 / SMCC steracode仕様書「店舗詳細」商材 最大30桁必須");
    }

    private void checkRequiredMaxLength(
            List<String> fields, int rowNum, List<CsvValidationError> errors,
            int index, String columnLabel, int maxLength, String basis) {
        String value = trim(fields.get(index));
        if (value.isEmpty()) {
            errors.add(new CsvValidationError(rowNum, columnLabel,
                    columnLabel + "は決済会社への申込に必須の項目です。（根拠: " + basis + "）"));
            return;
        }
        if (value.length() > maxLength) {
            errors.add(new CsvValidationError(rowNum, columnLabel,
                    columnLabel + "「" + value + "」: 決済会社の申込フォーマットの上限（" + maxLength
                            + "桁）を超えています。（根拠: " + basis + "）"));
        }
    }

    private void checkOptionalMaxLength(
            List<String> fields, int rowNum, List<CsvValidationError> errors,
            int index, String columnLabel, int maxLength, String basis) {
        String value = trim(fields.get(index));
        if (value.isEmpty()) {
            return;
        }
        if (value.length() > maxLength) {
            errors.add(new CsvValidationError(rowNum, columnLabel,
                    columnLabel + "「" + value + "」: 決済会社の申込フォーマットの上限（" + maxLength
                            + "桁）を超えています。（根拠: " + basis + "）"));
        }
    }

    private void checkFixedLength(
            List<String> fields, int rowNum, List<CsvValidationError> errors,
            int index, String columnLabel, int fixedLength, String basis) {
        String value = trim(fields.get(index));
        if (value.isEmpty()) {
            errors.add(new CsvValidationError(rowNum, columnLabel,
                    columnLabel + "は決済会社への申込に必須の項目です。（根拠: " + basis + "）"));
            return;
        }
        if (value.length() != fixedLength) {
            errors.add(new CsvValidationError(rowNum, columnLabel,
                    columnLabel + "「" + value + "」: 決済会社の申込フォーマットは" + fixedLength
                            + "桁固定です。（根拠: " + basis + "）"));
        }
    }

    private void checkOptionalFixedLength(
            List<String> fields, int rowNum, List<CsvValidationError> errors,
            int index, String columnLabel, int fixedLength, String basis) {
        String value = trim(fields.get(index));
        if (value.isEmpty()) {
            return;
        }
        if (value.length() != fixedLength) {
            errors.add(new CsvValidationError(rowNum, columnLabel,
                    columnLabel + "「" + value + "」: 決済会社の申込フォーマットは" + fixedLength
                            + "桁固定です。（根拠: " + basis + "）"));
        }
    }

    private void checkPairedMaxLength(
            List<String> fields, int rowNum, List<CsvValidationError> errors,
            int index1, String label1, int maxLength1,
            int index2, String label2, int maxLength2, String basis) {
        String value1 = trim(fields.get(index1));
        String value2 = trim(fields.get(index2));
        if (value1.isEmpty() && value2.isEmpty()) {
            return;
        }
        if (value1.isEmpty() || value2.isEmpty()) {
            errors.add(new CsvValidationError(rowNum, label1,
                    label1 + "と" + label2 + "はセットで入力が必要です。（根拠: " + basis + "）"));
            return;
        }
        if (value1.length() > maxLength1) {
            errors.add(new CsvValidationError(rowNum, label1,
                    label1 + "「" + value1 + "」: 決済会社の申込フォーマットの上限（" + maxLength1
                            + "桁）を超えています。（根拠: " + basis + "）"));
        }
        if (value2.length() > maxLength2) {
            errors.add(new CsvValidationError(rowNum, label2,
                    label2 + "「" + value2 + "」: 決済会社の申込フォーマットの上限（" + maxLength2
                            + "桁）を超えています。（根拠: " + basis + "）"));
        }
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }

}
