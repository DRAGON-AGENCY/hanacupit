package com.cupit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.cupit.model.MemberInfo;
import com.cupit.service.MemberInfoService;

@Controller
public class MenuController {

    private static final String DEFAULT_TRANSACTION_CODE = "01-001";
    private static final String ATTRIBUTE_NAME_INFO = "info";
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
    private static final String VIEW_NAME_APPLICATION_FORM = "application_form";
    private static final String VIEW_NAME_JFTD_SETTLEMENT = "jftd_settlement";
    private static final String VIEW_NAME_JFTD = "jftd";
    private static final String VIEW_NAME_OTHER_SETTLEMENT = "other_settlement";
    private static final String VIEW_NAME_OTHER_JFTD = "other_jftd";
    private static final String VIEW_NAME_REPORT_OUTPUT = "report_output";
    private static final String VIEW_NAME_EMPLOYEE_LIST = "employee_list";
    private static final String VIEW_NAME_EMPLOYEE_EDIT = "employee_edit";

    private final MemberInfoService memberInfoService;

    public MenuController(MemberInfoService memberInfoService) {
        this.memberInfoService = memberInfoService;
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
            @RequestParam(name = "transactionCode",
                    required = false,
                    defaultValue = DEFAULT_TRANSACTION_CODE) String transactionCode,
            Model model) {
        MemberInfo memberInfo = memberInfoService.findByTransactionCode(transactionCode);
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

    @GetMapping("/application_form")
    public String applicationForm() {
        return VIEW_NAME_APPLICATION_FORM;
    }

    @GetMapping("/jftd_settlement")
    public String jftdSettlement() {
        return VIEW_NAME_JFTD_SETTLEMENT;
    }

    @GetMapping("/jftd")
    public String jftd() {
        return VIEW_NAME_JFTD;
    }

    @GetMapping("/other_settlement")
    public String otherSettlement() {
        return VIEW_NAME_OTHER_SETTLEMENT;
    }

    @GetMapping("/other_jftd")
    public String otherJftd() {
        return VIEW_NAME_OTHER_JFTD;
    }

    @GetMapping("/report_output")
    public String reportOutput() {
        return VIEW_NAME_REPORT_OUTPUT;
    }

    @GetMapping("/employee_list")
    public String employeeList() {
        return VIEW_NAME_EMPLOYEE_LIST;
    }

    @GetMapping("/employee_edit")
    public String employeeEdit() {
        return VIEW_NAME_EMPLOYEE_EDIT;
    }
}
