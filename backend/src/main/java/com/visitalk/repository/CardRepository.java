package com.visitalk.repository;

import com.visitalk.model.PictogramCard;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CardRepository extends JpaRepository<PictogramCard, Long> {
    List<PictogramCard> findByFamilyIdOrderBySortOrderAsc(String familyId);
    List<PictogramCard> findByFamilyIdAndCategoryOrderBySortOrderAsc(String familyId, String category);
    java.util.Optional<PictogramCard> findFirstByFamilyIdAndCategoryAndLabelI18nAndIsCustom(
        String familyId, String category, String labelI18n, boolean isCustom);
}
