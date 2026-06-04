package com.visitalk.pecs;

import com.visitalk.model.PictogramCard;
import com.visitalk.repository.CardRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardRepository cardRepository;

    public CardController(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @GetMapping
    public List<PictogramCard> getCards(@RequestParam String familyId,
                                         @RequestParam(required = false) String category) {
        if (category != null) {
            return cardRepository.findByFamilyIdAndCategoryOrderBySortOrderAsc(familyId, category);
        }
        return cardRepository.findByFamilyIdOrderBySortOrderAsc(familyId);
    }

    @PostMapping
    public PictogramCard createCard(@RequestBody PictogramCard card) {
        return cardRepository.save(card);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCard(@PathVariable Long id) {
        if (!cardRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        cardRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PutMapping("/reorder")
    public ResponseEntity<?> reorderCards(@RequestBody List<PictogramCard> cards) {
        for (int i = 0; i < cards.size(); i++) {
            PictogramCard card = cards.get(i);
            card.setSortOrder(i);
        }
        cardRepository.saveAll(cards);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> renameCard(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return cardRepository.findById(id)
            .map(card -> {
                String label = body.get("labelI18n");
                if (label != null && !label.isBlank()) {
                    card.setLabelI18n(label.trim());
                    cardRepository.save(card);
                }
                return ResponseEntity.ok((Object) card);
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
