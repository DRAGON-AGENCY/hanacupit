package com.cupit.dto;

import java.util.List;

/**
 * CSVフォーマット検証結果を JSON で返すレスポンス DTO。
 */
public class CsvValidationResponse {

    private final boolean valid;
    private final int totalRowCount;
    private final boolean errorLimitReached;
    private final List<ErrorDetail> errors;

    public CsvValidationResponse(
            boolean valid,
            int totalRowCount,
            boolean errorLimitReached,
            List<ErrorDetail> errors) {
        this.valid = valid;
        this.totalRowCount = totalRowCount;
        this.errorLimitReached = errorLimitReached;
        this.errors = errors;
    }

    public boolean isValid() {
        return valid;
    }

    public int getTotalRowCount() {
        return totalRowCount;
    }

    public boolean isErrorLimitReached() {
        return errorLimitReached;
    }

    public List<ErrorDetail> getErrors() {
        return errors;
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
