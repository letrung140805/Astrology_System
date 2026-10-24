package com.example.astrology.model;

import java.util.List;
import java.util.Objects;

import com.example.astrology.calculation.CalculationAssessment;
import com.example.astrology.calculation.CalculationGap;
import com.example.astrology.calculation.calendar.VietnameseLunarCalendarProvider;
import com.example.astrology.calculation.chart.TracuutuviChart;

public record BirthChartPageData(
		CalculationAssessment assessment,
		TracuutuviChart chart,
		String chartInputMessage,
		VietnameseLunarCalendarProvider.LunarDate lunarDate,
		String lunarCalendarName,
		String lunarCalendarVersion,
		String yearCanChi,
		String yearNayinElement,
		String yearNayinSource,
		List<ChartPalaceDisplay> chartPalaces,
		List<CalculationGap> remainingGaps,
		List<String> chartLimitations) {

	public BirthChartPageData {
		Objects.requireNonNull(assessment, "Đánh giá dữ liệu sinh không được để trống.");
		chartPalaces = List.copyOf(Objects.requireNonNull(chartPalaces, "Danh sách cung không được để trống."));
		remainingGaps = List.copyOf(Objects.requireNonNull(remainingGaps, "Danh sách khoảng trống không được để trống."));
		chartLimitations = List.copyOf(
				Objects.requireNonNull(chartLimitations, "Danh sách giới hạn không được để trống."));
	}
}
