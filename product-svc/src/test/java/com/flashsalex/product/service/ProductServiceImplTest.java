package com.flashsalex.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.flashsalex.product.dto.ProductRequest;
import com.flashsalex.product.entity.Product;
import com.flashsalex.product.exception.DuplicateProductException;
import com.flashsalex.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void searchProductsTreatsQueryAsLiteralText() {
        var pageable = PageRequest.of(0, 20);
        var product = Product.builder()
            .id("product-1")
            .sku("PHONE-001")
            .title("Phone.* case")
            .category("electronics")
            .price(new BigDecimal("19.99"))
            .currency("USD")
            .active(true)
            .build();

        when(productRepository.searchActiveProducts(Pattern.quote("phone.*"), pageable))
            .thenReturn(new PageImpl<>(List.of(product), pageable, 1));

        var result = productService.searchProducts(" phone.* ", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Phone.* case", result.getContent().getFirst().title());
    }

    @Test
    void searchProductsRejectsBlankQuery() {
        assertThrows(
            IllegalArgumentException.class,
            () -> productService.searchProducts("  ", PageRequest.of(0, 20))
        );
    }

    @Test
    void createProductRejectsDuplicateSku() {
        var request = new ProductRequest(
            "PHONE-001",
            "Phone",
            "Smartphone",
            "electronics",
            "Example",
            new BigDecimal("299.99"),
            "USD",
            List.of(),
            Map.of()
        );
        when(productRepository.existsBySku(request.sku())).thenReturn(true);

        assertThrows(DuplicateProductException.class, () -> productService.createProduct(request));
    }
}
