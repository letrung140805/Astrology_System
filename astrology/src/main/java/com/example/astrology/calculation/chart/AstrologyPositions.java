package com.example.astrology.calculation.chart;

import java.util.Objects;

import com.example.astrology.calculation.calendar.EarthlyBranch;

public record AstrologyPositions(
		EarthlyBranch mingPalace,
		EarthlyBranch thanPalace,
		PalaceLayout palaces,
		FiveElementsBureau bureau) {

	public AstrologyPositions {
		Objects.requireNonNull(mingPalace, "Cung Mệnh không được để trống.");
		Objects.requireNonNull(thanPalace, "Cung Thân không được để trống.");
		Objects.requireNonNull(palaces, "Bố cục cung không được để trống.");
		Objects.requireNonNull(bureau, "Ngũ hành Cục không được để trống.");
		if (palaces.positions().get(PalaceFunction.MENH) != mingPalace) {
			throw new IllegalArgumentException("Vị trí cung Mệnh không khớp với bố cục cung.");
		}
	}
}
