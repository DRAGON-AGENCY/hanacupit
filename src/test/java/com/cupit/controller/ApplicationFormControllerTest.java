package com.cupit.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.service.applicationform.ApplicationFormGenerateResult;
import com.cupit.service.applicationform.ApplicationFormService;
import com.cupit.service.applicationform.ApplicationFormService.Destination;

/**
 * {@link ApplicationFormController} のテスト。Serviceをモック化し、申請先ごとの
 * ファイル名・拡張子・Content-Type、成功時のレスポンスヘッダー（件数・エラー概要）、
 * 例外種別ごとのHTTPステータス（IllegalArgumentException→400、
 * IOException／RuntimeException→500、result.isSuccess()==false→400）を検証する。
 */
@ExtendWith(MockitoExtension.class)
class ApplicationFormControllerTest {

    @Mock
    private ApplicationFormService applicationFormService;

    private ApplicationFormController controller;
    private MockMultipartFile file;

    @BeforeEach
    void setUp() {
        controller = new ApplicationFormController(applicationFormService);
        file = new MockMultipartFile(
                "file", "application_form_input.csv", "text/csv", new byte[] {1});
    }

    @Test
    void uploadReturnsFileBytesWithHeadersOnSuccessForJcb() throws Exception {
        ApplicationFormGenerateResult result = ApplicationFormGenerateResult.success(
                new byte[] {1, 2, 3}, 2, 2, List.of());
        when(applicationFormService.generate(Destination.JCB, file)).thenReturn(result);

        ResponseEntity<?> response = controller.generate(Destination.JCB, file);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(new byte[] {1, 2, 3});
        assertThat(response.getHeaders().getContentDisposition().isAttachment()).isTrue();
        assertThat(response.getHeaders().getContentDisposition().getFilename())
                .startsWith("JCB申込フォーム_").endsWith(".xlsx");
        assertThat(response.getHeaders().getContentType().toString())
                .isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        assertThat(response.getHeaders().getFirst("X-Success-Count")).isEqualTo("2");
        assertThat(response.getHeaders().getFirst("X-Total-Row-Count")).isEqualTo("2");
        assertThat(response.getHeaders().getFirst("X-Error-Count")).isEqualTo("0");
    }

    @Test
    void uploadReturnsXlsmContentTypeAndFilenameForSmccKamei() throws Exception {
        ApplicationFormGenerateResult result = ApplicationFormGenerateResult.success(
                new byte[] {1}, 1, 1, List.of());
        when(applicationFormService.generate(Destination.SMCC_KAMEI, file)).thenReturn(result);

        ResponseEntity<?> response = controller.generate(Destination.SMCC_KAMEI, file);

        assertThat(response.getHeaders().getContentDisposition().getFilename())
                .startsWith("SMCC加盟店申込書_").endsWith(".xlsm");
        assertThat(response.getHeaders().getContentType().toString())
                .isEqualToIgnoringCase("application/vnd.ms-excel.sheet.macroEnabled.12");
    }

    @Test
    void uploadReturnsXlsxFilenameForSmccTenpo() throws Exception {
        ApplicationFormGenerateResult result = ApplicationFormGenerateResult.success(
                new byte[] {1}, 1, 1, List.of());
        when(applicationFormService.generate(Destination.SMCC_TENPO, file)).thenReturn(result);

        ResponseEntity<?> response = controller.generate(Destination.SMCC_TENPO, file);

        assertThat(response.getHeaders().getContentDisposition().getFilename())
                .startsWith("SMCC店舗情報一覧_").endsWith(".xlsx");
    }

    @Test
    void uploadReturnsBadRequestWhenResultNotSuccess() throws Exception {
        ApplicationFormGenerateResult result =
                ApplicationFormGenerateResult.error("登録可能な行がありません。");
        when(applicationFormService.generate(Destination.JCB, file)).thenReturn(result);

        ResponseEntity<?> response = controller.generate(Destination.JCB, file);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        ApplicationFormController.ErrorResponse body =
                (ApplicationFormController.ErrorResponse) response.getBody();
        assertThat(body.isSuccess()).isFalse();
        assertThat(body.getErrorMessage()).isEqualTo("登録可能な行がありません。");
    }

    @Test
    void uploadReturnsBadRequestOnIllegalArgument() throws Exception {
        when(applicationFormService.generate(any(), any()))
                .thenThrow(new IllegalArgumentException("ファイルの文字コードがサポートされていません"));

        ResponseEntity<?> response = controller.generate(Destination.JCB, file);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        ApplicationFormController.ErrorResponse body =
                (ApplicationFormController.ErrorResponse) response.getBody();
        assertThat(body.getErrorMessage()).isEqualTo("ファイルの文字コードがサポートされていません");
    }

    @Test
    void uploadReturnsServerErrorOnIoException() throws Exception {
        when(applicationFormService.generate(any(), any())).thenThrow(new IOException("read failed"));

        ResponseEntity<?> response = controller.generate(Destination.JCB, file);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        ApplicationFormController.ErrorResponse body =
                (ApplicationFormController.ErrorResponse) response.getBody();
        assertThat(body.getErrorMessage()).contains("ファイルの読み込みに失敗しました");
    }

    @Test
    void uploadReturnsServerErrorOnRuntimeException() throws Exception {
        when(applicationFormService.generate(any(), any()))
                .thenThrow(new RuntimeException("テンプレートの読み込みに失敗しました"));

        ResponseEntity<?> response = controller.generate(Destination.JCB, file);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        ApplicationFormController.ErrorResponse body =
                (ApplicationFormController.ErrorResponse) response.getBody();
        assertThat(body.getErrorMessage()).contains("予期せぬエラーが発生しました");
    }

    @Test
    void encodesErrorSummaryHeaderWithRowAndMessage() throws Exception {
        List<CsvValidationError> errors = List.of(
                new CsvValidationError(3, "取引コード", "取引コードは必須です。"));
        ApplicationFormGenerateResult result = ApplicationFormGenerateResult.success(
                new byte[] {1}, 1, 2, errors);
        when(applicationFormService.generate(Destination.JCB, file)).thenReturn(result);

        ResponseEntity<?> response = controller.generate(Destination.JCB, file);

        String encoded = response.getHeaders().getFirst("X-Error-Summary");
        String decoded = URLDecoder.decode(encoded, StandardCharsets.UTF_8);
        assertThat(decoded).isEqualTo("3行目:取引コードは必須です。");
        assertThat(response.getHeaders().getFirst("X-Error-Count")).isEqualTo("1");
    }

    @Test
    void limitsErrorSummaryToFirstFiveErrors() throws Exception {
        List<CsvValidationError> errors = List.of(
                new CsvValidationError(2, "取引コード", "エラー1"),
                new CsvValidationError(3, "取引コード", "エラー2"),
                new CsvValidationError(4, "取引コード", "エラー3"),
                new CsvValidationError(5, "取引コード", "エラー4"),
                new CsvValidationError(6, "取引コード", "エラー5"),
                new CsvValidationError(7, "取引コード", "エラー6"),
                new CsvValidationError(8, "取引コード", "エラー7"));
        ApplicationFormGenerateResult result = ApplicationFormGenerateResult.success(
                new byte[] {1}, 1, 8, errors);
        when(applicationFormService.generate(Destination.JCB, file)).thenReturn(result);

        ResponseEntity<?> response = controller.generate(Destination.JCB, file);

        String decoded = URLDecoder.decode(
                response.getHeaders().getFirst("X-Error-Summary"), StandardCharsets.UTF_8);
        assertThat(decoded).doesNotContain("エラー6");
        assertThat(decoded).doesNotContain("エラー7");
        assertThat(decoded).contains("エラー5");
        assertThat(response.getHeaders().getFirst("X-Error-Count")).isEqualTo("7");
    }

}
