package com.example.astrology.calculation.chart;

import java.util.Objects;

public record ChartMismatch(
		String field,
		MismatchCategory category,
		Object expected,
		Object actual) implements Comparable<ChartMismatch> {

	public ChartMismatch {
		if (field == null || field.isBlank()) {
			throw new IllegalArgumentException("Trường sai khác không được để trống.");
		}
		Objects.requireNonNull(category, "Nhóm sai khác không được để trống.");
	}

	@Override
	public int compareTo(ChartMismatch other) {
		int categoryOrder = category.compareTo(other.category);
		return categoryOrder != 0 ? categoryOrder : field.compareTo(other.field);
	}
}
