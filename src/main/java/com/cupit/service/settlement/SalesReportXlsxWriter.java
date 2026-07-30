package com.cupit.service.settlement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.model.CalculationChain;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCalcChain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 売上報告書（サンプル_売上報告書.xlsx）をテンプレートとしてそのまま読み込み、
 * 金額項目のセルにだけ値を設定して書き出す。レイアウト・ラベル・罫線・合計行の
 * SUM式（D30〜K30）等の見た目はテンプレート（{@code report.template.dir}で
 * 指定した外部フォルダの{@code sales_report_template.xlsx}）に完全に委ね、
 * Java側は「どの行のどの列に何を書くか」だけを持つ。将来サンプルのフォーマットが
 * 変わった場合は、外部フォルダのテンプレートファイルを差し替えるだけで対応でき、
 * 再ビルド・再デプロイは不要（行番号を変える場合はReportLineDefinition側の対応・
 * 再デプロイが必要）。
 * 行の並びはReportLineDefinition#salesReportOrder()を参照（小計グループ無しの
 * 単一リスト。合計行は既存のSUM式に計算を任せるためJava側では書き込まない）。
 * 「弊社手数料」（手数料②、弊社→加盟店の上乗せ手数料）は計算式が未確定のため
 * 常に0円で出力する（調査メモ「論点・オープン事項」項番6を参照）。
 */
@Component
public class SalesReportXlsxWriter {

    private static final String TEMPLATE_FILE_NAME = "sales_report_template.xlsx";

    private static final int COL_B_PERIOD = 1;
    private static final int COL_D_GROSS = 3;
    private static final int COL_E_COUNT = 4;
    private static final int COL_F_FEE_TAX_FREE = 5;
    private static final int COL_G_FEE_BASE_COMBINED = 6;
    private static final int COL_H_FEE_TAX_COMBINED = 7;
    private static final int COL_I_ACQUIRER_TAX_FREE = 8;
    private static final int COL_J_ACQUIRER_BASE = 9;
    private static final int COL_K_ACQUIRER_TAX = 10;
    private static final int COL_L_NET_PAYABLE = 11;

    private final String templateDir;

    public SalesReportXlsxWriter(@Value("${report.template.dir}") String templateDir) {
        this.templateDir = templateDir;
    }

    public byte[] write(List<ReportRow> rows) {
        Path templatePath = Path.of(templateDir, TEMPLATE_FILE_NAME);
        try (InputStream template = Files.newInputStream(templatePath);
                XSSFWorkbook workbook = new XSSFWorkbook(template)) {
            workbook.setForceFormulaRecalculation(true);
            Sheet sheet = workbook.getSheetAt(0);

            writeDataRows(sheet, rows);
            clearCalculationChain(workbook);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("売上報告書の作成に失敗しました。", e);
        }
    }

    /**
     * データ行の書き込みでテンプレートの数式セルを上書き・削除すると、ブックの
     * 計算チェーン（xl/calcChain.xml）が実際のセル構成と不整合になり、Excelで
     * 開いた際に「一部の内容に問題が見つかりました」という修復ダイアログが
     * 表示されることがある（各決済会社所定申込フォーム作成のSMCC加盟店申込書で
     * 実際に発生した不具合と同根のため、同じ計算チェーンを持つ本テンプレートにも
     * 予防的に対策する）。POIは計算チェーンの項目数が0件の場合のみ書き込み時に
     * このパートを自動的に除去するため、明示的に全項目をクリアして0件にする
     * （計算チェーンが無くてもExcelは開くたびに再計算するため問題ない。なお
     * {@code setForceFormulaRecalculation(true)}は再計算を強制するだけで、
     * 計算チェーンパート自体の不整合は解消しないため、この対策とは別に必要）。
     */
    private void clearCalculationChain(XSSFWorkbook workbook) {
        CalculationChain calcChain = workbook.getCalculationChain();
        if (calcChain == null) {
            return;
        }
        CTCalcChain ctCalcChain = calcChain.getCTCalcChain();
        while (ctCalcChain.sizeOfCArray() > 0) {
            ctCalcChain.removeC(0);
        }
    }

    private void writeDataRows(Sheet sheet, List<ReportRow> rows) {
        List<ReportLineDefinition> definitions = ReportLineDefinition.salesReportOrder();
        List<ResolvedReportLine> lines = ResolvedReportLine.resolveAll(definitions, rows);

        for (int i = 0; i < definitions.size(); i++) {
            writeDataRow(sheet, definitions.get(i).getRow(), lines.get(i));
        }
    }

    private void writeDataRow(Sheet sheet, int excelRow, ResolvedReportLine line) {
        blankCell(sheet, excelRow, COL_B_PERIOD);
        setCellValue(sheet, excelRow, COL_D_GROSS, line.getGrossAmount());
        setCellValue(sheet, excelRow, COL_E_COUNT, line.getCount());
        setCellValue(sheet, excelRow, COL_F_FEE_TAX_FREE, line.getAcquirerFeeTaxFreeAmount());
        setCellValue(sheet, excelRow, COL_G_FEE_BASE_COMBINED,
                line.getAcquirerFeeBaseAmount() + line.getOurFeeBaseAmount());
        setCellValue(sheet, excelRow, COL_H_FEE_TAX_COMBINED,
                line.getAcquirerFeeTaxAmount() + line.getOurFeeTaxAmount());
        setCellValue(sheet, excelRow, COL_I_ACQUIRER_TAX_FREE, line.getAcquirerFeeTaxFreeAmount());
        setCellValue(sheet, excelRow, COL_J_ACQUIRER_BASE, line.getAcquirerFeeBaseAmount());
        setCellValue(sheet, excelRow, COL_K_ACQUIRER_TAX, line.getAcquirerFeeTaxAmount());
        setCellValue(sheet, excelRow, COL_L_NET_PAYABLE, line.getNetPayableAmount());
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

}
