package com.cupit.service.settlement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

/**
 * 売上報告書（帳票\売上報告書.xlsx）と同じ列構成でxlsxを書き出す。
 * 決済会社×カードブランド単位（全店舗合算）の集計行を1行ずつ出力する。
 * 手数料②が未実装のため、弊社手数料本体・消費税の列は0円になる
 * （調査メモ「論点・オープン事項」項番6を参照。将来PDF出力を追加する場合は
 * ReportRowの集計ロジック（JftdReportDataService）をそのまま再利用できる）。
 */
@Component
public class SalesReportXlsxWriter {

    private static final String[] HEADERS = {
            "摘要", "件数", "決済手数料控除後金額（暫定）", "弊社手数料（本体・未実装）", "弊社手数料（消費税・未実装）"
    };

    public byte[] write(List<ReportRow> rows) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("売上報告書");

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("キャッシュレス手数料売上報告書（暫定版・手数料②未反映）");
            titleCell.setCellStyle(titleStyle);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(2);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 3;
            int totalPayment = 0;
            int totalFeeBase = 0;
            int totalFeeTax = 0;
            for (ReportRow row : rows) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(row.getPaymentCompany() + " " + row.getCardBrand());
                dataRow.createCell(1).setCellValue(row.getCount());
                dataRow.createCell(2).setCellValue(row.getPaymentAmount());
                dataRow.createCell(3).setCellValue(row.getFeeBaseAmount());
                dataRow.createCell(4).setCellValue(row.getFeeTaxAmount());
                totalPayment += row.getPaymentAmount();
                totalFeeBase += row.getFeeBaseAmount();
                totalFeeTax += row.getFeeTaxAmount();
            }

            Row totalRow = sheet.createRow(rowNum + 1);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("合計");
            totalLabelCell.setCellStyle(headerStyle);
            totalRow.createCell(2).setCellValue(totalPayment);
            totalRow.createCell(3).setCellValue(totalFeeBase);
            totalRow.createCell(4).setCellValue(totalFeeTax);

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("売上報告書の作成に失敗しました。", e);
        }
    }

}
