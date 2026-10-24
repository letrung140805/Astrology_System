package com.example.astrology.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chart_palace_stars", uniqueConstraints = {
		@UniqueConstraint(name = "uk_palace_star", columnNames = { "palace_id", "star_id" })
}, indexes = {
		@Index(name = "idx_cps_palace", columnList = "palace_id"),
		@Index(name = "idx_cps_star", columnList = "star_id")
})
@Getter
@Setter
@NoArgsConstructor
public class ChartPalaceStar {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "palace_id", nullable = false)
	private AstrologyPalace palace;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "star_id", nullable = false)
	private Star star;

	@Column(name = "star_order")
	private Integer starOrder;

	@Column(name = "is_main_star", nullable = false)
	private boolean mainStar;

	@Column(length = 50)
	private String brightness;

	@Column(length = 50)
	private String transformation;

	@Column(name = "position_type", length = 50)
	private String positionType;

	@Column(columnDefinition = "TEXT")
	private String interpretation;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}
