package com.flashsalex.product.repository;



import org.springframework.data.mongodb.repository.MongoRepository;
import com.flashsalex.product.entity.Product;
import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, String> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, String id);

}

