package com.flashsalex.inventory.controller;

import com.flashsalex.inventory.dto.ReservationRequest;
import com.flashsalex.inventory.dto.ReservationResponse;
import com.flashsalex.inventory.dto.StockAdjustmentRequest;
import com.flashsalex.inventory.dto.StockResponse;
import com.flashsalex.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{productId}")
    public ResponseEntity<StockResponse> getStock(@PathVariable String productId) {
        return ResponseEntity.ok(inventoryService.getStock(productId));
    }

    @PostMapping("/{productId}/stock")
    public ResponseEntity<StockResponse> addStock(
        @PathVariable String productId,
        @Valid @RequestBody StockAdjustmentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(inventoryService.addStock(productId, request));
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> reserve(
        @RequestHeader("X-User-Id") Long userId,
        @Valid @RequestBody ReservationRequest request
    ) {
        validateUserId(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(inventoryService.reserve(userId, request));
    }

    @PostMapping("/reservations/{reservationId}/confirm")
    public ResponseEntity<ReservationResponse> confirm(
        @RequestHeader("X-User-Id") Long userId,
        @PathVariable java.util.UUID reservationId
    ) {
        validateUserId(userId);
        return ResponseEntity.ok(inventoryService.confirm(userId, reservationId));
    }

    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<ReservationResponse> release(
        @RequestHeader("X-User-Id") Long userId,
        @PathVariable java.util.UUID reservationId
    ) {
        validateUserId(userId);
        return ResponseEntity.ok(inventoryService.release(userId, reservationId));
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("X-User-Id must be a positive number");
        }
    }
}
