package com.example.astrology.calculation.chart;

import java.util.Objects;

import com.example.astrology.calculation.calendar.EarthlyBranch;

public record CanChiValue(String heavenlyStem, EarthlyBranch earthlyBranch) {

	public CanChiValue {
		if (heavenlyStem == null || heavenlyStem.isBlank()) {
			throw new IllegalArgumentException("Thiên can không được để trống.");
		}
		Objects.requireNonNull(earthlyBranch, "Địa chi không được để trống.");
	}
}
