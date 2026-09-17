package org.schemeguard.backend.service;

import org.schemeguard.backend.dto.CardSchemes.Requests.CreateCardSchemeRequest;
import org.schemeguard.backend.dto.CardSchemes.Requests.UpdateCardSchemeRequest;
import org.schemeguard.backend.dto.CardSchemes.Responses.CardSchemeResponse;
import org.schemeguard.backend.dto.CardSchemes.Responses.CreateCardSchemeResponse;
import org.schemeguard.backend.dto.CardSchemes.Responses.GetAllCardSchemesResponse;
import org.schemeguard.backend.dto.CardSchemes.Responses.GetCardSchemeResponse;
import org.schemeguard.backend.dto.CardSchemes.Responses.UpdateCardSchemeResponse;
import org.schemeguard.backend.entity.CardScheme;
import org.schemeguard.backend.exception.ConflictException;
import org.schemeguard.backend.exception.ResourceNotFoundException;
import org.schemeguard.backend.repository.CardScheme.CardSchemeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class CardSchemeService {

    private static final int MAX_PAGE_SIZE = 100;

    private final CardSchemeRepository cardSchemeRepository;

    public CardSchemeService(CardSchemeRepository cardSchemeRepository) {
        this.cardSchemeRepository = cardSchemeRepository;
    }

    @Transactional(readOnly = true)
    public GetAllCardSchemesResponse getAllCardSchemes(
            boolean includeInactive,
            String search,
            int page,
            int size,
            String sort
    ) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        String normalizedSearch = normalizeSearch(search);
        Pageable pageable = PageRequest.of(
                normalizedPage,
                normalizedSize,
                parseSort(sort)
        );

        Page<CardScheme> result = cardSchemeRepository.search(
                includeInactive,
                normalizedSearch,
                pageable
        );

        return new GetAllCardSchemesResponse(
                result.getContent().stream().map(this::toResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                normalizedSearch,
                pageable.getSort().toString(),
                includeInactive
        );
    }

    @Transactional(readOnly = true)
    public GetCardSchemeResponse getCardScheme(
            UUID cardSchemeId,
            boolean includeInactive
    ) {
        return new GetCardSchemeResponse(
                toResponse(findCardScheme(cardSchemeId, includeInactive))
        );
    }

    @Transactional
    public CreateCardSchemeResponse createCardScheme(CreateCardSchemeRequest request) {
        String code = normalizeCode(request.code());

        if (cardSchemeRepository.existsByCodeIgnoreCase(code)) {
            throw new ConflictException("A card scheme with this code already exists.");
        }

        CardScheme cardScheme = new CardScheme();
        cardScheme.setCode(code);
        cardScheme.setName(request.name().trim());
        cardScheme.setActive(true);

        return new CreateCardSchemeResponse(
                toResponse(cardSchemeRepository.save(cardScheme))
        );
    }

    @Transactional
    public UpdateCardSchemeResponse updateCardScheme(
            UUID cardSchemeId,
            UpdateCardSchemeRequest request
    ) {
        CardScheme cardScheme = findCardScheme(cardSchemeId, true);
        String code = normalizeCode(request.code());

        if (cardSchemeRepository.existsByCodeIgnoreCaseAndIdNot(code, cardSchemeId)) {
            throw new ConflictException("A card scheme with this code already exists.");
        }

        cardScheme.setCode(code);
        cardScheme.setName(request.name().trim());

        return new UpdateCardSchemeResponse(
                toResponse(cardSchemeRepository.save(cardScheme))
        );
    }

    @Transactional
    public void activateCardScheme(UUID cardSchemeId, boolean cascade) {
        CardScheme cardScheme = findCardScheme(cardSchemeId, true);

        if (cascade) {
            cardSchemeRepository.activateCascade(cardSchemeId);
            return;
        }

        cardScheme.setActive(true);
        cardSchemeRepository.save(cardScheme);
    }

    @Transactional
    public void deactivateCardScheme(UUID cardSchemeId) {
        ensureCardSchemeExists(cardSchemeId);
        cardSchemeRepository.deactivateCascade(cardSchemeId);
    }

    @Transactional
    public void deleteCardScheme(UUID cardSchemeId) {
        ensureCardSchemeExists(cardSchemeId);
        cardSchemeRepository.deleteCascade(cardSchemeId);
    }

    private CardScheme findCardScheme(UUID cardSchemeId, boolean includeInactive) {
        return cardSchemeRepository.findById(cardSchemeId)
                .filter(scheme -> includeInactive || Boolean.TRUE.equals(scheme.getActive()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Card scheme not found with id: " + cardSchemeId
                ));
    }

    private void ensureCardSchemeExists(UUID cardSchemeId) {
        if (!cardSchemeRepository.existsById(cardSchemeId)) {
            throw new ResourceNotFoundException(
                    "Card scheme not found with id: " + cardSchemeId
            );
        }
    }

    private Sort parseSort(String sort) {
        String[] parts = sort == null ? new String[0] : sort.split(",", 2);
        String property = switch (parts.length == 0 ? "" : parts[0].trim().toLowerCase(Locale.ROOT)) {
            case "code" -> "code";
            case "active" -> "active";
            default -> "name";
        };
        Sort.Direction direction = parts.length > 1
                && "desc".equalsIgnoreCase(parts[1].trim())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return "";
        }
        return search.trim();
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
