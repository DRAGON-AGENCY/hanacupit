package com.cupit.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cupit.csv.ApplicationFormColumn;
import com.cupit.exception.MemberInfoNotFoundException;
import com.cupit.interceptor.AuthenticationInterceptor;
import com.cupit.model.Employee;
import com.cupit.model.JftdTransferBatch;
import com.cupit.model.MemberInfo;
import com.cupit.model.SettlementFeeRate;
import com.cupit.model.SettlementItemCode;
import com.cupit.model.SmccMerchantNo;
import com.cupit.model.SteraStore;
import com.cupit.model.SteraTerminal;
import com.cupit.model.SteraTransferBatch;
import com.cupit.model.TransferFeeRate;
import com.cupit.repository.JftdTransferBatchRepository;
import com.cupit.repository.SmccMerchantNoRepository;
import com.cupit.repository.SteraStoreRepository;
import com.cupit.repository.SteraTerminalRepository;
import com.cupit.repository.SteraTransferBatchRepository;
import com.cupit.service.EmployeeService;
import com.cupit.service.JftdReportDataService;
import com.cupit.service.MemberInfoService;
import com.cupit.service.SettlementFeeRateService;
import com.cupit.service.SettlementItemCodeService;
import com.cupit.service.TransferFeeRateService;

@Controller
public class MenuController {

    private static final String ATTRIBUTE_NAME_INFO = "info";
    private static final String ATTRIBUTE_NAME_MEMBERS = "members";
    private static final String ATTRIBUTE_NAME_STORE = "store";
    private static final String ATTRIBUTE_NAME_TERMINALS = "terminals";
    private static final String ATTRIBUTE_NAME_MERCHANT_NUMBERS = "merchantNumbers";
    private static final String ATTRIBUTE_NAME_EMPLOYEES = "employees";
    private static final String ATTRIBUTE_NAME_TRANSFER_BATCHES = "transferBatches";
    private static final String ATTRIBUTE_NAME_LATEST_JFTD_TRANSFER_BATCH_ID = "latestJftdTransferBatchId";
    private static final String ATTRIBUTE_NAME_LATEST_STERA_TRANSFER_BATCH_ID = "latestSteraTransferBatchId";
    private static final String ATTRIBUTE_NAME_AUTHORITY_CODE = "authorityCode";
    private static final String ATTRIBUTE_NAME_EMPLOYEE = "employee";
    private static final String ATTRIBUTE_NAME_MODE = "mode";
    private static final String ATTRIBUTE_NAME_EXPECTED_COLUMN_COUNT = "expectedColumnCount";
    private static final String ATTRIBUTE_NAME_FEE_RATES = "feeRates";
    private static final String ATTRIBUTE_NAME_FEE_RATE = "feeRate";
    private static final String ATTRIBUTE_NAME_ITEM_CODES = "itemCodes";
    private static final String ATTRIBUTE_NAME_ITEM_CODE = "itemCode";
    private static final String ATTRIBUTE_NAME_TRANSFER_FEE_RATES = "transferFeeRates";
    private static final String ATTRIBUTE_NAME_TRANSFER_FEE_RATE = "transferFeeRate";
    private static final String MODE_NEW = "new";
    private static final String AUTHORITY_ADMINISTRATOR = "01";
    private static final String REDIRECT_EMPLOYEE_LIST =
            "redirect:/employee_list";
    private static final String REDIRECT_SETTLEMENT_FEE_RATE_LIST =
            "redirect:/settlement_fee_rate_list";
    private static final String REDIRECT_SETTLEMENT_ITEM_CODE_LIST =
            "redirect:/settlement_item_code_list";
    private static final String REDIRECT_TRANSFER_FEE_RATE_LIST =
            "redirect:/transfer_fee_rate_list";
    private static final String VIEW_NAME_LOGIN = "login";
    private static final String VIEW_NAME_MENU = "menu";
    private static final String VIEW_NAME_MEMBER_INFO = "member_info";
    private static final String VIEW_NAME_MEMBER_LIST = "member_list";
    private static final String VIEW_NAME_STORE_TERMINAL_SMCC = "store_terminal_smcc";
    private static final String VIEW_NAME_STERA_JCB = "stera_jcb";
    private static final String VIEW_NAME_STERA_SMCC = "stera_smcc";
    private static final String VIEW_NAME_PAYGATE_STATION = "paygate_station";
    private static final String VIEW_NAME_MEMBER_MASTER = "member_master";
    private static final String VIEW_NAME_SHOP_DATA_CREATE = "shop_data_create";
    private static final String VIEW_NAME_PAYGATE_MAPPING_CREATE = "paygate_mapping_create";
    private static final String VIEW_NAME_PAYGATE_MAPPING_INFO = "paygate_mapping_info";
    private static final String VIEW_NAME_APPLICATION_FORM = "application_form";
    private static final String VIEW_NAME_JFTD_SETTLEMENT = "jftd_settlement";
    private static final String VIEW_NAME_JFTD_TRANSFER = "jftd_transfer";
    private static final String VIEW_NAME_JFTD_REPORT = "jftd_report";
    private static final String VIEW_NAME_OTHER_SETTLEMENT = "other_settlement";
    private static final String VIEW_NAME_EMPLOYEE_LIST = "employee_list";
    private static final String VIEW_NAME_EMPLOYEE_EDIT = "employee_edit";
    private static final String VIEW_NAME_SETTLEMENT_FEE_RATE_LIST =
            "settlement_fee_rate_list";
    private static final String VIEW_NAME_SETTLEMENT_FEE_RATE_EDIT =
            "settlement_fee_rate_edit";
    private static final String VIEW_NAME_SETTLEMENT_ITEM_CODE_LIST =
            "settlement_item_code_list";
    private static final String VIEW_NAME_SETTLEMENT_ITEM_CODE_EDIT =
            "settlement_item_code_edit";
    private static final String VIEW_NAME_TRANSFER_FEE_RATE_LIST =
            "transfer_fee_rate_list";
    private static final String VIEW_NAME_TRANSFER_FEE_RATE_EDIT =
            "transfer_fee_rate_edit";

    private final MemberInfoService memberInfoService;
    private final EmployeeService employeeService;
    private final JftdReportDataService jftdReportDataService;
    private final SteraStoreRepository steraStoreRepository;
    private final SteraTerminalRepository steraTerminalRepository;
    private final SmccMerchantNoRepository smccMerchantNoRepository;
    private final SettlementFeeRateService settlementFeeRateService;
    private final SettlementItemCodeService settlementItemCodeService;
    private final TransferFeeRateService transferFeeRateService;
    private final JftdTransferBatchRepository jftdTransferBatchRepository;
    private final SteraTransferBatchRepository steraTransferBatchRepository;

    public MenuController(
            MemberInfoService memberInfoService,
            EmployeeService employeeService,
            JftdReportDataService jftdReportDataService,
            SteraStoreRepository steraStoreRepository,
            SteraTerminalRepository steraTerminalRepository,
            SmccMerchantNoRepository smccMerchantNoRepository,
            SettlementFeeRateService settlementFeeRateService,
            SettlementItemCodeService settlementItemCodeService,
            TransferFeeRateService transferFeeRateService,
            JftdTransferBatchRepository jftdTransferBatchRepository,
            SteraTransferBatchRepository steraTransferBatchRepository) {
        this.memberInfoService = memberInfoService;
        this.employeeService = employeeService;
        this.jftdReportDataService = jftdReportDataService;
        this.steraStoreRepository = steraStoreRepository;
        this.steraTerminalRepository = steraTerminalRepository;
        this.smccMerchantNoRepository = smccMerchantNoRepository;
        this.settlementFeeRateService = settlementFeeRateService;
        this.settlementItemCodeService = settlementItemCodeService;
        this.transferFeeRateService = transferFeeRateService;
        this.jftdTransferBatchRepository = jftdTransferBatchRepository;
        this.steraTransferBatchRepository = steraTransferBatchRepository;
    }

    @GetMapping({"/", "/login"})
    public String login() {
        return VIEW_NAME_LOGIN;
    }

    @GetMapping("/menu")
    public String menu() {
        return VIEW_NAME_MENU;
    }

    @GetMapping("/member_info")
    public String memberInfo(
            @RequestParam(name = "tradeCode", required = false) String tradeCode,
            Model model) {
        MemberInfo memberInfo = null;
        try {
            memberInfo = memberInfoService.findByTradeCode(tradeCode);
        } catch (MemberInfoNotFoundException e) {
            // 該当する会員情報が無い場合は画面側で「見つかりません」メッセージを表示する
        }
        model.addAttribute(ATTRIBUTE_NAME_INFO, memberInfo);
        return VIEW_NAME_MEMBER_INFO;
    }

    @GetMapping("/member_list")
    public String memberList(Model model) {
        model.addAttribute(ATTRIBUTE_NAME_MEMBERS, memberInfoService.findAllForList());
        return VIEW_NAME_MEMBER_LIST;
    }

    @GetMapping("/store_terminal_smcc")
    public String storeTerminalSmcc(
            @RequestParam(name = "tradeCode", required = false) String tradeCode,
            Model model) {
        SteraStore store = steraStoreRepository.findByTradeCode(tradeCode).orElse(null);
        List<SteraTerminal> terminals = store != null
                ? steraTerminalRepository.findByTradeCodeOrderByRecordNoAsc(tradeCode)
                : List.of();
        List<SmccMerchantNo> merchantNumbers = store != null
                ? smccMerchantNoRepository.findByTradeCodeOrderByRecordNoAsc(tradeCode)
                : List.of();
        model.addAttribute(ATTRIBUTE_NAME_STORE, store);
        model.addAttribute(ATTRIBUTE_NAME_TERMINALS, terminals);
        model.addAttribute(ATTRIBUTE_NAME_MERCHANT_NUMBERS, merchantNumbers);
        return VIEW_NAME_STORE_TERMINAL_SMCC;
    }

    @GetMapping("/stera_jcb")
    public String steraJcb() {
        return VIEW_NAME_STERA_JCB;
    }

    @GetMapping("/stera_smcc")
    public String steraSmcc() {
        return VIEW_NAME_STERA_SMCC;
    }

    @GetMapping("/paygate_station")
    public String paygateStation() {
        return VIEW_NAME_PAYGATE_STATION;
    }

    @GetMapping("/member_master")
    public String memberMaster() {
        return VIEW_NAME_MEMBER_MASTER;
    }

    @GetMapping("/shop_data_create")
    public String shopDataCreate() {
        return VIEW_NAME_SHOP_DATA_CREATE;
    }

    @GetMapping("/paygate_mapping_create")
    public String paygateMappingCreate() {
        return VIEW_NAME_PAYGATE_MAPPING_CREATE;
    }

    @GetMapping("/paygate_mapping_info")
    public String paygateMappingInfo(
            @RequestParam(name = "tradeCode", required = false) String tradeCode,
            Model model) {
        model.addAttribute("tradeCode", tradeCode);
        return VIEW_NAME_PAYGATE_MAPPING_INFO;
    }

    @GetMapping("/application_form")
    public String applicationForm(Model model) {
        // フロントエンドの列数チェックがApplicationFormColumnの定義数と常に一致するよう、
        // ハードコードせずここから渡す（CLAUDE.md「多列固定フォーマットCSVの列位置管理」参照）。
        model.addAttribute(ATTRIBUTE_NAME_EXPECTED_COLUMN_COUNT, ApplicationFormColumn.values().length);
        return VIEW_NAME_APPLICATION_FORM;
    }

    @GetMapping("/jftd_settlement")
    public String jftdSettlement() {
        return VIEW_NAME_JFTD_SETTLEMENT;
    }

    /**
     * JFTD・その他統合振込CSV作成画面。確定処理自体は副作用を伴わないダウンロードとは
     * 分離済みのため、確定直後のCSVダウンロードに失敗しても再確定せずに直近の確定分だけ
     * 再ダウンロードできるよう、直近の確定バッチIDを画面へ渡す（無ければnullのまま）。
     */
    @GetMapping("/jftd_transfer")
    public String jftdTransfer(Model model) {
        Integer latestJftdTransferBatchId = jftdTransferBatchRepository.findFirstByOrderByCreatedAtDesc()
                .map(JftdTransferBatch::getTransferBatchId)
                .orElse(null);
        Integer latestSteraTransferBatchId = steraTransferBatchRepository.findFirstByOrderByCreatedAtDesc()
                .map(SteraTransferBatch::getTransferBatchId)
                .orElse(null);
        model.addAttribute(ATTRIBUTE_NAME_LATEST_JFTD_TRANSFER_BATCH_ID, latestJftdTransferBatchId);
        model.addAttribute(ATTRIBUTE_NAME_LATEST_STERA_TRANSFER_BATCH_ID, latestSteraTransferBatchId);
        return VIEW_NAME_JFTD_TRANSFER;
    }

    /**
     * 帳票出力画面。「確定」操作は持たず、CSV作成画面（/jftd_transfer）で
     * 確定済みの統合振込バッチを一覧から選んで、売上報告書・支払明細書を
     * 何度でも再ダウンロードできる参照専用画面。
     */
    @GetMapping("/jftd_report")
    public String jftdReport(Model model) {
        model.addAttribute(
                ATTRIBUTE_NAME_TRANSFER_BATCHES, jftdReportDataService.listConfirmedBatches());
        return VIEW_NAME_JFTD_REPORT;
    }

    @GetMapping("/other_settlement")
    public String otherSettlement() {
        return VIEW_NAME_OTHER_SETTLEMENT;
    }

    @GetMapping("/employee_list")
    public String employeeList(HttpSession session, Model model) {
        model.addAttribute(
                ATTRIBUTE_NAME_EMPLOYEES, employeeService.findAllEmployees());
        model.addAttribute(
                ATTRIBUTE_NAME_AUTHORITY_CODE, getAuthorityCode(session));
        return VIEW_NAME_EMPLOYEE_LIST;
    }

    @GetMapping("/employee_edit")
    public String employeeEdit(
            @RequestParam(name = "mode", required = false) String mode,
            @RequestParam(name = "email", required = false) String email,
            HttpSession session,
            Model model) {
        String authorityCode = getAuthorityCode(session);

        // メンテナンスは管理者 (01) のみ。それ以外は一覧へ戻す
        if (!AUTHORITY_ADMINISTRATOR.equals(authorityCode)) {
            return REDIRECT_EMPLOYEE_LIST;
        }

        // 編集モードでは選択された社員の内容を読み込む
        if (!MODE_NEW.equals(mode)) {
            Employee employee = employeeService.findByEmail(email);
            model.addAttribute(ATTRIBUTE_NAME_EMPLOYEE, employee);
        }
        model.addAttribute(ATTRIBUTE_NAME_MODE, mode);
        model.addAttribute(ATTRIBUTE_NAME_AUTHORITY_CODE, authorityCode);
        return VIEW_NAME_EMPLOYEE_EDIT;
    }

    @GetMapping("/settlement_fee_rate_list")
    public String settlementFeeRateList(HttpSession session, Model model) {
        model.addAttribute(
                ATTRIBUTE_NAME_FEE_RATES, settlementFeeRateService.findAllFeeRates());
        model.addAttribute(
                ATTRIBUTE_NAME_AUTHORITY_CODE, getAuthorityCode(session));
        return VIEW_NAME_SETTLEMENT_FEE_RATE_LIST;
    }

    @GetMapping("/settlement_fee_rate_edit")
    public String settlementFeeRateEdit(
            @RequestParam(name = "mode", required = false) String mode,
            @RequestParam(name = "feeRateId", required = false) Integer feeRateId,
            HttpSession session,
            Model model) {
        String authorityCode = getAuthorityCode(session);

        // メンテナンスは管理者 (01) のみ。それ以外は一覧へ戻す
        if (!AUTHORITY_ADMINISTRATOR.equals(authorityCode)) {
            return REDIRECT_SETTLEMENT_FEE_RATE_LIST;
        }

        // 編集モードでは選択された手数料率の内容を読み込む
        if (!MODE_NEW.equals(mode) && feeRateId != null) {
            SettlementFeeRate feeRate = settlementFeeRateService.findById(feeRateId);
            model.addAttribute(ATTRIBUTE_NAME_FEE_RATE, feeRate);
        }
        model.addAttribute(ATTRIBUTE_NAME_MODE, mode);
        model.addAttribute(ATTRIBUTE_NAME_AUTHORITY_CODE, authorityCode);
        return VIEW_NAME_SETTLEMENT_FEE_RATE_EDIT;
    }

    @GetMapping("/settlement_item_code_list")
    public String settlementItemCodeList(HttpSession session, Model model) {
        model.addAttribute(
                ATTRIBUTE_NAME_ITEM_CODES, settlementItemCodeService.findAllItemCodes());
        model.addAttribute(
                ATTRIBUTE_NAME_AUTHORITY_CODE, getAuthorityCode(session));
        return VIEW_NAME_SETTLEMENT_ITEM_CODE_LIST;
    }

    @GetMapping("/settlement_item_code_edit")
    public String settlementItemCodeEdit(
            @RequestParam(name = "mode", required = false) String mode,
            @RequestParam(name = "itemCodeId", required = false) Integer itemCodeId,
            HttpSession session,
            Model model) {
        String authorityCode = getAuthorityCode(session);

        // メンテナンスは管理者 (01) のみ。それ以外は一覧へ戻す
        if (!AUTHORITY_ADMINISTRATOR.equals(authorityCode)) {
            return REDIRECT_SETTLEMENT_ITEM_CODE_LIST;
        }

        // 編集モードでは選択された項目コードの内容を読み込む
        if (!MODE_NEW.equals(mode) && itemCodeId != null) {
            SettlementItemCode itemCode = settlementItemCodeService.findById(itemCodeId);
            model.addAttribute(ATTRIBUTE_NAME_ITEM_CODE, itemCode);
        }
        model.addAttribute(ATTRIBUTE_NAME_MODE, mode);
        model.addAttribute(ATTRIBUTE_NAME_AUTHORITY_CODE, authorityCode);
        return VIEW_NAME_SETTLEMENT_ITEM_CODE_EDIT;
    }

    @GetMapping("/transfer_fee_rate_list")
    public String transferFeeRateList(HttpSession session, Model model) {
        model.addAttribute(
                ATTRIBUTE_NAME_TRANSFER_FEE_RATES, transferFeeRateService.findAllTransferFeeRates());
        model.addAttribute(
                ATTRIBUTE_NAME_AUTHORITY_CODE, getAuthorityCode(session));
        return VIEW_NAME_TRANSFER_FEE_RATE_LIST;
    }

    @GetMapping("/transfer_fee_rate_edit")
    public String transferFeeRateEdit(
            @RequestParam(name = "mode", required = false) String mode,
            @RequestParam(name = "transferFeeId", required = false) Integer transferFeeId,
            HttpSession session,
            Model model) {
        String authorityCode = getAuthorityCode(session);

        // メンテナンスは管理者 (01) のみ。それ以外は一覧へ戻す
        if (!AUTHORITY_ADMINISTRATOR.equals(authorityCode)) {
            return REDIRECT_TRANSFER_FEE_RATE_LIST;
        }

        // 編集モードでは選択された振込手数料の内容を読み込む
        if (!MODE_NEW.equals(mode) && transferFeeId != null) {
            TransferFeeRate transferFeeRate = transferFeeRateService.findById(transferFeeId);
            model.addAttribute(ATTRIBUTE_NAME_TRANSFER_FEE_RATE, transferFeeRate);
        }
        model.addAttribute(ATTRIBUTE_NAME_MODE, mode);
        model.addAttribute(ATTRIBUTE_NAME_AUTHORITY_CODE, authorityCode);
        return VIEW_NAME_TRANSFER_FEE_RATE_EDIT;
    }

    /**
     * セッションに保持された権限コードを取得する。
     *
     * @param session 対象のセッション
     * @return 権限コード。未設定の場合は null
     */
    private String getAuthorityCode(HttpSession session) {
        Object authorityCode = session.getAttribute(
                AuthenticationInterceptor.SESSION_ATTRIBUTE_AUTHORITY_CODE);
        if (authorityCode == null) {
            return null;
        }
        return authorityCode.toString();
    }
}
