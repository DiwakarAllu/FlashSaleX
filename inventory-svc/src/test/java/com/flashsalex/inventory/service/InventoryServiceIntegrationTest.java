package com.flashsalex.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.flashsalex.inventory.dto.ReservationItemRequest;
import com.flashsalex.inventory.dto.ReservationRequest;
import com.flashsalex.inventory.dto.StockAdjustmentRequest;
import com.flashsalex.inventory.entity.ReservationStatus;
import com.flashsalex.inventory.exception.InsufficientStockException;
import com.flashsalex.inventory.exception.ResourceNotFoundException;
import com.flashsalex.inventory.repository.InventoryReservationRepository;
import com.flashsalex.inventory.repository.InventoryStockRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class InventoryServiceIntegrationTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryReservationRepository reservationRepository;

    @Autowired
    private InventoryStockRepository stockRepository;

    @BeforeEach
    void clearDatabase() {
        reservationRepository.deleteAll();
        stockRepository.deleteAll();
    }

    @Test
    void releaseRestoresStockAndIsIdempotent() {
        inventoryService.addStock("phone-1", new StockAdjustmentRequest(5));

        var reservation = inventoryService.reserve(1001L, request(
            new ReservationItemRequest("phone-1", 3)
        ));
        inventoryService.release(1001L, reservation.reservationId());
        var releasedAgain = inventoryService.release(1001L, reservation.reservationId());

        assertThat(releasedAgain.status()).isEqualTo(ReservationStatus.RELEASED);
        assertThat(inventoryService.getStock("phone-1").availableQuantity()).isEqualTo(5);
        assertThat(inventoryService.getStock("phone-1").reservedQuantity()).isZero();
    }

    @Test
    void confirmationConsumesStockAndIsIdempotent() {
        inventoryService.addStock("phone-1", new StockAdjustmentRequest(5));
        var reservation = inventoryService.reserve(1001L, request(
            new ReservationItemRequest("phone-1", 2)
        ));

        var confirmed = inventoryService.confirm(1001L, reservation.reservationId());
        var confirmedAgain = inventoryService.confirm(1001L, reservation.reservationId());

        assertThat(confirmed.status()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(confirmedAgain.status()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(inventoryService.getStock("phone-1").quantityOnHand()).isEqualTo(3);
        assertThat(inventoryService.getStock("phone-1").availableQuantity()).isEqualTo(3);
    }

    @Test
    void failedMultiProductReservationDoesNotPartiallyHoldStock() {
        inventoryService.addStock("phone-1", new StockAdjustmentRequest(5));
        inventoryService.addStock("phone-2", new StockAdjustmentRequest(1));

        assertThatThrownBy(() -> inventoryService.reserve(1001L, request(
            new ReservationItemRequest("phone-1", 2),
            new ReservationItemRequest("phone-2", 2)
        ))).isInstanceOf(InsufficientStockException.class);

        assertThat(inventoryService.getStock("phone-1").availableQuantity()).isEqualTo(5);
        assertThat(inventoryService.getStock("phone-1").reservedQuantity()).isZero();
    }

    @Test
    void reservationCannotBeChangedByAnotherUser() {
        inventoryService.addStock("phone-1", new StockAdjustmentRequest(5));
        var reservation = inventoryService.reserve(1001L, request(
            new ReservationItemRequest("phone-1", 2)
        ));

        assertThatThrownBy(() -> inventoryService.release(1002L, reservation.reservationId()))
            .isInstanceOf(ResourceNotFoundException.class);
        assertThat(inventoryService.getStock("phone-1").availableQuantity()).isEqualTo(3);
    }

    private ReservationRequest request(ReservationItemRequest... items) {
        return new ReservationRequest(List.of(items));
    }
}
