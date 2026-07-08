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
    private final ReplaceConfirmation replaceConfirmation;

    public ImportResponse(boolean success, int importedCount, Integer batchId,
            String errorMessage, List<ErrorDetail> errors,
            int totalRowCount, boolean errorLimitReached) {
        this(success, importedCount, batchId, errorMessage, errors,
                totalRowCount, errorLimitReached, null);
    }

    public ImportResponse(boolean success, int importedCount, Integer batchId,
            String errorMessage, List<ErrorDetail> errors,
            int totalRowCount, boolean errorLimitReached, ReplaceConfirmation replaceConfirmation) {
        this.success = success;
        this.importedCount = importedCount;
        this.batchId = batchId;
        this.errorMessage = errorMessage;
        this.errors = errors != null ? errors : List.of();
        this.totalRowCount = totalRowCount;
        this.errorLimitReached = errorLimitReached;
        this.replaceConfirmation = replaceConfirmation;
    }

    public ImportResponse(boolean success, int importedCount, Integer batchId, String errorMessage) {
        this(success, importedCount, batchId, errorMessage, List.of(), 0, false, null);
    }

    /**
     * 同じ決済種別で、エラーを含んだまま未確定のバッチが既に存在する場合に、
     * 今回のアップロードで置き換えるかどうかの確認をユーザーに求めるためのレスポンスを生成する。
     * このレスポンスを受け取った画面側は、確認後に replace=true を付けて再送信する。
     */
    public static ImportResponse replaceConfirmationRequired(ReplaceConfirmation confirmation) {
        return new ImportResponse(false, 0, null,
                "同じ決済種別でエラーを含んだまま未確定のデータが既に存在します。",
                List.of(), 0, false, confirmation);
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

    public ReplaceConfirmation getReplaceConfirmation() {
        return replaceConfirmation;
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

    /**
     * 同じ決済種別で既に存在する、エラーを含んだまま未確定のインポートバッチの情報。
     * 画面側でユーザーに置き換えの可否を確認するために使用する。
     */
    public static class ReplaceConfirmation {

        private final int existingBatchId;
        private final String existingFileName;
        private final int existingRecordCount;
        private final int existingErrorCount;

        public ReplaceConfirmation(
                int existingBatchId, String existingFileName,
                int existingRecordCount, int existingErrorCount) {
            this.existingBatchId = existingBatchId;
            this.existingFileName = existingFileName;
            this.existingRecordCount = existingRecordCount;
            this.existingErrorCount = existingErrorCount;
        }

        public int getExistingBatchId() {
            return existingBatchId;
        }

        public String getExistingFileName() {
            return existingFileName;
        }

        public int getExistingRecordCount() {
            return existingRecordCount;
        }

        public int getExistingErrorCount() {
            return existingErrorCount;
        }
    }
}
