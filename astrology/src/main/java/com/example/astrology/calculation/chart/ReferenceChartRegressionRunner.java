package com.example.astrology.calculation.chart;

import java.util.Objects;

import com.example.astrology.calculation.calendar.NormalizedBirthInput;

public class ReferenceChartRegressionRunner {

	private final ReferenceChartComparator comparator;

	public ReferenceChartRegressionRunner(ReferenceChartComparator comparator) {
		this.comparator = Objects.requireNonNull(comparator, "Bộ so sánh không được để trống.");
	}

	public ChartComparisonResult compare(ReferenceChartFixture fixture, ChartSnapshot actual) {
		Objects.requireNonNull(fixture, "Fixture không được để trống.");
		if (fixture.status() != ReferenceFixtureStatus.READY_FOR_COMPARISON) {
			throw new IllegalStateException("Fixture chưa có dữ liệu tham chiếu đã xác minh: "
					+ fixture.fixtureId() + ".");
		}
		return comparator.compare(fixture.fixtureId(), fixture.expected(), actual);
	}

	public ChartComparisonResult compare(
			ReferenceChartFixture fixture,
			ChartSnapshot actual,
			NormalizedBirthInput actualNormalization) {
		return comparator.compare(fixture, actual, actualNormalization);
	}
}
