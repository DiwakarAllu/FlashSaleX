package com.flashsalex.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ReservationRequest(
    @NotEmpty(message = "At least one item is required")
    List<@Valid ReservationItemRequest> items
) {}
