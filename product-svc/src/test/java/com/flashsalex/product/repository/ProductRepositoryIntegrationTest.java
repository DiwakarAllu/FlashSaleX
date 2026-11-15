package com.flashsalex.product.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.flashsalex.product.entity.Product;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ProductRepositoryIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void searchTreatsInputAsLiteralTextAndReturnsOnlyActiveProducts() {
        String uniqueToken = UUID.randomUUID().toString();
        Product activeProduct = productRepository.save(Product.builder()
            .sku("SEARCH-" + uniqueToken)
            .title("Phone.* " + uniqueToken)
            .description("Search integration test")
            .category("test")
            .brand("Test")
            .price(new BigDecimal("1.00"))
            .currency("USD")
            .specifications(Map.of("screenSizeInches", 6.2))
            .active(true)
            .build());
        Product inactiveProduct = productRepository.save(Product.builder()
            .sku("INACTIVE-" + uniqueToken)
            .title("Phone.* " + uniqueToken)
            .description("Search integration test")
            .category("test")
            .brand("Test")
            .price(new BigDecimal("1.00"))
            .currency("USD")
            .active(false)
            .build());

        try {
            var results = productRepository.searchActiveProducts(
                Pattern.quote("phone.* " + uniqueToken),
                PageRequest.of(0, 10)
            );

            assertEquals(1, results.getTotalElements());
            assertEquals(activeProduct.getId(), results.getContent().getFirst().getId());
            assertNotNull(results.getContent().getFirst().getCreatedAt());
            assertNotNull(results.getContent().getFirst().getUpdatedAt());
            assertEquals(
                0,
                new BigDecimal("1.00").compareTo(results.getContent().getFirst().getPrice())
            );
            assertEquals(
                6.2,
                results.getContent().getFirst().getSpecifications().get("screenSizeInches")
            );

            var categoryResults = productRepository.findByActiveTrueAndCategoryIgnoreCase(
                "TEST",
                PageRequest.of(0, 10)
            );
            assertEquals(1, categoryResults.getTotalElements());
            assertEquals(activeProduct.getId(), categoryResults.getContent().getFirst().getId());
            assertTrue(results.getContent().stream()
                .noneMatch(product -> product.getId().equals(inactiveProduct.getId())));
        } finally {
            productRepository.deleteAllById(List.of(activeProduct.getId(), inactiveProduct.getId()));
        }
    }

    @Test
    void searchEndpointReturnsPagedResultsWithCappedPageSize() throws Exception {
        String uniqueToken = UUID.randomUUID().toString();
        Product product = productRepository.save(Product.builder()
            .sku("API-SEARCH-" + uniqueToken)
            .title("Search endpoint " + uniqueToken)
            .description("Search integration test")
            .category("test")
            .brand("Test")
            .price(new BigDecimal("1.00"))
            .currency("USD")
            .active(true)
            .build());

        try {
            mockMvc.perform(get("/api/v1/products/search")
                    .param("q", uniqueToken)
                    .param("page", "0")
                    .param("size", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].sku").value(product.getSku()))
                .andExpect(jsonPath("$.page.size").value(100));
        } finally {
            productRepository.deleteById(product.getId());
        }
    }
}
