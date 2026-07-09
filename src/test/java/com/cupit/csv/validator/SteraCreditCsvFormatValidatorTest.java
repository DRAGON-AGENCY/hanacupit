package com.cupit.csv.validator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cupit.csv.CsvValidationError;
import com.cupit.csv.CsvValidationResult;
import com.cupit.testsupport.CsvFiles;

/**
 * {@link SteraCreditCsvFormatValidator} のテスト。18列固定のフォーマット検証を確認する。
 * INPUTファイルは Shift-JIS だが、検証本文は列数・数値列（請求金額・利用元金額）に絞られ、
 * ヘッダー列名・ファイル名の表記揺れは検証対象外である点も確認する。
 */
class SteraCreditCsvFormatValidatorTest {

    private static final String HEADER =
            "利用加盟店番号,送付日,取扱区分,取扱区分２,利用会員番号,利用日,金額符号,請求金額,"
            + "利用元金額,承認番号,CAT(POS)端末番号,異動データ識別,屋号,ブランド名称,"
            + "端末処理通番,サマリ件数,ＲＷ－ＩＤ,代表加盟店番号";
    private static final String VALID_ROW =
            "12348894,20251103,１回払,,4***-****-****-6426,20251103,0,27500,27500,"
            + "0847146,71134-620-36114,,花キューピット　村松花店,ＶＭ,00050,,,68473628";

    private SteraCreditCsvFormatValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SteraCreditCsvFormatValidator();
    }

    @Test
    void validatesRealSampleFileAsValid() throws Exception {
        CsvValidationResult result =
                validator.validate(CsvFiles.fromClasspath("stera_credit_valid.csv"));

        assertThat(result.isFatal()).isFalse();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getTotalRowCount()).isEqualTo(2);
    }

    @Test
    void passesWhenHeaderNamesDifferButColumnCountIs18() throws Exception {
        String genericHeader =
                "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r";
        CsvValidationResult result = validator.validate(CsvFiles.ms932("x.csv", genericHeader, VALID_ROW));

        assertThat(result.isFatal()).isFalse();
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void marksFatalWhenExtensionIsNotCsv() throws Exception {
        CsvValidationResult result = validator.validate(CsvFiles.ms932("x.dat", HEADER));

        assertThat(result.isFatal()).isTrue();
        assertThat(result.getErrors().get(0).getMessage()).contains("拡張子が不正");
    }

    @Test
    void marksFatalWhenHeaderColumnCountIsWrong() throws Exception {
        CsvValidationResult result = validator.validate(CsvFiles.ms932("x.csv", "利用加盟店番号,送付日"));

        assertThat(result.isFatal()).isTrue();
        assertThat(result.getTotalRowCount()).isZero();
    }

    @Test
    void reportsNumericErrorForBillingAmount() throws Exception {
        String row =
                "12348894,20251103,１回払,,,20251103,0,ABC,27500,"
                + "0847146,71134-620-36114,,店舗,ＶＭ,00050,,,68473628";
        CsvValidationResult result = validator.validate(CsvFiles.ms932("x.csv", HEADER, row));

        assertThat(hasNumericErrorFor(result, "請求金額")).isTrue();
    }

    @Test
    void reportsNumericErrorForOriginalAmount() throws Exception {
        String row =
                "12348894,20251103,１回払,,,20251103,0,27500,XYZ,"
                + "0847146,71134-620-36114,,店舗,ＶＭ,00050,,,68473628";
        CsvValidationResult result = validator.validate(CsvFiles.ms932("x.csv", HEADER, row));

        assertThat(hasNumericErrorFor(result, "利用元金額")).isTrue();
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
