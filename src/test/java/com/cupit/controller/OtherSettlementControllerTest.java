package com.cupit.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import com.cupit.dto.CsvValidationResponse;
import com.cupit.dto.ImportResponse;
import com.cupit.interceptor.AuthenticationInterceptor;
import com.cupit.service.OtherSettlementService;

import jakarta.servlet.http.HttpSession;

/**
 * {@link OtherSettlementController} のテスト。サービスをモック化し、
 * 正常時のHTTP 200・例外種別ごとのステータス（IllegalArgumentException→400、
 * IOException／RuntimeException→500）と、セッションからのログインユーザー取得を検証する。
 */
@ExtendWith(MockitoExtension.class)
class OtherSettlementControllerTest {

    private static final String STERA_CODE = "stera code";

    @Mock
    private OtherSettlementService otherSettlementService;

    @Mock
    private HttpSession session;

    private OtherSettlementController controller;
    private MockMultipartFile file;

    @BeforeEach
    void setUp() {
        controller = new OtherSettlementController(otherSettlementService);
        file = new MockMultipartFile("file", "stera_code.csv", "text/csv", new byte[] {1});
    }

    @Test
    void validateReturnsOkWithBody() throws Exception {
        CsvValidationResponse body = new CsvValidationResponse(true, 1, false, java.util.List.of());
        when(otherSettlementService.validateFileFormat(any(), eq(STERA_CODE))).thenReturn(body);

        ResponseEntity<CsvValidationResponse> response = controller.validate(file, STERA_CODE);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isSameAs(body);
    }

    @Test
    void validateReturnsBadRequestOnIllegalArgument() throws Exception {
        when(otherSettlementService.validateFileFormat(any(), eq(STERA_CODE)))
                .thenThrow(new IllegalArgumentException("不明な決済種類です"));

        ResponseEntity<CsvValidationResponse> response = controller.validate(file, STERA_CODE);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void validateReturnsServerErrorOnIoException() throws Exception {
        when(otherSettlementService.validateFileFormat(any(), eq(STERA_CODE)))
                .thenThrow(new IOException("read failed"));

        ResponseEntity<CsvValidationResponse> response = controller.validate(file, STERA_CODE);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
    }

    @Test
    void importFileUsesLoginUserFromSession() throws Exception {
        when(session.getAttribute(AuthenticationInterceptor.SESSION_ATTRIBUTE_LOGIN_USER))
                .thenReturn("user001");
        when(otherSettlementService.importFile(any(), eq(STERA_CODE), any(), eq(false)))
                .thenReturn(new ImportResponse(true, 2, 100, null));

        ResponseEntity<ImportResponse> response = controller.importFile(file, STERA_CODE, false, session);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        ArgumentCaptor<String> memberNo = ArgumentCaptor.forClass(String.class);
        org.mockito.Mockito.verify(otherSettlementService)
                .importFile(any(), eq(STERA_CODE), memberNo.capture(), eq(false));
        assertThat(memberNo.getValue()).isEqualTo("user001");
    }

    @Test
    void importFileFallsBackToUnknownWhenNoLoginUser() throws Exception {
        when(session.getAttribute(AuthenticationInterceptor.SESSION_ATTRIBUTE_LOGIN_USER))
                .thenReturn(null);
        when(otherSettlementService.importFile(any(), eq(STERA_CODE), eq("UNKNOWN"), eq(false)))
                .thenReturn(new ImportResponse(true, 0, 1, null));

        ResponseEntity<ImportResponse> response = controller.importFile(file, STERA_CODE, false, session);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void importFileReturnsBadRequestOnIllegalArgument() throws Exception {
        when(session.getAttribute(AuthenticationInterceptor.SESSION_ATTRIBUTE_LOGIN_USER))
                .thenReturn("user001");
        when(otherSettlementService.importFile(any(), eq(STERA_CODE), any(), eq(false)))
                .thenThrow(new IllegalArgumentException("識別キー"));

        ResponseEntity<ImportResponse> response = controller.importFile(file, STERA_CODE, false, session);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void importFileReturnsServerErrorOnIoException() throws Exception {
        when(session.getAttribute(AuthenticationInterceptor.SESSION_ATTRIBUTE_LOGIN_USER))
                .thenReturn("user001");
        when(otherSettlementService.importFile(any(), eq(STERA_CODE), any(), eq(false)))
                .thenThrow(new IOException("io"));

        ResponseEntity<ImportResponse> response = controller.importFile(file, STERA_CODE, false, session);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
    }

    @Test
    void importFileReturnsServerErrorOnRuntimeException() throws Exception {
        when(session.getAttribute(AuthenticationInterceptor.SESSION_ATTRIBUTE_LOGIN_USER))
                .thenReturn("user001");
        when(otherSettlementService.importFile(any(), eq(STERA_CODE), any(), eq(false)))
                .thenThrow(new RuntimeException("boom"));

        ResponseEntity<ImportResponse> response = controller.importFile(file, STERA_CODE, false, session);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
    }
}
