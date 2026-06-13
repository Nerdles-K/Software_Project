package com.visitalk.repository;

import com.visitalk.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    /** True if any user already belongs to this family (used to validate a family code). */
    boolean existsByFamilyId(String familyId);

    /** Every distinct family id — used at boot to top up each family's default library. */
    @Query("select distinct u.familyId from User u")
    List<String> findDistinctFamilyIds();
}
