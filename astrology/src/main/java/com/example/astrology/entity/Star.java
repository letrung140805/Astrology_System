package com.example.astrology.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stars", indexes = {
		@Index(name = "idx_stars_category", columnList = "category"),
		@Index(name = "idx_stars_type", columnList = "star_type"),
		@Index(name = "idx_stars_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
public class Star {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 50)
	private String code;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(name = "vietnamese_name", length = 100)
	private String vietnameseName;

	@Column(length = 50)
	private String category;

	@Column(name = "star_group", length = 100)
	private String starGroup;

	@Column(name = "star_type", length = 50)
	private String starType;

	@Column(columnDefinition = "TEXT")
	private String description;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}
