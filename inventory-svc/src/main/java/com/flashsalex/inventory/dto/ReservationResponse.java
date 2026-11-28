package com.flashsalex.inventory.dto;

import com.flashsalex.inventory.entity.ReservationStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReservationResponse(
    UUID reservationId,
    ReservationStatus status,
    List<ReservationItemResponse> items,
    Instant createdAt
) {}
