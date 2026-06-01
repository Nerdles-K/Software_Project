package com.visitalk.dto;

public class LoginResponse {
    private String token;
    private String role;
    private String familyId;

    public LoginResponse(String token, String role, String familyId) {
        this.token = token;
        this.role = role;
        this.familyId = familyId;
    }

    public String getToken() { return token; }
    public String getRole() { return role; }
    public String getFamilyId() { return familyId; }
}
