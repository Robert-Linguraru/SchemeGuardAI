package org.schemeguard.backend;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.schemeguard.backend.dto.Rule.RuleRequestDto;
import org.schemeguard.backend.entity.CardScheme;
import org.schemeguard.backend.exception.ConflictException;
import org.schemeguard.backend.exception.GlobalExceptionHandler;
import org.schemeguard.backend.exception.ResourceNotFoundException;
import org.schemeguard.backend.repository.CardSchemeRepository;
import org.schemeguard.backend.repository.RuleRepository;
import org.schemeguard.backend.service.RuleService;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RuleServiceErrorHandlingTests {

    private final RuleRepository ruleRepository = mock(RuleRepository.class);
    private final CardSchemeRepository cardSchemeRepository = mock(CardSchemeRepository.class);
    private final RuleService ruleService = new RuleService(ruleRepository, cardSchemeRepository);
    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    void missingCardSchemeUsesNotFoundResponse() {
        UUID schemeId = UUID.randomUUID();
        RuleRequestDto request = requestFor(schemeId);
        when(cardSchemeRepository.findById(schemeId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> ruleService.createRule(request)
        );

        var response = exceptionHandler.handleResourceNotFound(exception);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("CARD_SCHEME_NOT_FOUND", response.getBody().errorCode());
    }

    @Test
    void missingRuleUsesNotFoundResponse() {
        UUID ruleId = UUID.randomUUID();
        when(ruleRepository.findById(ruleId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> ruleService.getRuleById(ruleId)
        );

        var response = exceptionHandler.handleResourceNotFound(exception);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("This rule could not be found.", response.getBody().message());
    }

    @Test
    void duplicateRuleKeepsConflictBehavior() {
        UUID schemeId = UUID.randomUUID();
        RuleRequestDto request = requestFor(schemeId);
        when(cardSchemeRepository.findById(schemeId)).thenReturn(Optional.of(new CardScheme()));
        when(ruleRepository.existsBySchemeIdAndRuleCodeAndVersion(schemeId, "RULE-1", 1))
                .thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> ruleService.createRule(request)
        );

        var response = exceptionHandler.handleConflict(exception);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("A rule with this code already exists.", response.getBody().message());
    }

    private RuleRequestDto requestFor(UUID schemeId) {
        RuleRequestDto request = new RuleRequestDto();
        request.setSchemeId(schemeId);
        request.setRuleCode("RULE-1");
        request.setVersion(1);
        return request;
    }
}