package com.cupit.service.settlement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

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
 * SupportStatementXlsxWriter のテスト。生成したxlsxを実際に読み戻し、
 * 会社・振込先情報（レターヘッド）と、ReportLineDefinitionの並び順どおりの明細行・
 * 各グループの小計・末尾の合計行が正しく出力されることを検証する。
 */
@ExtendWith(MockitoExtension.class)
class SupportStatementXlsxWriterTest {

    @Mock
    private JftdReportCompanyInfoRepository companyInfoRepository;

    @Test
    void writeWithoutCompanyInfoProducesRowsInDefinedOrderWithSubtotalsAndTotal() throws IOException {
        when(companyInfoRepository.findById(1)).thenReturn(Optional.empty());
        SupportStatementXlsxWriter writer = new SupportStatementXlsxWriter(companyInfoRepository);
        List<ReportRow> rows = List.of(
                new ReportRow("住信SBI", "Visa/Master", 1, 59080, 1760, 0, 0, 57320, 0, 0),
                new ReportRow("JCB", "【ＪＣＢカード】", 2, 183500, 4592, 0, 0, 178908, 0, 0));

        byte[] xlsxBytes = writer.write(rows);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(xlsxBytes))) {
            Sheet sheet = workbook.getSheet("支払明細書");

            Row headerRow = sheet.getRow(3);
            assertThat(headerRow.getCell(0).getStringCellValue()).isEqualTo("決済種別");

            Row sbiRow = sheet.getRow(4);
            assertThat(sbiRow.getCell(0).getStringCellValue()).isEqualTo("Visa/Master Card");
            assertThat(sbiRow.getCell(2).getNumericCellValue()).isEqualTo(59080);
            assertThat(sbiRow.getCell(6).getNumericCellValue()).isEqualTo(57320);

            Row jcbRow = sheet.getRow(9);
            assertThat(jcbRow.getCell(0).getStringCellValue()).isEqualTo("JCB");
            assertThat(jcbRow.getCell(2).getNumericCellValue()).isEqualTo(183500);

            Row firstSubtotalRow = sheet.getRow(15);
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
    void writeWithCompanyInfoProducesLetterheadSection() throws IOException {
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
        SupportStatementXlsxWriter writer = new SupportStatementXlsxWriter(companyInfoRepository);

        byte[] xlsxBytes = writer.write(List.of());

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(xlsxBytes))) {
            Sheet sheet = workbook.getSheet("支払明細書");

            assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("支 払 明 細 書");
            assertThat(sheet.getRow(3).getCell(0).getStringCellValue())
                    .contains("一般社団法人ＪＦＴＤ").contains("140-8709");
            assertThat(sheet.getRow(5).getCell(0).getStringCellValue())
                    .contains("花キューピット株式会社");
            Row bankValueRow = sheet.getRow(9);
            assertThat(bankValueRow.getCell(1).getStringCellValue()).isEqualTo("みずほ銀行");
            assertThat(bankValueRow.getCell(4).getStringCellValue()).isEqualTo("2498314");

            Row headerRow = sheet.getRow(11);
            assertThat(headerRow.getCell(0).getStringCellValue()).isEqualTo("決済種別");
        }
    }

}
