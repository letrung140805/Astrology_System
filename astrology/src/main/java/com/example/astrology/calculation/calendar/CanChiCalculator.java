package com.example.astrology.calculation.calendar;

/**
 * Not implemented: year/month/day/hour boundary conventions and independent
 * reference fixtures have not been selected and verified.
 */
public interface CanChiCalculator {

	CanChiResult calculate(NormalizedBirthInput input);

	record CanChiResult(
			String year,
			String month,
			String day,
			String hour) {
	}
}
