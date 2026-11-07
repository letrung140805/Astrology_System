package com.example.astrology.entity;

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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "couple_charts")
@Getter
@Setter
@NoArgsConstructor
public class CoupleChart {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private AppUser user;

	// --- NGƯỜI THỨ NHẤT (PERSON 1) ---
	@Column(name = "name_1", nullable = false, length = 150)
	private String name1;

	@Column(name = "gender_1", nullable = false, length = 20)
	private String gender1;

	@Column(name = "birth_date_1", nullable = false)
	private LocalDate birthDate1;

	@Column(name = "birth_time_1")
	private LocalTime birthTime1;

	@Column(name = "birthplace_1", length = 150)
	private String birthplace1;

	@Column(name = "ban_menh_1", length = 100)
	private String banMenh1;

	@Column(name = "can_chi_1", length = 50)
	private String canChi1;

	// --- NGƯỜI THỨ HAI (PERSON 2) ---
	@Column(name = "name_2", nullable = false, length = 150)
	private String name2;

	@Column(name = "gender_2", nullable = false, length = 20)
	private String gender2;

	@Column(name = "birth_date_2", nullable = false)
	private LocalDate birthDate2;

	@Column(name = "birth_time_2")
	private LocalTime birthTime2;

	@Column(name = "birthplace_2", length = 150)
	private String birthplace2;

	@Column(name = "ban_menh_2", length = 100)
	private String banMenh2;

	@Column(name = "can_chi_2", length = 50)
	private String canChi2;

	// --- ĐIỂM SỐ TƯƠNG HỢP ---
	@Column(name = "total_score", nullable = false)
	private Integer totalScore;

	@Column(name = "rating", length = 100)
	private String rating;

	@Column(name = "rating_color", length = 50)
	private String ratingColor;

	// 5 điểm số trụ cột cốt lõi
	@Column(name = "emotional_score")
	private Double emotionalScore;

	@Column(name = "communication_score")
	private Double communicationScore;

	@Column(name = "financial_score")
	private Double financialScore;

	@Column(name = "stability_score")
	private Double stabilityScore;

	@Column(name = "living_together_score")
	private Double livingTogetherScore;

	// --- LUẬN GIẢI CHI TIẾT 5 YẾU TỐ ---
	@Column(name = "emotional_interpretation", columnDefinition = "TEXT")
	private String emotionalInterpretation;

	@Column(name = "communication_interpretation", columnDefinition = "TEXT")
	private String communicationInterpretation;

	@Column(name = "financial_interpretation", columnDefinition = "TEXT")
	private String financialInterpretation;

	@Column(name = "stability_interpretation", columnDefinition = "TEXT")
	private String stabilityInterpretation;

	@Column(name = "living_together_interpretation", columnDefinition = "TEXT")
	private String livingTogetherInterpretation;

	// --- LỜI KẾT VÀ LỜI KHUYÊN ---
	@Column(name = "conclusion", columnDefinition = "TEXT")
	private String conclusion;

	@Column(name = "advice", columnDefinition = "TEXT")
	private String advice;

	@Column(name = "access_token_hash", length = 64)
	private String accessTokenHash;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
}
