package com.flashsalex.inventory.repository;

import com.flashsalex.inventory.entity.InventoryReservation;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryReservationRepository
    extends JpaRepository<InventoryReservation, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select reservation
        from InventoryReservation reservation
        where reservation.reservationId = :reservationId
          and reservation.userId = :userId
        """)
    Optional<InventoryReservation> findByIdAndUserIdForUpdate(
        @Param("reservationId") UUID reservationId,
        @Param("userId") Long userId
    );
}
