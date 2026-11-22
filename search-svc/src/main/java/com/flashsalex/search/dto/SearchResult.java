package com.flashsalex.search.dto;

import java.math.BigDecimal;
import java.util.List;

public record SearchResult(
        String productId,
        String name,
        BigDecimal price,
        String currency,
        List<String> imageUrls
) {}
