package org.schemeguard.backend.controller;

import jakarta.validation.Valid;
import org.schemeguard.backend.dto.CardSchemes.Requests.CreateCardSchemeRequest;
import org.schemeguard.backend.dto.CardSchemes.Responses.CreateCardSchemeResponse;
import org.schemeguard.backend.dto.CardSchemes.Responses.GetAllCardSchemesResponse;
import org.schemeguard.backend.dto.CardSchemes.Responses.GetCardSchemeResponse;
import org.schemeguard.backend.dto.CardSchemes.Requests.UpdateCardSchemeRequest;
import org.schemeguard.backend.dto.CardSchemes.Responses.UpdateCardSchemeResponse;
import org.schemeguard.backend.service.CardSchemeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/card-schemes")
public class CardSchemesController {

    private final CardSchemeService cardSchemeService;

    public CardSchemesController(CardSchemeService cardSchemeService) {
        this.cardSchemeService = cardSchemeService;
    }

    @GetMapping
    public GetAllCardSchemesResponse getAllCardSchemes() {
        return cardSchemeService.getAllCardSchemes();
    }

    @GetMapping("/{cardSchemeId}")
    public GetCardSchemeResponse getCardScheme(@PathVariable UUID cardSchemeId) {
        return cardSchemeService.getCardScheme(cardSchemeId);
    }

    @PostMapping
    public ResponseEntity<CreateCardSchemeResponse> createCardScheme(
            @Valid @RequestBody CreateCardSchemeRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cardSchemeService.createCardScheme(request));
    }

    @PutMapping("/{cardSchemeId}")
    public UpdateCardSchemeResponse updateCardScheme(
            @PathVariable UUID cardSchemeId,
            @Valid @RequestBody UpdateCardSchemeRequest request
    ) {
        return cardSchemeService.updateCardScheme(cardSchemeId, request);
    }

    @DeleteMapping("/{cardSchemeId}")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCardScheme(@PathVariable UUID cardSchemeId) {
        cardSchemeService.deleteCardScheme(cardSchemeId);
    }
}
