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
@Table(name = "astrology_palaces", uniqueConstraints = {
		@UniqueConstraint(name = "uk_chart_palace_order", columnNames = { "chart_id", "palace_order" })
}, indexes = {
		@Index(name = "idx_palaces_chart", columnList = "chart_id"),
		@Index(name = "idx_palaces_name", columnList = "palace_name")
})
@Getter
@Setter
@NoArgsConstructor
public class AstrologyPalace {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "chart_id", nullable = false)
	private BirthChart chart;

	@Column(name = "palace_order", nullable = false)
	private Integer palaceOrder;

	@Column(name = "palace_name", nullable = false, length = 100)
	private String palaceName;

	@Column(name = "earthly_branch", length = 30)
	private String earthlyBranch;

	@Column(name = "heavenly_stem", length = 30)
	private String heavenlyStem;

	@Column(name = "life_stage", length = 50)
	private String lifeStage;

	@Column(name = "tieu_han_start_age")
	private Integer tieuHanStartAge;

	@Column(name = "tieu_han_end_age")
	private Integer tieuHanEndAge;

	@Column(columnDefinition = "TEXT")
	private String interpretation;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}
