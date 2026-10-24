package com.example.astrology.calculation.chart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.example.astrology.calculation.calendar.EarthlyBranch;
import com.example.astrology.calculation.calendar.HeavenlyStem;
import com.example.astrology.calculation.calendar.HourBranch;

class TracuutuviChartCalculatorTests {

	private final TracuutuviChartCalculator calculator = new TracuutuviChartCalculator(
			new TracuutuviPositionsCalculator(new FiveElementsBureauCalculator()),
			new TracuutuviMajorStarsCalculator(new StarPlacementService()),
			new TracuutuviYearStarsCalculator(new StarPlacementService()),
			new TracuutuviMonthHourStarsCalculator(new StarPlacementService()),
			new TracuutuviFourTransformationsCalculator(new StarPlacementService()),
			new TracuutuviChangShengCalculator(new StarPlacementService()));

	@Test
	void returnsStructuredCandidateChartFromACompleteLunarInput() {
		var input = new LunarChartInput(
				1999, HeavenlyStem.JI, EarthlyBranch.MAO, 5, false, 15, HourBranch.MAO, BirthSex.FEMALE);

		var chart = calculator.calculate(input);

		assertEquals(input, chart.input());
		assertEquals(EarthlyBranch.MAO, chart.positions().mingPalace());
		assertEquals(EarthlyBranch.DAU, chart.positions().thanPalace());
		assertEquals(FiveElementsBureau.FIRE_SIX, chart.positions().bureau());
		assertEquals(14, majorStarCount(chart.stars()));
		assertTrue(chart.stars().branchOf(StarId.HOA_LOC).isPresent());
		assertTrue(chart.stars().branchOf(StarId.TRUONG_SINH).isPresent());
		assertFalse(chart.fullyVerified());
		assertEquals("tracuutuvi-public-rules-2026-10-24+quanshu-supplement-1", chart.ruleSetVersion());
	}

	private int majorStarCount(StarChart chart) {
		int count = 0;
		for (var starsAtBranch : chart.placements().values()) {
			for (StarId star : starsAtBranch) {
				if (switch (star) {
					case TU_VI, THIEN_CO, THAI_DUONG, VU_KHUC, THIEN_DONG, LIEM_TRINH,
							THIEN_PHU, THAI_AM, THAM_LANG, CU_MON, THIEN_TUONG, THIEN_LUONG,
							THAT_SAT, PHA_QUAN -> true;
					default -> false;
				}) {
					count++;
				}
			}
		}
		return count;
	}
}
