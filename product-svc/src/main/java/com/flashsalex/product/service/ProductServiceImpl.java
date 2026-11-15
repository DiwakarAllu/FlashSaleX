package com.flashsalex.product.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.flashsalex.product.dto.ProductRequest;
import com.flashsalex.product.dto.ProductResponse;
import com.flashsalex.product.entity.Product;
import com.flashsalex.product.exception.DuplicateProductException;
import com.flashsalex.product.exception.ProductNotFoundException;
import com.flashsalex.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public ProductResponse createProduct(ProductRequest request) {

        if (productRepository.existsBySku(request.sku())) {
            throw new DuplicateProductException(
                "Product SKU already exists"
            );
        }

        Instant now = Instant.now();

        Product product = Product.builder()
            .sku(request.sku())
            .title(request.title())
            .description(request.description())
            .category(request.category())
            .brand(request.brand())
            .price(request.price())
            .currency(request.currency())
            .images(request.images() == null ? List.of() : request.images())
            .specifications(request.specifications() == null ? java.util.Map.of() : request.specifications())
            .active(true)
            .createdAt(now)
            .updatedAt(now)
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

        Query query = new Query()
            .addCriteria(Criteria.where("active").is(true))
            .with(pageable);

        List<Product> products =
            mongoTemplate.find(query, Product.class);

        long total = mongoTemplate.count(
            Query.query(Criteria.where("active").is(true)),
            Product.class
        );

        return new PageImpl<>(
            products.stream().map(this::toResponse).toList(),
            pageable,
            total
        );
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
            ? java.util.Map.of() : request.specifications());
        product.setUpdatedAt(Instant.now());

        return toResponse(productRepository.save(product));
    }

    @Override
    public void deleteProduct(String id) {

        Product product = findProduct(id);

        product.setActive(false);
        product.setUpdatedAt(Instant.now());

        productRepository.save(product);
    }

    @Override
    public Page<ProductResponse> getProductsByCategory(String category,Pageable pageable) {

        Query query = new Query()
            .addCriteria(
                Criteria.where("active").is(true)
                    .and("category").is(category)
            )
            .with(pageable);

        List<Product> products = mongoTemplate.find(query, Product.class);

        Query countQuery = Query.query(
            Criteria.where("active").is(true)
                .and("category").is(category)
        );

        long total = mongoTemplate.count(
            countQuery, Product.class
        );

        return new PageImpl<>(
            products.stream().map(this::toResponse).toList(),
            pageable,
            total
        );
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
