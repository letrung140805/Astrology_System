package com.example.astrology.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "birth_profiles", indexes = {
		@Index(name = "idx_birth_profiles_user", columnList = "user_id"),
		@Index(name = "idx_birth_profiles_birth_date", columnList = "birth_date")
})
@Getter
@Setter
@NoArgsConstructor
public class BirthProfile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@Column(name = "profile_name", length = 150)
	private String profileName;

	@Column(name = "full_name", nullable = false, length = 150)
	private String fullName;

	@Column(nullable = false, length = 20)
	private String gender;

	@Column(name = "birth_date", nullable = false)
	private LocalDate birthDate;

	@Column(name = "birth_time")
	private LocalTime birthTime;

	@Column(name = "birth_time_unknown", nullable = false)
	private boolean birthTimeUnknown;

	@Column(name = "birth_city", length = 100)
	private String birthCity;

	@Column(name = "birth_country", length = 100)
	private String birthCountry = "Vietnam";

	@Column(precision = 10, scale = 7)
	private BigDecimal latitude;

	@Column(precision = 10, scale = 7)
	private BigDecimal longitude;

	@Column(length = 50)
	private String timezone = "Asia/Ho_Chi_Minh";

	@Column(name = "is_primary", nullable = false)
	private boolean primary = true;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
}
