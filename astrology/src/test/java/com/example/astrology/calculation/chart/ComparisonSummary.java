package com.example.astrology.calculation.chart;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

public record ComparisonSummary(
		int fixtureCount,
		int comparedFields,
		int matchingFields,
		Map<MismatchCategory, Integer> mismatchCounts) {

	public ComparisonSummary {
		if (fixtureCount < 0 || comparedFields < 0 || matchingFields < 0 || matchingFields > comparedFields) {
			throw new IllegalArgumentException("Các số liệu tổng hợp đối chiếu không hợp lệ.");
		}
		var counts = new EnumMap<MismatchCategory, Integer>(MismatchCategory.class);
		counts.putAll(mismatchCounts);
		mismatchCounts = Map.copyOf(counts);
	}

	public static ComparisonSummary from(List<ChartComparisonResult> results) {
		var mismatchCounts = new EnumMap<MismatchCategory, Integer>(MismatchCategory.class);
		int fields = 0;
		int matched = 0;
		for (ChartComparisonResult result : results) {
			fields += result.comparedFields();
			matched += result.matchingFields();
			for (ChartMismatch mismatch : result.mismatches()) {
				mismatchCounts.put(mismatch.category(),
						mismatchCounts.getOrDefault(mismatch.category(), 0) + 1);
			}
		}
		return new ComparisonSummary(results.size(), fields, matched, mismatchCounts);
	}

	public OptionalDouble matchRate() {
		return comparedFields == 0
				? OptionalDouble.empty()
				: OptionalDouble.of((double) matchingFields / comparedFields);
	}
}
