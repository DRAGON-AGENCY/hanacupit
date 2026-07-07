package com.cupit.csv;

/**
 * CSVフォーマット検証で検出された1件のエラー情報。
 */
public class CsvValidationError {

    private final int rowNumber;
    private final String columnName;
    private final String message;

    public CsvValidationError(int rowNumber, String columnName, String message) {
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
