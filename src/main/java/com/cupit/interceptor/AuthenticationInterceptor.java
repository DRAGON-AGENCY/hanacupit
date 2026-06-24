package com.cupit.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * ログイン状態を検査するインターセプター。
 * セッションにログイン済みのユーザーが保持されていない要求は、
 * ログイン画面へリダイレクトしてアクセスを遮断する。
 */
public class AuthenticationInterceptor implements HandlerInterceptor {

    /** ログイン済みユーザーを保持するセッション属性名。 */
    public static final String SESSION_ATTRIBUTE_LOGIN_USER = "loginUser";

    /** ログイン済みユーザーの権限コードを保持するセッション属性名。 */
    public static final String SESSION_ATTRIBUTE_AUTHORITY_CODE = "authorityCode";

    private static final String LOGIN_PATH = "/login";

    /**
     * 要求の処理前にログイン状態を検査する。
     * 未ログインの場合はログイン画面へリダイレクトして処理を中断する。
     *
     * @param request 受信した HTTP 要求
     * @param response 返却する HTTP 応答
     * @param handler 実行対象のハンドラ
     * @return ログイン済みの場合は true。未ログインの場合は false
     * @throws Exception リダイレクト処理で入出力例外が発生した場合
     */
    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null
                && session.getAttribute(SESSION_ATTRIBUTE_LOGIN_USER) != null) {
            return true;
        }
        response.sendRedirect(request.getContextPath() + LOGIN_PATH);
        return false;
    }
}
