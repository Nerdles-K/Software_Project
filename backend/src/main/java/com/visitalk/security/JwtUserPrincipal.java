package com.visitalk.security;

/** Authenticated user info extracted from JWT (set by JwtFilter). */
public record JwtUserPrincipal(String userId, String role, String familyId) {}
