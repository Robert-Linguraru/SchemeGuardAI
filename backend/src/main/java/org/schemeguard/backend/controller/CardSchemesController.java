package org.schemeguard.backend.controller;

import jakarta.validation.Valid;
import org.schemeguard.backend.dto.CardSchemes.Requests.ActivateCardSchemeRequest;
import org.schemeguard.backend.dto.CardSchemes.Requests.CreateCardSchemeRequest;
import org.schemeguard.backend.dto.CardSchemes.Responses.CreateCardSchemeResponse;
import org.schemeguard.backend.dto.CardSchemes.Responses.GetAllCardSchemesResponse;
import org.schemeguard.backend.dto.CardSchemes.Responses.GetCardSchemeResponse;
import org.schemeguard.backend.dto.CardSchemes.Requests.UpdateCardSchemeRequest;
import org.schemeguard.backend.dto.CardSchemes.Responses.UpdateCardSchemeResponse;
import org.schemeguard.backend.service.CardSchemeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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
    @PreAuthorize("!#includeInactive or hasRole('ADMIN')")
    public GetAllCardSchemesResponse getAllCardSchemes(
            @RequestParam(defaultValue = "false") boolean includeInactive,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name,asc") String sort
    ) {
        return cardSchemeService.getAllCardSchemes(includeInactive, search, page, size, sort);
    }

    @GetMapping("/{cardSchemeId}")
    @PreAuthorize("!#includeInactive or hasRole('ADMIN')")
    public GetCardSchemeResponse getCardScheme(
            @PathVariable UUID cardSchemeId,
            @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        return cardSchemeService.getCardScheme(cardSchemeId, includeInactive);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CreateCardSchemeResponse> createCardScheme(
            @Valid @RequestBody CreateCardSchemeRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cardSchemeService.createCardScheme(request));
    }

    @PatchMapping("/{cardSchemeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public UpdateCardSchemeResponse updateCardScheme(
            @PathVariable UUID cardSchemeId,
            @Valid @RequestBody UpdateCardSchemeRequest request
    ) {
        return cardSchemeService.updateCardScheme(cardSchemeId, request);
    }

    @PatchMapping("/{cardSchemeId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.NO_CONTENT)
    public void activateCardScheme(
            @PathVariable UUID cardSchemeId,
            @RequestBody(required = false) ActivateCardSchemeRequest request
    ) {
        cardSchemeService.activateCardScheme(cardSchemeId, request != null && request.cascade());
    }

    @PatchMapping("/{cardSchemeId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateCardScheme(@PathVariable UUID cardSchemeId) {
        cardSchemeService.deactivateCardScheme(cardSchemeId);
    }

    @DeleteMapping("/{cardSchemeId}")
    @PreAuthorize("hasRole('ADMIN')")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCardScheme(@PathVariable UUID cardSchemeId) {
        cardSchemeService.deleteCardScheme(cardSchemeId);
    }
}
