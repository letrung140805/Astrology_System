package com.example.astrology.calculation.chart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.astrology.calculation.calendar.EarthlyBranch;
import com.example.astrology.calculation.calendar.HeavenlyStem;
import com.example.astrology.calculation.calendar.HourBranch;

class TracuutuviChangShengCalculatorTests {

	private final TracuutuviChangShengCalculator calculator =
			new TracuutuviChangShengCalculator(new StarPlacementService());
	private final List<StarId> cycle = List.of(
			StarId.TRUONG_SINH, StarId.MOC_DUC, StarId.QUAN_DOI, StarId.LAM_QUAN,
			StarId.DE_VUONG, StarId.SUY, StarId.BENH, StarId.TU, StarId.MO,
			StarId.TUYET, StarId.THAI, StarId.DUONG);

	@Test
	void startsEachBureauAtItsPublishedChangShengBranch() {
		EarthlyBranch[] expectedStarts = {
				EarthlyBranch.THAN, EarthlyBranch.HOI, EarthlyBranch.TI,
				EarthlyBranch.THAN, EarthlyBranch.DAN
		};
		FiveElementsBureau[] bureaus = FiveElementsBureau.values();
		for (int index = 0; index < bureaus.length; index++) {
			var chart = calculator.calculate(
					StarChart.empty(),
					positions(bureaus[index]),
					input(HeavenlyStem.JIA, BirthSex.MALE));
			assertEquals(expectedStarts[index], chart.branchOf(StarId.TRUONG_SINH).orElseThrow());
		}
	}

	@Test
	void usesDirectionFromYearPolarityAndSex() {
		var yangMale = calculate(HeavenlyStem.JIA, BirthSex.MALE);
		var yinFemale = calculate(HeavenlyStem.YI, BirthSex.FEMALE);
		var yinMale = calculate(HeavenlyStem.YI, BirthSex.MALE);
		var yangFemale = calculate(HeavenlyStem.JIA, BirthSex.FEMALE);

		assertEquals(EarthlyBranch.TY, yangMale.branchOf(StarId.MOC_DUC).orElseThrow());
		assertEquals(EarthlyBranch.TY, yinFemale.branchOf(StarId.MOC_DUC).orElseThrow());
		assertEquals(EarthlyBranch.TUAT, yinMale.branchOf(StarId.MOC_DUC).orElseThrow());
		assertEquals(EarthlyBranch.TUAT, yangFemale.branchOf(StarId.MOC_DUC).orElseThrow());
	}

	@Test
	void assignsAllTwelveDistinctStages() {
		var chart = calculate(HeavenlyStem.JIA, BirthSex.MALE);
		for (StarId star : cycle) {
			assertTrue(chart.branchOf(star).isPresent());
		}
		java.util.Set<EarthlyBranch> branches = new java.util.HashSet<>();
		for (StarId star : cycle) {
			branches.add(chart.branchOf(star).orElseThrow());
		}
		assertEquals(12, branches.size());
	}

	private StarChart calculate(HeavenlyStem stem, BirthSex sex) {
		return calculator.calculate(StarChart.empty(), positions(FiveElementsBureau.WOOD_THREE), input(stem, sex));
	}

	private AstrologyPositions positions(FiveElementsBureau bureau) {
		var layout = new TracuutuviPositionsCalculator(new FiveElementsBureauCalculator())
				.calculate(input(HeavenlyStem.JIA, BirthSex.MALE));
		return new AstrologyPositions(
				layout.mingPalace(), layout.thanPalace(), layout.palaces(), bureau);
	}

	private LunarChartInput input(HeavenlyStem stem, BirthSex sex) {
		return new LunarChartInput(2000, stem, EarthlyBranch.TY,
				1, false, 1, HourBranch.ZI, sex);
	}
}
