package com.example.astrology.calculation.chart;

import org.springframework.stereotype.Service;

@Service
public class TracuutuviChartCalculator {

	private final TracuutuviPositionsCalculator positionsCalculator;
	private final TracuutuviMajorStarsCalculator majorStarsCalculator;
	private final TracuutuviYearStarsCalculator yearStarsCalculator;
	private final TracuutuviMonthHourStarsCalculator monthHourStarsCalculator;
	private final TracuutuviFourTransformationsCalculator transformationsCalculator;
	private final TracuutuviChangShengCalculator changShengCalculator;
	private final StarRuleSet ruleSet = StarRuleSet.tracuutuviPublicCandidate();

	public TracuutuviChartCalculator(
			TracuutuviPositionsCalculator positionsCalculator,
			TracuutuviMajorStarsCalculator majorStarsCalculator,
			TracuutuviYearStarsCalculator yearStarsCalculator,
			TracuutuviMonthHourStarsCalculator monthHourStarsCalculator,
			TracuutuviFourTransformationsCalculator transformationsCalculator,
			TracuutuviChangShengCalculator changShengCalculator) {
		this.positionsCalculator = positionsCalculator;
		this.majorStarsCalculator = majorStarsCalculator;
		this.yearStarsCalculator = yearStarsCalculator;
		this.monthHourStarsCalculator = monthHourStarsCalculator;
		this.transformationsCalculator = transformationsCalculator;
		this.changShengCalculator = changShengCalculator;
	}

	public TracuutuviChart calculate(LunarChartInput input) {
		AstrologyPositions positions = positionsCalculator.calculate(input);
		StarChart stars = majorStarsCalculator.calculate(input.lunarDay(), positions.bureau());
		stars = monthHourStarsCalculator.calculate(stars, input);
		stars = yearStarsCalculator.calculate(stars, input);
		stars = changShengCalculator.calculate(stars, positions, input);
		stars = transformationsCalculator.calculate(stars, input.yearStem());
		return new TracuutuviChart(input, positions, stars, ruleSet);
	}
}
