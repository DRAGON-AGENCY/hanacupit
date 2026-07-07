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
    private static final String HEADER_REQUESTED_WITH = "X-Requested-With";
    private static final String VALUE_XML_HTTP_REQUEST = "XMLHttpRequest";

    /**
     * 要求の処理前にログイン状態を検査する。
     * 未ログインの場合、通常の画面遷移（ブラウザのページ読み込み）はログイン画面へ
     * リダイレクトする。一方、fetch による非同期要求（アップロード等）はリダイレクトを
     * 追わせても画面遷移が発生せず、レスポンス本文（ログイン画面のHTML）をJSONとして
     * 解釈しようとして通信エラー扱いになってしまう。そのため、要求ヘッダー
     * 「X-Requested-With: XMLHttpRequest」が付与された非同期要求には 401 を返し、
     * 呼び出し側のJSでログイン画面へのリダイレクトを行わせる。
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
        if (VALUE_XML_HTTP_REQUEST.equals(request.getHeader(HEADER_REQUESTED_WITH))) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        response.sendRedirect(request.getContextPath() + LOGIN_PATH);
        return false;
    }
}
