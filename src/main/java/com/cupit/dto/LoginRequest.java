package com.cupit.dto;

/**
 * ログイン要求の入力値を保持するクラス。
 * 画面から送信された JSON をバインドする。
 */
public class LoginRequest {

    private String userId;
    private String password;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
