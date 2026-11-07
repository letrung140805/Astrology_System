package com.example.astrology.model;

import java.util.List;

public record CoupleCompatibilityResult(
		int totalScore,
		String rating,
		String ratingColor,
		String summary,
		String menhTitle,
		int menhScore,
		String menhAnalysis,
		String canTitle,
		int canScore,
		String canAnalysis,
		String chiTitle,
		int chiScore,
		String chiAnalysis,
		String cungPhiTitle,
		int cungPhiScore,
		String cungPhiAnalysis,
		String phuTheTitle,
		String phuTheAnalysis,
		String advice,
		double emotionalScore,
		double communicationScore,
		double financialScore,
		double stabilityScore,
		double livingTogetherScore,
		String emotionalInterpretation,
		String communicationInterpretation,
		String financialInterpretation,
		String stabilityInterpretation,
		String livingTogetherInterpretation,
		String conclusion,
		String aiFullInterpretation,
		List<TriggeredRule> triggeredRules
) {
	// Backward compatible constructor without conclusion (31 parameters)
	public CoupleCompatibilityResult(
			int totalScore,
			String rating,
			String ratingColor,
			String summary,
			String menhTitle,
			int menhScore,
			String menhAnalysis,
			String canTitle,
			int canScore,
			String canAnalysis,
			String chiTitle,
			int chiScore,
			String chiAnalysis,
			String cungPhiTitle,
			int cungPhiScore,
			String cungPhiAnalysis,
			String phuTheTitle,
			String phuTheAnalysis,
			String advice,
			double emotionalScore,
			double communicationScore,
			double financialScore,
			double stabilityScore,
			double livingTogetherScore,
			String emotionalInterpretation,
			String communicationInterpretation,
			String financialInterpretation,
			String stabilityInterpretation,
			String livingTogetherInterpretation,
			String aiFullInterpretation,
			List<TriggeredRule> triggeredRules) {
		this(totalScore, rating, ratingColor, summary,
				menhTitle, menhScore, menhAnalysis,
				canTitle, canScore, canAnalysis,
				chiTitle, chiScore, chiAnalysis,
				cungPhiTitle, cungPhiScore, cungPhiAnalysis,
				phuTheTitle, phuTheAnalysis, advice,
				emotionalScore, communicationScore, financialScore, stabilityScore, livingTogetherScore,
				emotionalInterpretation, communicationInterpretation, financialInterpretation,
				stabilityInterpretation, livingTogetherInterpretation,
				summary,
				aiFullInterpretation,
				triggeredRules);
	}

	// Backward compatible constructor with 19 parameters
	public CoupleCompatibilityResult(
			int totalScore,
			String rating,
			String ratingColor,
			String summary,
			String menhTitle,
			int menhScore,
			String menhAnalysis,
			String canTitle,
			int canScore,
			String canAnalysis,
			String chiTitle,
			int chiScore,
			String chiAnalysis,
			String cungPhiTitle,
			int cungPhiScore,
			String cungPhiAnalysis,
			String phuTheTitle,
			String phuTheAnalysis,
			String advice) {
		this(totalScore, rating, ratingColor, summary,
				menhTitle, menhScore, menhAnalysis,
				canTitle, canScore, canAnalysis,
				chiTitle, chiScore, chiAnalysis,
				cungPhiTitle, cungPhiScore, cungPhiAnalysis,
				phuTheTitle, phuTheAnalysis, advice,
				7.5, 7.0, 7.0, 6.5, 7.0,
				"Hòa hợp tình cảm ổn định và có sự nâng đỡ tốt.",
				"Giao tiếp chân thành, cùng nhau chia sẻ quan điểm.",
				"Tài chính gia đình có sự tương trợ, hướng tới bền vững.",
				"Có khả năng vượt qua thử thách nhờ sự nhẫn nại.",
				"Thói quen sinh hoạt và tổ ấm thuận hòa.",
				summary,
				null,
				List.of());
	}

	// Helper accessors for backward compatibility
	public static CoupleCompatibilityResult fromSaved(
			int totalScore,
			String rating,
			String ratingColor,
			String summary,
			String advice,
			Double emotionalScore,
			Double communicationScore,
			Double financialScore,
			Double stabilityScore,
			Double livingTogetherScore,
			String emotionalInterpretation,
			String communicationInterpretation,
			String financialInterpretation,
			String stabilityInterpretation,
			String livingTogetherInterpretation,
			String conclusion) {
		return new CoupleCompatibilityResult(
				totalScore,
				rating != null ? rating : "",
				ratingColor != null ? ratingColor : "badge-primary",
				summary != null ? summary : "",
				"", 0, "",
				"", 0, "",
				"", 0, "",
				"", 0, "",
				"", "",
				advice != null ? advice : "",
				emotionalScore != null ? emotionalScore : 7.0,
				communicationScore != null ? communicationScore : 7.0,
				financialScore != null ? financialScore : 7.0,
				stabilityScore != null ? stabilityScore : 7.0,
				livingTogetherScore != null ? livingTogetherScore : 7.0,
				emotionalInterpretation != null ? emotionalInterpretation : "",
				communicationInterpretation != null ? communicationInterpretation : "",
				financialInterpretation != null ? financialInterpretation : "",
				stabilityInterpretation != null ? stabilityInterpretation : "",
				livingTogetherInterpretation != null ? livingTogetherInterpretation : "",
				conclusion != null ? conclusion : "",
				"",
				List.of()
		);
	}

	public double attractionScore() {
		return emotionalScore;
	}

	public double personalityScore() {
		return emotionalScore;
	}

	public double partnerFitScore() {
		return emotionalScore;
	}

	public double dailyLifeScore() {
		return livingTogetherScore;
	}

	public String aiOverview() {
		return emotionalInterpretation;
	}

	public String aiStrengths() {
		return communicationInterpretation;
	}

	public String aiWarnings() {
		return stabilityInterpretation;
	}

	public String aiAdvice() {
		return advice;
	}
}
