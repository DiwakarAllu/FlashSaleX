package com.flashsalex.inventory.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory_reservations")
@Getter
@NoArgsConstructor
public class InventoryReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "reservation_id")
    private UUID reservationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventoryReservationItem> items = new ArrayList<>();

    public InventoryReservation(Long userId) {
        this.userId = userId;
        this.status = ReservationStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

    public void addItem(String productId, int quantity) {
        items.add(new InventoryReservationItem(this, productId, quantity));
    }

    public void confirm() {
        status = ReservationStatus.CONFIRMED;
    }

    public void release() {
        status = ReservationStatus.RELEASED;
    }
}
