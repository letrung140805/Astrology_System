package com.example.astrology.calculation.chart;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.astrology.calculation.calendar.EarthlyBranch;
import com.example.astrology.calculation.calendar.HeavenlyStem;

class FiveElementsBureauCalculatorTests {

	private final FiveElementsBureauCalculator calculator = new FiveElementsBureauCalculator();
	private static final List<EarthlyBranch> BRANCH_GROUPS = List.of(
			EarthlyBranch.TY, EarthlyBranch.DAN, EarthlyBranch.THIN,
			EarthlyBranch.NGO, EarthlyBranch.THAN, EarthlyBranch.TUAT);

	@Test
	void matchesThePublishedJiMaoMingAtDanExample() {
		assertEquals(FiveElementsBureau.FIRE_SIX,
				calculator.calculate(HeavenlyStem.JI, EarthlyBranch.DAN));
	}

	@Test
	void resolvesEveryStemAndPublishedBranchPairAgainstTheSourceTable() {
		List<List<FiveElementsBureau>> expected = List.of(
				row(FiveElementsBureau.WATER_TWO, FiveElementsBureau.FIRE_SIX,
						FiveElementsBureau.WOOD_THREE, FiveElementsBureau.EARTH_FIVE,
						FiveElementsBureau.METAL_FOUR, FiveElementsBureau.FIRE_SIX),
				row(FiveElementsBureau.FIRE_SIX, FiveElementsBureau.EARTH_FIVE,
						FiveElementsBureau.METAL_FOUR, FiveElementsBureau.WOOD_THREE,
						FiveElementsBureau.WATER_TWO, FiveElementsBureau.EARTH_FIVE),
				row(FiveElementsBureau.EARTH_FIVE, FiveElementsBureau.WOOD_THREE,
						FiveElementsBureau.WATER_TWO, FiveElementsBureau.METAL_FOUR,
						FiveElementsBureau.FIRE_SIX, FiveElementsBureau.WOOD_THREE),
				row(FiveElementsBureau.WOOD_THREE, FiveElementsBureau.METAL_FOUR,
						FiveElementsBureau.FIRE_SIX, FiveElementsBureau.WATER_TWO,
						FiveElementsBureau.EARTH_FIVE, FiveElementsBureau.METAL_FOUR),
				row(FiveElementsBureau.METAL_FOUR, FiveElementsBureau.WATER_TWO,
						FiveElementsBureau.EARTH_FIVE, FiveElementsBureau.FIRE_SIX,
						FiveElementsBureau.WOOD_THREE, FiveElementsBureau.WATER_TWO));
		List<List<HeavenlyStem>> stemGroups = List.of(
				List.of(HeavenlyStem.JIA, HeavenlyStem.JI),
				List.of(HeavenlyStem.YI, HeavenlyStem.GENG),
				List.of(HeavenlyStem.BING, HeavenlyStem.XIN),
				List.of(HeavenlyStem.DING, HeavenlyStem.REN),
				List.of(HeavenlyStem.WU, HeavenlyStem.GUI));

		for (int row = 0; row < stemGroups.size(); row++) {
			for (HeavenlyStem stem : stemGroups.get(row)) {
				for (int column = 0; column < BRANCH_GROUPS.size(); column++) {
					assertEquals(expected.get(row).get(column),
							calculator.calculate(stem, BRANCH_GROUPS.get(column)));
				}
			}
		}
	}

	private List<FiveElementsBureau> row(FiveElementsBureau... values) {
		return List.of(values);
	}
}
