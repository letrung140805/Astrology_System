package com.example.astrology.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "astrology_charts")
@Getter
@Setter
@NoArgsConstructor
public class BirthChart {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "birth_profile_id", nullable = false)
	private BirthProfile birthProfile;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private AppUser user;

	@Column(name = "chart_name", length = 150)
	private String chartName;

	@Column(name = "chart_type", nullable = false, length = 50)
	private String chartType = "TU_VI_DAU_SO";

	@Column(name = "lunar_day")
	private Integer lunarDay;

	@Column(name = "lunar_month")
	private Integer lunarMonth;

	@Column(name = "lunar_year")
	private Integer lunarYear;

	@Column(name = "lunar_leap_month")
	private Boolean lunarLeapMonth;

	@Column(name = "year_can", length = 30)
	private String yearCan;

	@Column(name = "year_chi", length = 30)
	private String yearChi;

	@Column(name = "menh_name", length = 100)
	private String menhName;

	@Column(name = "menh_element", length = 100)
	private String menhElement;

	@Column(name = "cuc_name", length = 100)
	private String cucName;

	@Column(name = "cuc_element", length = 100)
	private String cucElement;

	@Column(name = "cung_menh_branch", length = 30)
	private String mingPalaceBranch;

	@Column(name = "cung_than_branch", length = 30)
	private String thanPalaceBranch;

	@Column(name = "than_location", length = 100)
	private String thanLocation;

	@Column(name = "chart_status", nullable = false, length = 30)
	private String chartStatus = "COMPLETED";

	@Column(name = "calculation_version", length = 120)
	private String calculationVersion;

	@Column(name = "raw_calculation_data", columnDefinition = "LONGTEXT")
	private String rawCalculationData;

	@Column(name = "access_token_hash", length = 64, unique = true)
	private String accessTokenHash;

	@CreationTimestamp
	@Column(name = "generated_at", nullable = false, updatable = false)
	private LocalDateTime generatedAt;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
}
