package com.example.astrology.calculation.chart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import com.example.astrology.calculation.calendar.EarthlyBranch;
import com.example.astrology.calculation.calendar.HeavenlyStem;
import com.example.astrology.calculation.calendar.HourBranch;

class TracuutuviPositionsCalculatorTests {

	private final TracuutuviPositionsCalculator calculator =
			new TracuutuviPositionsCalculator(new FiveElementsBureauCalculator());

	@Test
	void placesMingAndThanForPublishedMonthFiveMaoHourExample() {
		var input = input(5, false, HourBranch.MAO, HeavenlyStem.JI, EarthlyBranch.MAO);

		var positions = calculator.calculate(input);

		assertEquals(EarthlyBranch.MAO, positions.mingPalace());
		assertEquals(EarthlyBranch.DAU, positions.thanPalace());
		assertEquals(FiveElementsBureau.FIRE_SIX, positions.bureau());
	}

	@Test
	void generatesTwelveDistinctNamedPalacesAndUsesSupplementalReverseDirection() {
		var positions = calculator.calculate(
				input(5, false, HourBranch.MAO, HeavenlyStem.JI, EarthlyBranch.MAO));

		assertEquals(12, positions.palaces().positions().size());
		assertEquals(EarthlyBranch.MAO, positions.palaces().positions().get(PalaceFunction.MENH));
		assertEquals(EarthlyBranch.DAN, positions.palaces().positions().get(PalaceFunction.HUYNH_DE));
		assertNotEquals(
				positions.palaces().positions().get(PalaceFunction.HUYNH_DE),
				positions.palaces().positions().get(PalaceFunction.PHU_MAU));
	}

	@Test
	void appliesSupplementalLeapMonthRuleByCountingLeapMonthAsFollowingMonth() {
		var regular = calculator.calculate(
				input(4, false, HourBranch.ZI, HeavenlyStem.JIA, EarthlyBranch.TY));
		var leap = calculator.calculate(
				input(4, true, HourBranch.ZI, HeavenlyStem.JIA, EarthlyBranch.TY));

		assertEquals(EarthlyBranch.TI, regular.mingPalace());
		assertEquals(EarthlyBranch.NGO, leap.mingPalace());
	}

	@Test
	void everyMonthAndHourCombinationProducesAValidTwelvePalaceBijection() {
		for (int month = 1; month <= 12; month++) {
			for (HourBranch hour : HourBranch.values()) {
				var positions = calculator.calculate(
						input(month, false, hour, HeavenlyStem.JIA, EarthlyBranch.TY));
				assertEquals(12, positions.palaces().positions().values().stream().distinct().count());
				assertEquals(positions.mingPalace(),
						positions.palaces().positions().get(PalaceFunction.MENH));
			}
		}
	}

	private LunarChartInput input(
			int month,
			boolean leap,
			HourBranch hour,
			HeavenlyStem stem,
			EarthlyBranch yearBranch) {
		return new LunarChartInput(1999, stem, yearBranch, month, leap, 1, hour, BirthSex.MALE);
	}
}
