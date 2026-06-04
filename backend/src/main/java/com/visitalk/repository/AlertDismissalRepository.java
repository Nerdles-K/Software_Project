package com.visitalk.repository;

import com.visitalk.model.AlertDismissal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertDismissalRepository extends JpaRepository<AlertDismissal, Long> {
    List<AlertDismissal> findByFamilyIdAndTriggerTag(String familyId, String triggerTag);
}
