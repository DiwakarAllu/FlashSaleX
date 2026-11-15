package com.flashsalex.user.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import com.flashsalex.user.dto.SignupRequest;
import com.flashsalex.user.dto.UpdateProfileRequest;
import com.flashsalex.user.dto.UserResponse;
import com.flashsalex.user.entity.UserAccount;
import com.flashsalex.user.exception.DuplicateEmailException;
import com.flashsalex.user.exception.InvalidRequestException;
import com.flashsalex.user.exception.ResourceNotFoundException;
import com.flashsalex.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public UserResponse register(SignupRequest request) {
		String email = normalizeEmail(request.email());
		if (userRepository.existsByEmail(email)) {
			throw new DuplicateEmailException();
		}
		if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
			throw new InvalidRequestException("Password must be at most 72 bytes when UTF-8 encoded");
		}

		UserAccount user = new UserAccount(
			request.name().trim(),
			email,
			passwordEncoder.encode(request.password())
		);
		return toResponse(userRepository.save(user));
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponse getProfile(Long userId) {
		return toResponse(findUser(userId));
	}

	@Override
	@Transactional
	public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
		UserAccount user = findUser(userId);
		String email = normalizeEmail(request.email());
		if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
			throw new DuplicateEmailException();
		}
		user.setName(request.name().trim());
		user.setEmail(email);
		return toResponse(userRepository.save(user));
	}

	private UserAccount findUser(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException("User"));
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private UserResponse toResponse(UserAccount user) {
		return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getCreatedAt());
	}
}
