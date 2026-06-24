package com.cupit.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cupit.model.Employee;
import com.cupit.model.LoginResult;
import com.cupit.repository.EmployeeRepository;

/**
 * 社員のログイン認証を担うサービス。
 * 画面から入力されたユーザIDとパスワードを、
 * 社員マスタ (m_employee) に登録された値と照合する。
 */
@Service
public class EmployeeAuthenticationService {

    private static final int MAX_PASSWORD_ERROR_COUNT = 5;
    private static final int RESET_PASSWORD_ERROR_COUNT = 0;
    private static final String BCRYPT_PREFIX_2A = "$2a$";
    private static final String BCRYPT_PREFIX_2B = "$2b$";
    private static final String BCRYPT_PREFIX_2Y = "$2y$";

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeAuthenticationService(
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * ユーザIDとパスワードを照合してログイン可否を判定する。
     * パスワードが一致した場合は誤り回数を 0 に戻し、
     * 一致しなかった場合は誤り回数を 1 加算する。
     * 誤り回数が上限に達した社員はロック状態とする。
     *
     * @param userId 画面から入力されたユーザID
     * @param password 画面から入力されたパスワード
     * @return 認証結果
     */
    @Transactional
    public LoginResult authenticate(String userId, String password) {
        if (userId == null || userId.isBlank()
                || password == null || password.isEmpty()) {
            return LoginResult.INVALID_CREDENTIALS;
        }

        Employee employee = employeeRepository.findById(userId).orElse(null);
        if (employee == null) {
            return LoginResult.INVALID_CREDENTIALS;
        }

        if (employee.getPasswordErrorCount() >= MAX_PASSWORD_ERROR_COUNT) {
            return LoginResult.LOCKED;
        }

        if (matchesPassword(password, employee.getPassword())) {
            // 誤り回数が既に 0 の場合は更新不要。
            // 成功時の大半を占めるこのケースで DB 書き込みを省き応答を速くする
            if (employee.getPasswordErrorCount() != RESET_PASSWORD_ERROR_COUNT) {
                employeeRepository.updatePasswordErrorCount(
                        employee.getUserId(), RESET_PASSWORD_ERROR_COUNT);
            }
            return LoginResult.SUCCESS;
        }

        int updatedErrorCount = employee.getPasswordErrorCount() + 1;
        employeeRepository.updatePasswordErrorCount(
                employee.getUserId(), updatedErrorCount);
        if (updatedErrorCount >= MAX_PASSWORD_ERROR_COUNT) {
            return LoginResult.LOCKED;
        }
        return LoginResult.INVALID_CREDENTIALS;
    }

    /**
     * 入力されたパスワードと保存済みパスワードを照合する。
     * 保存値が BCrypt ハッシュの場合は BCrypt で検証し、
     * それ以外は平文として比較する (ハッシュ移行期の互換のため)。
     *
     * @param rawPassword 画面から入力されたパスワード
     * @param storedPassword 社員マスタに保存されたパスワード
     * @return 一致する場合は true
     */
    private boolean matchesPassword(String rawPassword, String storedPassword) {
        if (storedPassword == null) {
            return false;
        }
        if (isBcryptHash(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return rawPassword.equals(storedPassword);
    }

    /**
     * 文字列が BCrypt ハッシュの形式かどうかを判定する。
     *
     * @param value 判定対象の文字列
     * @return BCrypt ハッシュの接頭辞で始まる場合は true
     */
    private boolean isBcryptHash(String value) {
        return value.startsWith(BCRYPT_PREFIX_2A)
                || value.startsWith(BCRYPT_PREFIX_2B)
                || value.startsWith(BCRYPT_PREFIX_2Y);
    }
}
