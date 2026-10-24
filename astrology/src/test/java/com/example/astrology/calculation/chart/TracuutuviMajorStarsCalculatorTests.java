package com.example.astrology.calculation.chart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.example.astrology.calculation.calendar.EarthlyBranch;

class TracuutuviMajorStarsCalculatorTests {

	private final TracuutuviMajorStarsCalculator calculator =
			new TracuutuviMajorStarsCalculator(new StarPlacementService());

	@Test
	void placesZiweiAtThePublishedDivisibleExample() {
		var chart = calculator.calculate(15, FiveElementsBureau.WOOD_THREE);

		assertEquals(EarthlyBranch.NGO, chart.branchOf(StarId.TU_VI).orElseThrow());
		int placementCount = 0;
		for (Set<StarId> starsAtBranch : chart.placements().values()) {
			placementCount += starsAtBranch.size();
		}
		assertEquals(14, placementCount);
	}

	@Test
	void appliesPublishedRemainderBorrowAndParityAdjustment() {
		var chart = calculator.calculate(10, FiveElementsBureau.WOOD_THREE);

		assertEquals(EarthlyBranch.MUI, chart.branchOf(StarId.TU_VI).orElseThrow());
	}

	@Test
	void placesAllFourteenUniqueMajorStarsForEveryValidLunarDayAndBureau() {
		for (FiveElementsBureau bureau : FiveElementsBureau.values()) {
			for (int day = 1; day <= 30; day++) {
				var chart = calculator.calculate(day, bureau);
				var stars = EnumSet.noneOf(StarId.class);
				chart.placements().values().forEach(stars::addAll);
				assertTrue(stars.containsAll(EnumSet.range(StarId.TU_VI, StarId.PHA_QUAN)));
				assertEquals(14, Set.of(
						StarId.TU_VI, StarId.THIEN_CO, StarId.THAI_DUONG, StarId.VU_KHUC,
						StarId.THIEN_DONG, StarId.LIEM_TRINH, StarId.THIEN_PHU, StarId.THAI_AM,
						StarId.THAM_LANG, StarId.CU_MON, StarId.THIEN_TUONG, StarId.THIEN_LUONG,
						StarId.THAT_SAT, StarId.PHA_QUAN).size());
			}
		}
	}

	@Test
	void rejectsMissingOrOutOfRangeInputs() {
		assertThrows(IllegalArgumentException.class,
				() -> calculator.calculate(0, FiveElementsBureau.WATER_TWO));
		assertThrows(IllegalArgumentException.class,
				() -> calculator.calculate(31, FiveElementsBureau.WATER_TWO));
		assertThrows(IllegalArgumentException.class,
				() -> calculator.calculate(1, null));
	}
}
