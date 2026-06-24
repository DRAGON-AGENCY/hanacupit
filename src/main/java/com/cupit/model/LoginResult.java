package com.cupit.model;

/**
 * ログイン認証の結果を表す区分。
 */
public enum LoginResult {

    /** 認証成功。 */
    SUCCESS,

    /** メールアドレス未登録、またはパスワード不一致。 */
    INVALID_CREDENTIALS,

    /** アカウントがロックされている。 */
    LOCKED;
}
