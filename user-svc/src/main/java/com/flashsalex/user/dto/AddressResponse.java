package com.flashsalex.user.dto;

import com.flashsalex.user.entity.UserAddress;

public record AddressResponse(
	Long addressId,
	String addressLine,
	String city,
	String state,
	String postalCode,
	String country,
	boolean isDefault
) {
	public static AddressResponse from(UserAddress address) {
		return new AddressResponse(
			address.getId(),
			address.getAddressLine(),
			address.getCity(),
			address.getState(),
			address.getPostalCode(),
			address.getCountry(),
			address.isDefault()
		);
	}
}
