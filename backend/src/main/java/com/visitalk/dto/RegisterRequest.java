package com.visitalk.dto;

public class RegisterRequest {
    private String email;
    private String password;
    private String role; // "parent" or "child"
    private String familyCode; // optional: join an existing family; blank = create a new one

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getFamilyCode() { return familyCode; }
    public void setFamilyCode(String familyCode) { this.familyCode = familyCode; }
}
