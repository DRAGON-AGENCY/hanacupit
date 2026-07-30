package com.cupit.service.applicationform;

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
 * ApplicationFormJcbWriter
 * テンプレート（{@code application.form.template.dir}で指定した外部フォルダの
 * application_form_jcb_template.xlsx）をそのまま読み込み、「【入力用】」シートの
 * データ行（6行目〜）だけに値を書き込んで返す。
 * レイアウト・見出し・書式・数式等はテンプレート側にすべて委ね、
 * Java側は「どの行のどの列に何を書くか」だけを持つ
 * （{@link com.cupit.service.settlement.SalesReportXlsxWriter 実装パターン参照}）。
 */
@Component
public class ApplicationFormJcbWriter {

    private static final String TEMPLATE_FILE_NAME = "application_form_jcb_template.xlsx";
    private static final String SHEET_NAME = "【入力用】";
    private static final int DATA_START_ROW = 6;

    private static final List<ApplicationFormFieldMapping> MAPPINGS = List.of(
        new ApplicationFormFieldMapping(
                2, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("jcb_application_classification"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                3, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("store_name"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                4, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("store_name_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                5, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("store_name_alphabet"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                6, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("hcp_town_url"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                7, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_zip"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                8, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_pref", "addr_city", "addr_town", "addr_block", "addr_building"),
                ApplicationFormFieldMapping.Transform.CONCAT_ADDRESS),
        new ApplicationFormFieldMapping(
                9, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of(
                        "addr_pref_kana",
                        "addr_city_kana",
                        "addr_town_kana",
                        "addr_block_kana",
                        "addr_building_kana"
                ),
                ApplicationFormFieldMapping.Transform.CONCAT_ADDRESS_KANA),
        new ApplicationFormFieldMapping(
                10, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_tel"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                11, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_tel"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                12, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("corp_legal_form", "corp_name"),
                ApplicationFormFieldMapping.Transform.CONCAT_CORP_NAME),
        new ApplicationFormFieldMapping(
                13, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("corp_legal_form_kana", "corp_name_kana"),
                ApplicationFormFieldMapping.Transform.CONCAT_CORP_NAME_KANA),
        new ApplicationFormFieldMapping(
                14, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("corp_zip"), ApplicationFormFieldMapping.Transform.HYPHEN_STRIP),
        new ApplicationFormFieldMapping(
                15, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("corp_pref", "corp_city", "corp_town", "corp_block", "corp_building"),
                ApplicationFormFieldMapping.Transform.CONCAT_CORP_ADDRESS),
        new ApplicationFormFieldMapping(
                16, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of(
                        "corp_pref_kana",
                        "corp_city_kana",
                        "corp_town_kana",
                        "corp_block_kana",
                        "corp_building_kana"
                ),
                ApplicationFormFieldMapping.Transform.CONCAT_CORP_ADDRESS_KANA),
        new ApplicationFormFieldMapping(
                17, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("rep_last_name", "rep_first_name"),
                ApplicationFormFieldMapping.Transform.CONCAT_NAME),
        new ApplicationFormFieldMapping(
                18, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("rep_last_name_kana", "rep_first_name_kana"),
                ApplicationFormFieldMapping.Transform.CONCAT_NAME_KANA),
        new ApplicationFormFieldMapping(
                19, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("rep_birth"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                20, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("rep_zip"), ApplicationFormFieldMapping.Transform.HYPHEN_STRIP),
        new ApplicationFormFieldMapping(
                21, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("rep_pref", "rep_city", "rep_town", "rep_block", "rep_building"),
                ApplicationFormFieldMapping.Transform.CONCAT_REP_ADDRESS),
        new ApplicationFormFieldMapping(
                22, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("rep_address_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                23, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_tel"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                24, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("app_industry_1", "app_industry_2", "app_industry_3"),
                ApplicationFormFieldMapping.Transform.CONCAT_INDUSTRY),
        new ApplicationFormFieldMapping(
                25, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("handling_items"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                26, ApplicationFormFieldMapping.SourceType.PAYGATE,
                List.of("jcb_merchant_no"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                27, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("mgmt_type"), ApplicationFormFieldMapping.Transform.CODE_MGMT_TYPE),
        new ApplicationFormFieldMapping(
                28, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("corp_number"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                29, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("door_to_door_sales_flag"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                30, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("telemarketing_sales_flag"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                31, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("chain_sales_flag"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                32, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("business_opportunity_sales_flag"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                33, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("continuous_service_flag"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                34, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("card_data_retention_status"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                35, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("pci_dss_compliance_status"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                36, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("non_retention_planned_month"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                37, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("pci_dss_compliance_planned_month"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                38, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("terminal_ic_status"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                39, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("terminal_ic_planned_month"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                40, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("acquirer_unique_key"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                41, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("terminal_id"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                42, ApplicationFormFieldMapping.SourceType.DERIVE,
                List.of("SYSTEM_DATE_SLASH"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                43, ApplicationFormFieldMapping.SourceType.DERIVE,
                List.of("EXISTING_CONTRACT_FLAG"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                44, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("classification"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                45, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("contract_source"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                46, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("gift_contract_flag"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                47, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("edy_contract_flag"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                48, ApplicationFormFieldMapping.SourceType.DERIVE,
                List.of("CANCEL_INTENTION"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                49, ApplicationFormFieldMapping.SourceType.DERIVE,
                List.of("CANCEL_STATUS"), ApplicationFormFieldMapping.Transform.DIRECT)
    );

    private final String templateDir;
    private final ApplicationFormFieldResolver resolver;

    public ApplicationFormJcbWriter(
            @Value("${application.form.template.dir}") String templateDir,
            ApplicationFormFieldResolver resolver) {
        this.templateDir = templateDir;
        this.resolver = resolver;
    }

    public byte[] write(List<ApplicationFormRowContext> rows) {
        Path templatePath = Path.of(templateDir, TEMPLATE_FILE_NAME);
        try (InputStream template = Files.newInputStream(templatePath);
                XSSFWorkbook workbook = new XSSFWorkbook(template)) {
            Sheet sheet = workbook.getSheet(SHEET_NAME);

            clearSampleData(sheet);
            for (int i = 0; i < rows.size(); i++) {
                writeDataRow(sheet, DATA_START_ROW + i, rows.get(i));
            }
            clearCalculationChain(workbook);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("ApplicationFormJcbWriter: 出力Excelの作成に失敗しました。", e);
        }
    }

    /**
     * データ行の書き込みでテンプレートの数式セルを上書き・削除すると、ブックの
     * 計算チェーン（xl/calcChain.xml）が実際のセル構成と不整合になり、Excelで
     * 開いた際に「一部の内容に問題が見つかりました」という修復ダイアログが
     * 表示されてしまう（実際にSMCC加盟店申込書で発生した不具合と同根のため、
     * 全Writerで対策する）。POIは計算チェーンの項目数が0件の場合のみ書き込み時に
     * このパートを自動的に除去するため、明示的に全項目をクリアして0件にする
     * （計算チェーンが無くてもExcelは開くたびに再計算するため問題ない）。
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

    /**
     * テンプレートの「入力例」行（7〜9行目等）に埋め込まれたサンプルデータが、
     * 実データを書き込まなかった行・列にそのまま残って出力に混入しないよう、
     * データ開始行以降・マッピング対象の全列を空にする。
     */
    private void clearSampleData(Sheet sheet) {
        int lastRow = sheet.getLastRowNum() + 1;
        for (int excelRow = DATA_START_ROW; excelRow <= lastRow; excelRow++) {
            clearCellValue(sheet, excelRow, 1); // 「項目名」列。マッピング対象外だが「入力例：01：新規出店」等の文言が入っている
            for (ApplicationFormFieldMapping mapping : MAPPINGS) {
                clearCellValue(sheet, excelRow, mapping.getExcelCol());
            }
        }
    }

    private void clearCellValue(Sheet sheet, int excelRow, int col) {
        Row row = sheet.getRow(excelRow - 1);
        if (row == null) {
            return;
        }
        Cell cell = row.getCell(col - 1);
        if (cell != null) {
            cell.setBlank();
        }
    }

    private void writeDataRow(Sheet sheet, int excelRow, ApplicationFormRowContext ctx) {
        for (ApplicationFormFieldMapping mapping : MAPPINGS) {
            String value = resolver.resolve(mapping, ctx);
            if (value == null) {
                continue;
            }
            setCellValue(sheet, excelRow, mapping.getExcelCol(), value);
        }
    }

    private void setCellValue(Sheet sheet, int excelRow, int col, String value) {
        Row row = sheet.getRow(excelRow - 1);
        if (row == null) {
            row = sheet.createRow(excelRow - 1);
        }
        Cell cell = row.getCell(col - 1);
        if (cell == null) {
            cell = row.createCell(col - 1);
        }
        cell.setCellValue(value);
    }

}
