package com.visitalk.pecs.service;

import com.visitalk.pecs.dto.CardResponse;
import com.visitalk.pecs.model.PictogramCard;
import com.visitalk.pecs.repository.PictogramCardRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class PecsCardService {

    private static final Set<String> ALLOWED_CATEGORIES = Set.of("core", "eat", "drink", "play", "feel");

    private final PictogramCardRepository cardRepository;

    public PecsCardService(PictogramCardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public List<CardResponse> listByCategory(String familyId, String category) {
        if (!ALLOWED_CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("Invalid category: " + category);
        }
        return cardRepository.findByFamilyIdAndCategoryOrderBySortOrderAsc(familyId, category)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private CardResponse toResponse(PictogramCard card) {
        return new CardResponse(
            card.getId(),
            card.getCategory(),
            card.getImageUrl(),
            card.getLabelI18n(),
            Boolean.TRUE.equals(card.getIsCustom()),
            card.getSortOrder() != null ? card.getSortOrder() : 0
        );
    }
}
