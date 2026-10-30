package com.example.astrology.calculation.chart;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.example.astrology.calculation.calendar.EarthlyBranch;

public record ChartSnapshot(
		Set<ChartSection> coveredSections,
		Map<CanChiPeriod, CanChiValue> canChi,
		String natalElement,
		String fiveElementsBureau,
		EarthlyBranch mingPalace,
		EarthlyBranch thanPalace,
		Map<PalaceFunction, EarthlyBranch> twelvePalaces,
		Map<String, EarthlyBranch> stars,
		Map<String, String> starBrightness,
		Map<RulingStar, String> rulingStars) {

	public ChartSnapshot {
		Objects.requireNonNull(coveredSections, "Các phần dữ liệu được đối chiếu không được để trống.");
		coveredSections = Collections.unmodifiableSet(
				coveredSections.isEmpty() ? EnumSet.noneOf(ChartSection.class) : EnumSet.copyOf(coveredSections));
		canChi = immutableEnumMap(canChi, CanChiPeriod.class);
		twelvePalaces = immutableEnumMap(twelvePalaces, PalaceFunction.class);
		stars = immutableStringMap(stars);
		starBrightness = immutableStringMap(starBrightness);
		rulingStars = immutableEnumMap(rulingStars, RulingStar.class);

		requireCoveredValue(ChartSection.CAN_CHI, !canChi.isEmpty(), coveredSections);
		requireCoveredValue(ChartSection.NATAL_ELEMENT, hasText(natalElement), coveredSections);
		requireCoveredValue(ChartSection.FIVE_ELEMENTS_BUREAU, hasText(fiveElementsBureau), coveredSections);
		requireCoveredValue(ChartSection.MING_PALACE, mingPalace != null, coveredSections);
		requireCoveredValue(ChartSection.THAN_PALACE, thanPalace != null, coveredSections);
		requireCoveredValue(ChartSection.TWELVE_PALACES,
				twelvePalaces.keySet().equals(EnumSet.allOf(PalaceFunction.class)), coveredSections);
		requireCoveredValue(ChartSection.STARS, !stars.isEmpty(), coveredSections);
		requireCoveredValue(ChartSection.STAR_BRIGHTNESS, !starBrightness.isEmpty(), coveredSections);
		requireCoveredValue(ChartSection.RULING_STARS,
				rulingStars.keySet().equals(EnumSet.allOf(RulingStar.class)), coveredSections);
	}

	public static ChartSnapshot empty() {
		return new ChartSnapshot(Set.of(), Map.of(), null, null, null, null,
				Map.of(), Map.of(), Map.of(), Map.of());
	}

	private static boolean hasText(String value) {
		return value != null && !value.isBlank();
	}

	private static void requireCoveredValue(
			ChartSection section,
			boolean hasValue,
			Set<ChartSection> coveredSections) {
		if (coveredSections.contains(section) && !hasValue) {
			throw new IllegalArgumentException("Thiếu dữ liệu cho phần đối chiếu " + section + ".");
		}
	}

	private static <K extends Enum<K>, V> Map<K, V> immutableEnumMap(Map<K, V> source, Class<K> keyType) {
		Objects.requireNonNull(source, "Dữ liệu snapshot không được để trống.");
		var copy = new EnumMap<K, V>(keyType);
		copy.putAll(source);
		return Collections.unmodifiableMap(copy);
	}

	private static <V> Map<String, V> immutableStringMap(Map<String, V> source) {
		Objects.requireNonNull(source, "Dữ liệu snapshot không được để trống.");
		var copy = new java.util.LinkedHashMap<String, V>();
		source.forEach((key, value) -> {
			if (key == null || key.isBlank()) {
				throw new IllegalArgumentException("Mã sao không được để trống.");
			}
			Objects.requireNonNull(value, "Giá trị sao không được để trống.");
			copy.put(key, value);
		});
		return Collections.unmodifiableMap(copy);
	}

	public enum CanChiPeriod {
		YEAR,
		MONTH,
		DAY,
		HOUR
	}

	public enum RulingStar {
		CHU_MENH,
		CHU_THAN
	}
}
