package com.cupit.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cupit.dto.SettlementFeeRateRequest;
import com.cupit.dto.SettlementFeeRateResponse;
import com.cupit.interceptor.AuthenticationInterceptor;
import com.cupit.model.Employee;
import com.cupit.service.EmployeeService;
import com.cupit.service.SettlementFeeRateService;

/**
 * 手数料率マスタの保存・削除要求を処理するコントローラ。
 * メンテナンスは管理者 (権限コード 01) のみに許可する。
 */
@Controller
public class SettlementFeeRateController {

    private static final String AUTHORITY_ADMINISTRATOR = "01";
    private static final String MESSAGE_FORBIDDEN = "権限がありません。";

    private final SettlementFeeRateService settlementFeeRateService;
    private final EmployeeService employeeService;

    public SettlementFeeRateController(
            SettlementFeeRateService settlementFeeRateService,
            EmployeeService employeeService) {
        this.settlementFeeRateService = settlementFeeRateService;
        this.employeeService = employeeService;
    }

    /**
     * 手数料率を登録または更新する。管理者以外の要求は拒否する。
     *
     * @param request 画面から送信された手数料率情報
     * @param session ログイン状態と権限を保持するセッション
     * @return 処理結果
     */
    @PostMapping(
            value = "/settlement_fee_rate/save",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public SettlementFeeRateResponse save(
            @RequestBody SettlementFeeRateRequest request,
            HttpSession session) {
        if (!isAdministrator(session)) {
            return new SettlementFeeRateResponse(false, MESSAGE_FORBIDDEN);
        }
        return settlementFeeRateService.saveFeeRate(
                request, getLoginUserId(session));
    }

    /**
     * 手数料率を削除する。管理者以外の要求は拒否する。
     *
     * @param request 削除対象の手数料率 ID を含む要求
     * @param session ログイン状態と権限を保持するセッション
     * @return 処理結果
     */
    @PostMapping(
            value = "/settlement_fee_rate/delete",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public SettlementFeeRateResponse delete(
            @RequestBody SettlementFeeRateRequest request,
            HttpSession session) {
        if (!isAdministrator(session)) {
            return new SettlementFeeRateResponse(false, MESSAGE_FORBIDDEN);
        }
        return settlementFeeRateService.deleteFeeRate(request.getFeeRateId());
    }

    /**
     * 操作中のログインユーザの user_id を取得する。
     *
     * @param session ログイン状態を保持するセッション
     * @return ログインユーザの user_id。特定できない場合は null
     */
    private String getLoginUserId(HttpSession session) {
        Object loginUserId = session.getAttribute(
                AuthenticationInterceptor.SESSION_ATTRIBUTE_LOGIN_USER);
        if (loginUserId == null) {
            return null;
        }
        Employee loginUser = employeeService.findByUserId(loginUserId.toString());
        if (loginUser == null) {
            return null;
        }
        return loginUser.getUserId();
    }

    /**
     * セッションの権限コードが管理者 (01) かどうかを判定する。
     *
     * @param session 判定対象のセッション
     * @return 管理者の場合は true
     */
    private boolean isAdministrator(HttpSession session) {
        Object authorityCode = session.getAttribute(
                AuthenticationInterceptor.SESSION_ATTRIBUTE_AUTHORITY_CODE);
        return AUTHORITY_ADMINISTRATOR.equals(authorityCode);
    }
}
