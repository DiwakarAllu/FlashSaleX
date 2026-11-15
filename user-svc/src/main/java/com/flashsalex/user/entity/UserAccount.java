package com.flashsalex.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAccount {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;

	@Column(nullable = false, length = 255)
	@Setter
	private String name;

	@Column(nullable = false, unique = true, length = 255)
	@Setter
	private String email;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	public UserAccount(String name, String email, String passwordHash) {
		this.name = name;
		this.email = email;
		this.passwordHash = passwordHash;
	}

	@PrePersist
	void setCreatedAt() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}
}
