package com.cupit.security;

import java.security.SecureRandom;
import java.util.Base64;

import jakarta.servlet.http.HttpSession;

/**
 * CSRF 対策に用いるトークンを生成・保持するためのユーティリティ。
 * セッションごとに 1 つのトークンを保持し、状態を変更する要求の
 * 正当性検査と、画面へのトークン埋め込みに使用する。
 */
public final class CsrfTokenManager {

    /** CSRF トークンを保持するセッション属性名。 */
    public static final String SESSION_ATTRIBUTE_TOKEN = "csrfToken";

    /** 画面 (テンプレート) へトークンを渡すモデル属性名。 */
    public static final String MODEL_ATTRIBUTE_TOKEN = "csrfToken";

    /** 画面からの要求でトークンを格納する HTTP ヘッダー名。 */
    public static final String HEADER_NAME = "X-CSRF-TOKEN";

    private static final int TOKEN_BYTE_LENGTH = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private CsrfTokenManager() {
    }

    /**
     * セッションに保持されたトークンを取得する。
     * まだトークンが無い場合は新たに生成して保持してから返す。
     *
     * @param session 対象のセッション
     * @return セッションに保持された CSRF トークン
     */
    public static String resolveToken(HttpSession session) {
        String token = findToken(session);
        if (token != null) {
            return token;
        }
        String generatedToken = generateToken();
        session.setAttribute(SESSION_ATTRIBUTE_TOKEN, generatedToken);
        return generatedToken;
    }

    /**
     * セッションに保持されたトークンを取得する。生成は行わない。
     *
     * @param session 対象のセッション
     * @return 保持されたトークン。存在しない場合は null
     */
    public static String findToken(HttpSession session) {
        Object token = session.getAttribute(SESSION_ATTRIBUTE_TOKEN);
        if (token instanceof String && !((String) token).isEmpty()) {
            return (String) token;
        }
        return null;
    }

    /**
     * 推測困難な CSRF トークンを生成する。
     *
     * @return 生成したトークン
     */
    private static String generateToken() {
        byte[] randomBytes = new byte[TOKEN_BYTE_LENGTH];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(randomBytes);
    }
}
