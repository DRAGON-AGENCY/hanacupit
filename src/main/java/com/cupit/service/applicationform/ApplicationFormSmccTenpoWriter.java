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
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * ApplicationFormSmccTenpoWriter
 * テンプレート（{@code application.form.template.dir}で指定した外部フォルダの
 * application_form_smcc_tenpo_template.xlsx）をそのまま読み込み、「【連携シート】」シートの
 * データ行（4行目〜）だけに値を書き込んで返す。
 * レイアウト・見出し・書式・数式等はテンプレート側にすべて委ね、
 * Java側は「どの行のどの列に何を書くか」だけを持つ
 * （{@link com.cupit.service.settlement.SalesReportXlsxWriter 実装パターン参照}）。
 */
@Component
public class ApplicationFormSmccTenpoWriter {

    private static final String TEMPLATE_FILE_NAME = "application_form_smcc_tenpo_template.xlsx";
    private static final String SHEET_NAME = "【連携シート】";
    private static final int DATA_START_ROW = 4;

    private static final List<ApplicationFormFieldMapping> MAPPINGS = List.of(
        new ApplicationFormFieldMapping(
                1, ApplicationFormFieldMapping.SourceType.SYSTEM,
                List.of("ROW_SEQUENCE"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                2, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("store_name"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                3, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("store_name_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                4, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("store_name_alphabet"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                5, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_tel"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                6, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_zip"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                7, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_pref", "addr_city", "addr_town", "addr_block", "addr_building"),
                ApplicationFormFieldMapping.Transform.CONCAT_ADDRESS),
        new ApplicationFormFieldMapping(
                8, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of(
                        "addr_pref_kana",
                        "addr_city_kana",
                        "addr_town_kana",
                        "addr_block_kana",
                        "addr_building_kana"
                ),
                ApplicationFormFieldMapping.Transform.CONCAT_ADDRESS_KANA),
        new ApplicationFormFieldMapping(
                9, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("store_name_kana_short", "store_name_kana"),
                ApplicationFormFieldMapping.Transform.TRUNCATE23_WITH_FALLBACK),
        new ApplicationFormFieldMapping(
                10, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_tel"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                11, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("mgmt_type"), ApplicationFormFieldMapping.Transform.CODE_MGMT_TYPE_NUMERIC),
        new ApplicationFormFieldMapping(
                12, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("corp_name"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                13, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("corp_name_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                14, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("corp_number"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                15, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("corp_pref", "corp_city", "corp_town", "corp_block", "corp_building"),
                ApplicationFormFieldMapping.Transform.CONCAT_ADDRESS),
        new ApplicationFormFieldMapping(
                16, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("rep_last_name", "rep_first_name"),
                ApplicationFormFieldMapping.Transform.CONCAT_NAME),
        new ApplicationFormFieldMapping(
                17, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("rep_last_name_kana", "rep_first_name_kana"),
                ApplicationFormFieldMapping.Transform.CONCAT_NAME_KANA),
        new ApplicationFormFieldMapping(
                18, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("rep_birth"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                19, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_tel"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                20, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("terminal_count"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                21, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("line_type"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                22, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("contact_first_name"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                23, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("contact_tel"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                24, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("pos_connection_flag"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                25, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("pos_maker_name"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                26, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("pos_vendor_contact_name"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                27, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("pos_vendor_contact_tel"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                28, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("d_point_usage_flag"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                29, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("d_point_merchant_code"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                30, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("d_point_store_code"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                31, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("d_point_branch_code"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                32, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("visa_master_merchant_number"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                33, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("nanaco_merchant_number"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                34, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("id_merchant_number"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                35, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("transit_merchant_number"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                36, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("unionpay_merchant_number"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                37, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("waon_merchant_number"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                38, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("edy_merchant_number"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                39, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("nfc_merchant_number"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                40, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("transit_operator"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                41, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("edy_id"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                42, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("stera_terminal_number_1"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                43, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("stera_terminal_number_2"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                44, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("stera_terminal_number_3"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                45, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("stera_terminal_number_4"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                46, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("stera_terminal_number_5"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                47, ApplicationFormFieldMapping.SourceType.SYSTEM,
                List.of("SKIP"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                48, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("jcb_usage_flag"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                49, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("smart_code_connection_flag"), ApplicationFormFieldMapping.Transform.DIRECT)
    );

    private final String templateDir;
    private final ApplicationFormFieldResolver resolver;

    public ApplicationFormSmccTenpoWriter(
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

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("ApplicationFormSmccTenpoWriter: 出力Excelの作成に失敗しました。", e);
        }
    }

    /**
     * テンプレートのデータ行（4行目〜）に埋め込まれたサンプルデータが、実データを
     * 書き込まなかった行・列にそのまま残って出力に混入しないよう空にする。
     * SKIP（重複チェック等、テンプレート側の数式に委ねる列）は対象外。
     */
    private void clearSampleData(Sheet sheet) {
        int lastRow = sheet.getLastRowNum() + 1;
        for (int excelRow = DATA_START_ROW; excelRow <= lastRow; excelRow++) {
            for (ApplicationFormFieldMapping mapping : MAPPINGS) {
                if (mapping.getSourceType() == ApplicationFormFieldMapping.SourceType.SYSTEM
                        && "SKIP".equals(mapping.getSource().get(0))) {
                    continue;
                }
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
