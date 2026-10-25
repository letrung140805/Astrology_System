package com.example.astrology.calculation.chart;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record TracuutuviChart(
		LunarChartInput input,
		AstrologyPositions positions,
		StarChart stars,
		StarRuleSet ruleSet) {

	public TracuutuviChart {
		Objects.requireNonNull(input, "Dữ liệu đầu vào không được để trống.");
		Objects.requireNonNull(positions, "Vị trí cung không được để trống.");
		Objects.requireNonNull(stars, "Vị trí sao không được để trống.");
		Objects.requireNonNull(ruleSet, "Bộ quy tắc không được để trống.");
	}

	@JsonIgnore
	public String ruleSetVersion() {
		return ruleSet.version();
	}

	@JsonIgnore
	public boolean fullyVerified() {
		return ruleSet.isFullyVerified();
	}
}
