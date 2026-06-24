package com.cupit.interceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

import com.cupit.security.CsrfTokenManager;

/**
 * 状態を変更する要求の CSRF トークンを検査するインターセプター。
 * 参照系 (GET/HEAD/OPTIONS/TRACE) は検査せず、更新系
 * (POST/PUT/PATCH/DELETE) のみセッションのトークンと
 * 要求ヘッダーのトークンが一致することを確認する。
 * 一致しない要求は 403 (Forbidden) として処理を中断する。
 */
public class CsrfProtectionInterceptor implements HandlerInterceptor {

    private static final String METHOD_GET = "GET";
    private static final String METHOD_HEAD = "HEAD";
    private static final String METHOD_OPTIONS = "OPTIONS";
    private static final String METHOD_TRACE = "TRACE";
    private static final String MESSAGE_INVALID_TOKEN =
            "CSRF token validation failed.";

    /**
     * 要求の処理前に CSRF トークンを検査する。
     * 更新系の要求でトークンが一致しない場合は 403 を返して中断する。
     *
     * @param request 受信した HTTP 要求
     * @param response 返却する HTTP 応答
     * @param handler 実行対象のハンドラ
     * @return 検査を通過した場合は true。失敗した場合は false
     * @throws Exception 応答の書き込みで入出力例外が発生した場合
     */
    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) throws Exception {
        if (isSafeMethod(request.getMethod())) {
            return true;
        }
        HttpSession session = request.getSession(false);
        String sessionToken =
                (session == null) ? null : CsrfTokenManager.findToken(session);
        String requestToken = request.getHeader(CsrfTokenManager.HEADER_NAME);
        if (isValidToken(sessionToken, requestToken)) {
            return true;
        }
        response.sendError(
                HttpServletResponse.SC_FORBIDDEN, MESSAGE_INVALID_TOKEN);
        return false;
    }

    /**
     * 状態を変更しない参照系の HTTP メソッドかどうかを判定する。
     *
     * @param method HTTP メソッド名
     * @return 参照系の場合は true
     */
    private boolean isSafeMethod(String method) {
        return METHOD_GET.equals(method)
                || METHOD_HEAD.equals(method)
                || METHOD_OPTIONS.equals(method)
                || METHOD_TRACE.equals(method);
    }

    /**
     * セッションのトークンと要求のトークンが一致するかどうかを判定する。
     * タイミング攻撃を避けるため一定時間で比較する。
     *
     * @param sessionToken セッションに保持されたトークン
     * @param requestToken 要求ヘッダーで送信されたトークン
     * @return 双方が存在し一致する場合は true
     */
    private boolean isValidToken(String sessionToken, String requestToken) {
        if (sessionToken == null || requestToken == null) {
            return false;
        }
        byte[] sessionBytes = sessionToken.getBytes(StandardCharsets.UTF_8);
        byte[] requestBytes = requestToken.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(sessionBytes, requestBytes);
    }
}
