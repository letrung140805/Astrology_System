package com.example.astrology.calculation;

import com.example.astrology.calculation.calendar.NormalizedBirthInput;
import java.util.List;

public record CalculationAssessment(
		AstrologyCalculationInput input,
		NormalizedBirthInput normalizedBirthInput,
		List<CalculationGap> gaps) {

	public CalculationAssessment {
		gaps = List.copyOf(gaps);
	}

	public boolean isComplete() {
		return gaps.isEmpty();
	}
}
