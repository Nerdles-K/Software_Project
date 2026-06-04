package com.visitalk.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final SecretKey key;

    public JwtFilter(@Value("${app.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/")
            || path.equals("/api/health")
            || path.startsWith("/uploads/")
            || path.startsWith("/share/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(header.substring(7))
                    .getPayload();

                String userId = claims.getSubject();
                String role = claims.get("role", String.class);
                String familyId = claims.get("family_id", String.class);

                var auth = new UsernamePasswordAuthenticationToken(
                    userId, null,
                    List.of(() -> "ROLE_" + role.toUpperCase())
                );
                SecurityContextHolder.getContext().setAuthentication(auth);

                // Expose trusted claims so downstream controllers don't have to re-parse.
                request.setAttribute("userId", Long.parseLong(userId));
                request.setAttribute("role", role);
                request.setAttribute("familyId", familyId);
            } catch (Exception e) {
                // Stale/invalid token must not block public endpoints (login, register, health).
                if (!isPublicPath(path)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }
}
