package com.example.astrology.model;

import java.util.List;
import java.util.Objects;

import com.example.astrology.calculation.calendar.EarthlyBranch;
import com.example.astrology.calculation.chart.PalaceFunction;

public record ChartPalaceDisplay(
		PalaceFunction function,
		EarthlyBranch branch,
		List<ChartStarDisplay> stars) {

	public ChartPalaceDisplay {
		Objects.requireNonNull(function, "Cung chức không được để trống.");
		Objects.requireNonNull(branch, "Địa chi không được để trống.");
		stars = List.copyOf(Objects.requireNonNull(stars, "Danh sách sao không được để trống."));
	}
}
