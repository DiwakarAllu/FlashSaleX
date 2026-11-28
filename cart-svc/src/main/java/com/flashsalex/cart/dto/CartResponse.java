package com.flashsalex.cart.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CartResponse(
    UUID cartId,
    Long userId,
    List<CartItemResponse> items,
    BigDecimal totalAmount,
    String currency,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
