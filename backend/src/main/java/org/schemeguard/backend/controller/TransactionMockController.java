package org.schemeguard.backend.controller;

import lombok.RequiredArgsConstructor;
import org.schemeguard.backend.dto.TransactionMockResponse;
import org.schemeguard.backend.service.TransactionMockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions/mock")
@RequiredArgsConstructor
public class TransactionMockController {

    private final TransactionMockService transactionMockService;

    @GetMapping
    public ResponseEntity<List<TransactionMockResponse>> findAll() {
        return ResponseEntity.ok(
                transactionMockService.findAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionMockResponse> findById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                transactionMockService.findById(id)
        );
    }
}