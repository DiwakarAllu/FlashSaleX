package com.flashsalex.inventory.dto;

public record StockResponse(
    String productId,
    int quantityOnHand,
    int reservedQuantity,
    int availableQuantity
) {}
