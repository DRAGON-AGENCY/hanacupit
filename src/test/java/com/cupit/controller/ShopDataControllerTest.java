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

import com.cupit.csv.writer.SmccMerchantNoCsvWriter;
import com.cupit.csv.writer.SteraStoreCsvWriter;
import com.cupit.csv.writer.SteraTerminalCsvWriter;
import com.cupit.dto.ImportResponse;
import com.cupit.interceptor.AuthenticationInterceptor;
import com.cupit.model.SmccMerchantNo;
import com.cupit.model.SteraStore;
import com.cupit.model.SteraTerminal;
import com.cupit.repository.SmccMerchantNoRepository;
import com.cupit.repository.SteraStoreRepository;
import com.cupit.repository.SteraTerminalRepository;
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
    private SteraStoreRepository steraStoreRepository;

    @Mock
    private SteraTerminalRepository steraTerminalRepository;

    @Mock
    private SmccMerchantNoRepository smccMerchantNoRepository;

    @Mock
    private SteraStoreCsvWriter steraStoreCsvWriter;

    @Mock
    private SteraTerminalCsvWriter steraTerminalCsvWriter;

    @Mock
    private SmccMerchantNoCsvWriter smccMerchantNoCsvWriter;

    @Mock
    private HttpSession session;

    private ShopDataController controller;
    private MockMultipartFile file;

    @BeforeEach
    void setUp() {
        controller = new ShopDataController(
                shopDataService, steraStoreRepository, steraTerminalRepository,
                smccMerchantNoRepository, steraStoreCsvWriter, steraTerminalCsvWriter,
                smccMerchantNoCsvWriter);
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
    void downloadReturnsSteraStoreCsvWithAttachmentFilenameForShop() {
        SteraStore data = new SteraStore();
        data.setTradeCode("01-001");
        when(steraStoreRepository.findAllByOrderByTradeCodeAsc()).thenReturn(List.of(data));
        byte[] csvBytes = "dummy".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        when(steraStoreCsvWriter.writeCsv(List.of(data))).thenReturn(csvBytes);

        ResponseEntity<byte[]> response = controller.download(DataType.SHOP);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(csvBytes);
        assertThat(response.getHeaders().getContentDisposition().isAttachment()).isTrue();
        assertThat(response.getHeaders().getContentDisposition().getFilename())
                .startsWith("stera_store_").endsWith(".csv");
    }

    @Test
    void downloadReturnsSteraTerminalCsvWithAttachmentFilenameForTerminal() {
        SteraTerminal data = new SteraTerminal();
        data.setTradeCode("01-001");
        when(steraTerminalRepository.findAllByOrderByTradeCodeAsc()).thenReturn(List.of(data));
        byte[] csvBytes = "dummy".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        when(steraTerminalCsvWriter.writeCsv(List.of(data))).thenReturn(csvBytes);

        ResponseEntity<byte[]> response = controller.download(DataType.TERMINAL);

        assertThat(response.getBody()).isEqualTo(csvBytes);
        assertThat(response.getHeaders().getContentDisposition().getFilename())
                .startsWith("stera_terminal_").endsWith(".csv");
    }

    @Test
    void downloadReturnsSmccMerchantNoCsvWithAttachmentFilenameForMerchantNumber() {
        SmccMerchantNo data = new SmccMerchantNo();
        data.setTradeCode("01-001");
        when(smccMerchantNoRepository.findAllByOrderByTradeCodeAsc()).thenReturn(List.of(data));
        byte[] csvBytes = "dummy".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        when(smccMerchantNoCsvWriter.writeCsv(List.of(data))).thenReturn(csvBytes);

        ResponseEntity<byte[]> response = controller.download(DataType.MERCHANT_NUMBER);

        assertThat(response.getBody()).isEqualTo(csvBytes);
        assertThat(response.getHeaders().getContentDisposition().getFilename())
                .startsWith("smcc_merchant_no_").endsWith(".csv");
    }

}
