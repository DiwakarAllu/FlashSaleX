package com.flashsalex.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(
	@NotBlank @Size(max = 500) String addressLine,
	@NotBlank @Size(max = 100) String city,
	@NotBlank @Size(max = 100) String state,
	@NotBlank @Size(max = 20) String postalCode,
	@NotBlank @Size(max = 100) String country,
	boolean isDefault
) {
}
