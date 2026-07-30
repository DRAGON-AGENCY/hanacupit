package com.cupit.dto;

/**
 * 項目コード情報の保存・削除結果を画面へ返すためのクラス。
 * 処理の成否と、失敗時に表示するメッセージを保持する。
 */
public class SettlementItemCodeResponse {

    private final boolean success;
    private final String message;

    public SettlementItemCodeResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
