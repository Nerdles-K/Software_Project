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

    public LoginResponse register(String email, String password, String role, String familyCode) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (!"parent".equals(role) && !"child".equals(role)) {
            throw new IllegalArgumentException("Role must be 'parent' or 'child'");
        }

        String code = familyCode == null ? "" : familyCode.trim().toUpperCase();
        boolean joiningExisting = !code.isEmpty();
        String familyId;

        if (joiningExisting) {
            // Join an existing family via its code. A family may hold multiple
            // parents (e.g. mum + dad) and multiple children; isolation between
            // families is enforced elsewhere by family_id.
            if (!userRepository.existsByFamilyId(code)) {
                throw new IllegalArgumentException("Invalid family code");
            }
            familyId = code;
        } else {
            // No code → start a brand-new family.
            familyId = "FAM" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(password));
        user.setRole(role);
        user.setFamilyId(familyId);

        if ("parent".equals(role)) {
            user.setPinHash(encoder.encode("1234"));
        }

        user = userRepository.save(user);
        // Only a brand-new family needs the default card library seeded.
        if (!joiningExisting) {
            cardSeeder.seedDefaultCards(familyId);
        }
        String token = jwtUtil.generateToken(user.getId(), user.getRole(), user.getFamilyId());
        return new LoginResponse(token, user.getRole(), user.getFamilyId());
    }
}
