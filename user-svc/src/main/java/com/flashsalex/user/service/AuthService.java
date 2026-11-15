package com.flashsalex.user.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Locale;
import com.flashsalex.user.dto.LoginRequest;
import com.flashsalex.user.dto.LoginResponse;
import com.flashsalex.user.entity.UserAccount;
import com.flashsalex.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtEncoder jwtEncoder;
	private final String issuer;
	private final long expirationSeconds;

	public AuthService(
		UserRepository userRepository,
		PasswordEncoder passwordEncoder,
		JwtEncoder jwtEncoder,
		@Value("${app.jwt.issuer}") String issuer,
		@Value("${app.jwt.expiration-seconds}") long expirationSeconds
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtEncoder = jwtEncoder;
		this.issuer = issuer;
		this.expirationSeconds = expirationSeconds;
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		UserAccount user = userRepository.findByEmail(request.email().trim().toLowerCase(Locale.ROOT))
			.filter(account -> request.password().getBytes(StandardCharsets.UTF_8).length <= 72)
			.filter(account -> passwordEncoder.matches(request.password(), account.getPasswordHash()))
			.orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

		Instant issuedAt = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuer(issuer)
			.issuedAt(issuedAt)
			.expiresAt(issuedAt.plusSeconds(expirationSeconds))
			.subject(user.getId().toString())
			.claim("email", user.getEmail())
			.build();
		String token = jwtEncoder.encode(JwtEncoderParameters.from(
			JwsHeader.with(MacAlgorithm.HS256).build(),
			claims
		)).getTokenValue();

		return new LoginResponse(token, "Bearer", expirationSeconds);
	}
}
