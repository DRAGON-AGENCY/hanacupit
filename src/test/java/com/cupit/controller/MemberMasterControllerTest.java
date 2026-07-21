package com.cupit.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import com.cupit.csv.writer.MemberMasterCsvWriter;
import com.cupit.dto.ImportResponse;
import com.cupit.interceptor.AuthenticationInterceptor;
import com.cupit.model.MemberInfo;
import com.cupit.repository.MemberInfoRepository;
import com.cupit.service.MemberMasterService;

import jakarta.servlet.http.HttpSession;

/**
 * {@link MemberMasterController} のテスト。サービスをモック化し、
 * 正常時のHTTP 200・例外種別ごとのステータス（IllegalArgumentException→400、
 * IOException／RuntimeException→500）、セッションからのログインユーザー取得
 * （未ログイン時は"UNKNOWN"）、およびダウンロード時のCSV生成とファイル名を検証する。
 */
@ExtendWith(MockitoExtension.class)
class MemberMasterControllerTest {

    @Mock
    private MemberMasterService memberMasterService;

    @Mock
    private MemberInfoRepository memberInfoRepository;

    @Mock
    private MemberMasterCsvWriter memberMasterCsvWriter;

    @Mock
    private HttpSession session;

    private MemberMasterController controller;
    private MockMultipartFile file;

    @BeforeEach
    void setUp() {
        controller = new MemberMasterController(
                memberMasterService, memberInfoRepository, memberMasterCsvWriter);
        file = new MockMultipartFile("file", "member_master.csv", "text/csv", new byte[] {1});
    }

    @Test
    void uploadReturnsOkWithBodyOnSuccess() throws Exception {
        ImportResponse body = new ImportResponse(true, 3, 1, null);
        when(session.getAttribute(AuthenticationInterceptor.SESSION_ATTRIBUTE_LOGIN_USER))
                .thenReturn("user001");
        when(memberMasterService.importFile(eq(file), eq("user001"))).thenReturn(body);

        ResponseEntity<ImportResponse> response = controller.upload(file, session);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isSameAs(body);
    }

    @Test
    void uploadUsesUnknownWhenSessionHasNoLoginUser() throws Exception {
        when(session.getAttribute(AuthenticationInterceptor.SESSION_ATTRIBUTE_LOGIN_USER))
                .thenReturn(null);
        when(memberMasterService.importFile(eq(file), eq("UNKNOWN")))
                .thenReturn(new ImportResponse(true, 1, 1, null));

        ResponseEntity<ImportResponse> response = controller.upload(file, session);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    @Test
    void uploadReturnsBadRequestOnIllegalArgument() throws Exception {
        when(session.getAttribute(AuthenticationInterceptor.SESSION_ATTRIBUTE_LOGIN_USER))
                .thenReturn("user001");
        when(memberMasterService.importFile(any(), any()))
                .thenThrow(new IllegalArgumentException("不正なファイルです"));

        ResponseEntity<ImportResponse> response = controller.upload(file, session);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getErrorMessage()).isEqualTo("不正なファイルです");
    }

    @Test
    void uploadReturnsServerErrorOnIoException() throws Exception {
        when(session.getAttribute(AuthenticationInterceptor.SESSION_ATTRIBUTE_LOGIN_USER))
                .thenReturn("user001");
        when(memberMasterService.importFile(any(), any()))
                .thenThrow(new IOException("read failed"));

        ResponseEntity<ImportResponse> response = controller.upload(file, session);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().getErrorMessage()).contains("ファイルの読み込みに失敗しました");
    }

    @Test
    void uploadReturnsServerErrorOnRuntimeException() throws Exception {
        when(session.getAttribute(AuthenticationInterceptor.SESSION_ATTRIBUTE_LOGIN_USER))
                .thenReturn("user001");
        when(memberMasterService.importFile(any(), any()))
                .thenThrow(new RuntimeException("could not execute statement"));

        ResponseEntity<ImportResponse> response = controller.upload(file, session);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().getErrorMessage()).contains("予期せぬエラーが発生しました");
    }

    @Test
    void downloadReturnsCsvWithAttachmentFilename() {
        MemberInfo info = new MemberInfo();
        info.setTradeCode("01-001");
        when(memberInfoRepository.findAllByOrderByTradeCodeAsc()).thenReturn(List.of(info));
        byte[] csvBytes = "dummy".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        when(memberMasterCsvWriter.writeCsv(List.of(info))).thenReturn(csvBytes);

        ResponseEntity<byte[]> response = controller.download();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(csvBytes);
        assertThat(response.getHeaders().getContentDisposition().isAttachment()).isTrue();
        assertThat(response.getHeaders().getContentDisposition().getFilename())
                .startsWith("member_master_").endsWith(".csv");
    }

}
