package com.cupit.service.applicationform;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import com.cupit.model.ApplicationFormInput;
import com.cupit.model.MemberInfo;

/**
 * {@link ApplicationFormSmccTenpoWriter} のテスト。外部テンプレート
 * （application_form_smcc_tenpo_template.xlsx）の「【連携シート】」シートのデータ行
 * （4行目〜）に値が書き込まれること、SYSTEM(ROW_SEQUENCE)による連番付与、
 * SYSTEM(SKIP)列が書き込まれないことを検証する。
 */
class ApplicationFormSmccTenpoWriterTest {

    private static final String TEMPLATE_DIR = "C:/work/20260401_花キューピット/09_帳票テンプレート";

    private static Row rowAt(Sheet sheet, int excelRow) {
        return sheet.getRow(excelRow - 1);
    }

    @Test
    void clearsSampleDataInRowsBeyondWrittenRecords() throws IOException {
        ApplicationFormSmccTenpoWriter writer = new ApplicationFormSmccTenpoWriter(
                TEMPLATE_DIR, new ApplicationFormFieldResolver());
        ApplicationFormRowContext ctx = new ApplicationFormRowContext(
                new ApplicationFormInput(), null, null, Map.of(), 1);

        byte[] bytes = writer.write(List.of(ctx));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("【連携シート】");
            // 1件しか書き込んでいないため5行目は書き込み対象外。マッピング対象列に
            // サンプルデータが残っていないことを検証する。
            Row row = rowAt(sheet, 5);
            Cell cell = row == null ? null : row.getCell(1);
            assertThat(cell == null || cell.getCellType() == CellType.BLANK).isTrue();
        }
    }

    @Test
    void writesRowSequenceToCol1() throws IOException {
        ApplicationFormSmccTenpoWriter writer = new ApplicationFormSmccTenpoWriter(
                TEMPLATE_DIR, new ApplicationFormFieldResolver());
        ApplicationFormRowContext ctx = new ApplicationFormRowContext(
                new ApplicationFormInput(), null, null, Map.of(), 1);

        byte[] bytes = writer.write(List.of(ctx));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("【連携シート】");
            Row row = rowAt(sheet, 4);
            assertThat(row.getCell(0).getStringCellValue()).isEqualTo("1");
        }
    }

    @Test
    void writesStoreNameToCol2() throws IOException {
        ApplicationFormSmccTenpoWriter writer = new ApplicationFormSmccTenpoWriter(
                TEMPLATE_DIR, new ApplicationFormFieldResolver());
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setStoreName("フラワーショップやざき");
        ApplicationFormRowContext ctx = new ApplicationFormRowContext(
                new ApplicationFormInput(), memberInfo, null, Map.of(), 1);

        byte[] bytes = writer.write(List.of(ctx));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("【連携シート】");
            Row row = rowAt(sheet, 4);
            assertThat(row.getCell(1).getStringCellValue()).isEqualTo("フラワーショップやざき");
        }
    }

    @Test
    void convertsMgmtTypeNumericForCol11() throws IOException {
        ApplicationFormSmccTenpoWriter writer = new ApplicationFormSmccTenpoWriter(
                TEMPLATE_DIR, new ApplicationFormFieldResolver());
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setMgmtType("個人");
        ApplicationFormRowContext ctx = new ApplicationFormRowContext(
                new ApplicationFormInput(), memberInfo, null, Map.of(), 1);

        byte[] bytes = writer.write(List.of(ctx));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("【連携シート】");
            Row row = rowAt(sheet, 4);
            assertThat(row.getCell(10).getStringCellValue()).isEqualTo("2");
        }
    }

    @Test
    void leavesSkipColumnUntouched() throws IOException {
        ApplicationFormSmccTenpoWriter writer = new ApplicationFormSmccTenpoWriter(
                TEMPLATE_DIR, new ApplicationFormFieldResolver());
        ApplicationFormRowContext ctx = new ApplicationFormRowContext(
                new ApplicationFormInput(), null, null, Map.of(), 1);

        byte[] bytes = writer.write(List.of(ctx));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("【連携シート】");
            Row row = rowAt(sheet, 4);
            // 列47はSYSTEM(SKIP、テンプレート側の重複チェック数式セル）のため書き込まれず、
            // 数式（FORMULA）のまま保たれる
            Cell cell = row.getCell(46);
            assertThat(cell.getCellType()).isEqualTo(CellType.FORMULA);
        }
    }

    @Test
    void writesMultipleRowsWithIncrementingRowSequence() throws IOException {
        ApplicationFormSmccTenpoWriter writer = new ApplicationFormSmccTenpoWriter(
                TEMPLATE_DIR, new ApplicationFormFieldResolver());
        ApplicationFormRowContext ctx1 = new ApplicationFormRowContext(
                new ApplicationFormInput(), null, null, Map.of(), 1);
        ApplicationFormRowContext ctx2 = new ApplicationFormRowContext(
                new ApplicationFormInput(), null, null, Map.of(), 2);

        byte[] bytes = writer.write(List.of(ctx1, ctx2));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("【連携シート】");
            assertThat(rowAt(sheet, 4).getCell(0).getStringCellValue()).isEqualTo("1");
            assertThat(rowAt(sheet, 5).getCell(0).getStringCellValue()).isEqualTo("2");
        }
    }

}
