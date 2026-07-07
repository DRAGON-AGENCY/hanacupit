package com.cupit.csv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CSVフォーマット検証の結果を保持するクラス。
 * 発生した全エラーを件数上限なく収集する（旧 MAX_ERRORS=50 による打ち切りは廃止）。
 */
public class CsvValidationResult {

    private final List<CsvValidationError> errors = new ArrayList<>();
    private int totalRowCount;
    private boolean fatal;

    public void addError(CsvValidationError error) {
        errors.add(error);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    /**
     * ファイル拡張子不正・空ファイル・ヘッダー行の列数不正など、データ行単位の
     * 部分登録では救済できない致命的エラーであることを示す。
     * データ行単位のエラー（列数不足・重複・数値変換エラー等）はマークしない。
     */
    public void markFatal() {
        this.fatal = true;
    }

    public boolean isFatal() {
        return fatal;
    }

    public List<CsvValidationError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public int getTotalRowCount() {
        return totalRowCount;
    }

    public void setTotalRowCount(int totalRowCount) {
        this.totalRowCount = totalRowCount;
    }

    /**
     * 旧仕様のエラー件数上限（MAX_ERRORS=50）は廃止されたため常に false を返す。
     * {@link com.cupit.dto.CsvValidationResponse#isErrorLimitReached()} との
     * 互換のためメソッドとして残している。
     */
    public boolean isErrorLimitReached() {
        return false;
    }
}
