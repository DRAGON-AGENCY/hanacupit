package com.cupit.service.applicationform;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import com.cupit.model.ApplicationFormInput;
import com.cupit.model.MemberInfo;

/**
 * {@link ApplicationFormSmccKameiWriter} のテスト。外部テンプレート
 * （application_form_smcc_kamei_template.xlsm）の「入力シート」シートのデータ行
 * （7行目〜）に値が書き込まれること、MANUAL／PROTECTEDの列は書き込まれず
 * テンプレートの状態が保たれること、マクロ有効ブックのVBAプロジェクト
 * （xl/vbaProject.bin）が生成後も保持されることを検証する。
 */
class ApplicationFormSmccKameiWriterTest {

    private static final String TEMPLATE_DIR = "C:/work/20260401_花キューピット/09_帳票テンプレート";
    private static final String TEMPLATE_FILE_NAME = "application_form_smcc_kamei_template.xlsm";

    private static Row rowAt(Sheet sheet, int excelRow) {
        return sheet.getRow(excelRow - 1);
    }

    @Test
    void writesSmccApplicationClassificationFromInputToCol2() throws IOException {
        ApplicationFormSmccKameiWriter writer = new ApplicationFormSmccKameiWriter(
                TEMPLATE_DIR, new ApplicationFormFieldResolver());
        ApplicationFormInput input = new ApplicationFormInput();
        input.setSmccApplicationClassification("1：新規加盟店");
        ApplicationFormRowContext ctx = new ApplicationFormRowContext(
                input, null, null,
                Map.of("SYSTEM_DATE_SLASH", "2026/07/17",
                        "DEFAULT_STORE_COUNT_1", "1",
                        "DEFAULT_REPRESENTATIVE_STORE_FLAG", "有"), 1);

        byte[] bytes = writer.write(List.of(ctx));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("入力シート");
            Row row = rowAt(sheet, 7);
            assertThat(row.getCell(1).getStringCellValue()).isEqualTo("1：新規加盟店");
        }
    }

    @Test
    void prefersCorpNameForCol7ViaCorpOrStoreName() throws IOException {
        ApplicationFormSmccKameiWriter writer = new ApplicationFormSmccKameiWriter(
                TEMPLATE_DIR, new ApplicationFormFieldResolver());
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setCorpName("有限会社赤坂生花店");
        memberInfo.setStoreName("赤坂生花店");
        ApplicationFormRowContext ctx = new ApplicationFormRowContext(
                new ApplicationFormInput(), memberInfo, null, Map.of(), 1);

        byte[] bytes = writer.write(List.of(ctx));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("入力シート");
            Row row = rowAt(sheet, 7);
            assertThat(row.getCell(6).getStringCellValue()).isEqualTo("有限会社赤坂生花店");
        }
    }

    @Test
    void leavesManualColumnsUntouched() throws IOException {
        ApplicationFormSmccKameiWriter writer = new ApplicationFormSmccKameiWriter(
                TEMPLATE_DIR, new ApplicationFormFieldResolver());
        ApplicationFormRowContext ctx = new ApplicationFormRowContext(
                new ApplicationFormInput(), null, null, Map.of(), 1);

        byte[] bytes = writer.write(List.of(ctx));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("入力シート");
            Row row = rowAt(sheet, 7);
            // 列76（rep_address MANUAL）は担当者手入力欄のため書き込まれない
            Cell cell = row == null ? null : row.getCell(75);
            assertThat(cell == null || cell.getCellType() == CellType.BLANK).isTrue();
        }
    }

    @Test
    void leavesProtectedColumnsUntouched() throws IOException {
        ApplicationFormSmccKameiWriter writer = new ApplicationFormSmccKameiWriter(
                TEMPLATE_DIR, new ApplicationFormFieldResolver());
        ApplicationFormRowContext ctx = new ApplicationFormRowContext(
                new ApplicationFormInput(), null, null, Map.of(), 1);

        byte[] bytes = writer.write(List.of(ctx));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("入力シート");
            Row row = rowAt(sheet, 7);
            // 列235は保護セル（PROTECTED、テンプレート側の数式セル）のため書き込まれず、
            // 数式（FORMULA）のまま保たれる
            Cell cell = row.getCell(234);
            assertThat(cell.getCellType()).isEqualTo(CellType.FORMULA);
        }
    }

    @Test
    void clearsSampleDataInRowsBeyondWrittenRecords() throws IOException {
        ApplicationFormSmccKameiWriter writer = new ApplicationFormSmccKameiWriter(
                TEMPLATE_DIR, new ApplicationFormFieldResolver());
        ApplicationFormRowContext ctx = new ApplicationFormRowContext(
                new ApplicationFormInput(), null, null, Map.of(), 1);

        byte[] bytes = writer.write(List.of(ctx));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getSheet("入力シート");
            // テンプレートのデータ行（7行目〜）には「花キューピット」のサンプルデータが
            // シート最終行まで埋め込まれているが、1件しか書き込んでいないため
            // 20行目は書き込み対象外。それでもサンプルデータが出力に残らないことを検証する。
            Row row = rowAt(sheet, 20);
            Cell cell = row == null ? null : row.getCell(6);
            assertThat(cell == null || cell.getCellType() == CellType.BLANK).isTrue();
        }
    }

    @Test
    void writesMultipleRowsStartingAtRow7() throws IOException {
        ApplicationFormSmccKameiWriter writer = new ApplicationFormSmccKameiWriter(
                TEMPLATE_DIR, new ApplicationFormFieldResolver());
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
            Sheet sheet = workbook.getSheet("入力シート");
            assertThat(rowAt(sheet, 7).getCell(8).getStringCellValue()).isEqualTo("店舗A");
            assertThat(rowAt(sheet, 8).getCell(8).getStringCellValue()).isEqualTo("店舗B");
        }
    }

    @Test
    void preservesVbaProjectAfterGeneration() throws IOException {
        ApplicationFormSmccKameiWriter writer = new ApplicationFormSmccKameiWriter(
                TEMPLATE_DIR, new ApplicationFormFieldResolver());
        assertThat(zipEntryNames(Files.readAllBytes(Path.of(TEMPLATE_DIR, TEMPLATE_FILE_NAME))))
                .contains("xl/vbaProject.bin");
        ApplicationFormRowContext ctx = new ApplicationFormRowContext(
                new ApplicationFormInput(), null, null, Map.of(), 1);

        byte[] bytes = writer.write(List.of(ctx));

        assertThat(zipEntryNames(bytes)).contains("xl/vbaProject.bin");
    }

    private List<String> zipEntryNames(byte[] xlsmBytes) throws IOException {
        List<String> names = new java.util.ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(xlsmBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                names.add(entry.getName());
            }
        }
        return names;
    }

}
