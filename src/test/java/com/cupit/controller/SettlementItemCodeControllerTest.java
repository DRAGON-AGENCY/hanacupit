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

import com.cupit.dto.SettlementItemCodeRequest;
import com.cupit.dto.SettlementItemCodeResponse;
import com.cupit.interceptor.AuthenticationInterceptor;
import com.cupit.model.Employee;
import com.cupit.service.EmployeeService;
import com.cupit.service.SettlementItemCodeService;

/**
 * SettlementItemCodeController のテスト。
 * Service をモック化し、管理者以外の要求を拒否することを検証する。
 */
@ExtendWith(MockitoExtension.class)
class SettlementItemCodeControllerTest {

    private static final String AUTHORITY_ADMINISTRATOR = "01";
    private static final String AUTHORITY_OPERATOR = "02";
    private static final String LOGIN_USER_ID = "user001";
    private static final String MESSAGE_FORBIDDEN = "権限がありません。";

    @Mock
    private SettlementItemCodeService settlementItemCodeService;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private HttpSession session;

    private SettlementItemCodeController createController() {
        return new SettlementItemCodeController(
                settlementItemCodeService, employeeService);
    }

    @Test
    void saveRejectsRequestWhenNotAdministrator() {
        when(session.getAttribute(
                AuthenticationInterceptor.SESSION_ATTRIBUTE_AUTHORITY_CODE))
                .thenReturn(AUTHORITY_OPERATOR);

        SettlementItemCodeResponse response =
                createController().save(new SettlementItemCodeRequest(), session);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_FORBIDDEN);
        verify(settlementItemCodeService, never()).saveItemCode(any(), any());
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
        SettlementItemCodeResponse expected = new SettlementItemCodeResponse(true, null);
        SettlementItemCodeRequest request = new SettlementItemCodeRequest();
        when(settlementItemCodeService.saveItemCode(request, LOGIN_USER_ID))
                .thenReturn(expected);

        SettlementItemCodeResponse response = createController().save(request, session);

        assertThat(response).isEqualTo(expected);
    }

    @Test
    void deleteRejectsRequestWhenNotAdministrator() {
        when(session.getAttribute(
                AuthenticationInterceptor.SESSION_ATTRIBUTE_AUTHORITY_CODE))
                .thenReturn(AUTHORITY_OPERATOR);

        SettlementItemCodeResponse response =
                createController().delete(new SettlementItemCodeRequest(), session);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_FORBIDDEN);
        verify(settlementItemCodeService, never()).deleteItemCode(any(Integer.class));
    }

    @Test
    void deleteDelegatesToServiceWhenAdministrator() {
        when(session.getAttribute(
                AuthenticationInterceptor.SESSION_ATTRIBUTE_AUTHORITY_CODE))
                .thenReturn(AUTHORITY_ADMINISTRATOR);
        SettlementItemCodeRequest request = new SettlementItemCodeRequest();
        request.setItemCodeId(5);
        SettlementItemCodeResponse expected = new SettlementItemCodeResponse(true, null);
        when(settlementItemCodeService.deleteItemCode(5)).thenReturn(expected);

        SettlementItemCodeResponse response = createController().delete(request, session);

        assertThat(response).isEqualTo(expected);
    }
}
