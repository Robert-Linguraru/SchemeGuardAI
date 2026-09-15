package org.schemeguard.backend.service;

import org.schemeguard.backend.dto.CardSchemes.Responses.CardSchemeResponse;
import org.schemeguard.backend.dto.CardSchemes.Requests.CreateCardSchemeRequest;
import org.schemeguard.backend.dto.CardSchemes.Responses.CreateCardSchemeResponse;
import org.schemeguard.backend.dto.CardSchemes.Responses.GetAllCardSchemesResponse;
import org.schemeguard.backend.dto.CardSchemes.Responses.GetCardSchemeResponse;
import org.schemeguard.backend.dto.CardSchemes.Requests.UpdateCardSchemeRequest;
import org.schemeguard.backend.dto.CardSchemes.Responses.UpdateCardSchemeResponse;
import org.schemeguard.backend.entity.CardScheme;
import org.schemeguard.backend.exception.ConflictException;
import org.schemeguard.backend.exception.ResourceNotFoundException;
import org.schemeguard.backend.repository.CardSchemeRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CardSchemeService {

    private final CardSchemeRepository cardSchemeRepository;

    public CardSchemeService(CardSchemeRepository cardSchemeRepository) {
        this.cardSchemeRepository = cardSchemeRepository;
    }

    public GetAllCardSchemesResponse getAllCardSchemes() {
        return new GetAllCardSchemesResponse(
                cardSchemeRepository.findAll().stream().map(this::toResponse).toList()
        );
    }

    public GetCardSchemeResponse getCardScheme(UUID cardSchemeId) {
        return new GetCardSchemeResponse(toResponse(findCardScheme(cardSchemeId)));
    }

    public CreateCardSchemeResponse createCardScheme(CreateCardSchemeRequest request) {
        if (cardSchemeRepository.existsByCode(request.code())) {
            throw new ConflictException("A card scheme with this code already exists.");
        }

        CardScheme cardScheme = new CardScheme();
        cardScheme.setCode(request.code());
        cardScheme.setName(request.name());
        cardScheme.setActive(true); // Set active to true by default when creating a new card scheme

        return new CreateCardSchemeResponse(toResponse(cardSchemeRepository.save(cardScheme)));
    }

    public UpdateCardSchemeResponse updateCardScheme(UUID cardSchemeId, UpdateCardSchemeRequest request) {
        CardScheme cardScheme = findCardScheme(cardSchemeId);

        if (cardSchemeRepository.existsByCodeAndIdNot(request.code(), cardSchemeId)) {
            throw new ConflictException("A card scheme with this code already exists.");
        }

        cardScheme.setCode(request.code());
        cardScheme.setName(request.name());
        cardScheme.setActive(request.active());

        return new UpdateCardSchemeResponse(toResponse(cardSchemeRepository.save(cardScheme)));
    }

    public void deleteCardScheme(UUID cardSchemeId) {
        cardSchemeRepository.delete(findCardScheme(cardSchemeId));
    }

    private CardScheme findCardScheme(UUID cardSchemeId) {
        return cardSchemeRepository.findById(cardSchemeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Card scheme not found with id: " + cardSchemeId
                ));
    }

    private CardSchemeResponse toResponse(CardScheme cardScheme) {
        return new CardSchemeResponse(
                cardScheme.getId(),
                cardScheme.getCode(),
                cardScheme.getName(),
                cardScheme.getActive()
        );
    }
}
