package com.flashsalex.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ProductRequest(

    @NotBlank(message = "SKU is required")
    @Size(max = 64)
    String sku,

    @NotBlank(message = "Product title is required")
    @Size(max = 200)
    String title,

    @NotBlank(message = "Description is required")
    @Size(max = 4000)
    String description,

    @NotBlank(message = "Category is required")
    @Size(max = 100)
    String category,

    @NotBlank(message = "Brand is required")
    @Size(max = 100)
    String brand,

    @NotNull
    @DecimalMin(value = "0.01")
    @Digits(integer = 10, fraction = 2)
    BigDecimal price,

    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$",
             message = "Currency must be a 3-letter ISO code")
    String currency,

    List<String> images,

    Map<String, Object> specifications

) {}

