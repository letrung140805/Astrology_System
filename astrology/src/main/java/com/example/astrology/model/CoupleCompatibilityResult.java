package com.example.astrology.model;

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
		String advice
) {}

