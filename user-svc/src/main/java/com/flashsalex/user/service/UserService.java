package com.flashsalex.user.service;

import com.flashsalex.user.dto.SignupRequest;
import com.flashsalex.user.dto.UpdateProfileRequest;
import com.flashsalex.user.dto.UserResponse;

public interface UserService {

	UserResponse register(SignupRequest request);

	UserResponse getProfile(Long userId);

	UserResponse updateProfile(Long userId, UpdateProfileRequest request);
}
