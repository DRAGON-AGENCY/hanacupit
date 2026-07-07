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
 * 支払明細書（帳票\支払明細書.xlsx）と同じ列構成でxlsxを書き出す。
 * 花キューピットからJFTDへの振込通知書に相当する。決済会社×カードブランド単位
 * （全店舗合算）の内訳行と、末尾に差引振込額の合計を出力する。
 * 手数料②が未実装のため、弊社手数料の列は0円になる
 * （調査メモ「論点・オープン事項」項番6を参照。将来PDF出力を追加する場合は
 * ReportRowの集計ロジック（JftdReportDataService）をそのまま再利用できる）。
 */
@Component
public class SupportStatementXlsxWriter {

    private static final String[] HEADERS = {
            "決済種別", "件数", "(A)決済金額控除後合計（暫定）", "弊社手数料（未実装）", "差引振込額（暫定）"
    };

    public byte[] write(List<ReportRow> rows) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("支払明細書");

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("支払明細書（暫定版・手数料②未反映）");
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
            int totalPayable = 0;
            for (ReportRow row : rows) {
                int payable = row.getPaymentAmount() - row.getFeeBaseAmount() - row.getFeeTaxAmount();
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(row.getPaymentCompany() + " " + row.getCardBrand());
                dataRow.createCell(1).setCellValue(row.getCount());
                dataRow.createCell(2).setCellValue(row.getPaymentAmount());
                dataRow.createCell(3).setCellValue(row.getFeeBaseAmount() + row.getFeeTaxAmount());
                dataRow.createCell(4).setCellValue(payable);
                totalPayable += payable;
            }

            Row totalRow = sheet.createRow(rowNum + 1);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("お支払金額合計（暫定）");
            totalLabelCell.setCellStyle(headerStyle);
            totalRow.createCell(4).setCellValue(totalPayable);

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("支払明細書の作成に失敗しました。", e);
        }
    }

}
