package com.flashsalex.search.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.flashsalex.search.document.ProductDocument;

public interface ProductSearchRepository
        extends ElasticsearchRepository<ProductDocument, String> {

    Page<ProductDocument> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );
}
