package com.flashsalex.cart.controller;

import com.flashsalex.cart.dto.AddCartItemRequest;
import com.flashsalex.cart.dto.CartResponse;
import com.flashsalex.cart.dto.UpdateCartItemRequest;
import com.flashsalex.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
        @RequestHeader("X-User-Id") Long userId,
        @Valid @RequestBody AddCartItemRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(cartService.addItem(userId, request));
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
        @RequestHeader("X-User-Id") Long userId
    ) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateItem(
        @RequestHeader("X-User-Id") Long userId,
        @PathVariable String productId,
        @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return ResponseEntity.ok(cartService.updateItem(userId, productId, request));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(
        @RequestHeader("X-User-Id") Long userId,
        @PathVariable String productId
    ) {
        return ResponseEntity.ok(cartService.removeItem(userId, productId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(
        @RequestHeader("X-User-Id") Long userId
    ) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
