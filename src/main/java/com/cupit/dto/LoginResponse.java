package com.cupit.dto;

/**
 * ログイン結果を画面へ返すためのクラス。
 * 認証可否と、失敗時に表示するメッセージを保持する。
 */
public class LoginResponse {

    private final boolean success;
    private final String message;

    public LoginResponse(boolean success, String message) {
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
