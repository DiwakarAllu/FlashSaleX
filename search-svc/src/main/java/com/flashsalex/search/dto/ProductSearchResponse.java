package com.flashsalex.search.dto;

import java.math.BigDecimal;

public record ProductSearchResponse(
        String productId,
        String name,
        String category,
        BigDecimal price,
        String currency,
        String thumbnail
) {}
