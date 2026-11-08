package com.example.astrology.model;

import java.util.List;

public record FriendCompatibilityResult(
		int totalScore,
		String rating,
		String ratingColor,
		String summary,
		double partnerScore,
		double communicationScore,
		double visionScore,
		double valuesScore,
		double moneyScore,
		String partnerInterpretation,
		String communicationInterpretation,
		String visionInterpretation,
		String valuesInterpretation,
		String moneyInterpretation,
		String conclusion,
		String advice,
		String aiFullInterpretation,
		List<TriggeredRule> triggeredRules
) {

	public static FriendCompatibilityResult fromSaved(
			int totalScore,
			String rating,
			String ratingColor,
			String summary,
			String advice,
			Double partnerScore,
			Double communicationScore,
			Double visionScore,
			Double valuesScore,
			Double moneyScore,
			String partnerInterpretation,
			String communicationInterpretation,
			String visionInterpretation,
			String valuesInterpretation,
			String moneyInterpretation,
			String conclusion) {
		return new FriendCompatibilityResult(
				totalScore,
				rating != null ? rating : "",
				ratingColor != null ? ratingColor : "badge-primary",
				summary != null ? summary : "",
				partnerScore != null ? partnerScore : 7.0,
				communicationScore != null ? communicationScore : 7.0,
				visionScore != null ? visionScore : 7.0,
				valuesScore != null ? valuesScore : 7.0,
				moneyScore != null ? moneyScore : 7.0,
				partnerInterpretation != null ? partnerInterpretation : "",
				communicationInterpretation != null ? communicationInterpretation : "",
				visionInterpretation != null ? visionInterpretation : "",
				valuesInterpretation != null ? valuesInterpretation : "",
				moneyInterpretation != null ? moneyInterpretation : "",
				conclusion != null ? conclusion : "",
				advice != null ? advice : "",
				null,
				List.of()
		);
	}
}
