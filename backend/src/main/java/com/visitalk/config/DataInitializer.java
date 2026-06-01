package com.visitalk.config;

import com.visitalk.model.User;
import com.visitalk.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        User parent = new User();
        parent.setEmail("parent@test.com");
        parent.setPasswordHash(encoder.encode("password123"));
        parent.setRole("parent");
        parent.setFamilyId("FAM001");
        parent.setPinHash(encoder.encode("1234"));
        userRepository.save(parent);

        User child = new User();
        child.setEmail("child@test.com");
        child.setPasswordHash(encoder.encode("password123"));
        child.setRole("child");
        child.setFamilyId("FAM001");
        userRepository.save(child);
    }
}
