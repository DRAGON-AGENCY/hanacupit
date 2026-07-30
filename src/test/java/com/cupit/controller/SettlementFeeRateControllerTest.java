package com.cupit.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cupit.dto.SettlementFeeRateRequest;
import com.cupit.dto.SettlementFeeRateResponse;
import com.cupit.interceptor.AuthenticationInterceptor;
import com.cupit.model.Employee;
import com.cupit.service.EmployeeService;
import com.cupit.service.SettlementFeeRateService;

/**
 * SettlementFeeRateController のテスト。
 * Service をモック化し、管理者以外の要求を拒否することを検証する。
 */
@ExtendWith(MockitoExtension.class)
class SettlementFeeRateControllerTest {

    private static final String AUTHORITY_ADMINISTRATOR = "01";
    private static final String AUTHORITY_OPERATOR = "02";
    private static final String LOGIN_USER_ID = "user001";
    private static final String MESSAGE_FORBIDDEN = "権限がありません。";

    @Mock
    private SettlementFeeRateService settlementFeeRateService;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private HttpSession session;

    private SettlementFeeRateController createController() {
        return new SettlementFeeRateController(
                settlementFeeRateService, employeeService);
    }

    @Test
    void saveRejectsRequestWhenNotAdministrator() {
        when(session.getAttribute(
                AuthenticationInterceptor.SESSION_ATTRIBUTE_AUTHORITY_CODE))
                .thenReturn(AUTHORITY_OPERATOR);

        SettlementFeeRateResponse response =
                createController().save(new SettlementFeeRateRequest(), session);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_FORBIDDEN);
        verify(settlementFeeRateService, never()).saveFeeRate(any(), any());
    }

    @Test
    void saveDelegatesToServiceWhenAdministrator() {
        when(session.getAttribute(
                AuthenticationInterceptor.SESSION_ATTRIBUTE_AUTHORITY_CODE))
                .thenReturn(AUTHORITY_ADMINISTRATOR);
        when(session.getAttribute(
                AuthenticationInterceptor.SESSION_ATTRIBUTE_LOGIN_USER))
                .thenReturn(LOGIN_USER_ID);
        Employee loginEmployee = new Employee();
        loginEmployee.setUserId(LOGIN_USER_ID);
        when(employeeService.findByUserId(LOGIN_USER_ID)).thenReturn(loginEmployee);
        SettlementFeeRateResponse expected = new SettlementFeeRateResponse(true, null);
        SettlementFeeRateRequest request = new SettlementFeeRateRequest();
        when(settlementFeeRateService.saveFeeRate(request, LOGIN_USER_ID))
                .thenReturn(expected);

        SettlementFeeRateResponse response = createController().save(request, session);

        assertThat(response).isEqualTo(expected);
    }

    @Test
    void deleteRejectsRequestWhenNotAdministrator() {
        when(session.getAttribute(
                AuthenticationInterceptor.SESSION_ATTRIBUTE_AUTHORITY_CODE))
                .thenReturn(AUTHORITY_OPERATOR);

        SettlementFeeRateResponse response =
                createController().delete(new SettlementFeeRateRequest(), session);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_FORBIDDEN);
        verify(settlementFeeRateService, never()).deleteFeeRate(any(Integer.class));
    }

    @Test
    void deleteDelegatesToServiceWhenAdministrator() {
        when(session.getAttribute(
                AuthenticationInterceptor.SESSION_ATTRIBUTE_AUTHORITY_CODE))
                .thenReturn(AUTHORITY_ADMINISTRATOR);
        SettlementFeeRateRequest request = new SettlementFeeRateRequest();
        request.setFeeRateId(5);
        SettlementFeeRateResponse expected = new SettlementFeeRateResponse(true, null);
        when(settlementFeeRateService.deleteFeeRate(5)).thenReturn(expected);

        SettlementFeeRateResponse response = createController().delete(request, session);

        assertThat(response).isEqualTo(expected);
    }
}
