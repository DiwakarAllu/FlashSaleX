package com.flashsalex.product.service;

import com.flashsalex.product.dto.ProductRequest;
import com.flashsalex.product.dto.ProductResponse;
import com.flashsalex.product.entity.Product;
import com.flashsalex.product.exception.DuplicateProductException;
import com.flashsalex.product.exception.ProductNotFoundException;
import com.flashsalex.product.repository.ProductRepository;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public ProductResponse createProduct(ProductRequest request) {

        if (productRepository.existsBySku(request.sku())) {
            throw new DuplicateProductException(
                "Product SKU already exists"
            );
        }

        Product product = Product.builder()
            .sku(request.sku())
            .title(request.title())
            .description(request.description())
            .category(request.category())
            .brand(request.brand())
            .price(request.price())
            .currency(request.currency())
            .images(request.images() == null ? List.of() : request.images())
            .specifications(request.specifications() == null ? Map.of() : request.specifications())
            .active(true)
            .build();

        return toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse getProductById(String id) {

        Product product = findProduct(id);
        return toResponse(product);
    }

    @Override
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable)
            .map(this::toResponse);
    }

    @Override
    public Page<ProductResponse> searchProducts(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query must not be blank");
        }

        return productRepository.searchActiveProducts(
                Pattern.quote(query.trim()),
                pageable
            )
            .map(this::toResponse);
    }

    @Override
    public ProductResponse updateProduct(
            String id,
            ProductRequest request) {

        Product product = findProduct(id);

        if (productRepository.existsBySkuAndIdNot(
                request.sku(), id)) {
            throw new DuplicateProductException(
                "Another product already uses this SKU"
            );
        }

        product.setSku(request.sku());
        product.setTitle(request.title());
        product.setDescription(request.description());
        product.setCategory(request.category());
        product.setBrand(request.brand());
        product.setPrice(request.price());
        product.setCurrency(request.currency());
        product.setImages(request.images() == null
            ? List.of() : request.images());
        product.setSpecifications(request.specifications() == null
            ? Map.of() : request.specifications());

        return toResponse(productRepository.save(product));
    }

    @Override
    public void deleteProduct(String id) {

        Product product = findProduct(id);

        product.setActive(false);
        productRepository.save(product);
    }

    @Override
    public Page<ProductResponse> getProductsByCategory(String category, Pageable pageable) {
        return productRepository.findByActiveTrueAndCategoryIgnoreCase(category, pageable)
            .map(this::toResponse);
    }

    private Product findProduct(String id) {

        return productRepository.findById(id)
            .filter(Product::isActive)
            .orElseThrow(() ->
                new ProductNotFoundException(
                    "Product not found: " + id
                )
            );
    }

    private ProductResponse toResponse(Product product) {

        return new ProductResponse(
            product.getId(),
            product.getSku(),
            product.getTitle(),
            product.getDescription(),
            product.getCategory(),
            product.getBrand(),
            product.getPrice(),
            product.getCurrency(),
            product.getImages(),
            product.getSpecifications(),
            product.isActive(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }
}
