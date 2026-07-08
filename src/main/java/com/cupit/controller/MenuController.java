package com.cupit.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cupit.interceptor.AuthenticationInterceptor;
import com.cupit.model.Employee;
import com.cupit.model.MemberInfo;
import com.cupit.service.EmployeeService;
import com.cupit.service.JftdReportDataService;
import com.cupit.service.MemberInfoService;

@Controller
public class MenuController {

    private static final String DEFAULT_TRADE_CODE = "01-001";
    private static final String ATTRIBUTE_NAME_INFO = "info";
    private static final String ATTRIBUTE_NAME_EMPLOYEES = "employees";
    private static final String ATTRIBUTE_NAME_TRANSFER_BATCHES = "transferBatches";
    private static final String ATTRIBUTE_NAME_AUTHORITY_CODE = "authorityCode";
    private static final String ATTRIBUTE_NAME_EMPLOYEE = "employee";
    private static final String ATTRIBUTE_NAME_MODE = "mode";
    private static final String MODE_NEW = "new";
    private static final String AUTHORITY_ADMINISTRATOR = "01";
    private static final String REDIRECT_EMPLOYEE_LIST =
            "redirect:/employee_list";
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

    private final MemberInfoService memberInfoService;
    private final EmployeeService employeeService;
    private final JftdReportDataService jftdReportDataService;

    public MenuController(
            MemberInfoService memberInfoService,
            EmployeeService employeeService,
            JftdReportDataService jftdReportDataService) {
        this.memberInfoService = memberInfoService;
        this.employeeService = employeeService;
        this.jftdReportDataService = jftdReportDataService;
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
            @RequestParam(name = "tradeCode",
                    required = false,
                    defaultValue = DEFAULT_TRADE_CODE) String tradeCode,
            Model model) {
        MemberInfo memberInfo = memberInfoService.findByTradeCode(tradeCode);
        model.addAttribute(ATTRIBUTE_NAME_INFO, memberInfo);
        return VIEW_NAME_MEMBER_INFO;
    }

    @GetMapping("/member_list")
    public String memberList() {
        return VIEW_NAME_MEMBER_LIST;
    }

    @GetMapping("/store_terminal_smcc")
    public String storeTerminalSmcc() {
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
    public String applicationForm() {
        return VIEW_NAME_APPLICATION_FORM;
    }

    @GetMapping("/jftd_settlement")
    public String jftdSettlement() {
        return VIEW_NAME_JFTD_SETTLEMENT;
    }

    @GetMapping("/jftd_transfer")
    public String jftdTransfer() {
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
