package com.cupit.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.cupit.csv.CsvValidationError;
import com.cupit.service.applicationform.ApplicationFormGenerateResult;
import com.cupit.service.applicationform.ApplicationFormService;
import com.cupit.service.applicationform.ApplicationFormService.Destination;

/**
 * 「各決済会社所定申込フォーム作成」画面のコントローラ。
 * INPUT CSVをアップロードし、指定された決済会社所定フォーマット（JCB／SMCC加盟店申込書／
 * SMCC店舗情報一覧）のExcelを生成してダウンロードさせる。
 * 生成に失敗した場合はJSONでエラーを返し、成功した場合はExcelバイナリを返す
 * （行単位のエラーで一部の行がスキップされた場合も、正常な行が1件以上あれば
 * レスポンスヘッダーで件数を通知したうえでExcelを返す。バイナリレスポンスと
 * JSONエラーレスポンスを同時に返せないため、行単位のエラー詳細は
 * レスポンスヘッダーの件数のみで通知し、詳細はアップロード前のクライアント側
 * フォーマットチェックで防止する想定）。
 */
@Controller
public class ApplicationFormController {

    private static final DateTimeFormatter FILENAME_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ApplicationFormService applicationFormService;

    public ApplicationFormController(ApplicationFormService applicationFormService) {
        this.applicationFormService = applicationFormService;
    }

    /**
     * INPUT CSVを検証・解析し、指定された申請先のExcelを生成してダウンロードさせる。
     *
     * @param destination 申請先（JCB／SMCC_KAMEI／SMCC_TENPO）
     * @param file        アップロードファイル
     * @return 成功時はExcelファイル、失敗時はJSONエラー
     */
    @PostMapping(value = "/application_form/generate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> generate(
            @RequestParam("destination") Destination destination,
            @RequestParam("file") MultipartFile file) {
        try {
            ApplicationFormGenerateResult result = applicationFormService.generate(destination, file);
            if (!result.isSuccess()) {
                return ResponseEntity.badRequest().body(new ErrorResponse(result.getErrorMessage()));
            }
            return buildFileResponse(destination, result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("ファイルの読み込みに失敗しました: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("予期せぬエラーが発生しました: " + e.getMessage()));
        }
    }

    private ResponseEntity<byte[]> buildFileResponse(
            Destination destination, ApplicationFormGenerateResult result) {
        String filename = filePrefix(destination) + "_"
                + LocalDate.now().format(FILENAME_DATE_FORMAT) + fileExtension(destination);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build());
        headers.add("X-Success-Count", String.valueOf(result.getSuccessCount()));
        headers.add("X-Total-Row-Count", String.valueOf(result.getTotalRowCount()));
        headers.add("X-Error-Count", String.valueOf(result.getErrors().size()));
        headers.add("X-Error-Summary", encodeErrorSummary(result.getErrors()));
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                "X-Success-Count, X-Total-Row-Count, X-Error-Count, X-Error-Summary");
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(contentType(destination)))
                .body(result.getExcelBytes());
    }

    /**
     * 行単位のエラー詳細をレスポンスヘッダーで通知するため、改行等を含まない
     * 1行のサマリー文字列にエンコードする（詳細な行番号・列名・メッセージの一覧は
     * アップロード前のクライアント側フォーマットチェックで防止する想定のため、
     * ここでは先頭数件のみを簡潔に伝える）。
     */
    private String encodeErrorSummary(List<CsvValidationError> errors) {
        if (errors.isEmpty()) {
            return "";
        }
        String summary = errors.stream()
                .limit(5)
                .map(e -> e.getRowNumber() + "行目:" + e.getMessage())
                .collect(Collectors.joining(" / "));
        String ascii = java.net.URLEncoder.encode(summary, StandardCharsets.UTF_8);
        return ascii;
    }

    private String filePrefix(Destination destination) {
        return switch (destination) {
            case JCB -> "JCB申込フォーム";
            case SMCC_KAMEI -> "SMCC加盟店申込書";
            case SMCC_TENPO -> "SMCC店舗情報一覧";
        };
    }

    private String fileExtension(Destination destination) {
        return destination == Destination.SMCC_KAMEI ? ".xlsm" : ".xlsx";
    }

    private String contentType(Destination destination) {
        return destination == Destination.SMCC_KAMEI
                ? "application/vnd.ms-excel.sheet.macroEnabled.12"
                : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    /**
     * 生成失敗時のJSONエラーレスポンス。
     */
    public static class ErrorResponse {

        private final boolean success = false;
        private final String errorMessage;

        public ErrorResponse(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

}
