package com.flashsalex.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory_stock")
@Getter
@NoArgsConstructor
public class InventoryStock {

    @Id
    @Column(name = "product_id", nullable = false, updatable = false)
    private String productId;

    @Column(name = "quantity_on_hand", nullable = false)
    private int quantityOnHand;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    @Version
    private Long version;

    public InventoryStock(String productId) {
        this.productId = productId;
    }

    public int getAvailableQuantity() {
        return quantityOnHand - reservedQuantity;
    }

    public void addStock(int quantity) {
        quantityOnHand = Math.addExact(quantityOnHand, quantity);
    }

    public void reserve(int quantity) {
        if (quantity > getAvailableQuantity()) {
            throw new IllegalStateException("Insufficient stock for product: " + productId);
        }
        reservedQuantity = Math.addExact(reservedQuantity, quantity);
    }

    public void release(int quantity) {
        reservedQuantity -= quantity;
    }

    public void confirm(int quantity) {
        reservedQuantity -= quantity;
        quantityOnHand -= quantity;
    }
}
