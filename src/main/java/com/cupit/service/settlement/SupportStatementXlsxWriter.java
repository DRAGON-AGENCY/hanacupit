package com.cupit.service.settlement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cupit.model.JftdReportCompanyInfo;
import com.cupit.repository.JftdReportCompanyInfoRepository;

/**
 * 支払明細書（サンプル_支払明細書.xlsx）をテンプレートとしてそのまま読み込み、
 * 金額項目のセルにだけ値を設定して書き出す。レイアウト・ラベル・罫線等の見た目は
 * テンプレート（{@code report.template.dir}で指定した外部フォルダの
 * {@code support_statement_template.xlsx}）に完全に委ね、Java側は「どの行の
 * どの列に何を書くか」だけを持つ。将来サンプルのフォーマットが変わった場合は、
 * 外部フォルダのテンプレートファイルを差し替えるだけで対応でき、再ビルド・
 * 再デプロイは不要（行番号を変える場合はReportLineDefinition側の対応・再デプロイが必要）。
 * 行の並び・グルーピングはReportLineDefinition#supportStatementOrder()を参照。
 * 「(C)株式会社手数料」（手数料②、弊社→加盟店の上乗せ手数料）は計算式が未確定のため
 * 常に0円で出力する（調査メモ「論点・オープン事項」項番6を参照）。
 */
@Component
public class SupportStatementXlsxWriter {

    private static final String TEMPLATE_FILE_NAME = "support_statement_template.xlsx";

    private static final int COL_C_PERIOD = 2;
    private static final int COL_D_COUNT = 3;
    private static final int COL_E_GROSS = 4;
    private static final int COL_F_FEE_TAX_FREE = 5;
    private static final int COL_G_FEE_BASE = 6;
    private static final int COL_H_FEE_TAX = 7;
    private static final int COL_I_FEE_TOTAL = 8;
    private static final int COL_J_AFTER_FEE = 9;
    private static final int COL_K_OUR_FEE_BASE = 10;
    private static final int COL_L_OUR_FEE_TAX = 11;
    private static final int COL_M_OUR_FEE_TOTAL = 12;
    private static final int COL_N_TOTAL_FEE = 13;
    private static final int COL_O_NET_PAYABLE = 14;

    /** 各データ行の直後に「(2.60％)」等の手数料率注記行があるテンプレート上の行番号。 */
    private static final int[] FEE_RATE_NOTE_ROWS = {
        18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 41, 43, 45, 47, 49, 79
    };

    private static final int ROW_GRAND_TOTAL = 107;
    private static final int ROW_MEMO_TAX_FREE_TOTAL = 108;
    private static final int ROW_MEMO_TAXABLE_TOTAL = 109;
    private static final int ROW_MEMO_TAX_TOTAL = 110;

    private final JftdReportCompanyInfoRepository companyInfoRepository;

    private final String templateDir;

    public SupportStatementXlsxWriter(
            JftdReportCompanyInfoRepository companyInfoRepository,
            @Value("${report.template.dir}") String templateDir) {
        this.companyInfoRepository = companyInfoRepository;
        this.templateDir = templateDir;
    }

    /**
     * @param paymentDate 「お支払日」欄に出力する日付。確定日時（振込CSV確定日）を
     *                    そのまま使う運用とする（ユーザー確認済み）。
     */
    public byte[] write(List<ReportRow> rows, LocalDate paymentDate) {
        Path templatePath = Path.of(templateDir, TEMPLATE_FILE_NAME);
        try (InputStream template = Files.newInputStream(templatePath);
                XSSFWorkbook workbook = new XSSFWorkbook(template)) {
            workbook.setForceFormulaRecalculation(true);
            Sheet sheet = workbook.getSheetAt(0);
            JftdReportCompanyInfo info = companyInfoRepository.findById(1).orElse(null);

            int netPayableTotal = writeDataRows(sheet, rows);
            writeLetterhead(sheet, info, paymentDate, netPayableTotal);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("支払明細書の作成に失敗しました。", e);
        }
    }

    private void writeLetterhead(
            Sheet sheet, JftdReportCompanyInfo info, LocalDate paymentDate, int paymentAmountTotal) {
        setCellValue(sheet, 2, 14, LocalDate.now());
        setCellValue(sheet, 12, 2, paymentAmountTotal);

        if (info != null) {
            setCellValue(sheet, 4, 1, "〒" + nullToEmpty(info.getRecipientZip()));
            setCellValue(sheet, 5, 1, nullToEmpty(info.getRecipientAddress()));
            setCellValue(sheet, 6, 1, nullToEmpty(info.getRecipientName()));
            setCellValue(sheet, 7, 1, "登録番号：" + nullToEmpty(info.getRecipientInvoiceNo()));

            setCellValue(sheet, 4, 11, "〒" + nullToEmpty(info.getSenderZip()));
            setCellValue(sheet, 5, 11, nullToEmpty(info.getSenderAddress()));
            setCellValue(sheet, 6, 11, nullToEmpty(info.getSenderName()));
            setCellValue(sheet, 7, 11, "登録番号：" + nullToEmpty(info.getSenderInvoiceNo()));
            setCellValue(sheet, 8, 11, "TEL：" + nullToEmpty(info.getSenderTel())
                    + "　FAX：" + nullToEmpty(info.getSenderFax()));
            setCellValue(sheet, 9, 11, "担当：" + nullToEmpty(info.getSenderContact()));

            setCellValue(sheet, 12, 4, nullToEmpty(info.getBankName()));
            setCellValue(sheet, 12, 5, nullToEmpty(info.getBankBranchName()));
            setCellValue(sheet, 12, 6, nullToEmpty(info.getBankAccountType()));
            setCellValue(sheet, 12, 7, nullToEmpty(info.getBankAccountNumber()));
            setCellValue(sheet, 12, 8, nullToEmpty(info.getBankAccountHolderKana()));
        }

        setCellValue(sheet, 12, 1, paymentDate);
    }

    /**
     * @return 総合計の差引振込額（お支払金額合計欄に転記する値）
     */
    private int writeDataRows(Sheet sheet, List<ReportRow> rows) {
        List<ReportLineDefinition> definitions = ReportLineDefinition.supportStatementOrder();
        List<ResolvedReportLine> lines = ResolvedReportLine.resolveAll(definitions, rows);

        Totals subtotal = new Totals();
        Totals grandTotal = new Totals();

        for (int i = 0; i < definitions.size(); i++) {
            ReportLineDefinition def = definitions.get(i);
            if (def.isSubtotalMarker()) {
                writeTotalsRow(sheet, def.getRow(), subtotal);
                grandTotal.add(subtotal);
                subtotal = new Totals();
                continue;
            }
            ResolvedReportLine line = lines.get(i);
            writeDataRow(sheet, def.getRow(), line);
            subtotal.addLine(line);
        }

        writeTotalsRow(sheet, ROW_GRAND_TOTAL, grandTotal);
        clearFeeRateNoteRows(sheet);

        setCellValue(sheet, ROW_MEMO_TAX_FREE_TOTAL, COL_N_TOTAL_FEE, grandTotal.acquirerFeeTaxFree);
        setCellValue(sheet, ROW_MEMO_TAXABLE_TOTAL, COL_N_TOTAL_FEE,
                grandTotal.acquirerFeeBase + grandTotal.ourFeeBase);
        setCellValue(sheet, ROW_MEMO_TAX_TOTAL, COL_N_TOTAL_FEE,
                grandTotal.acquirerFeeTax + grandTotal.ourFeeTax);

        int feeTotal = grandTotal.acquirerFeeTaxFree + grandTotal.acquirerFeeBase + grandTotal.acquirerFeeTax
                + grandTotal.ourFeeBase + grandTotal.ourFeeTax;
        return grandTotal.grossAmount - feeTotal;
    }

    private void writeDataRow(Sheet sheet, int excelRow, ResolvedReportLine line) {
        blankCell(sheet, excelRow, COL_C_PERIOD);
        setCellValue(sheet, excelRow, COL_D_COUNT, line.getCount());
        setCellValue(sheet, excelRow, COL_E_GROSS, line.getGrossAmount());
        setCellValue(sheet, excelRow, COL_F_FEE_TAX_FREE, line.getAcquirerFeeTaxFreeAmount());
        setCellValue(sheet, excelRow, COL_G_FEE_BASE, line.getAcquirerFeeBaseAmount());
        setCellValue(sheet, excelRow, COL_H_FEE_TAX, line.getAcquirerFeeTaxAmount());
        setCellValue(sheet, excelRow, COL_I_FEE_TOTAL, line.getAcquirerFeeTotal());
        setCellValue(sheet, excelRow, COL_J_AFTER_FEE, line.getAfterAcquirerFeeAmount());
        setCellValue(sheet, excelRow, COL_K_OUR_FEE_BASE, line.getOurFeeBaseAmount());
        setCellValue(sheet, excelRow, COL_L_OUR_FEE_TAX, line.getOurFeeTaxAmount());
        setCellValue(sheet, excelRow, COL_M_OUR_FEE_TOTAL, line.getOurFeeTotal());
        setCellValue(sheet, excelRow, COL_N_TOTAL_FEE, line.getTotalFeeAmount());
        setCellValue(sheet, excelRow, COL_O_NET_PAYABLE, line.getNetPayableAmount());
    }

    private void writeTotalsRow(Sheet sheet, int excelRow, Totals totals) {
        setCellValue(sheet, excelRow, COL_E_GROSS, totals.grossAmount);
        setCellValue(sheet, excelRow, COL_I_FEE_TOTAL,
                totals.acquirerFeeTaxFree + totals.acquirerFeeBase + totals.acquirerFeeTax);
        setCellValue(sheet, excelRow, COL_J_AFTER_FEE, totals.grossAmount
                - totals.acquirerFeeTaxFree - totals.acquirerFeeBase - totals.acquirerFeeTax);
        int ourFeeTotal = totals.ourFeeBase + totals.ourFeeTax;
        setCellValue(sheet, excelRow, COL_M_OUR_FEE_TOTAL, ourFeeTotal);
        int feeTotal = totals.acquirerFeeTaxFree + totals.acquirerFeeBase + totals.acquirerFeeTax + ourFeeTotal;
        setCellValue(sheet, excelRow, COL_N_TOTAL_FEE, feeTotal);
        setCellValue(sheet, excelRow, COL_O_NET_PAYABLE, totals.grossAmount - feeTotal);
    }

    /**
     * テンプレートのサンプルデータに含まれる手数料率の注記（「(2.60％)」等）は、
     * 実データに基づく値ではなく手入力のサンプル値であり、かつI列側は決済事業者の
     * 契約レートを都度参照しないと正しい値を再現できない。誤った値をそのまま残すと
     * 実際の集計結果と矛盾するため、空欄にクリアする。
     */
    private void clearFeeRateNoteRows(Sheet sheet) {
        for (int excelRow : FEE_RATE_NOTE_ROWS) {
            blankCell(sheet, excelRow, COL_I_FEE_TOTAL);
            blankCell(sheet, excelRow, COL_M_OUR_FEE_TOTAL);
        }
    }

    private void blankCell(Sheet sheet, int excelRow, int col) {
        Cell cell = getCell(sheet, excelRow, col);
        if (cell != null) {
            cell.setBlank();
        }
    }

    private void setCellValue(Sheet sheet, int excelRow, int col, int value) {
        Cell cell = getOrCreateCell(sheet, excelRow, col);
        cell.setCellValue(value);
    }

    private void setCellValue(Sheet sheet, int excelRow, int col, String value) {
        Cell cell = getOrCreateCell(sheet, excelRow, col);
        cell.setCellValue(value);
    }

    private void setCellValue(Sheet sheet, int excelRow, int col, LocalDate value) {
        Cell cell = getOrCreateCell(sheet, excelRow, col);
        cell.setCellValue(value);
    }

    private Cell getCell(Sheet sheet, int excelRow, int col) {
        Row row = sheet.getRow(excelRow - 1);
        return row != null ? row.getCell(col) : null;
    }

    private Cell getOrCreateCell(Sheet sheet, int excelRow, int col) {
        Row row = sheet.getRow(excelRow - 1);
        if (row == null) {
            row = sheet.createRow(excelRow - 1);
        }
        Cell cell = row.getCell(col);
        return cell != null ? cell : row.createCell(col);
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

}
