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

import com.cupit.dto.TransferFeeRateRequest;
import com.cupit.dto.TransferFeeRateResponse;
import com.cupit.interceptor.AuthenticationInterceptor;
import com.cupit.model.Employee;
import com.cupit.service.EmployeeService;
import com.cupit.service.TransferFeeRateService;

/**
 * TransferFeeRateController のテスト。
 * Service をモック化し、管理者以外の要求を拒否することを検証する。
 */
@ExtendWith(MockitoExtension.class)
class TransferFeeRateControllerTest {

    private static final String AUTHORITY_ADMINISTRATOR = "01";
    private static final String AUTHORITY_OPERATOR = "02";
    private static final String LOGIN_USER_ID = "user001";
    private static final String MESSAGE_FORBIDDEN = "権限がありません。";

    @Mock
    private TransferFeeRateService transferFeeRateService;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private HttpSession session;

    private TransferFeeRateController createController() {
        return new TransferFeeRateController(
                transferFeeRateService, employeeService);
    }

    @Test
    void saveRejectsRequestWhenNotAdministrator() {
        when(session.getAttribute(
                AuthenticationInterceptor.SESSION_ATTRIBUTE_AUTHORITY_CODE))
                .thenReturn(AUTHORITY_OPERATOR);

        TransferFeeRateResponse response =
                createController().save(new TransferFeeRateRequest(), session);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_FORBIDDEN);
        verify(transferFeeRateService, never()).saveTransferFeeRate(any(), any());
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
        TransferFeeRateResponse expected = new TransferFeeRateResponse(true, null);
        TransferFeeRateRequest request = new TransferFeeRateRequest();
        when(transferFeeRateService.saveTransferFeeRate(request, LOGIN_USER_ID))
                .thenReturn(expected);

        TransferFeeRateResponse response = createController().save(request, session);

        assertThat(response).isEqualTo(expected);
    }

    @Test
    void deleteRejectsRequestWhenNotAdministrator() {
        when(session.getAttribute(
                AuthenticationInterceptor.SESSION_ATTRIBUTE_AUTHORITY_CODE))
                .thenReturn(AUTHORITY_OPERATOR);

        TransferFeeRateResponse response =
                createController().delete(new TransferFeeRateRequest(), session);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_FORBIDDEN);
        verify(transferFeeRateService, never()).deleteTransferFeeRate(any(Integer.class));
    }

    @Test
    void deleteDelegatesToServiceWhenAdministrator() {
        when(session.getAttribute(
                AuthenticationInterceptor.SESSION_ATTRIBUTE_AUTHORITY_CODE))
                .thenReturn(AUTHORITY_ADMINISTRATOR);
        TransferFeeRateRequest request = new TransferFeeRateRequest();
        request.setTransferFeeId(5);
        TransferFeeRateResponse expected = new TransferFeeRateResponse(true, null);
        when(transferFeeRateService.deleteTransferFeeRate(5)).thenReturn(expected);

        TransferFeeRateResponse response = createController().delete(request, session);

        assertThat(response).isEqualTo(expected);
    }
}
