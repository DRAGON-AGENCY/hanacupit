package com.cupit.service.applicationform;

import java.util.List;

import com.cupit.csv.CsvValidationError;

/**
 * 各決済会社所定申込フォーム作成の生成結果。
 * 成功時（登録可能な行が1件以上ある場合、行単位のエラーがあっても部分的に生成する）は
 * Excelバイト列と件数を、失敗時（ファイル未選択・致命的フォーマットエラー・
 * 登録可能な行が0件）はエラーメッセージのみを保持する。
 */
public class ApplicationFormGenerateResult {

    private final boolean success;
    private final byte[] excelBytes;
    private final int successCount;
    private final int totalRowCount;
    private final List<CsvValidationError> errors;
    private final String errorMessage;

    private ApplicationFormGenerateResult(
            boolean success, byte[] excelBytes, int successCount, int totalRowCount,
            List<CsvValidationError> errors, String errorMessage) {
        this.success = success;
        this.excelBytes = excelBytes;
        this.successCount = successCount;
        this.totalRowCount = totalRowCount;
        this.errors = errors;
        this.errorMessage = errorMessage;
    }

    public static ApplicationFormGenerateResult success(
            byte[] excelBytes, int successCount, int totalRowCount,
            List<CsvValidationError> errors) {
        return new ApplicationFormGenerateResult(
                true, excelBytes, successCount, totalRowCount, errors, null);
    }

    public static ApplicationFormGenerateResult error(String errorMessage) {
        return new ApplicationFormGenerateResult(false, null, 0, 0, List.of(), errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public byte[] getExcelBytes() {
        return excelBytes;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getTotalRowCount() {
        return totalRowCount;
    }

    public List<CsvValidationError> getErrors() {
        return errors;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
