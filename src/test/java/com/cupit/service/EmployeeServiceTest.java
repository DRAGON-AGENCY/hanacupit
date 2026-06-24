package com.cupit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cupit.dto.EmployeeRequest;
import com.cupit.dto.EmployeeResponse;
import com.cupit.model.Employee;
import com.cupit.repository.EmployeeRepository;

/**
 * EmployeeService のテスト。
 * リポジトリをモック化し、登録・更新時のパスワードポリシー検査を検証する。
 * パスワードのハッシュ化には実際の BCryptPasswordEncoder を使用する。
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class EmployeeServiceTest {

    private static final String VALID_PASSWORD = "Passw0rd!";
    private static final String EXISTING_USER_ID = "user001";
    private static final String STORED_PASSWORD_HASH =
            "$2a$10$existinghashplaceholderexistinghashplaceholderexample";

    private static final String MESSAGE_PASSWORD_REQUIRED =
            "パスワードを入力してください。";
    private static final String MESSAGE_PASSWORD_LENGTH =
            "パスワードは 8 文字以上 72 文字以内で入力してください。";
    private static final String MESSAGE_PASSWORD_CHARACTER =
            "パスワードは半角の英数字と記号で入力してください。";
    private static final String MESSAGE_PASSWORD_POLICY =
            "パスワードは英大文字・英小文字・数字・記号を"
            + "それぞれ 1 文字以上含めてください。";
    private static final String MESSAGE_PHONE_FORMAT =
            "電話番号は半角数字とハイフンで入力してください。";
    private static final String MESSAGE_FAX_FORMAT =
            "FAX は半角数字とハイフンで入力してください。";

    @Mock
    private EmployeeRepository employeeRepository;

    private PasswordEncoder passwordEncoder;
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        employeeService = new EmployeeService(
                employeeRepository, passwordEncoder);
    }

    private EmployeeRequest createValidRequest(String mode, String password) {
        EmployeeRequest request = new EmployeeRequest();
        request.setMode(mode);
        request.setUserId(EXISTING_USER_ID);
        request.setEmail("taro@example.com");
        request.setEmployeeName("山田太郎");
        request.setEmployeeNameKana("ヤマダタロウ");
        request.setDepartment("営業部");
        request.setAuthorityCode("01");
        request.setPhoneNumber("03-1234-5678");
        request.setFaxNumber("");
        request.setPassword(password);
        request.setPasswordErrorCount(0);
        return request;
    }

    private Employee createExistingEmployee() {
        Employee employee = new Employee();
        employee.setUserId(EXISTING_USER_ID);
        employee.setEmail("taro@example.com");
        employee.setPassword(STORED_PASSWORD_HASH);
        employee.setPasswordErrorCount(0);
        return employee;
    }

    @Test
    void saveEmployeeCreatesWhenPasswordSatisfiesPolicy() {
        when(employeeRepository.existsByEmail("taro@example.com"))
                .thenReturn(false);
        when(employeeRepository.findFirstByOrderByUserIdDesc())
                .thenReturn(Optional.empty());

        EmployeeResponse response = employeeService.saveEmployee(
                createValidRequest("new", VALID_PASSWORD), EXISTING_USER_ID);

        assertThat(response.isSuccess()).isTrue();
        verify(employeeRepository).save(any());
    }

    @Test
    void saveEmployeeRejectsBlankPasswordOnCreate() {
        when(employeeRepository.existsByEmail("taro@example.com"))
                .thenReturn(false);

        EmployeeResponse response = employeeService.saveEmployee(
                createValidRequest("new", ""), EXISTING_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_PASSWORD_REQUIRED);
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void saveEmployeeRejectsTooShortPasswordOnCreate() {
        when(employeeRepository.existsByEmail("taro@example.com"))
                .thenReturn(false);

        EmployeeResponse response = employeeService.saveEmployee(
                createValidRequest("new", "Pa1!"), EXISTING_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_PASSWORD_LENGTH);
    }

    @Test
    void saveEmployeeRejectsFullWidthCharacterOnCreate() {
        when(employeeRepository.existsByEmail("taro@example.com"))
                .thenReturn(false);

        EmployeeResponse response = employeeService.saveEmployee(
                createValidRequest("new", "Ｐassw0rd!"), EXISTING_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_PASSWORD_CHARACTER);
    }

    @Test
    void saveEmployeeRejectsPasswordMissingUppercaseOnCreate() {
        when(employeeRepository.existsByEmail("taro@example.com"))
                .thenReturn(false);

        EmployeeResponse response = employeeService.saveEmployee(
                createValidRequest("new", "passw0rd!"), EXISTING_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_PASSWORD_POLICY);
    }

    @Test
    void saveEmployeeRejectsPasswordMissingSymbolOnCreate() {
        when(employeeRepository.existsByEmail("taro@example.com"))
                .thenReturn(false);

        EmployeeResponse response = employeeService.saveEmployee(
                createValidRequest("new", "Passw0rd"), EXISTING_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_PASSWORD_POLICY);
    }

    @Test
    void saveEmployeeRejectsPhoneNumberWithInvalidCharacters() {
        EmployeeRequest request = createValidRequest("new", VALID_PASSWORD);
        request.setPhoneNumber("03-1234-abcd");

        EmployeeResponse response =
                employeeService.saveEmployee(request, EXISTING_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_PHONE_FORMAT);
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void saveEmployeeRejectsFullWidthPhoneNumber() {
        EmployeeRequest request = createValidRequest("new", VALID_PASSWORD);
        request.setPhoneNumber("０３－１２３４");

        EmployeeResponse response =
                employeeService.saveEmployee(request, EXISTING_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_PHONE_FORMAT);
    }

    @Test
    void saveEmployeeRejectsFaxNumberWithInvalidCharacters() {
        EmployeeRequest request = createValidRequest("new", VALID_PASSWORD);
        request.setFaxNumber("03-1234-XXXX");

        EmployeeResponse response =
                employeeService.saveEmployee(request, EXISTING_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_FAX_FORMAT);
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void saveEmployeeKeepsCurrentPasswordWhenBlankOnUpdate() {
        Employee employee = createExistingEmployee();
        when(employeeRepository.findById(EXISTING_USER_ID))
                .thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmailAndUserIdNot(
                "taro@example.com", EXISTING_USER_ID)).thenReturn(false);

        EmployeeResponse response = employeeService.saveEmployee(
                createValidRequest("edit", ""), EXISTING_USER_ID);

        assertThat(response.isSuccess()).isTrue();
        // 空欄の場合はパスワードを再設定せず現状を維持する
        assertThat(employee.getPassword()).isEqualTo(STORED_PASSWORD_HASH);
    }

    @Test
    void saveEmployeeRejectsInvalidPasswordOnUpdate() {
        Employee employee = createExistingEmployee();
        when(employeeRepository.findById(EXISTING_USER_ID))
                .thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmailAndUserIdNot(
                "taro@example.com", EXISTING_USER_ID)).thenReturn(false);

        EmployeeResponse response = employeeService.saveEmployee(
                createValidRequest("edit", "passw0rd!"), EXISTING_USER_ID);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(MESSAGE_PASSWORD_POLICY);
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void saveEmployeeUpdatesPasswordWhenValidOnUpdate() {
        Employee employee = createExistingEmployee();
        when(employeeRepository.findById(EXISTING_USER_ID))
                .thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmailAndUserIdNot(
                "taro@example.com", EXISTING_USER_ID)).thenReturn(false);

        EmployeeResponse response = employeeService.saveEmployee(
                createValidRequest("edit", VALID_PASSWORD), EXISTING_USER_ID);

        assertThat(response.isSuccess()).isTrue();
        // 新しいパスワードがハッシュ化されて設定されている
        assertThat(employee.getPassword()).isNotEqualTo(STORED_PASSWORD_HASH);
        assertThat(passwordEncoder.matches(
                VALID_PASSWORD, employee.getPassword())).isTrue();
    }
}
