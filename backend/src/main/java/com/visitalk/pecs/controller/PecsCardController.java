package com.visitalk.pecs.controller;

import com.visitalk.pecs.dto.CardResponse;
import com.visitalk.pecs.service.PecsCardService;
import com.visitalk.security.JwtUserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
public class PecsCardController {

    private final PecsCardService cardService;

    public PecsCardController(PecsCardService cardService) {
        this.cardService = cardService;
    }

    /** List pictogram cards for the logged-in user's family and category (A-1). */
    @GetMapping
    public ResponseEntity<?> listCards(
        @AuthenticationPrincipal JwtUserPrincipal principal,
        @RequestParam String category
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            List<CardResponse> cards = cardService.listByCategory(principal.familyId(), category);
            return ResponseEntity.ok(cards);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
