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
 * 決済会社×カードブランド単位の合計行が正しく出力されることを検証する。
 */
class SalesReportXlsxWriterTest {

    @Test
    void writeProducesRowsAndTotalsReadableBackFromXlsx() throws IOException {
        SalesReportXlsxWriter writer = new SalesReportXlsxWriter();
        List<ReportRow> rows = List.of(
                new ReportRow("JCB", "【ＪＣＢカード】", 2, 178908, 0, 0),
                new ReportRow("住信SBI", "Visa/Master", 1, 57320, 0, 0));

        byte[] xlsxBytes = writer.write(rows);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(xlsxBytes))) {
            Sheet sheet = workbook.getSheet("売上報告書");
            Row jcbRow = sheet.getRow(3);
            assertThat(jcbRow.getCell(0).getStringCellValue()).isEqualTo("JCB 【ＪＣＢカード】");
            assertThat(jcbRow.getCell(1).getNumericCellValue()).isEqualTo(2);
            assertThat(jcbRow.getCell(2).getNumericCellValue()).isEqualTo(178908);

            Row sbiRow = sheet.getRow(4);
            assertThat(sbiRow.getCell(0).getStringCellValue()).isEqualTo("住信SBI Visa/Master");
            assertThat(sbiRow.getCell(2).getNumericCellValue()).isEqualTo(57320);

            Row totalRow = sheet.getRow(6);
            assertThat(totalRow.getCell(0).getStringCellValue()).isEqualTo("合計");
            assertThat(totalRow.getCell(2).getNumericCellValue()).isEqualTo(236228);
        }
    }

}
