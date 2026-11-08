package com.example.astrology.model;

import java.time.LocalDateTime;

public record FriendHistoryItem(
		Long id,
		String viewUrl,
		String name1,
		String name2,
		int totalScore,
		String rating,
		String ratingColor,
		LocalDateTime createdAt,
		Double partnerScore,
		Double communicationScore,
		Double visionScore,
		Double valuesScore,
		Double moneyScore
) {
}
