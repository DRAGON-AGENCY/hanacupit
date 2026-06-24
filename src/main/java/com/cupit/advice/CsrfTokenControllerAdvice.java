package com.cupit.advice;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.cupit.security.CsrfTokenManager;

/**
 * すべての画面に CSRF トークンを渡すための共通処理。
 * 画面 (テンプレート) は受け取ったトークンを meta 要素へ埋め込み、
 * 更新系の要求でヘッダーに設定して送信する。
 */
@ControllerAdvice
public class CsrfTokenControllerAdvice {

    /**
     * セッションの CSRF トークンをモデルに追加する。
     * トークンが無い場合は新たに生成して保持する。
     *
     * @param session 現在のセッション
     * @return CSRF トークン
     */
    @ModelAttribute(CsrfTokenManager.MODEL_ATTRIBUTE_TOKEN)
    public String csrfToken(HttpSession session) {
        return CsrfTokenManager.resolveToken(session);
    }
}
