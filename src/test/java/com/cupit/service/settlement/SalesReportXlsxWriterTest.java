package com.cupit.service.settlement;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

/**
 * SalesReportXlsxWriter のテスト。サンプル帳票（サンプル_売上報告書.xlsx）を
 * そのままテンプレートとして使う方式のため、テンプレート上の固定行番号（Excel表記、
 * 1始まり）に実際の値が書き込まれること、期間列が空欄にクリアされること、
 * 未実装の決済種別は0円のプレースホルダー行になることを、生成したxlsxを
 * 読み戻して検証する。合計行（30行目）はテンプレート側のSUM式に委ねるため、
 * Java側では値を書き込まない。
 */
class SalesReportXlsxWriterTest {

    /** application.propertiesのreport.template.dir既定値と同じ外部テンプレートフォルダ。 */
    private static final String TEMPLATE_DIR =
            "C:/work/20260401_花キューピット/09_帳票テンプレート";

    /** Excel 1始まり行番号 → POI 0始まり行インデックスへの変換。 */
    private static Row rowAt(Sheet sheet, int excelRow) {
        return sheet.getRow(excelRow - 1);
    }

    @Test
    void writeFillsValueCellsAtFixedTemplateRowsAndClearsPeriodColumn() throws IOException {
        SalesReportXlsxWriter writer = new SalesReportXlsxWriter(TEMPLATE_DIR);
        List<ReportRow> rows = List.of(
                new ReportRow("住信SBI", "Visa/Master", 1, 59080, 1760, 0, 0, 57320, 0, 0),
                new ReportRow("JCB", "【ＪＣＢカード】", 2, 183500, 4592, 0, 0, 178908, 0, 0));

        byte[] xlsxBytes = writer.write(rows);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(xlsxBytes))) {
            Sheet sheet = workbook.getSheetAt(0);

            Row visaMasterRow = rowAt(sheet, 7);
            assertThat(visaMasterRow.getCell(2).getStringCellValue()).isEqualTo("Visa/Master Card");
            assertThat(visaMasterRow.getCell(3).getNumericCellValue()).isEqualTo(59080);
            assertThat(visaMasterRow.getCell(4).getNumericCellValue()).isEqualTo(1);
            assertThat(visaMasterRow.getCell(5).getNumericCellValue()).isEqualTo(1760);
            assertThat(visaMasterRow.getCell(11).getNumericCellValue()).isEqualTo(57320);

            Row jcbRow = rowAt(sheet, 12);
            assertThat(jcbRow.getCell(2).getStringCellValue()).isEqualTo("JCB");
            assertThat(jcbRow.getCell(3).getNumericCellValue()).isEqualTo(183500);
            assertThat(jcbRow.getCell(11).getNumericCellValue()).isEqualTo(178908);

            // 集計期間列（B）はサンプル値を残さず空欄にクリアする
            Cell periodCell = visaMasterRow.getCell(1);
            assertThat(periodCell == null || periodCell.getCellType() == CellType.BLANK).isTrue();
        }
    }

    @Test
    void writeProducesZeroValuePlaceholderForUnimplementedPaymentType() throws IOException {
        SalesReportXlsxWriter writer = new SalesReportXlsxWriter(TEMPLATE_DIR);

        byte[] xlsxBytes = writer.write(List.of());

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(xlsxBytes))) {
            Sheet sheet = workbook.getSheetAt(0);

            Row businessCommissionRow = rowAt(sheet, 29);
            assertThat(businessCommissionRow.getCell(2).getStringCellValue()).isEqualTo("業務委託料");
            assertThat(businessCommissionRow.getCell(3).getNumericCellValue()).isEqualTo(0);
        }
    }

}
