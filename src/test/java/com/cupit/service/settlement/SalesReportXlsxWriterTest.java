package com.cupit.service.settlement;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

/**
 * SalesReportXlsxWriter のテスト。生成したxlsxを実際に読み戻し、
 * ReportLineDefinitionの並び順どおりに行が出力され、未実装の決済種別は0円の
 * プレースホルダー行、各グループの末尾に小計行、末尾に合計行が出力されることを検証する。
 */
class SalesReportXlsxWriterTest {

    private static final int HEADER_ROW = 3;
    private static final int FIRST_DATA_ROW = 4;

    @Test
    void writeProducesRowsInDefinedOrderWithSubtotalsAndTotal() throws IOException {
        SalesReportXlsxWriter writer = new SalesReportXlsxWriter();
        List<ReportRow> rows = List.of(
                new ReportRow("住信SBI", "Visa/Master", 1, 59080, 1760, 0, 0, 57320, 0, 0),
                new ReportRow("JCB", "【ＪＣＢカード】", 2, 183500, 4592, 0, 0, 178908, 0, 0));

        byte[] xlsxBytes = writer.write(rows);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(xlsxBytes))) {
            Sheet sheet = workbook.getSheet("売上報告書");

            Row sbiRow = sheet.getRow(FIRST_DATA_ROW);
            assertThat(sbiRow.getCell(0).getStringCellValue()).isEqualTo("Visa/Master Card");
            assertThat(sbiRow.getCell(1).getNumericCellValue()).isEqualTo(1);
            assertThat(sbiRow.getCell(2).getNumericCellValue()).isEqualTo(59080);
            assertThat(sbiRow.getCell(3).getNumericCellValue()).isEqualTo(1760);
            assertThat(sbiRow.getCell(6).getNumericCellValue()).isEqualTo(57320);

            int jcbRowNum = FIRST_DATA_ROW + 5;
            Row jcbRow = sheet.getRow(jcbRowNum);
            assertThat(jcbRow.getCell(0).getStringCellValue()).isEqualTo("JCB");
            assertThat(jcbRow.getCell(2).getNumericCellValue()).isEqualTo(183500);

            int firstSubtotalRowNum = FIRST_DATA_ROW + 11;
            Row firstSubtotalRow = sheet.getRow(firstSubtotalRowNum);
            assertThat(firstSubtotalRow.getCell(0).getStringCellValue()).isEqualTo("小計");
            assertThat(firstSubtotalRow.getCell(2).getNumericCellValue()).isEqualTo(242580);
            assertThat(firstSubtotalRow.getCell(6).getNumericCellValue()).isEqualTo(236228);

            Row lastRow = sheet.getRow(sheet.getLastRowNum());
            assertThat(lastRow.getCell(0).getStringCellValue()).isEqualTo("合計");
            assertThat(lastRow.getCell(2).getNumericCellValue()).isEqualTo(242580);
            assertThat(lastRow.getCell(6).getNumericCellValue()).isEqualTo(236228);
        }
    }

    @Test
    void writeProducesZeroValuePlaceholderForUnimplementedPaymentType() throws IOException {
        SalesReportXlsxWriter writer = new SalesReportXlsxWriter();

        byte[] xlsxBytes = writer.write(List.of());

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(xlsxBytes))) {
            Sheet sheet = workbook.getSheet("売上報告書");
            Row headerRow = sheet.getRow(HEADER_ROW);
            assertThat(headerRow.getCell(0).getStringCellValue()).isEqualTo("決済種別");

            Row steraRow = sheet.getRow(sheet.getLastRowNum() - 2);
            assertThat(steraRow.getCell(0).getStringCellValue()).isEqualTo("stera 領収書アプリ");
            assertThat(steraRow.getCell(2).getNumericCellValue()).isEqualTo(0);
        }
    }

}
