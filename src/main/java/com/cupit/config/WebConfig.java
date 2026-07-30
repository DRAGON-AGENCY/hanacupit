package com.cupit.config;

import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.cupit.interceptor.AuthenticationInterceptor;
import com.cupit.interceptor.CsrfProtectionInterceptor;

/**
 * Spring MVC の追加設定。
 * ログイン状態を検査するインターセプターと、状態を変更する要求の
 * CSRF トークンを検査するインターセプターを登録する。
 * ログイン画面と静的リソースは認証の対象外とする。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String PATTERN_ALL = "/**";
    private static final String PATTERN_ROOT = "/";
    private static final String PATTERN_LOGIN = "/login";
    private static final String PATTERN_PASSWORD_RESET = "/password-reset";
    private static final String PATTERN_STATIC_CSS = "/css/**";
    private static final String PATTERN_STATIC_JS = "/js/**";
    private static final String PATTERN_STATIC_IMAGES = "/images/**";
    private static final String PATTERN_FAVICON = "/favicon.ico";
    private static final String PATTERN_ERROR = "/error";

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(new AuthenticationInterceptor())
                .addPathPatterns(PATTERN_ALL)
                .excludePathPatterns(
                        PATTERN_ROOT,
                        PATTERN_LOGIN,
                        PATTERN_PASSWORD_RESET,
                        PATTERN_STATIC_CSS,
                        PATTERN_STATIC_JS,
                        PATTERN_STATIC_IMAGES,
                        PATTERN_FAVICON,
                        PATTERN_ERROR);

        // CSRF 検査はログイン要求 (/login の POST) にも適用するため、
        // 認証とは別にログイン画面・ルートを対象から除外しない。
        // 参照系メソッドはインターセプター内で検査せず通過させる。
        registry.addInterceptor(new CsrfProtectionInterceptor())
                .addPathPatterns(PATTERN_ALL)
                .excludePathPatterns(
                        PATTERN_STATIC_CSS,
                        PATTERN_STATIC_JS,
                        PATTERN_STATIC_IMAGES,
                        PATTERN_FAVICON,
                        PATTERN_ERROR);
    }

    /**
     * セッションが一定時間操作されないだけで自動的にログイン画面へ戻される挙動を
     * 廃止する。本プロジェクトにはweb.xmlが無く、放置していたのはTomcatサーバー側の
     * デフォルトのセッションタイムアウト（未設定時は30分）だった。
     * ServletContext#setSessionTimeout()は0以下を指定するとセッションが
     * タイムアウトしなくなる仕様（Jakarta Servlet仕様）のため、これを使う。
     * ログイン自体（AuthenticationInterceptor）は引き続き必須で、
     * 「ログアウト」操作やブラウザ・Tomcat再起動によるセッション破棄では
     * 通常どおりログイン画面に戻る。
     */
    @Bean
    public ServletContextInitializer sessionTimeoutInitializer() {
        return servletContext -> servletContext.setSessionTimeout(-1);
    }
}
