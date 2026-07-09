package com.cupit.csv.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cupit.csv.CsvValidationError;
import com.cupit.csv.CsvValidationResult;
import com.cupit.testsupport.CsvFiles;

/**
 * {@link SteraCodeCsvFormatValidator} のテスト。10列固定のフォーマット検証・
 * 致命的エラー（拡張子・空ファイル・ヘッダー列数）と、データ行単位の非致命エラー
 * （列数・数値変換）を検証する。ヘッダー列名は検証対象外である点も確認する。
 */
class SteraCodeCsvFormatValidatorTest {

    private static final String HEADER =
            "\"ブランド\",\"端末識別番号\",\"伝票番号\",\"決済年月日\",\"決済時間\","
            + "\"1:売上2:返品\",\"決済金額\",\"手数料金額\",\"収納金額\",\"サブウォレット名\"";

    private SteraCodeCsvFormatValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SteraCodeCsvFormatValidator();
    }

    @Test
    void validatesRealSampleFileAsValid() throws Exception {
        CsvValidationResult result = validator.validate(CsvFiles.fromClasspath("stera_code_valid.csv"));

        assertThat(result.isFatal()).isFalse();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getTotalRowCount()).isEqualTo(3);
    }

    @Test
    void passesWhenHeaderNamesDifferButColumnCountIs10() throws Exception {
        CsvValidationResult result = validator.validate(CsvFiles.utf8Bom("x.csv",
                "c1,c2,c3,c4,c5,c6,c7,c8,c9,c10",
                "楽天ペイ,7113462036751,03447,20251101,091102,1,5000,,,"));

        assertThat(result.isFatal()).isFalse();
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void marksFatalWhenExtensionIsNotCsv() throws Exception {
        CsvValidationResult result = validator.validate(CsvFiles.utf8Bom("x.txt", HEADER));

        assertThat(result.isFatal()).isTrue();
        assertThat(result.getErrors().get(0).getMessage()).contains("拡張子が不正");
    }

    @Test
    void marksFatalWhenFileIsEmpty() throws Exception {
        CsvValidationResult result = validator.validate(CsvFiles.ofBytes("x.csv", new byte[0]));

        assertThat(result.isFatal()).isTrue();
        assertThat(result.getErrors().get(0).getMessage()).contains("ファイルが空です");
    }

    @Test
    void marksFatalWhenHeaderColumnCountIsWrong() throws Exception {
        CsvValidationResult result = validator.validate(CsvFiles.utf8Bom("x.csv",
                "ブランド,端末識別番号,伝票番号"));

        assertThat(result.isFatal()).isTrue();
        assertThat(result.getTotalRowCount()).isZero();
    }

    @Test
    void reportsColumnCountErrorForDataRowWithoutMarkingFatal() throws Exception {
        CsvValidationResult result = validator.validate(CsvFiles.utf8Bom("x.csv",
                HEADER,
                "楽天ペイ,7113462036751,03447"));

        assertThat(result.isFatal()).isFalse();
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors().get(0).getMessage()).contains("列数が不正");
    }

    @Test
    void reportsNumericErrorForSalesReturnFlag() throws Exception {
        CsvValidationResult result = validator.validate(CsvFiles.utf8Bom("x.csv",
                HEADER,
                "楽天ペイ,7113462036751,03447,20251101,091102,X,5000,,,"));

        assertThat(hasNumericErrorFor(result, "1:売上2:返品")).isTrue();
    }

    @Test
    void reportsNumericErrorForSettlementAmount() throws Exception {
        CsvValidationResult result = validator.validate(CsvFiles.utf8Bom("x.csv",
                HEADER,
                "楽天ペイ,7113462036751,03447,20251101,091102,1,ABC,,,"));

        assertThat(hasNumericErrorFor(result, "決済金額")).isTrue();
    }

    @Test
    void skipsBlankLinesWithoutError() throws Exception {
        CsvValidationResult result = validator.validate(CsvFiles.utf8Bom("x.csv",
                HEADER,
                "",
                "楽天ペイ,7113462036751,03447,20251101,091102,1,5000,,,"));

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void throwsForUtf16File() {
        // UTF-16（先頭 FF FE）は detectCharset が非対応として例外にする
        byte[] utf16 = {(byte) 0xFF, (byte) 0xFE, 0x42, 0x00};
        assertThatThrownBy(() -> validator.validate(CsvFiles.ofBytes("x.csv", utf16)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UTF-16");
    }

    private boolean hasNumericErrorFor(CsvValidationResult result, String columnName) {
        for (CsvValidationError error : result.getErrors()) {
            if (columnName.equals(error.getColumnName())) {
                return true;
            }
        }
        return false;
    }
}
