package com.cupit.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cupit.dto.LoginRequest;
import com.cupit.dto.LoginResponse;
import com.cupit.interceptor.AuthenticationInterceptor;
import com.cupit.model.Employee;
import com.cupit.model.LoginResult;
import com.cupit.service.EmployeeAuthenticationService;
import com.cupit.service.EmployeeService;

/**
 * ログイン認証要求を処理するコントローラ。
 * 画面から送信されたユーザIDとパスワードを照合し、
 * 認証結果を JSON で返す。
 */
@Controller
public class LoginController {

    private static final String MESSAGE_INVALID_CREDENTIALS =
            "ユーザIDまたはパスワードが正しくありません。";
    private static final String MESSAGE_LOCKED =
            "アカウントがロックされています。サポートまでお問い合わせください。";

    private final EmployeeAuthenticationService employeeAuthenticationService;
    private final EmployeeService employeeService;

    public LoginController(
            EmployeeAuthenticationService employeeAuthenticationService,
            EmployeeService employeeService) {
        this.employeeAuthenticationService = employeeAuthenticationService;
        this.employeeService = employeeService;
    }

    /**
     * ユーザIDとパスワードを照合し、認証結果を返す。
     * 認証に成功した場合はセッションにログイン済みユーザーを保持する。
     *
     * @param loginRequest 画面から送信されたユーザIDとパスワード
     * @param session ログイン状態を保持するセッション
     * @return 認証結果。成功可否と失敗時のメッセージを含む
     */
    @PostMapping(
            value = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public LoginResponse login(
            @RequestBody LoginRequest loginRequest,
            HttpSession session) {
        String userId = trimToNull(loginRequest.getUserId());
        LoginResult result = employeeAuthenticationService.authenticate(
                userId, loginRequest.getPassword());
        if (result == LoginResult.SUCCESS) {
            session.setAttribute(
                    AuthenticationInterceptor.SESSION_ATTRIBUTE_LOGIN_USER,
                    userId);
            // 画面の権限制御で参照するため、権限コードをセッションに保持する
            Employee employee = employeeService.findByUserId(userId);
            if (employee != null) {
                session.setAttribute(
                        AuthenticationInterceptor
                                .SESSION_ATTRIBUTE_AUTHORITY_CODE,
                        employee.getAuthorityCode());
            }
        }
        return toResponse(result);
    }

    /**
     * セッションを破棄してログアウトし、ログイン画面へ遷移する。
     *
     * @param session 破棄対象のセッション
     * @return ログイン画面へのリダイレクト
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    private LoginResponse toResponse(LoginResult result) {
        switch (result) {
            case SUCCESS:
                return new LoginResponse(true, null);
            case LOCKED:
                return new LoginResponse(false, MESSAGE_LOCKED);
            case INVALID_CREDENTIALS:
            default:
                return new LoginResponse(false, MESSAGE_INVALID_CREDENTIALS);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        return value.trim();
    }
}
