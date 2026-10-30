package com.example.astrology.calculation.chart;

import java.util.List;
import java.util.OptionalDouble;

public record ChartComparisonResult(
		String fixtureId,
		int comparedFields,
		List<ChartMismatch> mismatches) {

	public ChartComparisonResult {
		if (fixtureId == null || fixtureId.isBlank()) {
			throw new IllegalArgumentException("Mã fixture không được để trống.");
		}
		if (comparedFields < 0) {
			throw new IllegalArgumentException("Số trường đối chiếu không thể âm.");
		}
		mismatches = mismatches.stream().sorted().toList();
		if (mismatches.size() > comparedFields) {
			throw new IllegalArgumentException("Số sai khác không thể lớn hơn số trường đối chiếu.");
		}
	}

	public int matchingFields() {
		return comparedFields - mismatches.size();
	}

	public OptionalDouble matchRate() {
		if (comparedFields == 0) {
			return OptionalDouble.empty();
		}
		return OptionalDouble.of((double) matchingFields() / comparedFields);
	}

	public boolean matches() {
		return comparedFields > 0 && mismatches.isEmpty();
	}
}
