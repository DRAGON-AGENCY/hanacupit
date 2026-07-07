package com.cupit.csv.importer;

import java.util.Collections;
import java.util.List;

import com.cupit.csv.CsvValidationError;

/**
 * ファイルインポート処理の結果を保持するクラス。
 * エラーが発生した行はスキップし、正常な行のみ登録したうえで、
 * 発生した全エラーを呼び出し元に返す。
 */
public class ImportResult {

    private final int successCount;
    private final int totalRowCount;
    private final List<CsvValidationError> errors;

    public ImportResult(int successCount, int totalRowCount, List<CsvValidationError> errors) {
        this.successCount = successCount;
        this.totalRowCount = totalRowCount;
        this.errors = Collections.unmodifiableList(errors);
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

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
