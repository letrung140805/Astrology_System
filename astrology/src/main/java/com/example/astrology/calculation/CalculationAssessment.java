package com.example.astrology.calculation;

import java.util.List;

public record CalculationAssessment(
		AstrologyCalculationInput input,
		List<CalculationGap> gaps) {

	public CalculationAssessment {
		gaps = List.copyOf(gaps);
	}

	public boolean isComplete() {
		return gaps.isEmpty();
	}
}
