package com.flashsalex.search.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.flashsalex.search.dto.ProductSearchResponse;
import com.flashsalex.search.dto.SearchResult;
import com.flashsalex.search.repository.ProductSearchRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ProductSearchRepository repository;

    public ProductSearchResponse searchProducts(
            String keyword,
            int page,
            int size
    ) {
        PageRequest pageable = PageRequest.of(page, size);

        Page<SearchResult> results =
                repository.findByNameContainingIgnoreCase(
                        keyword,
                        pageable
                ).map(product -> new SearchResult(
                        product.getProductId(),
                        product.getName(),
                        product.getPrice(),
                        product.getCurrency(),
                        product.getImageUrls()
                ));

        return new ProductSearchResponse(
                results.getContent(),
                results.getNumber(),
                results.getSize(),
                results.getTotalElements(),
                results.getTotalPages()
        );
    }
}
