package com.flashsalex.user.service;

import java.util.List;
import com.flashsalex.user.dto.AddressRequest;
import com.flashsalex.user.dto.AddressResponse;

public interface AddressService {

	AddressResponse addAddress(Long userId, AddressRequest request);

	List<AddressResponse> getAddresses(Long userId);

	AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request);

	void deleteAddress(Long userId, Long addressId);
}
