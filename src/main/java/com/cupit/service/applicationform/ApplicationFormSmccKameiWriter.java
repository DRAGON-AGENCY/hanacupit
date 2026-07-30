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
 * ApplicationFormSmccKameiWriter
 * テンプレート（{@code application.form.template.dir}で指定した外部フォルダの
 * application_form_smcc_kamei_template.xlsm）をそのまま読み込み、「入力シート」シートの
 * データ行（7行目〜）だけに値を書き込んで返す。
 * レイアウト・見出し・書式・マクロ等はテンプレート側にすべて委ね、
 * Java側は「どの行のどの列に何を書くか」だけを持つ
 * （{@link com.cupit.service.settlement.SalesReportXlsxWriter 実装パターン参照}）。
 * マクロ有効ブック(.xlsm)のため、読み込み・書き込みともXSSFWorkbookで扱い、
 * VBAプロジェクトを含むxl/vbaProject.binはPOIにより自動的に保持される。
 */
@Component
public class ApplicationFormSmccKameiWriter {

    private static final String TEMPLATE_FILE_NAME = "application_form_smcc_kamei_template.xlsm";
    private static final String SHEET_NAME = "入力シート";
    private static final int DATA_START_ROW = 7;

    private static final List<ApplicationFormFieldMapping> MAPPINGS = List.of(
        new ApplicationFormFieldMapping(
                2, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("smcc_application_classification"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                3, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("applicant_type"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                4, ApplicationFormFieldMapping.SourceType.DERIVE,
                List.of("SYSTEM_DATE_SLASH"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                5, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("service_start_desired_date"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                6, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("service_end_date"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                7, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("corp_name", "store_name"), ApplicationFormFieldMapping.Transform.CORP_OR_STORE_NAME),
        new ApplicationFormFieldMapping(
                8, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("store_name_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                9, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("store_name"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                10, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("store_name_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                11, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("store_name_alphabet"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                12, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_tel"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                13, ApplicationFormFieldMapping.SourceType.PAYGATE,
                List.of("jcb_merchant_no"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                14, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("vm_merchant_number"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                15, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("terminal_id"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                16, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("closing_date_1"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                17, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("payment_date_1"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                18, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("closing_date_2"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                19, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("payment_date_2"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                20, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("settlement_cycle"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                21, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("bank_code"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                22, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("bank_name"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                23, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("bank_name_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                24, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("branch_code"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                25, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("branch_name"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                26, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("branch_name_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                27, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("account_type"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                28, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("account_number"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                29, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("account_holder"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                30, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("account_holder_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                31, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("contact_last_name"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                32, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("contact_first_name"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                33, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("contact_last_name_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                34, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("contact_first_name_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                35, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_tel", "order_delivery_tel"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                36, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("office_contact_email"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                37, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("merchant_type"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                38, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("franchise_flag"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                39, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("paypay_fc_agreement_flag"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                40, ApplicationFormFieldMapping.SourceType.DERIVE,
                List.of("DEFAULT_STORE_COUNT_1"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                41, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("store_count"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                42, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("terminal_type"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                43, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("corp_legal_form", "corp_name"),
                ApplicationFormFieldMapping.Transform.CONCAT_CORP_NAME),
        new ApplicationFormFieldMapping(
                44, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("corp_legal_form_kana", "corp_name_kana"),
                ApplicationFormFieldMapping.Transform.CONCAT_CORP_NAME_KANA),
        new ApplicationFormFieldMapping(
                45, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("corp_name_alphabet"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                46, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("store_annual_sales_yen"), ApplicationFormFieldMapping.Transform.DIVIDE_10000),
        new ApplicationFormFieldMapping(
                47, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("industry_category_major"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                48, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("industry_category_minor"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                49, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("mgmt_type"), ApplicationFormFieldMapping.Transform.CODE_MGMT_TYPE),
        new ApplicationFormFieldMapping(
                50, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("corp_number"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                51, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("store_founded_date", "parent_founded_date"),
                ApplicationFormFieldMapping.Transform.STORE_FOUNDED_DATE_8),
        new ApplicationFormFieldMapping(
                52, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_tel"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                54, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("hcp_town_url"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                55, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("corp_zip", "addr_zip"), ApplicationFormFieldMapping.Transform.ZIP_CORP_OR_STORE),
        new ApplicationFormFieldMapping(
                56, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_pref"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                57, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_city"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                58, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_town"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                59, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_block"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                60, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_building"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                61, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_pref_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                62, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_city_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                63, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_town_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                64, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_block_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                65, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_building_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                66, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("rep_last_name"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                67, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("rep_first_name"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                68, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("rep_last_name_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                69, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("rep_first_name_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                70, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("rep_last_name_alphabet"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                71, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("rep_first_name_alphabet"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                72, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("rep_birth"), ApplicationFormFieldMapping.Transform.DATE_8_TO_SLASH),
        new ApplicationFormFieldMapping(
                73, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("rep_gender"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                74, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_tel"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                75, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("rep_zip"), ApplicationFormFieldMapping.Transform.HYPHEN_STRIP),
        new ApplicationFormFieldMapping(
                76, ApplicationFormFieldMapping.SourceType.MANUAL,
                List.of("rep_address"), ApplicationFormFieldMapping.Transform.MANUAL),
        new ApplicationFormFieldMapping(
                77, ApplicationFormFieldMapping.SourceType.MANUAL,
                List.of("rep_address"), ApplicationFormFieldMapping.Transform.MANUAL),
        new ApplicationFormFieldMapping(
                78, ApplicationFormFieldMapping.SourceType.MANUAL,
                List.of("rep_address"), ApplicationFormFieldMapping.Transform.MANUAL),
        new ApplicationFormFieldMapping(
                79, ApplicationFormFieldMapping.SourceType.MANUAL,
                List.of("rep_address"), ApplicationFormFieldMapping.Transform.MANUAL),
        new ApplicationFormFieldMapping(
                80, ApplicationFormFieldMapping.SourceType.MANUAL,
                List.of("rep_address"), ApplicationFormFieldMapping.Transform.MANUAL),
        new ApplicationFormFieldMapping(
                81, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("rep_addr_pref_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                82, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("rep_addr_city_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                83, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("rep_addr_town_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                84, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("rep_addr_block_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                85, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("rep_addr_building_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                86, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("door_to_door_sales_flag"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                87, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("continuous_service_flag"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                88, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("telemarketing_sales_flag"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                89, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("chain_sales_flag"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                90, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("business_opportunity_sales_flag"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                91, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("prepaid_transaction_flag"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                92, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("legal_violation_history_flag"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                93, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fc_store_type"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                94, ApplicationFormFieldMapping.SourceType.DERIVE,
                List.of("DEFAULT_REPRESENTATIVE_STORE_FLAG"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                95, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("store_industry_major"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                96, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("store_industry_minor"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                97, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("handling_items"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                98, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("secondhand_dealer_license_number"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                99, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("store_name"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                100, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("store_name_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                101, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("store_name_alphabet"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                102, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_zip"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                103, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_pref"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                104, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_city"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                105, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_town"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                106, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_block"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                107, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_building"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                108, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_pref_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                109, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_city_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                110, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_town_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                111, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_block_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                112, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_building_kana"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                113, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("addr_tel"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                114, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("map_listing_flag"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                115, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("map_listing_desired_date_dpay_rakuten"),
                ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                116, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("map_listing_desired_date_paypay_aupay"),
                ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                117, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("store_image_listing_flag"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                118, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("store_image_url"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                119, ApplicationFormFieldMapping.SourceType.MEMBER_INFO,
                List.of("hcp_town_url"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                120, ApplicationFormFieldMapping.SourceType.MANUAL,
                List.of("regular_holiday"), ApplicationFormFieldMapping.Transform.MANUAL),
        new ApplicationFormFieldMapping(
                121, ApplicationFormFieldMapping.SourceType.MANUAL,
                List.of("regular_holiday"), ApplicationFormFieldMapping.Transform.MANUAL),
        new ApplicationFormFieldMapping(
                122, ApplicationFormFieldMapping.SourceType.MANUAL,
                List.of("regular_holiday"), ApplicationFormFieldMapping.Transform.MANUAL),
        new ApplicationFormFieldMapping(
                123, ApplicationFormFieldMapping.SourceType.MANUAL,
                List.of("regular_holiday"), ApplicationFormFieldMapping.Transform.MANUAL),
        new ApplicationFormFieldMapping(
                124, ApplicationFormFieldMapping.SourceType.MANUAL,
                List.of("regular_holiday"), ApplicationFormFieldMapping.Transform.MANUAL),
        new ApplicationFormFieldMapping(
                125, ApplicationFormFieldMapping.SourceType.MANUAL,
                List.of("regular_holiday"), ApplicationFormFieldMapping.Transform.MANUAL),
        new ApplicationFormFieldMapping(
                126, ApplicationFormFieldMapping.SourceType.MANUAL,
                List.of("regular_holiday"), ApplicationFormFieldMapping.Transform.MANUAL),
        new ApplicationFormFieldMapping(
                127, ApplicationFormFieldMapping.SourceType.MANUAL,
                List.of("business_hours_weekday"), ApplicationFormFieldMapping.Transform.MANUAL),
        new ApplicationFormFieldMapping(
                128, ApplicationFormFieldMapping.SourceType.MANUAL,
                List.of("business_hours_weekday"), ApplicationFormFieldMapping.Transform.MANUAL),
        new ApplicationFormFieldMapping(
                129, ApplicationFormFieldMapping.SourceType.MANUAL,
                List.of("business_hours_other"), ApplicationFormFieldMapping.Transform.MANUAL),
        new ApplicationFormFieldMapping(
                130, ApplicationFormFieldMapping.SourceType.MANUAL,
                List.of("business_hours_other"), ApplicationFormFieldMapping.Transform.MANUAL),
        new ApplicationFormFieldMapping(
                131, ApplicationFormFieldMapping.SourceType.MANUAL,
                List.of("business_hours_weekday"), ApplicationFormFieldMapping.Transform.MANUAL),
        new ApplicationFormFieldMapping(
                132, ApplicationFormFieldMapping.SourceType.MANUAL,
                List.of("business_hours_weekday"), ApplicationFormFieldMapping.Transform.MANUAL),
        new ApplicationFormFieldMapping(
                133, ApplicationFormFieldMapping.SourceType.MANUAL,
                List.of("business_hours_weekday"), ApplicationFormFieldMapping.Transform.MANUAL),
        new ApplicationFormFieldMapping(
                134, ApplicationFormFieldMapping.SourceType.MANUAL,
                List.of("business_hours_weekday"), ApplicationFormFieldMapping.Transform.MANUAL),
        new ApplicationFormFieldMapping(
                135, ApplicationFormFieldMapping.SourceType.MANUAL,
                List.of("business_hours_weekday"), ApplicationFormFieldMapping.Transform.MANUAL),
        new ApplicationFormFieldMapping(
                136, ApplicationFormFieldMapping.SourceType.MANUAL,
                List.of("business_hours_weekday"), ApplicationFormFieldMapping.Transform.MANUAL),
        new ApplicationFormFieldMapping(
                137, ApplicationFormFieldMapping.SourceType.MANUAL,
                List.of("business_hours_weekday"), ApplicationFormFieldMapping.Transform.MANUAL),
        new ApplicationFormFieldMapping(
                138, ApplicationFormFieldMapping.SourceType.MANUAL,
                List.of("business_hours_weekday"), ApplicationFormFieldMapping.Transform.MANUAL),
        new ApplicationFormFieldMapping(
                139, ApplicationFormFieldMapping.SourceType.MANUAL,
                List.of("business_hours_weekday"), ApplicationFormFieldMapping.Transform.MANUAL),
        new ApplicationFormFieldMapping(
                140, ApplicationFormFieldMapping.SourceType.MANUAL,
                List.of("business_hours_weekday"), ApplicationFormFieldMapping.Transform.MANUAL),
        new ApplicationFormFieldMapping(
                141, ApplicationFormFieldMapping.SourceType.MANUAL,
                List.of("business_hours_other"), ApplicationFormFieldMapping.Transform.MANUAL),
        new ApplicationFormFieldMapping(
                142, ApplicationFormFieldMapping.SourceType.MANUAL,
                List.of("business_hours_other"), ApplicationFormFieldMapping.Transform.MANUAL),
        new ApplicationFormFieldMapping(
                143, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("store_introduction"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                144, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_rakuten_pay"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                145, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_line_pay"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                146, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_paypay"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                147, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_d_barai"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                148, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_au_pay"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                149, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_merpay"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                150, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_yucho_pay"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                151, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_aeon_pay"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                152, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("atokara_rate"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                153, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_mdr_1"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                154, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_mdr_3"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                155, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_mdr_4"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                156, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_installment_5"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                157, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_installment_6"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                158, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_installment_10"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                159, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_installment_12"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                160, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_installment_15"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                161, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_installment_18"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                162, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_installment_20"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                163, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_installment_24"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                164, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_installment_30"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                165, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_installment_36"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                166, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_wesmo"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                167, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_bank_pay"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                168, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_wechat"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                169, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_alipay"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                170, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_unionpay_qr"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                171, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_brand_rakuten_pay"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                172, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_brand_line_pay"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                173, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_brand_paypay"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                174, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_brand_d_barai"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                175, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_brand_au_pay"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                176, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_brand_merpay"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                177, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_brand_yucho_pay"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                178, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_brand_aeon_pay"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                179, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("atokara_wholesale_rate"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                180, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("merchant_installment_fee_1"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                181, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("merchant_installment_fee_3"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                182, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("merchant_installment_fee_4"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                183, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("merchant_installment_fee_5"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                184, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("merchant_installment_fee_6"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                185, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("merchant_installment_fee_10"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                186, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("merchant_installment_fee_12"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                187, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("merchant_installment_fee_15"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                188, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("merchant_installment_fee_18"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                189, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("merchant_installment_fee_20"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                190, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("merchant_installment_fee_24"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                191, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("merchant_installment_fee_30"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                192, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("merchant_installment_fee_36"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                193, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_brand_wechat"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                194, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_brand_alipay"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                195, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_brand_wesmo"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                196, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("atokara_customer_rate"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                197, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("customer_installment_fee_1"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                198, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("customer_installment_fee_3"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                199, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("customer_installment_fee_4"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                200, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("customer_installment_fee_5"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                201, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("customer_installment_fee_6"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                202, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("customer_installment_fee_10"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                203, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("customer_installment_fee_12"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                204, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("customer_installment_fee_15"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                205, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("customer_installment_fee_18"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                206, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("customer_installment_fee_20"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                207, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("customer_installment_fee_24"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                208, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("customer_installment_fee_30"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                209, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("customer_installment_fee_36"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                210, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("cost_share_flag_rakuten_pay"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                211, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("cost_share_flag_line_pay"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                212, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("cost_share_flag_paypay"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                213, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("cost_share_flag_d_barai"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                214, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("cost_share_flag_au_pay"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                215, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("cost_share_flag_merpay"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                216, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("cost_share_flag_yucho_pay"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                217, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("cost_share_flag_aeon_pay"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                218, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("cost_share_flag_wesmo"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                219, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("unionpay_qr_merchant_number"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                220, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("aw_merchant_number"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                221, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("d_barai_ipid"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                222, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("alipay_pid"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                223, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("unionpay_qr_mid"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                224, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("smcc_department"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                225, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("smcc_contact_name"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                226, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("relocation_representative_merchant_number"),
                ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                227, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("relocation_platform_merchant_number"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                228, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("cancel_and_new_representative_merchant_number"),
                ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                229, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("cancel_and_new_platform_merchant_number"),
                ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                230, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("change_notes"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                231, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("cafis_arch_terminal_count"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                232, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("quo_card_pay_merchant_rate_nss"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                233, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("quo_card_pay_brand_rate_nss"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                234, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("bank_pay_nss_tid"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                235, ApplicationFormFieldMapping.SourceType.PROTECTED,
                List.of(), ApplicationFormFieldMapping.Transform.NONE),
        new ApplicationFormFieldMapping(
                236, ApplicationFormFieldMapping.SourceType.PROTECTED,
                List.of(), ApplicationFormFieldMapping.Transform.NONE),
        new ApplicationFormFieldMapping(
                237, ApplicationFormFieldMapping.SourceType.PROTECTED,
                List.of(), ApplicationFormFieldMapping.Transform.NONE),
        new ApplicationFormFieldMapping(
                238, ApplicationFormFieldMapping.SourceType.PROTECTED,
                List.of(), ApplicationFormFieldMapping.Transform.NONE),
        new ApplicationFormFieldMapping(
                239, ApplicationFormFieldMapping.SourceType.PROTECTED,
                List.of(), ApplicationFormFieldMapping.Transform.NONE),
        new ApplicationFormFieldMapping(
                240, ApplicationFormFieldMapping.SourceType.PROTECTED,
                List.of(), ApplicationFormFieldMapping.Transform.NONE),
        new ApplicationFormFieldMapping(
                241, ApplicationFormFieldMapping.SourceType.PROTECTED,
                List.of(), ApplicationFormFieldMapping.Transform.NONE),
        new ApplicationFormFieldMapping(
                242, ApplicationFormFieldMapping.SourceType.PROTECTED,
                List.of(), ApplicationFormFieldMapping.Transform.NONE),
        new ApplicationFormFieldMapping(
                243, ApplicationFormFieldMapping.SourceType.PROTECTED,
                List.of(), ApplicationFormFieldMapping.Transform.NONE),
        new ApplicationFormFieldMapping(
                244, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("smart_code_flag"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                245, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("mkp_flag"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                246, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_jcoin_pay_merchant"), ApplicationFormFieldMapping.Transform.DIRECT),
        new ApplicationFormFieldMapping(
                247, ApplicationFormFieldMapping.SourceType.INPUT,
                List.of("fee_rate_jcoin_pay_brand"), ApplicationFormFieldMapping.Transform.DIRECT)
    );

    private final String templateDir;
    private final ApplicationFormFieldResolver resolver;

    public ApplicationFormSmccKameiWriter(
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
            throw new UncheckedIOException("ApplicationFormSmccKameiWriter: 出力Excelの作成に失敗しました。", e);
        }
    }

    /**
     * データ行の書き込みでテンプレートの数式セルを上書き・削除すると、ブックの
     * 計算チェーン（xl/calcChain.xml）が実際のセル構成と不整合になり、Excelで
     * 開いた際に「一部の内容に問題が見つかりました」という修復ダイアログが
     * 表示されてしまう（実際に発生した不具合。calcChain.xmlパーツ内の数式が
     * 削除された旨の修復ログで確認済み）。POIは計算チェーンの項目数が0件の
     * 場合のみ書き込み時にこのパートを自動的に除去するため、明示的に全項目を
     * クリアして0件にする（計算チェーンが無くてもExcelは開くたびに再計算する
     * ため問題ない）。
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
     * テンプレートのデータ行（7行目〜）には「花キューピット」等のサンプルデータが
     * シート最終行まで埋め込まれており、実データを書き込まなかった行・列に
     * そのまま残って出力に混入する。PROTECTED（テンプレート側の数式セル）は
     * 書き換え禁止のため対象外とし、それ以外のマッピング対象列を空にする。
     */
    private void clearSampleData(Sheet sheet) {
        int lastRow = sheet.getLastRowNum() + 1;
        for (int excelRow = DATA_START_ROW; excelRow <= lastRow; excelRow++) {
            for (ApplicationFormFieldMapping mapping : MAPPINGS) {
                if (mapping.getSourceType() == ApplicationFormFieldMapping.SourceType.PROTECTED) {
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
