package com.visitalk.service;

import com.visitalk.dto.LoginResponse;
import com.visitalk.model.User;
import com.visitalk.repository.UserRepository;
import com.visitalk.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the registration / login business rules in {@link AuthService}.
 * All collaborators are mocked, so these run without a database and assert pure
 * branching logic: family creation vs joining, role validation, duplicate guard,
 * parent PIN provisioning, and when the default card library is seeded.
 */
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private JwtUtil jwtUtil;
    @Mock private PasswordEncoder encoder;
    @Mock private CardSeeder cardSeeder;

    @InjectMocks private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Echo back the saved user (with whatever fields AuthService set) and a stable token.
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(encoder.encode(anyString())).thenAnswer(inv -> "hashed:" + inv.getArgument(0));
        when(jwtUtil.generateToken(any(), anyString(), anyString())).thenReturn("jwt-token");
    }

    // ---------- register: new family ----------

    @Test
    void register_noFamilyCode_createsNewFamilyAndSeedsCards() {
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());

        LoginResponse res = authService.register("new@test.com", "pw", "parent", null);

        assertEquals("jwt-token", res.getToken());
        assertEquals("parent", res.getRole());
        assertNotNull(res.getFamilyId());
        assertTrue(res.getFamilyId().startsWith("FAM"), "new family id should be FAM-prefixed");

        // A brand-new family seeds the default library exactly once for its own id.
        verify(cardSeeder).seedDefaultCards(res.getFamilyId());
    }

    @Test
    void register_parent_getsDefaultPinHash() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        authService.register("p@test.com", "pw", "parent", null);

        verify(userRepository).save(captor.capture());
        assertEquals("hashed:1234", captor.getValue().getPinHash(), "parent gets the dev PIN 1234 hashed");
    }

    @Test
    void register_child_hasNoPin() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        authService.register("c@test.com", "pw", "child", null);

        verify(userRepository).save(captor.capture());
        assertNull(captor.getValue().getPinHash(), "child accounts have no PIN");
    }

    // ---------- register: join existing family ----------

    @Test
    void register_validFamilyCode_joinsWithoutReseeding() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.existsByFamilyId("FAM001")).thenReturn(true);

        LoginResponse res = authService.register("join@test.com", "pw", "child", "fam001");

        assertEquals("FAM001", res.getFamilyId(), "family code is upper-cased and reused");
        // Joining an existing family must NOT re-seed cards (would duplicate the library).
        verify(cardSeeder, never()).seedDefaultCards(anyString());
    }

    @Test
    void register_invalidFamilyCode_throws() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.existsByFamilyId("NOPE")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            authService.register("x@test.com", "pw", "parent", "nope"));
        assertTrue(ex.getMessage().contains("Invalid family code"));
        verify(userRepository, never()).save(any());
    }

    // ---------- register: validation guards ----------

    @Test
    void register_duplicateEmail_throws() {
        when(userRepository.findByEmail("dup@test.com")).thenReturn(Optional.of(new User()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            authService.register("dup@test.com", "pw", "parent", null));
        assertTrue(ex.getMessage().contains("already registered"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_invalidRole_throws() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            authService.register("x@test.com", "pw", "admin", null));
        assertTrue(ex.getMessage().contains("Role must be"));
    }

    // ---------- login ----------

    @Test
    void login_correctPassword_returnsToken() {
        User user = sampleParent();
        when(userRepository.findByEmail("p@test.com")).thenReturn(Optional.of(user));
        when(encoder.matches("pw", user.getPasswordHash())).thenReturn(true);

        Optional<LoginResponse> res = authService.login("p@test.com", "pw");

        assertTrue(res.isPresent());
        assertEquals("jwt-token", res.get().getToken());
        assertEquals("FAM001", res.get().getFamilyId());
    }

    @Test
    void login_wrongPassword_returnsEmpty() {
        User user = sampleParent();
        when(userRepository.findByEmail("p@test.com")).thenReturn(Optional.of(user));
        when(encoder.matches("bad", user.getPasswordHash())).thenReturn(false);

        assertTrue(authService.login("p@test.com", "bad").isEmpty());
        verify(jwtUtil, never()).generateToken(any(), anyString(), anyString());
    }

    @Test
    void login_unknownEmail_returnsEmpty() {
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertTrue(authService.login("ghost@test.com", "pw").isEmpty());
    }

    private User sampleParent() {
        User user = new User();
        user.setId(1L);
        user.setEmail("p@test.com");
        user.setPasswordHash("hashed:pw");
        user.setRole("parent");
        user.setFamilyId("FAM001");
        return user;
    }
}
