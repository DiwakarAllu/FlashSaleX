package com.flashsalex.search.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.flashsalex.search.model.ProductSearchDocument;

public interface ProductSearchRepository
        extends ElasticsearchRepository<ProductSearchDocument, String> {
}
