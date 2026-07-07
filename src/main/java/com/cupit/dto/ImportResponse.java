package com.cupit.dto;

import java.util.List;

/**
 * ファイルインポート処理の結果を返す DTO。
 */
public class ImportResponse {

    private final boolean success;
    private final int importedCount;
    private final Integer batchId;
    private final String errorMessage;
    private final List<ErrorDetail> errors;
    private final int totalRowCount;
    private final boolean errorLimitReached;

    public ImportResponse(boolean success, int importedCount, Integer batchId,
            String errorMessage, List<ErrorDetail> errors,
            int totalRowCount, boolean errorLimitReached) {
        this.success = success;
        this.importedCount = importedCount;
        this.batchId = batchId;
        this.errorMessage = errorMessage;
        this.errors = errors != null ? errors : List.of();
        this.totalRowCount = totalRowCount;
        this.errorLimitReached = errorLimitReached;
    }

    public ImportResponse(boolean success, int importedCount, Integer batchId, String errorMessage) {
        this(success, importedCount, batchId, errorMessage, List.of(), 0, false);
    }

    public boolean isSuccess() {
        return success;
    }

    public int getImportedCount() {
        return importedCount;
    }

    public Integer getBatchId() {
        return batchId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public List<ErrorDetail> getErrors() {
        return errors;
    }

    public int getTotalRowCount() {
        return totalRowCount;
    }

    public boolean isErrorLimitReached() {
        return errorLimitReached;
    }

    /**
     * インポートエラーの1件分の詳細。
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
