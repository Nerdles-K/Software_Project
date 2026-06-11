package com.visitalk.service;

import com.visitalk.dto.LoginResponse;
import com.visitalk.model.User;
import com.visitalk.repository.UserRepository;
import com.visitalk.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder encoder;
    private final CardSeeder cardSeeder;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder encoder,
                       CardSeeder cardSeeder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.encoder = encoder;
        this.cardSeeder = cardSeeder;
    }

    public Optional<LoginResponse> login(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return Optional.empty();

        User user = userOpt.get();
        if (!encoder.matches(password, user.getPasswordHash())) return Optional.empty();

        String token = jwtUtil.generateToken(user.getId(), user.getRole(), user.getFamilyId());
        return Optional.of(new LoginResponse(token, user.getRole(), user.getFamilyId()));
    }

    public LoginResponse register(String email, String password, String role) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (!"parent".equals(role) && !"child".equals(role)) {
            throw new IllegalArgumentException("Role must be 'parent' or 'child'");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(password));
        user.setRole(role);
        user.setFamilyId("FAM" + UUID.randomUUID().toString().substring(0, 5).toUpperCase());

        if ("parent".equals(role)) {
            user.setPinHash(encoder.encode("1234"));
        }

        user = userRepository.save(user);
        // Give the brand-new family the full default card library.
        cardSeeder.seedDefaultCards(user.getFamilyId());
        String token = jwtUtil.generateToken(user.getId(), user.getRole(), user.getFamilyId());
        return new LoginResponse(token, user.getRole(), user.getFamilyId());
    }
}
