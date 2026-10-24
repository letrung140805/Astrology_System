package com.example.astrology.calculation.chart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.example.astrology.calculation.calendar.EarthlyBranch;
import com.example.astrology.calculation.calendar.HeavenlyStem;
import com.example.astrology.calculation.calendar.HourBranch;

class TracuutuviYearStarsCalculatorTests {

	private final TracuutuviYearStarsCalculator calculator =
			new TracuutuviYearStarsCalculator(new StarPlacementService());

	@Test
	void placesLocTonByYearStemAndTheTwoTwelveStarCycles() {
		var input = input(HeavenlyStem.JI, EarthlyBranch.MAO, BirthSex.FEMALE);

		var chart = calculator.calculate(StarChart.empty(), input);

		assertEquals(EarthlyBranch.NGO, chart.branchOf(StarId.LOC_TON).orElseThrow());
		assertEquals(EarthlyBranch.NGO, chart.branchOf(StarId.BAC_SI).orElseThrow());
		assertEquals(EarthlyBranch.MUI, chart.branchOf(StarId.LUC_SI).orElseThrow());
		assertEquals(EarthlyBranch.MAO, chart.branchOf(StarId.THAI_TUE).orElseThrow());
		assertEquals(EarthlyBranch.THIN, chart.branchOf(StarId.THIEU_DUONG).orElseThrow());
		assertEquals(EarthlyBranch.THIN, chart.branchOf(StarId.THIEN_KHONG).orElseThrow());
	}

	@Test
	void reversesLocTonCycleForYinMaleAndYangFemale() {
		var yinMale = calculator.calculate(
				StarChart.empty(), input(HeavenlyStem.JI, EarthlyBranch.MAO, BirthSex.MALE));
		var yangFemale = calculator.calculate(
				StarChart.empty(), input(HeavenlyStem.JIA, EarthlyBranch.TY, BirthSex.FEMALE));

		assertEquals(EarthlyBranch.TI, yinMale.branchOf(StarId.LUC_SI).orElseThrow());
		assertEquals(EarthlyBranch.SUU, yangFemale.branchOf(StarId.LUC_SI).orElseThrow());
	}

	@Test
	void locTonTableCoversAllTenStems() {
		HeavenlyStem[] stems = HeavenlyStem.values();
		EarthlyBranch[] expected = {
				EarthlyBranch.DAN, EarthlyBranch.MAO, EarthlyBranch.TI, EarthlyBranch.NGO,
				EarthlyBranch.TI, EarthlyBranch.NGO, EarthlyBranch.THAN, EarthlyBranch.DAU,
				EarthlyBranch.HOI, EarthlyBranch.TY
		};
		for (int index = 0; index < stems.length; index++) {
			var chart = calculator.calculate(StarChart.empty(),
					input(stems[index], EarthlyBranch.TY, BirthSex.MALE));
			assertEquals(expected[index], chart.branchOf(StarId.LOC_TON).orElseThrow());
		}
	}

	@Test
	void emitsAllTwelveThaisTueStarsWithNoMissingPlacements() {
		var chart = calculator.calculate(
				StarChart.empty(), input(HeavenlyStem.JIA, EarthlyBranch.TY, BirthSex.MALE));

		assertTrue(chart.branchOf(StarId.TRUC_PHU).isPresent());
		assertEquals(12, chart.placements().values().stream()
				.mapToLong(stars -> stars.stream().filter(TracuutuviYearStarsCalculatorTests::isThaiTueStar).count())
				.sum());
	}

	private static boolean isThaiTueStar(StarId star) {
		return switch (star) {
			case THAI_TUE, THIEU_DUONG, TANG_MON, THIEU_AM, QUAN_PHU_THAI_TUE, TU_PHU, TUE_PHA,
					LONG_DUC, BACH_HO, PHUC_DUC_STAR, DIEU_KHACH, TRUC_PHU -> true;
			default -> false;
		};
	}

	private LunarChartInput input(HeavenlyStem stem, EarthlyBranch branch, BirthSex sex) {
		return new LunarChartInput(2000, stem, branch, 1, false, 1, HourBranch.ZI, sex);
	}
}
