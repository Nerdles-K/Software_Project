package com.visitalk.repository;

import com.visitalk.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    /** True if any user already belongs to this family (used to validate a family code). */
    boolean existsByFamilyId(String familyId);
}
