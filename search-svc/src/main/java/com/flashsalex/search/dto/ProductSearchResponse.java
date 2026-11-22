package com.flashsalex.search.dto;

import java.util.List;

public record ProductSearchResponse(
        List<SearchResult> products,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
