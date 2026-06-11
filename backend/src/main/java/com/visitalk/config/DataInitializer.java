package com.visitalk.config;

import com.visitalk.model.User;
import com.visitalk.repository.UserRepository;
import com.visitalk.service.CardSeeder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CardSeeder cardSeeder;
    private final PasswordEncoder encoder;

    private static final String FAMILY = "FAM001";

    public DataInitializer(UserRepository userRepository, CardSeeder cardSeeder, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.cardSeeder = cardSeeder;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User parent = new User();
            parent.setEmail("parent@test.com");
            parent.setPasswordHash(encoder.encode("password123"));
            parent.setRole("parent");
            parent.setFamilyId(FAMILY);
            parent.setPinHash(encoder.encode("1234"));
            userRepository.save(parent);

            User child = new User();
            child.setEmail("child@test.com");
            child.setPasswordHash(encoder.encode("password123"));
            child.setRole("child");
            child.setFamilyId(FAMILY);
            userRepository.save(child);
        }

        // Refresh the demo family's default library on every boot (idempotent).
        cardSeeder.seedDefaultCards(FAMILY);
    }
}
