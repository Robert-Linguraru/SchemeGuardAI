package org.schemeguard.backend.controller;

import org.schemeguard.backend.dto.TransactionPageResponse;
import org.schemeguard.backend.dto.TransactionStatisticsResponse;
import org.schemeguard.backend.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<TransactionPageResponse> findTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ResponseEntity.ok(transactionService.findTransactions(page, size));
    }

    @GetMapping("/statistics")
    public ResponseEntity<TransactionStatisticsResponse> getStatistics() {
        return ResponseEntity.ok(transactionService.getStatistics());
    }
}