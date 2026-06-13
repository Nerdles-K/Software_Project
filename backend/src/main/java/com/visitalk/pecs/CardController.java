package com.visitalk.pecs;

import com.visitalk.model.PictogramCard;
import com.visitalk.repository.CardRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
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

    /**
     * Cards are scoped to the requester's family, taken from the JWT — never from a
     * client-supplied id (Workflow §9.2: "controller 不接受 query 中的 familyId 覆盖").
     * The optional {@code familyId} query param is kept for backward compatibility but
     * ignored when the JWT carries a family.
     */
    @GetMapping
    public ResponseEntity<?> getCards(HttpServletRequest req,
                                      @RequestParam(required = false) String familyId,
                                      @RequestParam(required = false) String category) {
        String family = familyFromJwt(req, familyId);
        if (family == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "unauthenticated"));
        List<PictogramCard> cards = category != null
            ? cardRepository.findByFamilyIdAndCategoryOrderBySortOrderAsc(family, category)
            : cardRepository.findByFamilyIdOrderBySortOrderAsc(family);
        return ResponseEntity.ok(cards);
    }

    @PostMapping
    public ResponseEntity<?> createCard(HttpServletRequest req, @RequestBody PictogramCard card) {
        if (!isParent(req)) return forbidden();
        // Force the card into the caller's own family; never trust a body-supplied id.
        card.setFamilyId((String) req.getAttribute("familyId"));
        return ResponseEntity.ok(cardRepository.save(card));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCard(HttpServletRequest req, @PathVariable Long id) {
        if (!isParent(req)) return forbidden();
        return cardRepository.findById(id)
            .filter(c -> sameFamily(req, c))
            .map(c -> {
                cardRepository.deleteById(id);
                return ResponseEntity.ok((Object) Map.of("ok", true));
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/reorder")
    public ResponseEntity<?> reorderCards(HttpServletRequest req, @RequestBody List<PictogramCard> cards) {
        if (!isParent(req)) return forbidden();
        String family = (String) req.getAttribute("familyId");
        for (int i = 0; i < cards.size(); i++) {
            PictogramCard incoming = cards.get(i);
            // Only persist a new sort order for cards that actually belong to this family.
            cardRepository.findById(incoming.getId())
                .filter(c -> family != null && family.equals(c.getFamilyId()))
                .ifPresent(c -> {
                    c.setSortOrder(cards.indexOf(incoming));
                    cardRepository.save(c);
                });
        }
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> renameCard(HttpServletRequest req, @PathVariable Long id,
                                        @RequestBody Map<String, String> body) {
        if (!isParent(req)) return forbidden();
        return cardRepository.findById(id)
            .filter(c -> sameFamily(req, c))
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

    // ---- helpers ----

    private static boolean isParent(HttpServletRequest req) {
        return "parent".equals(req.getAttribute("role"));
    }

    private static boolean sameFamily(HttpServletRequest req, PictogramCard card) {
        String family = (String) req.getAttribute("familyId");
        return family != null && family.equals(card.getFamilyId());
    }

    private static String familyFromJwt(HttpServletRequest req, String queryFamilyId) {
        String fromJwt = (String) req.getAttribute("familyId");
        return fromJwt != null ? fromJwt : queryFamilyId;
    }

    private static ResponseEntity<?> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "forbidden"));
    }
}
