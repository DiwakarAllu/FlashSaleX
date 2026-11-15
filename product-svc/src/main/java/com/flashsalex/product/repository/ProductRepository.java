package com.flashsalex.product.repository;

import com.flashsalex.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ProductRepository extends MongoRepository<Product, String> {

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, String id);

    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByActiveTrueAndCategoryIgnoreCase(String category, Pageable pageable);

    @Query(value = """
        {
          'active': true,
          '$or': [
            { 'title': { '$regex': ?0, '$options': 'i' } },
            { 'description': { '$regex': ?0, '$options': 'i' } },
            { 'category': { '$regex': ?0, '$options': 'i' } },
            { 'brand': { '$regex': ?0, '$options': 'i' } }
          ]
        }
        """)
    Page<Product> searchActiveProducts(String escapedQuery, Pageable pageable);
}
