package com.flashsalex.cart.client;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class ProductServiceClient {

    private final RestClient restClient;

    public ProductServiceClient(
        @Value("${app.product-service.url}") String baseUrl
    ) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public ProductDetails getProduct(String productId) {
        try {
            ProductDetails product = restClient.get()
                .uri("/api/v1/products/{productId}", productId)
                .retrieve()
                .body(ProductDetails.class);

            if (product == null
                || product.name() == null
                || product.price() == null
                || product.price().signum() < 0
                || product.currency() == null
                || product.currency().isBlank()) {
                throw new IllegalStateException("Invalid product response from Product Service");
            }

            return product;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ProductNotFoundException("Product not found: " + productId);
            }
            throw new ProductServiceException("Product Service returned an error");
        } catch (ResourceAccessException ex) {
            throw new ProductServiceException("Product Service is currently unavailable");
        }
    }

    public record ProductDetails(
        @JsonAlias({"id", "productId"}) String productId,
        @JsonAlias({"title", "name"}) String name,
        BigDecimal price,
        String currency
    ) {}

    public static class ProductNotFoundException extends RuntimeException {
        public ProductNotFoundException(String message) {
            super(message);
        }
    }

    public static class ProductServiceException extends RuntimeException {
        public ProductServiceException(String message) {
            super(message);
        }
    }
}
