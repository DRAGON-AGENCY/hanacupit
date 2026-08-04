package com.cupit.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cupit.dto.EmployeeRequest;
import com.cupit.dto.EmployeeResponse;
import com.cupit.model.Employee;
import com.cupit.repository.EmployeeRepository;

/**
 * 社員情報の取得・登録・更新・削除を担うサービス。
 * 社員マスタ (m_employee) に対する一覧取得とメンテナンスを提供する。
 */
@Service
public class EmployeeService {

    private static final String MODE_NEW = "new";
    private static final String AUTHORITY_ADMINISTRATOR = "01";
    private static final String AUTHORITY_OPERATOR = "02";
    private static final String AUTHORITY_VIEWER = "03";

    // ユーザIDは半角英数字のみ許可する。m_employee.user_idはVARCHAR(255)だが、
    // 他テーブルの更新者記録カラム(update_employee等)がVARCHAR(50)のため、
    // コピー先で切り詰められないよう50文字以内に制限する。
    private static final int USER_ID_MAX_LENGTH = 50;
    private static final Pattern USER_ID_PATTERN =
            Pattern.compile("^[A-Za-z0-9]{1," + USER_ID_MAX_LENGTH + "}$");

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$");

    private static final int PASSWORD_MIN_LENGTH = 8;
    // BCrypt は 72 バイトを超える分を切り捨てるため上限を 72 文字とする
    private static final int PASSWORD_MAX_LENGTH = 72;
    // 使用を許可する文字 (空白を含まない半角の英数字・記号)
    private static final Pattern PASSWORD_ALLOWED_PATTERN =
            Pattern.compile("^[\\x21-\\x7E]+$");
    private static final Pattern PASSWORD_UPPERCASE_PATTERN =
            Pattern.compile("[A-Z]");
    private static final Pattern PASSWORD_LOWERCASE_PATTERN =
            Pattern.compile("[a-z]");
    private static final Pattern PASSWORD_DIGIT_PATTERN =
            Pattern.compile("[0-9]");
    private static final Pattern PASSWORD_SYMBOL_PATTERN =
            Pattern.compile("[!-/:-@\\[-`{-~]");

    // 電話番号・FAX は半角数字とハイフンのみを許可する
    private static final Pattern PHONE_NUMBER_PATTERN =
            Pattern.compile("^[0-9-]+$");

    private static final String MESSAGE_INVALID_INPUT =
            "入力内容が正しくありません。";
    private static final String MESSAGE_USER_ID_REQUIRED =
            "ユーザIDを入力してください。";
    private static final String MESSAGE_USER_ID_FORMAT =
            "ユーザIDは半角英数字50文字以内で入力してください。";
    private static final String MESSAGE_USER_ID_DUPLICATED =
            "入力されたユーザIDは既に登録されています。";
    private static final String MESSAGE_EMAIL_REQUIRED =
            "メールアドレスを入力してください。";
    private static final String MESSAGE_EMAIL_FORMAT =
            "メールアドレスの形式が正しくありません。";
    private static final String MESSAGE_EMAIL_DUPLICATED =
            "入力されたメールアドレスは既に登録されています。";
    private static final String MESSAGE_NAME_REQUIRED =
            "社員名を入力してください。";
    private static final String MESSAGE_KANA_REQUIRED =
            "社員名 (カナ) を入力してください。";
    private static final String MESSAGE_DEPARTMENT_REQUIRED =
            "部署を入力してください。";
    private static final String MESSAGE_AUTHORITY_INVALID =
            "権限コードが正しくありません。";
    private static final String MESSAGE_PHONE_REQUIRED =
            "電話番号を入力してください。";
    private static final String MESSAGE_PHONE_FORMAT =
            "電話番号は半角数字とハイフンで入力してください。";
    private static final String MESSAGE_FAX_FORMAT =
            "FAX は半角数字とハイフンで入力してください。";
    private static final String MESSAGE_PASSWORD_REQUIRED =
            "パスワードを入力してください。";
    private static final String MESSAGE_PASSWORD_LENGTH =
            "パスワードは 8 文字以上 72 文字以内で入力してください。";
    private static final String MESSAGE_PASSWORD_CHARACTER =
            "パスワードは半角の英数字と記号で入力してください。";
    private static final String MESSAGE_PASSWORD_POLICY =
            "パスワードは英大文字・英小文字・数字・記号を"
            + "それぞれ 1 文字以上含めてください。";
    private static final String MESSAGE_ERROR_COUNT_NEGATIVE =
            "パスワード入力間違い回数は 0 以上で入力してください。";
    private static final String MESSAGE_EMPLOYEE_NOT_FOUND =
            "対象の社員が見つかりません。";

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 全社員をユーザー ID の昇順で取得する。
     *
     * @return 社員の一覧
     */
    public List<Employee> findAllEmployees() {
        return employeeRepository.findAllByOrderByUserIdAsc();
    }

    /**
     * メールアドレスを指定して社員を 1 件取得する。
     *
     * @param email メールアドレス
     * @return 該当する社員。存在しない場合は null
     */
    public Employee findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return employeeRepository.findByEmail(email).orElse(null);
    }

    /**
     * ユーザー ID を指定して社員を 1 件取得する。
     *
     * @param userId ユーザー ID
     * @return 該当する社員。存在しない場合は null
     */
    public Employee findByUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        return employeeRepository.findById(userId).orElse(null);
    }

    /**
     * 社員情報を登録または更新する。
     * 入力値を検査し、問題があれば失敗結果とメッセージを返す。
     *
     * @param request 画面から送信された社員情報
     * @param loginUserId 操作中のログインユーザの user_id (更新者として記録する)
     * @return 処理結果
     */
    @Transactional
    public EmployeeResponse saveEmployee(
            EmployeeRequest request, String loginUserId) {
        if (request == null) {
            return new EmployeeResponse(false, MESSAGE_INVALID_INPUT);
        }

        String userId = trimToEmpty(request.getUserId());
        String email = trimToEmpty(request.getEmail());
        String employeeName = trimToEmpty(request.getEmployeeName());
        String employeeNameKana = trimToEmpty(request.getEmployeeNameKana());
        String department = trimToEmpty(request.getDepartment());
        String authorityCode = trimToEmpty(request.getAuthorityCode());
        String phoneNumber = trimToEmpty(request.getPhoneNumber());
        String faxNumber = trimToEmpty(request.getFaxNumber());
        String password = request.getPassword();

        boolean isNewMode = MODE_NEW.equals(request.getMode());
        // ユーザIDは新規登録時のみ画面からの入力値を検査する。
        // 編集時は既存社員を識別するIDとして使うのみで、形式は登録時に検査済みのため
        // 再検査しない。
        if (isNewMode) {
            String userIdMessage = validateUserId(userId);
            if (userIdMessage != null) {
                return new EmployeeResponse(false, userIdMessage);
            }
        }

        String validationMessage = validateInput(
                email, employeeName, employeeNameKana, department,
                authorityCode, phoneNumber, faxNumber,
                request.getPasswordErrorCount());
        if (validationMessage != null) {
            return new EmployeeResponse(false, validationMessage);
        }

        if (isNewMode) {
            return createEmployee(
                    userId, email, employeeName, employeeNameKana, department,
                    authorityCode, phoneNumber, faxNumber, password,
                    request.getPasswordErrorCount(), loginUserId);
        }
        return updateEmployee(
                userId, email, employeeName,
                employeeNameKana, department, authorityCode, phoneNumber,
                faxNumber, password, request.getPasswordErrorCount(),
                loginUserId);
    }

    /**
     * ユーザー ID を指定して社員を削除する。
     *
     * @param userId ユーザー ID
     * @return 処理結果
     */
    @Transactional
    public EmployeeResponse deleteEmployee(String userId) {
        String targetUserId = trimToEmpty(userId);
        if (targetUserId.isEmpty()
                || !employeeRepository.existsById(targetUserId)) {
            return new EmployeeResponse(false, MESSAGE_EMPLOYEE_NOT_FOUND);
        }
        employeeRepository.deleteById(targetUserId);
        return new EmployeeResponse(true, null);
    }

    /**
     * 新規社員を登録する。ユーザIDとメールアドレスの重複、パスワード必須を検査する。
     */
    private EmployeeResponse createEmployee(
            String userId, String email, String employeeName, String employeeNameKana,
            String department, String authorityCode, String phoneNumber,
            String faxNumber, String password, int passwordErrorCount,
            String loginUserId) {
        if (employeeRepository.existsById(userId)) {
            return new EmployeeResponse(false, MESSAGE_USER_ID_DUPLICATED);
        }
        if (employeeRepository.existsByEmail(email)) {
            return new EmployeeResponse(false, MESSAGE_EMAIL_DUPLICATED);
        }
        String passwordMessage = validatePassword(password);
        if (passwordMessage != null) {
            return new EmployeeResponse(false, passwordMessage);
        }

        Employee employee = new Employee();
        employee.setUserId(userId);
        employee.setEmail(email);
        employee.setEmployeeName(employeeName);
        employee.setEmployeeNameKana(employeeNameKana);
        employee.setDepartment(department);
        employee.setAuthorityCode(authorityCode);
        employee.setPhoneNumber(phoneNumber);
        employee.setFaxNumber(faxNumber);
        employee.setPassword(passwordEncoder.encode(password));
        employee.setPasswordErrorCount(passwordErrorCount);

        LocalDateTime now = LocalDateTime.now();
        employee.setCreatedAt(now);
        employee.setUpdatedAt(now);
        // 更新者として操作中のログインユーザの user_id を記録する
        employee.setUpdateUserId(loginUserId);

        employeeRepository.save(employee);
        return new EmployeeResponse(true, null);
    }

    /**
     * 既存社員を更新する。パスワードは空欄の場合に現状を維持する。
     */
    private EmployeeResponse updateEmployee(
            String userId, String email, String employeeName,
            String employeeNameKana, String department, String authorityCode,
            String phoneNumber, String faxNumber, String password,
            int passwordErrorCount, String loginUserId) {
        if (userId.isEmpty()) {
            return new EmployeeResponse(false, MESSAGE_EMPLOYEE_NOT_FOUND);
        }
        Employee employee = employeeRepository.findById(userId).orElse(null);
        if (employee == null) {
            return new EmployeeResponse(false, MESSAGE_EMPLOYEE_NOT_FOUND);
        }
        if (employeeRepository.existsByEmailAndUserIdNot(email, userId)) {
            return new EmployeeResponse(false, MESSAGE_EMAIL_DUPLICATED);
        }

        // パスワードは入力された場合のみ検査する (空欄は現状維持)
        boolean passwordProvided = password != null && !password.isBlank();
        if (passwordProvided) {
            String passwordMessage = validatePassword(password);
            if (passwordMessage != null) {
                return new EmployeeResponse(false, passwordMessage);
            }
        }

        employee.setEmail(email);
        employee.setEmployeeName(employeeName);
        employee.setEmployeeNameKana(employeeNameKana);
        employee.setDepartment(department);
        employee.setAuthorityCode(authorityCode);
        employee.setPhoneNumber(phoneNumber);
        employee.setFaxNumber(faxNumber);
        employee.setPasswordErrorCount(passwordErrorCount);

        // パスワードは入力された場合のみハッシュ化して更新する (空欄は現状維持)
        if (passwordProvided) {
            employee.setPassword(passwordEncoder.encode(password));
        }

        // 更新日時にシステム日時を、更新者に操作中のログインユーザの
        // user_id を設定する
        employee.setUpdatedAt(LocalDateTime.now());
        employee.setUpdateUserId(loginUserId);

        employeeRepository.save(employee);
        return new EmployeeResponse(true, null);
    }

    /**
     * 入力値の必須・形式・範囲を検査する。
     *
     * @return 問題があればエラーメッセージ。問題が無ければ null
     */
    private String validateInput(
            String email, String employeeName, String employeeNameKana,
            String department, String authorityCode, String phoneNumber,
            String faxNumber, int passwordErrorCount) {
        if (email.isEmpty()) {
            return MESSAGE_EMAIL_REQUIRED;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return MESSAGE_EMAIL_FORMAT;
        }
        if (employeeName.isEmpty()) {
            return MESSAGE_NAME_REQUIRED;
        }
        if (employeeNameKana.isEmpty()) {
            return MESSAGE_KANA_REQUIRED;
        }
        if (department.isEmpty()) {
            return MESSAGE_DEPARTMENT_REQUIRED;
        }
        if (!isValidAuthorityCode(authorityCode)) {
            return MESSAGE_AUTHORITY_INVALID;
        }
        if (phoneNumber.isEmpty()) {
            return MESSAGE_PHONE_REQUIRED;
        }
        if (!PHONE_NUMBER_PATTERN.matcher(phoneNumber).matches()) {
            return MESSAGE_PHONE_FORMAT;
        }
        // FAX は任意項目のため、入力された場合のみ形式を検査する
        if (!faxNumber.isEmpty()
                && !PHONE_NUMBER_PATTERN.matcher(faxNumber).matches()) {
            return MESSAGE_FAX_FORMAT;
        }
        if (passwordErrorCount < 0) {
            return MESSAGE_ERROR_COUNT_NEGATIVE;
        }
        return null;
    }

    /**
     * パスワードがポリシーを満たすかどうかを検査する。
     * 8〜72 文字の半角英数字・記号で構成され、英大文字・英小文字・数字・
     * 記号をそれぞれ 1 文字以上含むことを必須とする。
     *
     * @param password 検査対象のパスワード
     * @return 問題があればエラーメッセージ。問題が無ければ null
     */
    private String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return MESSAGE_PASSWORD_REQUIRED;
        }
        if (password.length() < PASSWORD_MIN_LENGTH
                || password.length() > PASSWORD_MAX_LENGTH) {
            return MESSAGE_PASSWORD_LENGTH;
        }
        if (!PASSWORD_ALLOWED_PATTERN.matcher(password).matches()) {
            return MESSAGE_PASSWORD_CHARACTER;
        }
        if (!PASSWORD_UPPERCASE_PATTERN.matcher(password).find()
                || !PASSWORD_LOWERCASE_PATTERN.matcher(password).find()
                || !PASSWORD_DIGIT_PATTERN.matcher(password).find()
                || !PASSWORD_SYMBOL_PATTERN.matcher(password).find()) {
            return MESSAGE_PASSWORD_POLICY;
        }
        return null;
    }

    /**
     * 権限コードが許可された値 (01/02/03) かどうかを判定する。
     *
     * @param authorityCode 権限コード
     * @return 許可された値の場合は true
     */
    private boolean isValidAuthorityCode(String authorityCode) {
        return AUTHORITY_ADMINISTRATOR.equals(authorityCode)
                || AUTHORITY_OPERATOR.equals(authorityCode)
                || AUTHORITY_VIEWER.equals(authorityCode);
    }

    /**
     * ユーザIDの必須・形式を検査する。新規登録時のみ呼び出す。
     *
     * @param userId 検査対象のユーザID
     * @return 問題があればエラーメッセージ。問題が無ければ null
     */
    private String validateUserId(String userId) {
        if (userId.isEmpty()) {
            return MESSAGE_USER_ID_REQUIRED;
        }
        if (!USER_ID_PATTERN.matcher(userId).matches()) {
            return MESSAGE_USER_ID_FORMAT;
        }
        return null;
    }

    /**
     * 文字列の前後の空白を除去する。null の場合は空文字を返す。
     *
     * @param value 対象の文字列
     * @return 前後の空白を除去した文字列
     */
    private String trimToEmpty(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
