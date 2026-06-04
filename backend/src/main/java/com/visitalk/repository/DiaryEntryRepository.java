package com.visitalk.repository;

import com.visitalk.model.DiaryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DiaryEntryRepository extends JpaRepository<DiaryEntry, Long> {

    List<DiaryEntry> findByChildIdOrderByCreatedAtDesc(Long childId);

    @Query("""
        SELECT d FROM DiaryEntry d
        WHERE d.childId = :childId
          AND d.createdAt >= :start AND d.createdAt < :end
        ORDER BY d.createdAt DESC
        """)
    List<DiaryEntry> findByChildIdAndDay(Long childId, LocalDateTime start, LocalDateTime end);

    Optional<DiaryEntry> findFirstByChildIdOrderByCreatedAtDesc(Long childId);
}
