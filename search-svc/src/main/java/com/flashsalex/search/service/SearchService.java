package com.flashsalex.search.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;

import lombok.RequiredArgsConstructor;

import com.flashsalex.search.dto.ProductSearchResponse;
import com.flashsalex.search.dto.SearchResultResponse;
import com.flashsalex.search.model.ProductSearchDocument;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public SearchResultResponse searchProducts(
            String keyword,
            int page,
            int size
    ) {
        var pageable = PageRequest.of(page, size);

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.multiMatch(mm -> mm
                        .query(keyword)
                        .fields("name^3", "description", "category")
                ))
                .withPageable(pageable)
                .build();

        SearchHits<ProductSearchDocument> searchHits =
                elasticsearchOperations.search(query, ProductSearchDocument.class);

        List<ProductSearchResponse> content = searchHits.getSearchHits()
                .stream()
                .map(this::toResponse)
                .toList();

        long totalElements = searchHits.getTotalHits();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return new SearchResultResponse(
                content,
                page,
                size,
                totalElements,
                totalPages
        );
    }

    private ProductSearchResponse toResponse(SearchHit<ProductSearchDocument> hit) {
        ProductSearchDocument product = hit.getContent();
        List<String> imageUrls = product.getImageUrls();
        String thumbnail = imageUrls == null || imageUrls.isEmpty()
                ? null
                : imageUrls.getFirst();

        return new ProductSearchResponse(
                product.getProductId(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getCurrency(),
                thumbnail
        );
    }
}
