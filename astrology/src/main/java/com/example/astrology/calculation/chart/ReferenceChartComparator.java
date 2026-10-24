package com.example.astrology.calculation.chart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.example.astrology.calculation.calendar.NormalizedBirthInput;

public class ReferenceChartComparator {

	public ChartComparisonResult compare(
			ReferenceChartFixture fixture,
			ChartSnapshot actual,
			NormalizedBirthInput actualNormalization) {
		Objects.requireNonNull(fixture, "Fixture không được để trống.");
		if (fixture.status() != ReferenceFixtureStatus.READY_FOR_COMPARISON) {
			throw new IllegalStateException("Fixture chưa có dữ liệu tham chiếu đã xác minh: "
					+ fixture.fixtureId() + ".");
		}

		ChartComparisonResult chartResult = compare(fixture.fixtureId(), fixture.expected(), actual);
		List<ChartMismatch> mismatches = new ArrayList<>(chartResult.mismatches());
		NormalizedInputSnapshot expected = fixture.expectedNormalization();
		NormalizedInputSnapshot observed = NormalizedInputSnapshot.from(
				Objects.requireNonNull(actualNormalization, "Chuẩn hóa engine không được để trống."));
		int normalizationFields = compareNormalizedInput(expected, observed, mismatches);
		return new ChartComparisonResult(
				fixture.fixtureId(),
				chartResult.comparedFields() + normalizationFields,
				mismatches);
	}

	public ChartComparisonResult compare(
			String fixtureId,
			ChartSnapshot expected,
			ChartSnapshot actual) {
		Objects.requireNonNull(expected, "Dữ liệu tham chiếu không được để trống.");
		Objects.requireNonNull(actual, "Dữ liệu engine không được để trống.");
		if (expected.coveredSections().isEmpty()) {
			throw new IllegalArgumentException("Fixture phải khai báo ít nhất một phần dữ liệu đối chiếu.");
		}

		List<ChartMismatch> mismatches = new ArrayList<>();
		int comparedFields = 0;
		for (ChartSection section : expected.coveredSections()) {
			switch (section) {
				case CAN_CHI -> {
					comparedFields += compareMap("canChi", expected.canChi(), actual.canChi(),
							MismatchCategory.CAN_CHI, mismatches);
				}
				case NATAL_ELEMENT -> {
					comparedFields++;
					addIfDifferent("natalElement", expected.natalElement(), actual.natalElement(),
							MismatchCategory.NATAL_ELEMENT, mismatches);
				}
				case FIVE_ELEMENTS_BUREAU -> {
					comparedFields++;
					addIfDifferent("fiveElementsBureau", expected.fiveElementsBureau(),
							actual.fiveElementsBureau(), MismatchCategory.RULE_DATA, mismatches);
				}
				case MING_PALACE -> {
					comparedFields++;
					addIfDifferent("mingPalace", expected.mingPalace(), actual.mingPalace(),
							MismatchCategory.PALACE_PLACEMENT, mismatches);
				}
				case THAN_PALACE -> {
					comparedFields++;
					addIfDifferent("thanPalace", expected.thanPalace(), actual.thanPalace(),
							MismatchCategory.PALACE_PLACEMENT, mismatches);
				}
				case TWELVE_PALACES -> {
					comparedFields += compareMap("twelvePalaces", expected.twelvePalaces(),
							actual.twelvePalaces(), MismatchCategory.PALACE_PLACEMENT, mismatches);
				}
				case STARS -> {
					comparedFields += compareMap("stars", expected.stars(), actual.stars(),
							MismatchCategory.STAR_PLACEMENT, mismatches);
				}
				case STAR_BRIGHTNESS -> {
					comparedFields += compareMap("starBrightness", expected.starBrightness(),
							actual.starBrightness(), MismatchCategory.RULE_DATA, mismatches);
				}
				case RULING_STARS -> {
					comparedFields += compareMap("rulingStars", expected.rulingStars(),
							actual.rulingStars(), MismatchCategory.RULE_DATA, mismatches);
				}
			}
		}
		return new ChartComparisonResult(fixtureId, comparedFields, mismatches);
	}

	private int compareNormalizedInput(
			NormalizedInputSnapshot expected,
			NormalizedInputSnapshot actual,
			List<ChartMismatch> mismatches) {
		int fields = 0;
		fields += compareNormalizationField("normalized.birthDate", expected.birthDate(), actual.birthDate(),
				MismatchCategory.CALENDAR, mismatches);
		fields += compareNormalizationField("normalized.timeZoneId", expected.timeZoneId(), actual.timeZoneId(),
				MismatchCategory.CALENDAR, mismatches);
		fields += compareNormalizationField("normalized.resolvedUtcOffset",
				expected.resolvedUtcOffset(), actual.resolvedUtcOffset(), MismatchCategory.CALENDAR, mismatches);
		fields += compareNormalizationField("normalized.localBirthTime",
				expected.localBirthTime(), actual.localBirthTime(),
				MismatchCategory.DAY_BOUNDARY_CONVENTION, mismatches);
		fields += compareNormalizationField("normalized.hourBranch",
				expected.hourBranch(), actual.hourBranch(),
				MismatchCategory.DAY_BOUNDARY_CONVENTION, mismatches);
		fields += compareNormalizationField("normalized.cycleDateAdjustment",
				expected.cycleDateAdjustment(), actual.cycleDateAdjustment(),
				MismatchCategory.DAY_BOUNDARY_CONVENTION, mismatches);
		return fields;
	}

	private int compareNormalizationField(
			String field,
			Object expected,
			Object actual,
			MismatchCategory category,
			List<ChartMismatch> mismatches) {
		addIfDifferent(field, expected, actual, category, mismatches);
		return 1;
	}

	private <K> int compareMap(
			String prefix,
			Map<K, ?> expected,
			Map<K, ?> actual,
			MismatchCategory category,
			List<ChartMismatch> mismatches) {
		Set<K> keys = new java.util.LinkedHashSet<>(expected.keySet());
		keys.addAll(actual.keySet());
		for (K key : keys) {
			addIfDifferent(prefix + "." + key, expected.get(key), actual.get(key), category, mismatches);
		}
		return keys.size();
	}

	private void addIfDifferent(
			String field,
			Object expected,
			Object actual,
			MismatchCategory category,
			List<ChartMismatch> mismatches) {
		if (!Objects.equals(expected, actual)) {
			mismatches.add(new ChartMismatch(field, category, expected, actual));
		}
	}
}
