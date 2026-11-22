package com.flashsalex.search.dto;

import java.util.List;

public record SearchResultResponse(
        List<ProductSearchResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
