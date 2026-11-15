package com.flashsalex.product.dto;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ProductResponse(
    String id,
    String sku,
    String title,
    String description,
    String category,
    String brand,
    BigDecimal price,
    String currency,
    List<String> images,
    Map<String, Object> specifications,
    boolean active,
    Instant createdAt,
    Instant updatedAt
) {}
