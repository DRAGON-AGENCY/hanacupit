package com.cupit.dto;

/**
 * 社員管理画面から送信された社員情報を保持するクラス。
 * 登録 (mode=new) と更新の両方で使用する。
 */
public class EmployeeRequest {

    private String mode;
    private String userId;
    private String email;
    private String employeeName;
    private String employeeNameKana;
    private String department;
    private String authorityCode;
    private String phoneNumber;
    private String faxNumber;
    private String password;
    private int passwordErrorCount;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeNameKana() {
        return employeeNameKana;
    }

    public void setEmployeeNameKana(String employeeNameKana) {
        this.employeeNameKana = employeeNameKana;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAuthorityCode() {
        return authorityCode;
    }

    public void setAuthorityCode(String authorityCode) {
        this.authorityCode = authorityCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFaxNumber() {
        return faxNumber;
    }

    public void setFaxNumber(String faxNumber) {
        this.faxNumber = faxNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPasswordErrorCount() {
        return passwordErrorCount;
    }

    public void setPasswordErrorCount(int passwordErrorCount) {
        this.passwordErrorCount = passwordErrorCount;
    }
}
