package com.example.astrology.model;

import java.time.LocalDateTime;

public record CoupleHistoryItem(
		Long id,
		String viewUrl,
		String name1,
		String name2,
		int totalScore,
		String rating,
		String ratingColor,
		LocalDateTime createdAt,
		Double emotionalScore,
		Double communicationScore,
		Double financialScore,
		Double stabilityScore,
		Double livingTogetherScore
) {
}
