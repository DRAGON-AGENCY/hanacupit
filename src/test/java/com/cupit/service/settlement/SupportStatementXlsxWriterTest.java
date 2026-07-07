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
 * SupportStatementXlsxWriter のテスト。生成したxlsxを実際に読み戻し、
 * 差引振込額（暫定）の行・合計が正しく出力されることを検証する。
 */
class SupportStatementXlsxWriterTest {

    @Test
    void writeProducesPayableAmountAndTotalReadableBackFromXlsx() throws IOException {
        SupportStatementXlsxWriter writer = new SupportStatementXlsxWriter();
        List<ReportRow> rows = List.of(
                new ReportRow("JCB", "【ＪＣＢカード】", 2, 178908, 0, 0),
                new ReportRow("住信SBI", "Visa/Master", 1, 57320, 0, 0));

        byte[] xlsxBytes = writer.write(rows);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(xlsxBytes))) {
            Sheet sheet = workbook.getSheet("支払明細書");
            Row jcbRow = sheet.getRow(3);
            assertThat(jcbRow.getCell(4).getNumericCellValue()).isEqualTo(178908);

            Row totalRow = sheet.getRow(6);
            assertThat(totalRow.getCell(0).getStringCellValue()).isEqualTo("お支払金額合計（暫定）");
            assertThat(totalRow.getCell(4).getNumericCellValue()).isEqualTo(236228);
        }
    }

}
