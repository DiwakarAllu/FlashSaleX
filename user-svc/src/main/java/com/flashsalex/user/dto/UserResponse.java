package com.flashsalex.user.dto;

import java.time.Instant;

public record UserResponse(Long userId, String name, String email, Instant createdAt) {
}
