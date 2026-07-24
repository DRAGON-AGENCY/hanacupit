package com.cupit.dto;

import java.util.List;

/**
 * 「各決済会社所定申込フォーム作成」画面の生成結果を JSON で返すレスポンス DTO。
 * 登録可能な行が1件以上あればExcelファイルをBase64エンコードして{@code fileData}に
 * 含める（この場合も行単位のエラーがあれば{@code success}はfalseになるが、
 * 生成できた行のExcelはダウンロードさせる）。
 */
public class ApplicationFormGenerateResponse {

    private final boolean success;
    private final int successCount;
    private final int totalRowCount;
    private final String errorMessage;
    private final List<ErrorDetail> errors;
    private final String fileName;
    private final String fileData;
    private final String contentType;

    public ApplicationFormGenerateResponse(
            boolean success, int successCount, int totalRowCount, String errorMessage,
            List<ErrorDetail> errors, String fileName, String fileData, String contentType) {
        this.success = success;
        this.successCount = successCount;
        this.totalRowCount = totalRowCount;
        this.errorMessage = errorMessage;
        this.errors = errors != null ? errors : List.of();
        this.fileName = fileName;
        this.fileData = fileData;
        this.contentType = contentType;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getTotalRowCount() {
        return totalRowCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public List<ErrorDetail> getErrors() {
        return errors;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileData() {
        return fileData;
    }

    public String getContentType() {
        return contentType;
    }

    /**
     * エラー1件分の詳細情報。
     */
    public static class ErrorDetail {

        private final int rowNumber;
        private final String columnName;
        private final String message;

        public ErrorDetail(int rowNumber, String columnName, String message) {
            this.rowNumber = rowNumber;
            this.columnName = columnName;
            this.message = message;
        }

        public int getRowNumber() {
            return rowNumber;
        }

        public String getColumnName() {
            return columnName;
        }

        public String getMessage() {
            return message;
        }
    }

}
