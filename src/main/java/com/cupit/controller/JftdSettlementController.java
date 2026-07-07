package com.cupit.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.cupit.dto.CsvValidationResponse;
import com.cupit.dto.ImportResponse;
import com.cupit.interceptor.AuthenticationInterceptor;
import com.cupit.service.JftdSettlementService;

import jakarta.servlet.http.HttpSession;

/**
 * JFTD精算データ作成（PAYGATE Station）画面のファイルアップロード・フォーマット検証・データ登録を処理するコントローラ。
 */
@Controller
public class JftdSettlementController {

    private final JftdSettlementService jftdSettlementService;

    public JftdSettlementController(JftdSettlementService jftdSettlementService) {
        this.jftdSettlementService = jftdSettlementService;
    }

    /**
     * アップロードされたINPUTファイルのフォーマットを検証して結果を JSON で返す。
     *
     * @param file        アップロードファイル
     * @param paymentType 決済種類の表示名（例: "JCB"、"スマレジ"）
     * @return フォーマット検証結果 JSON
     */
    @PostMapping(value = "/jftd_settlement/validate",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<CsvValidationResponse> validate(
            @RequestParam("file") MultipartFile file,
            @RequestParam("paymentType") String paymentType) {
        try {
            CsvValidationResponse response =
                    jftdSettlementService.validateFileFormat(file, paymentType);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            CsvValidationResponse errorResponse = new CsvValidationResponse(
                    false, 0, false,
                    java.util.List.of(
                            new CsvValidationResponse.ErrorDetail(0, "", e.getMessage())));
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (IOException e) {
            CsvValidationResponse errorResponse = new CsvValidationResponse(
                    false, 0, false,
                    java.util.List.of(
                            new CsvValidationResponse.ErrorDetail(
                                    0, "", "ファイルの読み込みに失敗しました: " + e.getMessage())));
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * アップロードされたINPUTファイルをフォーマット検証のうえDBに登録し、結果を JSON で返す。
     *
     * @param file        アップロードファイル
     * @param paymentType 決済種類の表示名
     * @param session     HTTPセッション（ログインユーザー取得用）
     * @return インポート結果 JSON
     */
    @PostMapping(value = "/jftd_settlement/import",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ImportResponse> importFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("paymentType") String paymentType,
            HttpSession session) {
        try {
            Object loginUserAttr = session.getAttribute(
                    AuthenticationInterceptor.SESSION_ATTRIBUTE_LOGIN_USER);
            String memberNo = (loginUserAttr != null) ? loginUserAttr.toString() : "UNKNOWN";
            ImportResponse response = jftdSettlementService.importFile(
                    file, paymentType, memberNo);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ImportResponse(false, 0, null, e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(new ImportResponse(
                            false, 0, null,
                            "ファイルの読み込みに失敗しました: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body(new ImportResponse(
                            false, 0, null,
                            "予期せぬエラーが発生しました: " + e.getMessage()));
        }
    }
}
