package org.schemeguard.backend.controller;

import lombok.RequiredArgsConstructor;
import org.schemeguard.backend.dto.Analytics.AnalyticsResponseDto;
import org.schemeguard.backend.entity.enums.QualificationStatus;
import org.schemeguard.backend.entity.enums.TransactionStatus;
import org.schemeguard.backend.service.AnalyticsAggregationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsAggregationService analyticsAggregationService;

    @GetMapping
    public ResponseEntity<AnalyticsResponseDto> getAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String scheme,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String merchant,
            @RequestParam(required = false) QualificationStatus qualification,
            @RequestParam(required = false) TransactionStatus status
    ) {
        return ResponseEntity.ok(analyticsAggregationService.aggregate(from, to, scheme, country, merchant, qualification, status));
    }
}
