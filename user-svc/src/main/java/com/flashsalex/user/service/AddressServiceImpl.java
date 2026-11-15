package com.flashsalex.user.service;

import java.util.List;
import com.flashsalex.user.dto.AddressRequest;
import com.flashsalex.user.dto.AddressResponse;
import com.flashsalex.user.entity.UserAccount;
import com.flashsalex.user.entity.UserAddress;
import com.flashsalex.user.exception.ResourceNotFoundException;
import com.flashsalex.user.repository.UserAddressRepository;
import com.flashsalex.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

	private final UserRepository userRepository;
	private final UserAddressRepository addressRepository;

	@Override
	@Transactional
	public AddressResponse addAddress(Long userId, AddressRequest request) {
		UserAccount user = findUser(userId);
		if (request.isDefault()) {
			clearDefaultAddress(userId);
		}
		UserAddress address = new UserAddress(
			user,
			request.addressLine().trim(),
			request.city().trim(),
			request.state().trim(),
			request.postalCode().trim(),
			request.country().trim(),
			request.isDefault()
		);
		return AddressResponse.from(addressRepository.save(address));
	}

	@Override
	@Transactional(readOnly = true)
	public List<AddressResponse> getAddresses(Long userId) {
		findUser(userId);
		return addressRepository.findAllByUser_IdOrderByIdAsc(userId).stream()
			.map(AddressResponse::from)
			.toList();
	}

	@Override
	@Transactional
	public AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request) {
		UserAddress address = findAddress(userId, addressId);
		if (request.isDefault()) {
			clearDefaultAddress(userId);
		}
		address.setAddressLine(request.addressLine().trim());
		address.setCity(request.city().trim());
		address.setState(request.state().trim());
		address.setPostalCode(request.postalCode().trim());
		address.setCountry(request.country().trim());
		address.setDefault(request.isDefault());
		return AddressResponse.from(addressRepository.save(address));
	}

	@Override
	@Transactional
	public void deleteAddress(Long userId, Long addressId) {
		addressRepository.delete(findAddress(userId, addressId));
	}

	private UserAccount findUser(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException("User"));
	}

	private UserAddress findAddress(Long userId, Long addressId) {
		return addressRepository.findByIdAndUser_Id(addressId, userId)
			.orElseThrow(() -> new ResourceNotFoundException("Address"));
	}

	private void clearDefaultAddress(Long userId) {
		addressRepository.findAllByUser_IdOrderByIdAsc(userId).stream()
			.filter(UserAddress::isDefault)
			.forEach(address -> address.setDefault(false));
	}
}
