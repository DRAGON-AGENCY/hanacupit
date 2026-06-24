package com.cupit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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

import com.cupit.model.Employee;
import com.cupit.model.LoginResult;
import com.cupit.repository.EmployeeRepository;

/**
 * EmployeeAuthenticationService のテスト。
 * リポジトリをモック化し、データベースに依存せず認証ロジックを検証する。
 * パスワード照合には実際の BCryptPasswordEncoder を使用する。
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class EmployeeAuthenticationServiceTest {

    private static final String EMAIL = "sato.taro@example.com";
    private static final String USER_ID = "user001";
    private static final String CORRECT_PASSWORD = "password001";

    @Mock
    private EmployeeRepository employeeRepository;

    private PasswordEncoder passwordEncoder;
    private EmployeeAuthenticationService employeeAuthenticationService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        employeeAuthenticationService = new EmployeeAuthenticationService(
                employeeRepository, passwordEncoder);
    }

    private Employee createEmployee(String storedPassword, int passwordErrorCount) {
        Employee employee = new Employee();
        employee.setUserId(USER_ID);
        employee.setEmail(EMAIL);
        employee.setPassword(storedPassword);
        employee.setPasswordErrorCount(passwordErrorCount);
        return employee;
    }

    @Test
    void authenticateReturnsSuccessWhenBcryptPasswordMatches() {
        String hashed = passwordEncoder.encode(CORRECT_PASSWORD);
        when(employeeRepository.findById(USER_ID))
                .thenReturn(Optional.of(createEmployee(hashed, 2)));

        LoginResult result = employeeAuthenticationService.authenticate(
                USER_ID, CORRECT_PASSWORD);

        assertThat(result).isEqualTo(LoginResult.SUCCESS);
        verify(employeeRepository).updatePasswordErrorCount(USER_ID, 0);
    }

    @Test
    void authenticateReturnsSuccessWhenPlaintextPasswordMatches() {
        when(employeeRepository.findById(USER_ID))
                .thenReturn(Optional.of(createEmployee(CORRECT_PASSWORD, 2)));

        LoginResult result = employeeAuthenticationService.authenticate(
                USER_ID, CORRECT_PASSWORD);

        assertThat(result).isEqualTo(LoginResult.SUCCESS);
        verify(employeeRepository).updatePasswordErrorCount(USER_ID, 0);
    }

    @Test
    void authenticateReturnsInvalidWhenBcryptPasswordWrong() {
        String hashed = passwordEncoder.encode(CORRECT_PASSWORD);
        when(employeeRepository.findById(USER_ID))
                .thenReturn(Optional.of(createEmployee(hashed, 1)));

        LoginResult result = employeeAuthenticationService.authenticate(
                USER_ID, "wrongPassword");

        assertThat(result).isEqualTo(LoginResult.INVALID_CREDENTIALS);
        verify(employeeRepository).updatePasswordErrorCount(USER_ID, 2);
    }

    @Test
    void authenticateReturnsInvalidAndIncrementsErrorCountWhenPlaintextWrong() {
        when(employeeRepository.findById(USER_ID))
                .thenReturn(Optional.of(createEmployee(CORRECT_PASSWORD, 1)));

        LoginResult result = employeeAuthenticationService.authenticate(
                USER_ID, "wrongPassword");

        assertThat(result).isEqualTo(LoginResult.INVALID_CREDENTIALS);
        verify(employeeRepository).updatePasswordErrorCount(USER_ID, 2);
    }

    @Test
    void authenticateReturnsLockedWhenErrorCountReachesMax() {
        when(employeeRepository.findById(USER_ID))
                .thenReturn(Optional.of(createEmployee(CORRECT_PASSWORD, 4)));

        LoginResult result = employeeAuthenticationService.authenticate(
                USER_ID, "wrongPassword");

        assertThat(result).isEqualTo(LoginResult.LOCKED);
        verify(employeeRepository).updatePasswordErrorCount(USER_ID, 5);
    }

    @Test
    void authenticateReturnsLockedWhenAccountAlreadyLocked() {
        when(employeeRepository.findById(USER_ID))
                .thenReturn(Optional.of(createEmployee(CORRECT_PASSWORD, 5)));

        LoginResult result = employeeAuthenticationService.authenticate(
                USER_ID, CORRECT_PASSWORD);

        assertThat(result).isEqualTo(LoginResult.LOCKED);
        verify(employeeRepository, never())
                .updatePasswordErrorCount(anyString(), anyInt());
    }

    @Test
    void authenticateReturnsInvalidWhenUserIdNotFound() {
        when(employeeRepository.findById("none001"))
                .thenReturn(Optional.empty());

        LoginResult result = employeeAuthenticationService.authenticate(
                "none001", CORRECT_PASSWORD);

        assertThat(result).isEqualTo(LoginResult.INVALID_CREDENTIALS);
        verify(employeeRepository, never())
                .updatePasswordErrorCount(anyString(), anyInt());
    }

    @Test
    void authenticateReturnsInvalidWhenUserIdBlank() {
        LoginResult result = employeeAuthenticationService.authenticate(
                "   ", CORRECT_PASSWORD);

        assertThat(result).isEqualTo(LoginResult.INVALID_CREDENTIALS);
        verify(employeeRepository, never()).findById(anyString());
    }
}
