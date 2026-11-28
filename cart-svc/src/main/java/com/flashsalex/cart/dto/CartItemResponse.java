package com.flashsalex.cart.dto;

import java.math.BigDecimal;

public record CartItemResponse(
    String productId,
    String productName,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal subtotal,
    String currency
) {}
