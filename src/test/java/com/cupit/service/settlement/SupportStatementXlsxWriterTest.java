package com.cupit.service.settlement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cupit.model.JftdReportCompanyInfo;
import com.cupit.repository.JftdReportCompanyInfoRepository;

/**
 * SupportStatementXlsxWriter のテスト。サンプル帳票（サンプル_支払明細書.xlsx）を
 * そのままテンプレートとして使う方式のため、テンプレート上の固定行番号（Excel表記、
 * 1始まり）に実際の値が書き込まれること・期間列や手数料率注記セルが空欄に
 * クリアされることを、生成したxlsxを読み戻して検証する。
 */
@ExtendWith(MockitoExtension.class)
class SupportStatementXlsxWriterTest {

    /** application.propertiesのreport.template.dir既定値と同じ外部テンプレートフォルダ。 */
    private static final String TEMPLATE_DIR =
            "C:/work/20260401_花キューピット/09_帳票テンプレート";

    /** Excel 1始まり行番号 → POI 0始まり行インデックスへの変換。 */
    private static Row rowAt(Sheet sheet, int excelRow) {
        return sheet.getRow(excelRow - 1);
    }

    @Mock
    private JftdReportCompanyInfoRepository companyInfoRepository;

    @Test
    void writeFillsValueCellsAtFixedTemplateRowsAndClearsPeriodAndFeeRateNoteCells() throws IOException {
        when(companyInfoRepository.findById(1)).thenReturn(Optional.empty());
        SupportStatementXlsxWriter writer =
                new SupportStatementXlsxWriter(companyInfoRepository, TEMPLATE_DIR);
        List<ReportRow> rows = List.of(
                new ReportRow("住信SBI", "Visa/Master", 1, 59080, 1760, 0, 0, 57320, 0, 0),
                new ReportRow("JCB", "【ＪＣＢカード】", 2, 183500, 4592, 0, 0, 178908, 0, 0));

        byte[] xlsxBytes = writer.write(rows, LocalDate.of(2026, 1, 5));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(xlsxBytes))) {
            Sheet sheet = workbook.getSheetAt(0);

            Row visaMasterRow = rowAt(sheet, 17);
            assertThat(visaMasterRow.getCell(1).getStringCellValue()).isEqualTo("Visa/Master Card");
            assertThat(visaMasterRow.getCell(3).getNumericCellValue()).isEqualTo(1);
            assertThat(visaMasterRow.getCell(4).getNumericCellValue()).isEqualTo(59080);
            assertThat(visaMasterRow.getCell(8).getNumericCellValue()).isEqualTo(1760);
            assertThat(visaMasterRow.getCell(9).getNumericCellValue()).isEqualTo(57320);
            assertThat(visaMasterRow.getCell(14).getNumericCellValue()).isEqualTo(57320);

            Row jcbRow = rowAt(sheet, 27);
            assertThat(jcbRow.getCell(1).getStringCellValue()).isEqualTo("JCB");
            assertThat(jcbRow.getCell(4).getNumericCellValue()).isEqualTo(183500);
            assertThat(jcbRow.getCell(9).getNumericCellValue()).isEqualTo(178908);

            Row group1Subtotal = rowAt(sheet, 39);
            assertThat(group1Subtotal.getCell(4).getNumericCellValue()).isEqualTo(242580);
            assertThat(group1Subtotal.getCell(9).getNumericCellValue()).isEqualTo(236228);

            Row grandTotal = rowAt(sheet, 107);
            assertThat(grandTotal.getCell(4).getNumericCellValue()).isEqualTo(242580);
            assertThat(grandTotal.getCell(14).getNumericCellValue()).isEqualTo(236228);

            Row memoRow = rowAt(sheet, 108);
            assertThat(memoRow.getCell(13).getNumericCellValue()).isEqualTo(6352);

            // 集計期間列（C）はサンプル値を残さず空欄にクリアする
            Cell periodCell = visaMasterRow.getCell(2);
            assertThat(periodCell == null || periodCell.getCellType() == CellType.BLANK).isTrue();

            // 手数料率の注記セル（I18/M18）はサンプルの固定値を残さず空欄にクリアする
            Row feeRateNoteRow = rowAt(sheet, 18);
            Cell feeRateNoteCell = feeRateNoteRow.getCell(8);
            assertThat(feeRateNoteCell == null || feeRateNoteCell.getCellType() == CellType.BLANK).isTrue();
        }
    }

    @Test
    void writeFillsLetterheadFromCompanyInfoAndPaymentDate() throws IOException {
        JftdReportCompanyInfo info = new JftdReportCompanyInfo();
        info.setRecipientName("一般社団法人ＪＦＴＤ");
        info.setRecipientZip("140-8709");
        info.setRecipientAddress("東京都品川区北品川４丁目１１番９号 日本フラワー会館");
        info.setRecipientInvoiceNo("T8010705001607");
        info.setSenderName("花キューピット株式会社");
        info.setSenderZip("107-0062");
        info.setSenderAddress("東京都港区南青山2-24-15 青山タワービル4F");
        info.setSenderInvoiceNo("T4010701016224");
        info.setSenderTel("03-5436-8736");
        info.setSenderFax("03-3470-8701");
        info.setSenderContact("グループマーケティング戦略部 北村");
        info.setBankName("みずほ銀行");
        info.setBankBranchName("五反田支店");
        info.setBankAccountType("普通");
        info.setBankAccountNumber("2498314");
        info.setBankAccountHolderKana("ｲｯﾊﾟﾝｼｬﾀﾞﾝﾎｳｼﾞﾝｼﾞｪｲｴﾌﾃｨｰﾃﾞｰｳﾝﾖｳ");
        when(companyInfoRepository.findById(1)).thenReturn(Optional.of(info));
        SupportStatementXlsxWriter writer =
                new SupportStatementXlsxWriter(companyInfoRepository, TEMPLATE_DIR);

        byte[] xlsxBytes = writer.write(List.of(), LocalDate.of(2026, 1, 5));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(xlsxBytes))) {
            Sheet sheet = workbook.getSheetAt(0);

            assertThat(rowAt(sheet, 4).getCell(1).getStringCellValue()).contains("140-8709");
            assertThat(rowAt(sheet, 5).getCell(1).getStringCellValue()).contains("日本フラワー会館");
            assertThat(rowAt(sheet, 6).getCell(1).getStringCellValue()).isEqualTo("一般社団法人ＪＦＴＤ");
            assertThat(rowAt(sheet, 7).getCell(1).getStringCellValue()).contains("T8010705001607");

            assertThat(rowAt(sheet, 6).getCell(11).getStringCellValue()).isEqualTo("花キューピット株式会社");
            assertThat(rowAt(sheet, 8).getCell(11).getStringCellValue())
                    .contains("03-5436-8736").contains("03-3470-8701");

            Row bankValueRow = rowAt(sheet, 12);
            assertThat(bankValueRow.getCell(4).getStringCellValue()).isEqualTo("みずほ銀行");
            assertThat(bankValueRow.getCell(7).getStringCellValue()).isEqualTo("2498314");
            assertThat(bankValueRow.getCell(1).getLocalDateTimeCellValue().toLocalDate())
                    .isEqualTo(LocalDate.of(2026, 1, 5));
        }
    }

}
