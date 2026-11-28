package com.flashsalex.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory_reservation_items")
@Getter
@NoArgsConstructor
public class InventoryReservationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "reservation_item_id")
    private UUID reservationItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private InventoryReservation reservation;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(nullable = false)
    private int quantity;

    public InventoryReservationItem(
        InventoryReservation reservation,
        String productId,
        int quantity
    ) {
        this.reservation = reservation;
        this.productId = productId;
        this.quantity = quantity;
    }
}
