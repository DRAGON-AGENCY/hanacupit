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
 * {@link ApplicationFormJcbWriter} のテスト。外部テンプレート
 * （application_form_jcb_template.xlsx）の「【入力用】」シートのデータ行（6行目〜）に
 * 値が書き込まれること、複数行が連続して書き込まれること、値がnullの項目は
 * セルが上書きされない（テンプレートの初期状態が保たれる）ことを検証する。
 */
class ApplicationFormJcbWriterTest {

    private static final String TEMPLATE_DIR = "C:/work/20260401_花キューピット/09_帳票テンプレート";

    private static Row rowAt(Sheet sheet, int excelRow) {
        return sheet.getRow(excelRow - 1);
    }

    @Test
    void writesStoreNameToExpectedCell() throws IOException {
        ApplicationFormJcbWriter writer =
                new ApplicationFormJcbWriter(TEMPLATE_DIR, new ApplicationFormFieldResolver());
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setStoreName("フラワーショップやざき");
        ApplicationFormRowContext ctx = new ApplicationFormRowContext(
                new ApplicationFormInput(), memberInfo, null,
                Map.of("NEW_CHANGE_CANCEL_FLAG_JCB", "新規"), 1);

        byte[] bytes = writer.write(List.of(ctx));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("【入力用】");
            Row row = rowAt(sheet, 6);
            assertThat(row.getCell(2).getStringCellValue()).isEqualTo("フラワーショップやざき");
        }
    }

    @Test
    void appliesDeriveJcbFlagToFirstColumn() throws IOException {
        ApplicationFormJcbWriter writer =
                new ApplicationFormJcbWriter(TEMPLATE_DIR, new ApplicationFormFieldResolver());
        ApplicationFormRowContext ctx = new ApplicationFormRowContext(
                new ApplicationFormInput(), null, null,
                Map.of("NEW_CHANGE_CANCEL_FLAG_JCB", "新規",
                        "EXISTING_CONTRACT_FLAG", "無", "CANCEL_INTENTION", "", "CANCEL_STATUS", ""), 1);

        byte[] bytes = writer.write(List.of(ctx));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("【入力用】");
            Row row = rowAt(sheet, 6);
            assertThat(row.getCell(1).getStringCellValue()).isEqualTo("新規");
        }
    }

    @Test
    void writesMultipleRowsSequentiallyStartingAtRow6() throws IOException {
        ApplicationFormJcbWriter writer =
                new ApplicationFormJcbWriter(TEMPLATE_DIR, new ApplicationFormFieldResolver());
        MemberInfo memberInfo1 = new MemberInfo();
        memberInfo1.setStoreName("店舗A");
        MemberInfo memberInfo2 = new MemberInfo();
        memberInfo2.setStoreName("店舗B");
        ApplicationFormRowContext ctx1 = new ApplicationFormRowContext(
                new ApplicationFormInput(), memberInfo1, null, Map.of(), 1);
        ApplicationFormRowContext ctx2 = new ApplicationFormRowContext(
                new ApplicationFormInput(), memberInfo2, null, Map.of(), 2);

        byte[] bytes = writer.write(List.of(ctx1, ctx2));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("【入力用】");
            assertThat(rowAt(sheet, 6).getCell(2).getStringCellValue()).isEqualTo("店舗A");
            assertThat(rowAt(sheet, 7).getCell(2).getStringCellValue()).isEqualTo("店舗B");
        }
    }

    @Test
    void leavesCellUntouchedWhenResolvedValueIsNull() throws IOException {
        ApplicationFormJcbWriter writer =
                new ApplicationFormJcbWriter(TEMPLATE_DIR, new ApplicationFormFieldResolver());
        ApplicationFormRowContext ctx = new ApplicationFormRowContext(
                new ApplicationFormInput(), null, null, Map.of(), 1);

        byte[] bytes = writer.write(List.of(ctx));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("【入力用】");
            Row row = rowAt(sheet, 6);
            Cell cell = row == null ? null : row.getCell(2);
            assertThat(cell == null || cell.getCellType() == CellType.BLANK).isTrue();
        }
    }

    @Test
    void clearsSampleDataInRowsBeyondWrittenRecords() throws IOException {
        ApplicationFormJcbWriter writer =
                new ApplicationFormJcbWriter(TEMPLATE_DIR, new ApplicationFormFieldResolver());
        ApplicationFormRowContext ctx = new ApplicationFormRowContext(
                new ApplicationFormInput(), null, null, Map.of(), 1);

        byte[] bytes = writer.write(List.of(ctx));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("【入力用】");
            // テンプレートの7〜9行目には「入力例：01/02/03」のサンプルデータが
            // 埋め込まれているが、1件しか書き込んでいないため7行目は書き込み対象外。
            // それでもサンプルデータが出力に残らないことを検証する。
            Row row = rowAt(sheet, 7);
            assertThat(row == null || row.getCell(0) == null
                    || row.getCell(0).getCellType() == CellType.BLANK).isTrue();
            Cell storeNameCell = row == null ? null : row.getCell(2);
            assertThat(storeNameCell == null || storeNameCell.getCellType() == CellType.BLANK).isTrue();
        }
    }

    @Test
    void producesNoDataRowsWhenNoRecordsGiven() throws IOException {
        ApplicationFormJcbWriter writer =
                new ApplicationFormJcbWriter(TEMPLATE_DIR, new ApplicationFormFieldResolver());

        byte[] bytes = writer.write(List.of());

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("【入力用】");
            Row row = rowAt(sheet, 6);
            assertThat(row == null || row.getCell(2) == null
                    || row.getCell(2).getCellType() == CellType.BLANK).isTrue();
        }
    }

}
