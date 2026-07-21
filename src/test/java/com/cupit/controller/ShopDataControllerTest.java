package com.cupit.controller;

import static org.assertj.core.api.Assertions.assertThat;
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

import com.cupit.csv.writer.MerchantNumberDataCsvWriter;
import com.cupit.csv.writer.ShopDataCsvWriter;
import com.cupit.csv.writer.TerminalDataCsvWriter;
import com.cupit.dto.ImportResponse;
import com.cupit.interceptor.AuthenticationInterceptor;
import com.cupit.model.MerchantNumberData;
import com.cupit.model.ShopData;
import com.cupit.model.TerminalData;
import com.cupit.repository.MerchantNumberDataRepository;
import com.cupit.repository.ShopDataRepository;
import com.cupit.repository.TerminalDataRepository;
import com.cupit.service.ShopDataService;
import com.cupit.service.ShopDataService.DataType;

import jakarta.servlet.http.HttpSession;

/**
 * {@link ShopDataController} のテスト。Serviceをモック化し、正常時のHTTP 200・
 * 例外種別ごとのステータス（IllegalArgumentException→400、IOException／
 * RuntimeException→500）、セッションからのログインユーザー取得（未ログイン時は
 * "UNKNOWN"）、およびデータ種類ごとのダウンロード時のCSV生成とファイル名を検証する。
 */
@ExtendWith(MockitoExtension.class)
class ShopDataControllerTest {

    @Mock
    private ShopDataService shopDataService;

    @Mock
    private ShopDataRepository shopDataRepository;

    @Mock
    private TerminalDataRepository terminalDataRepository;

    @Mock
    private MerchantNumberDataRepository merchantNumberDataRepository;

    @Mock
    private ShopDataCsvWriter shopDataCsvWriter;

    @Mock
    private TerminalDataCsvWriter terminalDataCsvWriter;

    @Mock
    private MerchantNumberDataCsvWriter merchantNumberDataCsvWriter;

    @Mock
    private HttpSession session;

    private ShopDataController controller;
    private MockMultipartFile file;

    @BeforeEach
    void setUp() {
        controller = new ShopDataController(
                shopDataService, shopDataRepository, terminalDataRepository,
                merchantNumberDataRepository, shopDataCsvWriter, terminalDataCsvWriter,
                merchantNumberDataCsvWriter);
        file = new MockMultipartFile("file", "shop_data.csv", "text/csv", new byte[] {1});
    }

    @Test
    void uploadReturnsOkWithBodyOnSuccess() throws Exception {
        ImportResponse body = new ImportResponse(true, 3, 1, null);
        when(session.getAttribute(AuthenticationInterceptor.SESSION_ATTRIBUTE_LOGIN_USER))
                .thenReturn("user001");
        when(shopDataService.importFile(eq(DataType.SHOP), eq(file), eq("user001")))
                .thenReturn(body);

        ResponseEntity<ImportResponse> response = controller.upload(DataType.SHOP, file, session);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isSameAs(body);
    }

    @Test
    void uploadUsesUnknownWhenSessionHasNoLoginUser() throws Exception {
        when(session.getAttribute(AuthenticationInterceptor.SESSION_ATTRIBUTE_LOGIN_USER))
                .thenReturn(null);
        when(shopDataService.importFile(eq(DataType.SHOP), eq(file), eq("UNKNOWN")))
                .thenReturn(new ImportResponse(true, 1, 1, null));

        ResponseEntity<ImportResponse> response = controller.upload(DataType.SHOP, file, session);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().isSuccess()).isTrue();
    }

    @Test
    void uploadReturnsBadRequestOnIllegalArgument() throws Exception {
        when(session.getAttribute(AuthenticationInterceptor.SESSION_ATTRIBUTE_LOGIN_USER))
                .thenReturn("user001");
        when(shopDataService.importFile(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenThrow(new IllegalArgumentException("不正なファイルです"));

        ResponseEntity<ImportResponse> response = controller.upload(DataType.SHOP, file, session);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getErrorMessage()).isEqualTo("不正なファイルです");
    }

    @Test
    void uploadReturnsServerErrorOnIoException() throws Exception {
        when(session.getAttribute(AuthenticationInterceptor.SESSION_ATTRIBUTE_LOGIN_USER))
                .thenReturn("user001");
        when(shopDataService.importFile(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenThrow(new IOException("read failed"));

        ResponseEntity<ImportResponse> response = controller.upload(DataType.SHOP, file, session);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().getErrorMessage()).contains("ファイルの読み込みに失敗しました");
    }

    @Test
    void uploadReturnsServerErrorOnRuntimeException() throws Exception {
        when(session.getAttribute(AuthenticationInterceptor.SESSION_ATTRIBUTE_LOGIN_USER))
                .thenReturn("user001");
        when(shopDataService.importFile(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenThrow(new RuntimeException("could not execute statement"));

        ResponseEntity<ImportResponse> response = controller.upload(DataType.SHOP, file, session);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().getErrorMessage()).contains("予期せぬエラーが発生しました");
    }

    @Test
    void downloadReturnsShopDataCsvWithAttachmentFilenameForShop() {
        ShopData data = new ShopData();
        data.setTradeCode("01-001");
        when(shopDataRepository.findAllByOrderByTradeCodeAsc()).thenReturn(List.of(data));
        byte[] csvBytes = "dummy".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        when(shopDataCsvWriter.writeCsv(List.of(data))).thenReturn(csvBytes);

        ResponseEntity<byte[]> response = controller.download(DataType.SHOP);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(csvBytes);
        assertThat(response.getHeaders().getContentDisposition().isAttachment()).isTrue();
        assertThat(response.getHeaders().getContentDisposition().getFilename())
                .startsWith("shop_data_").endsWith(".csv");
    }

    @Test
    void downloadReturnsTerminalDataCsvWithAttachmentFilenameForTerminal() {
        TerminalData data = new TerminalData();
        data.setTradeCode("01-001");
        when(terminalDataRepository.findAllByOrderByTradeCodeAsc()).thenReturn(List.of(data));
        byte[] csvBytes = "dummy".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        when(terminalDataCsvWriter.writeCsv(List.of(data))).thenReturn(csvBytes);

        ResponseEntity<byte[]> response = controller.download(DataType.TERMINAL);

        assertThat(response.getBody()).isEqualTo(csvBytes);
        assertThat(response.getHeaders().getContentDisposition().getFilename())
                .startsWith("terminal_data_").endsWith(".csv");
    }

    @Test
    void downloadReturnsMerchantNumberDataCsvWithAttachmentFilenameForMerchantNumber() {
        MerchantNumberData data = new MerchantNumberData();
        data.setTradeCode("01-001");
        when(merchantNumberDataRepository.findAllByOrderByTradeCodeAsc()).thenReturn(List.of(data));
        byte[] csvBytes = "dummy".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        when(merchantNumberDataCsvWriter.writeCsv(List.of(data))).thenReturn(csvBytes);

        ResponseEntity<byte[]> response = controller.download(DataType.MERCHANT_NUMBER);

        assertThat(response.getBody()).isEqualTo(csvBytes);
        assertThat(response.getHeaders().getContentDisposition().getFilename())
                .startsWith("merchant_number_data_").endsWith(".csv");
    }

}
