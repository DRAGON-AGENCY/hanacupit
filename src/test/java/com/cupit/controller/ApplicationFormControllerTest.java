package com.cupit.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.dto.ApplicationFormGenerateResponse;
import com.cupit.service.applicationform.ApplicationFormGenerateResult;
import com.cupit.service.applicationform.ApplicationFormService;
import com.cupit.service.applicationform.ApplicationFormService.Destination;

/**
 * {@link ApplicationFormController} のテスト。Serviceをモック化し、常にJSON
 * （{@link ApplicationFormGenerateResponse}）で結果を返すこと（申請先ごとの
 * ファイル名・拡張子・Content-Type、Excelのbase64同梱、行単位のエラー詳細の
 * 全件反映、部分成功時のNG扱い、例外種別ごとのHTTPステータス）を検証する。
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
    void returnsBase64FileDataAndCountsOnFullSuccessForJcb() throws Exception {
        ApplicationFormGenerateResult result = ApplicationFormGenerateResult.success(
                new byte[] {1, 2, 3}, 2, 2, List.of());
        when(applicationFormService.generate(Destination.JCB, file)).thenReturn(result);

        ResponseEntity<ApplicationFormGenerateResponse> response =
                controller.generate(Destination.JCB, file);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        ApplicationFormGenerateResponse body = response.getBody();
        assertThat(body.isSuccess()).isTrue();
        assertThat(body.getSuccessCount()).isEqualTo(2);
        assertThat(body.getTotalRowCount()).isEqualTo(2);
        assertThat(body.getErrors()).isEmpty();
        assertThat(body.getErrorMessage()).isNull();
        assertThat(body.getFileName()).startsWith("JCB申込フォーム_").endsWith(".xlsx");
        assertThat(body.getContentType())
                .isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        assertThat(Base64.getDecoder().decode(body.getFileData())).isEqualTo(new byte[] {1, 2, 3});
    }

    @Test
    void returnsXlsmContentTypeAndFilenameForSmccKamei() throws Exception {
        ApplicationFormGenerateResult result = ApplicationFormGenerateResult.success(
                new byte[] {1}, 1, 1, List.of());
        when(applicationFormService.generate(Destination.SMCC_KAMEI, file)).thenReturn(result);

        ApplicationFormGenerateResponse body =
                controller.generate(Destination.SMCC_KAMEI, file).getBody();

        assertThat(body.getFileName()).startsWith("SMCC加盟店申込書_").endsWith(".xlsm");
        assertThat(body.getContentType())
                .isEqualToIgnoringCase("application/vnd.ms-excel.sheet.macroEnabled.12");
    }

    @Test
    void returnsXlsxFilenameForSmccTenpo() throws Exception {
        ApplicationFormGenerateResult result = ApplicationFormGenerateResult.success(
                new byte[] {1}, 1, 1, List.of());
        when(applicationFormService.generate(Destination.SMCC_TENPO, file)).thenReturn(result);

        ApplicationFormGenerateResponse body =
                controller.generate(Destination.SMCC_TENPO, file).getBody();

        assertThat(body.getFileName()).startsWith("SMCC店舗情報一覧_").endsWith(".xlsx");
    }

    @Test
    void returnsOkWithSuccessFalseAndNoFileWhenNoRegistrableRows() throws Exception {
        List<CsvValidationError> errors = List.of(
                new CsvValidationError(2, "取引コード", "取引コードは必須です。"));
        ApplicationFormGenerateResult result =
                ApplicationFormGenerateResult.error("取引コードは必須です。", errors);
        when(applicationFormService.generate(Destination.JCB, file)).thenReturn(result);

        ResponseEntity<ApplicationFormGenerateResponse> response =
                controller.generate(Destination.JCB, file);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        ApplicationFormGenerateResponse body = response.getBody();
        assertThat(body.isSuccess()).isFalse();
        assertThat(body.getErrorMessage()).isEqualTo("取引コードは必須です。");
        assertThat(body.getFileData()).isNull();
        assertThat(body.getErrors()).hasSize(1);
        assertThat(body.getErrors().get(0).getRowNumber()).isEqualTo(2);
        assertThat(body.getErrors().get(0).getColumnName()).isEqualTo("取引コード");
        assertThat(body.getErrors().get(0).getMessage()).isEqualTo("取引コードは必須です。");
    }

    @Test
    void treatsPartialSuccessAsNgWithFullErrorTableButStillReturnsFile() throws Exception {
        List<CsvValidationError> errors = List.of(
                new CsvValidationError(3, "取引コード", "取引コードは必須です。"),
                new CsvValidationError(5, "サービス開始希望日", "日付変換エラーです。"));
        ApplicationFormGenerateResult result = ApplicationFormGenerateResult.success(
                new byte[] {9}, 1, 3, errors);
        when(applicationFormService.generate(Destination.JCB, file)).thenReturn(result);

        ResponseEntity<ApplicationFormGenerateResponse> response =
                controller.generate(Destination.JCB, file);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        ApplicationFormGenerateResponse body = response.getBody();
        assertThat(body.isSuccess()).isFalse();
        assertThat(body.getErrors()).hasSize(2);
        assertThat(body.getFileData()).isNotNull();
        assertThat(Base64.getDecoder().decode(body.getFileData())).isEqualTo(new byte[] {9});
        assertThat(body.getErrorMessage()).contains("生成件数: 1 件").contains("エラー: 2 件");
    }

    @Test
    void returnsBadRequestOnIllegalArgument() throws Exception {
        when(applicationFormService.generate(any(), any()))
                .thenThrow(new IllegalArgumentException("ファイルの文字コードがサポートされていません"));

        ResponseEntity<ApplicationFormGenerateResponse> response =
                controller.generate(Destination.JCB, file);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getErrorMessage())
                .isEqualTo("ファイルの文字コードがサポートされていません");
    }

    @Test
    void returnsServerErrorOnIoException() throws Exception {
        when(applicationFormService.generate(any(), any())).thenThrow(new IOException("read failed"));

        ResponseEntity<ApplicationFormGenerateResponse> response =
                controller.generate(Destination.JCB, file);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().getErrorMessage()).contains("ファイルの読み込みに失敗しました");
    }

    @Test
    void returnsServerErrorOnRuntimeException() throws Exception {
        when(applicationFormService.generate(any(), any()))
                .thenThrow(new RuntimeException("テンプレートの読み込みに失敗しました"));

        ResponseEntity<ApplicationFormGenerateResponse> response =
                controller.generate(Destination.JCB, file);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().getErrorMessage()).contains("予期せぬエラーが発生しました");
    }

}
