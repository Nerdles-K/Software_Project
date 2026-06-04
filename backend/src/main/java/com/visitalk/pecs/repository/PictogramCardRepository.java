package com.visitalk.pecs.repository;

import com.visitalk.pecs.model.PictogramCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PictogramCardRepository extends JpaRepository<PictogramCard, Long> {

    List<PictogramCard> findByFamilyIdAndCategoryOrderBySortOrderAsc(
        String familyId,
        String category
    );

    long countByFamilyId(String familyId);

    boolean existsByFamilyIdAndLabelI18n(String familyId, String labelI18n);
}
