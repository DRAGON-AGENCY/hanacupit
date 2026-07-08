package com.cupit.service.settlement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.cupit.model.JftdReportCompanyInfo;
import com.cupit.repository.JftdReportCompanyInfoRepository;

/**
 * 支払明細書（サンプル_支払明細書.xlsx）と同じ列構成でxlsxを書き出す。
 * 花キューピットからJFTDへの振込通知書に相当する。決済種別（決済会社×カードブランド、
 * スマレジ端末月額利用料は本体・調整を合算）単位の内訳行と、3グループの小計・
 * 総合計を出力する。行の並び・グルーピングはReportLineDefinitionを参照。
 * 「(C)株式会社手数料」（手数料②、弊社→加盟店の上乗せ手数料）は計算式が未確定のため
 * 常に0円で出力する（調査メモ「論点・オープン事項」項番6を参照）。
 */
@Component
public class SupportStatementXlsxWriter {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final JftdReportCompanyInfoRepository companyInfoRepository;

    public SupportStatementXlsxWriter(JftdReportCompanyInfoRepository companyInfoRepository) {
        this.companyInfoRepository = companyInfoRepository;
    }

    public byte[] write(List<ReportRow> rows) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("支払明細書");
            Styles styles = new Styles(workbook);
            JftdReportCompanyInfo info = companyInfoRepository.findById(1).orElse(null);

            int rowNum = writeLetterhead(sheet, styles, info);
            rowNum = writeHeaderRow(sheet, styles, rowNum);
            rowNum = writeDataRows(sheet, styles, rowNum, rows);

            for (int col = 0; col <= 6; col++) {
                sheet.autoSizeColumn(col);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("支払明細書の作成に失敗しました。", e);
        }
    }

    private int writeLetterhead(Sheet sheet, Styles styles, JftdReportCompanyInfo info) {
        int r = 0;
        Row issueDateRow = sheet.createRow(r++);
        Cell issueDateCell = issueDateRow.createCell(5);
        issueDateCell.setCellValue("発行日：" + LocalDate.now().format(DATE_FMT));

        Row titleRow = sheet.createRow(r++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("支 払 明 細 書");
        titleCell.setCellStyle(styles.title);
        r++;

        if (info != null) {
            Row recipientRow = sheet.createRow(r++);
            recipientRow.createCell(0).setCellValue(
                    info.getRecipientName() + " 御中（〒" + nullToEmpty(info.getRecipientZip()) + " "
                    + nullToEmpty(info.getRecipientAddress()) + "）");
            Row recipientInvoiceRow = sheet.createRow(r++);
            recipientInvoiceRow.createCell(0).setCellValue(
                    "登録番号：" + nullToEmpty(info.getRecipientInvoiceNo()));

            Row senderRow = sheet.createRow(r++);
            senderRow.createCell(0).setCellValue(
                    info.getSenderName() + "（〒" + nullToEmpty(info.getSenderZip()) + " "
                    + nullToEmpty(info.getSenderAddress()) + "）");
            Row senderContactRow = sheet.createRow(r++);
            senderContactRow.createCell(0).setCellValue(
                    "登録番号：" + nullToEmpty(info.getSenderInvoiceNo())
                    + "　TEL：" + nullToEmpty(info.getSenderTel())
                    + "　FAX：" + nullToEmpty(info.getSenderFax())
                    + "　担当：" + nullToEmpty(info.getSenderContact()));
            r++;

            Row bankHeaderRow = sheet.createRow(r++);
            String[] bankHeaders = {
                "お支払条件", "ご指定金融機関", "支店名", "預金科目", "口座番号", "口座名義カナ"
            };
            for (int i = 0; i < bankHeaders.length; i++) {
                Cell c = bankHeaderRow.createCell(i);
                c.setCellValue(bankHeaders[i]);
                c.setCellStyle(styles.header);
            }
            Row bankValueRow = sheet.createRow(r++);
            bankValueRow.createCell(0).setCellValue("銀行振込");
            bankValueRow.createCell(1).setCellValue(nullToEmpty(info.getBankName()));
            bankValueRow.createCell(2).setCellValue(nullToEmpty(info.getBankBranchName()));
            bankValueRow.createCell(3).setCellValue(nullToEmpty(info.getBankAccountType()));
            bankValueRow.createCell(4).setCellValue(nullToEmpty(info.getBankAccountNumber()));
            bankValueRow.createCell(5).setCellValue(nullToEmpty(info.getBankAccountHolderKana()));
            r++;
        }
        return r;
    }

    private int writeHeaderRow(Sheet sheet, Styles styles, int rowNum) {
        String[] headers = {
            "決済種別", "件数", "(A)決済金額合計",
            "(B)事業者手数料\n非課税", "(B)事業者手数料\n課税本体", "(B)事業者手数料\n消費税",
            "事業者手数料差引後\n決済金額 (A)-(B)",
            "(C)弊社手数料\n本体（未実装）", "(C)弊社手数料\n消費税（未実装）",
            "手数料合計\n(B)+(C)", "差引振込額\n(A)-(B)-(C)"
        };
        Row headerRow = sheet.createRow(rowNum);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.header);
        }
        return rowNum + 1;
    }

    private int writeDataRows(Sheet sheet, Styles styles, int rowNum, List<ReportRow> rows) {
        List<ReportLineDefinition> definitions = ReportLineDefinition.defaultOrder();
        List<ResolvedReportLine> lines = ResolvedReportLine.resolveAll(definitions, rows);

        Totals subtotal = new Totals();
        Totals grandTotal = new Totals();

        for (int i = 0; i < definitions.size(); i++) {
            ReportLineDefinition def = definitions.get(i);
            if (def.isSubtotalMarker()) {
                writeTotalsRow(sheet, styles, rowNum++, "小計", subtotal);
                grandTotal.add(subtotal);
                subtotal = new Totals();
                continue;
            }
            ResolvedReportLine line = lines.get(i);
            writeDataRow(sheet, styles, rowNum++, line);
            subtotal.addLine(line);
        }

        writeTotalsRow(sheet, styles, rowNum++, "合計", grandTotal);
        return rowNum;
    }

    private void writeDataRow(Sheet sheet, Styles styles, int rowNum, ResolvedReportLine line) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(line.getLabel());
        setAmount(row.createCell(1), line.getCount(), styles);
        setAmount(row.createCell(2), line.getGrossAmount(), styles);
        setAmount(row.createCell(3), line.getAcquirerFeeTaxFreeAmount(), styles);
        setAmount(row.createCell(4), line.getAcquirerFeeBaseAmount(), styles);
        setAmount(row.createCell(5), line.getAcquirerFeeTaxAmount(), styles);
        setAmount(row.createCell(6), line.getAfterAcquirerFeeAmount(), styles);
        setAmount(row.createCell(7), line.getOurFeeBaseAmount(), styles);
        setAmount(row.createCell(8), line.getOurFeeTaxAmount(), styles);
        setAmount(row.createCell(9), line.getTotalFeeAmount(), styles);
        setAmount(row.createCell(10), line.getNetPayableAmount(), styles);
    }

    private void writeTotalsRow(Sheet sheet, Styles styles, int rowNum, String label, Totals totals) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(styles.header);
        setAmount(row.createCell(2), totals.grossAmount, styles);
        setAmount(row.createCell(3), totals.acquirerFeeTaxFree, styles);
        setAmount(row.createCell(4), totals.acquirerFeeBase, styles);
        setAmount(row.createCell(5), totals.acquirerFeeTax, styles);
        setAmount(row.createCell(6), totals.grossAmount - totals.acquirerFeeTaxFree
                - totals.acquirerFeeBase - totals.acquirerFeeTax, styles);
        setAmount(row.createCell(7), totals.ourFeeBase, styles);
        setAmount(row.createCell(8), totals.ourFeeTax, styles);
        int feeTotal = totals.acquirerFeeTaxFree + totals.acquirerFeeBase + totals.acquirerFeeTax
                + totals.ourFeeBase + totals.ourFeeTax;
        setAmount(row.createCell(9), feeTotal, styles);
        setAmount(row.createCell(10), totals.grossAmount - feeTotal, styles);
    }

    private void setAmount(Cell cell, int value, Styles styles) {
        cell.setCellValue(value);
        cell.setCellStyle(styles.amount);
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    /** グループ小計・総合計の積算用。 */
    private static final class Totals {
        private int grossAmount;
        private int acquirerFeeTaxFree;
        private int acquirerFeeBase;
        private int acquirerFeeTax;
        private int ourFeeBase;
        private int ourFeeTax;

        void addLine(ResolvedReportLine line) {
            grossAmount += line.getGrossAmount();
            acquirerFeeTaxFree += line.getAcquirerFeeTaxFreeAmount();
            acquirerFeeBase += line.getAcquirerFeeBaseAmount();
            acquirerFeeTax += line.getAcquirerFeeTaxAmount();
            ourFeeBase += line.getOurFeeBaseAmount();
            ourFeeTax += line.getOurFeeTaxAmount();
        }

        void add(Totals other) {
            grossAmount += other.grossAmount;
            acquirerFeeTaxFree += other.acquirerFeeTaxFree;
            acquirerFeeBase += other.acquirerFeeBase;
            acquirerFeeTax += other.acquirerFeeTax;
            ourFeeBase += other.ourFeeBase;
            ourFeeTax += other.ourFeeTax;
        }
    }

    /** ワークブック内で使い回すセルスタイル。 */
    private static final class Styles {
        private final CellStyle title;
        private final CellStyle header;
        private final CellStyle amount;

        Styles(XSSFWorkbook workbook) {
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            title = workbook.createCellStyle();
            title.setFont(titleFont);
            title.setAlignment(HorizontalAlignment.CENTER);

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            header = workbook.createCellStyle();
            header.setFont(headerFont);
            header.setBorderBottom(BorderStyle.THIN);
            header.setBorderTop(BorderStyle.THIN);
            header.setWrapText(true);

            DataFormat format = workbook.createDataFormat();
            amount = workbook.createCellStyle();
            amount.setDataFormat(format.getFormat("#,##0"));
            amount.setBorderBottom(BorderStyle.HAIR);
        }
    }

}
