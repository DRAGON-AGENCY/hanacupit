package com.cupit.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.cupit.dto.CsvValidationResponse;
import com.cupit.dto.ImportResponse;
import com.cupit.interceptor.AuthenticationInterceptor;
import com.cupit.model.PaygateStoreMapping;
import com.cupit.repository.PaygateMappingRepository;
import com.cupit.service.PaygateMappingService;

import jakarta.servlet.http.HttpSession;

/**
 * 取引コード紐付データ作成・照会のコントローラ。
 * CSV アップロード（フォーマット検証・取引コード単位での洗い替え登録）と取引コード検索を処理する。
 */
@Controller
public class PaygateMappingController {

    private final PaygateMappingService paygateMappingService;
    private final PaygateMappingRepository paygateMappingRepository;

    public PaygateMappingController(
            PaygateMappingService paygateMappingService,
            PaygateMappingRepository paygateMappingRepository) {
        this.paygateMappingService = paygateMappingService;
        this.paygateMappingRepository = paygateMappingRepository;
    }

    /**
     * アップロードされた CSV のフォーマットを検証して結果を JSON で返す。
     *
     * @param file アップロードファイル
     * @return フォーマット検証結果 JSON
     */
    @PostMapping(value = "/paygate_mapping/validate",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<CsvValidationResponse> validate(
            @RequestParam("file") MultipartFile file) {
        try {
            CsvValidationResponse response = paygateMappingService.validateFile(file);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            CsvValidationResponse errorResponse = new CsvValidationResponse(
                    false, 0, false,
                    List.of(new CsvValidationResponse.ErrorDetail(0, "", e.getMessage())));
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (IOException e) {
            CsvValidationResponse errorResponse = new CsvValidationResponse(
                    false, 0, false,
                    List.of(new CsvValidationResponse.ErrorDetail(
                            0, "", "ファイルの読み込みに失敗しました: " + e.getMessage())));
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * CSV を検証のうえ m_paygate_store_mapping を取引コード単位で洗い替えし、結果を JSON で返す。
     *
     * @param file    アップロードファイル
     * @param session HTTPセッション（ログインユーザー取得用）
     * @return インポート結果 JSON
     */
    @PostMapping(value = "/paygate_mapping/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ImportResponse> upload(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        try {
            Object loginUserAttr = session.getAttribute(
                    AuthenticationInterceptor.SESSION_ATTRIBUTE_LOGIN_USER);
            String loginUser = (loginUserAttr != null) ? loginUserAttr.toString() : "UNKNOWN";
            ImportResponse response = paygateMappingService.importFile(file, loginUser);
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

    /**
     * 取引コードで m_paygate_store_mapping を検索し、端末一覧を JSON で返す。
     *
     * @param tradeCode 取引コード
     * @return 紐付情報リスト JSON
     */
    @GetMapping(value = "/paygate_mapping_search",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<PaygateStoreMapping>> search(
            @RequestParam(name = "tradeCode", required = false) String tradeCode) {
        if (tradeCode == null || tradeCode.isBlank()) {
            return ResponseEntity.ok(
                    paygateMappingRepository.findAllByOrderByTradeCodeAscTerminalIdAsc());
        }
        List<PaygateStoreMapping> result =
                paygateMappingRepository.findByTradeCodeOrderByTerminalId(tradeCode.trim());
        return ResponseEntity.ok(result);
    }

}
