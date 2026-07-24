package com.cupit.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.cupit.dto.ApplicationFormGenerateResponse;
import com.cupit.service.applicationform.ApplicationFormGenerateResult;
import com.cupit.service.applicationform.ApplicationFormService;
import com.cupit.service.applicationform.ApplicationFormService.Destination;

/**
 * 「各決済会社所定申込フォーム作成」画面のコントローラ。
 * INPUT CSVをアップロードし、指定された決済会社所定フォーマット（JCB／SMCC加盟店申込書／
 * SMCC店舗情報一覧）のExcelを生成する。
 * 他のCSVインポート機能（member_master等）と同様、常にJSON（
 * {@link ApplicationFormGenerateResponse}）で結果を返す。Excelはバイナリを直接
 * 返すのではなくBase64エンコードしてJSON内に含め、行単位のエラー詳細（行番号・列名・
 * エラー内容の全件）も同じレスポンスで返せるようにする。クライアント側はBase64を
 * デコードしてダウンロードを実行する。
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
     * INPUT CSVを検証・解析し、指定された申請先のExcelを生成する。
     *
     * @param destination 申請先（JCB／SMCC_KAMEI／SMCC_TENPO）
     * @param file        アップロードファイル
     * @return 生成結果 JSON（Excelを生成できた場合はBase64で同梱）
     */
    @PostMapping(value = "/application_form/generate",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApplicationFormGenerateResponse> generate(
            @RequestParam("destination") Destination destination,
            @RequestParam("file") MultipartFile file) {
        try {
            ApplicationFormGenerateResult result = applicationFormService.generate(destination, file);
            return ResponseEntity.ok(buildResponse(destination, result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(errorOnly(e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(errorOnly("ファイルの読み込みに失敗しました: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError()
                    .body(errorOnly("予期せぬエラーが発生しました: " + e.getMessage()));
        }
    }

    private ApplicationFormGenerateResponse buildResponse(
            Destination destination, ApplicationFormGenerateResult result) {
        List<ApplicationFormGenerateResponse.ErrorDetail> errors = result.getErrors().stream()
                .map(e -> new ApplicationFormGenerateResponse.ErrorDetail(
                        e.getRowNumber(), e.getColumnName(), e.getMessage()))
                .collect(Collectors.toList());
        boolean success = result.isSuccess() && errors.isEmpty();

        String fileName = null;
        String fileData = null;
        String contentType = null;
        if (result.getExcelBytes() != null) {
            fileName = filePrefix(destination) + "_"
                    + LocalDate.now().format(FILENAME_DATE_FORMAT) + fileExtension(destination);
            fileData = Base64.getEncoder().encodeToString(result.getExcelBytes());
            contentType = contentType(destination);
        }

        String errorMessage = success ? null : buildErrorMessage(result);
        return new ApplicationFormGenerateResponse(
                success, result.getSuccessCount(), result.getTotalRowCount(),
                errorMessage, errors, fileName, fileData, contentType);
    }

    /**
     * 早期リターン（ファイル未選択・致命的フォーマットエラー・登録可能な行が0件）の
     * 場合はServiceが組み立てたメッセージをそのまま使う。1件以上生成できたが
     * 行単位のエラーが残っている場合（部分成功）は、ここでサマリーを組み立てる。
     */
    private String buildErrorMessage(ApplicationFormGenerateResult result) {
        if (!result.isSuccess()) {
            return result.getErrorMessage();
        }
        return "生成件数: " + result.getSuccessCount() + " 件、エラー: " + result.getErrors().size()
                + " 件（データ行数: " + result.getTotalRowCount() + "行）。"
                + "エラーが発生した行は生成されていません。";
    }

    private ApplicationFormGenerateResponse errorOnly(String errorMessage) {
        return new ApplicationFormGenerateResponse(
                false, 0, 0, errorMessage, List.of(), null, null, null);
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

}
