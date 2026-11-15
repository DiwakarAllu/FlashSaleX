package com.flashsalex.user.controller;

import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.flashsalex.user.dto.AddressRequest;
import com.flashsalex.user.dto.AddressResponse;
import com.flashsalex.user.dto.LoginRequest;
import com.flashsalex.user.dto.LoginResponse;
import com.flashsalex.user.dto.SignupRequest;
import com.flashsalex.user.dto.UpdateProfileRequest;
import com.flashsalex.user.dto.UserResponse;
import com.flashsalex.user.service.AddressService;
import com.flashsalex.user.service.AuthService;
import com.flashsalex.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final AuthService authService;
	private final AddressService addressService;

	@PostMapping("/signup")
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse signup(@Valid @RequestBody SignupRequest request) {
		return userService.register(request);
	}

	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@GetMapping("/profile")
	public UserResponse getProfile(@AuthenticationPrincipal Jwt jwt) {
		return userService.getProfile(authenticatedUserId(jwt));
	}

	@PutMapping("/profile")
	public UserResponse updateProfile(
		@AuthenticationPrincipal Jwt jwt,
		@Valid @RequestBody UpdateProfileRequest request
	) {
		return userService.updateProfile(authenticatedUserId(jwt), request);
	}

	@PostMapping("/addresses")
	@ResponseStatus(HttpStatus.CREATED)
	public AddressResponse addAddress(
		@AuthenticationPrincipal Jwt jwt,
		@Valid @RequestBody AddressRequest request
	) {
		return addressService.addAddress(authenticatedUserId(jwt), request);
	}

	@GetMapping("/addresses")
	public List<AddressResponse> getAddresses(@AuthenticationPrincipal Jwt jwt) {
		return addressService.getAddresses(authenticatedUserId(jwt));
	}

	@PutMapping("/addresses/{id}")
	public AddressResponse updateAddress(
		@AuthenticationPrincipal Jwt jwt,
		@PathVariable Long id,
		@Valid @RequestBody AddressRequest request
	) {
		return addressService.updateAddress(authenticatedUserId(jwt), id, request);
	}

	@DeleteMapping("/addresses/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteAddress(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
		addressService.deleteAddress(authenticatedUserId(jwt), id);
	}

	private Long authenticatedUserId(Jwt jwt) {
		return Long.valueOf(jwt.getSubject());
	}
}
