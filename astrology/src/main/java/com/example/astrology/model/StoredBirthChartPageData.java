package com.example.astrology.model;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.example.astrology.calculation.AstrologyCalculationInput;
import com.example.astrology.calculation.CalculationGap;
import com.example.astrology.calculation.calendar.VietnameseLunarCalendarProvider;
import com.example.astrology.calculation.chart.TracuutuviChart;

public record StoredBirthChartPageData(
		UUID accessToken,
		AstrologyCalculationInput input,
		TracuutuviChart chart,
		VietnameseLunarCalendarProvider.LunarDate lunarDate,
		String lunarCalendarName,
		String lunarCalendarVersion,
		String yearCanChi,
		String yearNayinElement,
		String yearNayinSource,
		List<ChartPalaceDisplay> chartPalaces,
		List<CalculationGap> remainingGaps,
		List<String> chartLimitations) {

	public StoredBirthChartPageData {
		Objects.requireNonNull(input, "Thông tin hồ sơ không được để trống.");
		Objects.requireNonNull(chart, "Lá số đã lưu không được để trống.");
		Objects.requireNonNull(lunarDate, "Ngày âm lịch đã lưu không được để trống.");
		chartPalaces = List.copyOf(Objects.requireNonNull(chartPalaces, "Danh sách cung không được để trống."));
		remainingGaps = List.copyOf(Objects.requireNonNull(remainingGaps, "Danh sách khoảng trống không được để trống."));
		chartLimitations = List.copyOf(
				Objects.requireNonNull(chartLimitations, "Danh sách giới hạn không được để trống."));
		if (chartPalaces.size() != 12) {
			throw new IllegalArgumentException("Lá số đã lưu phải có đủ 12 cung.");
		}
	}
}
