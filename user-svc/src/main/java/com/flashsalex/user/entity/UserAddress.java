package com.flashsalex.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_addresses")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAddress {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "address_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private UserAccount user;

	@Column(name = "address_line", nullable = false, length = 500)
	private String addressLine;

	@Column(nullable = false, length = 100)
	private String city;

	@Column(nullable = false, length = 100)
	private String state;

	@Column(name = "postal_code", nullable = false, length = 20)
	private String postalCode;

	@Column(nullable = false, length = 100)
	private String country;

	@Column(name = "is_default", nullable = false)
	private boolean isDefault;

	public UserAddress(
		UserAccount user,
		String addressLine,
		String city,
		String state,
		String postalCode,
		String country,
		boolean isDefault
	) {
		this.user = user;
		this.addressLine = addressLine;
		this.city = city;
		this.state = state;
		this.postalCode = postalCode;
		this.country = country;
		this.isDefault = isDefault;
	}
}
