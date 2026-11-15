package com.flashsalex.product.service;
import com.flashsalex.product.dto.ProductRequest;
import com.flashsalex.product.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    ProductResponse getProductById(String id);

    Page<ProductResponse> getAllProducts(Pageable pageable);

    Page<ProductResponse> searchProducts(String query, Pageable pageable);

    ProductResponse updateProduct(String id, ProductRequest request);

    void deleteProduct(String id);

    Page<ProductResponse> getProductsByCategory(String category, Pageable pageable);
}
