package com.example.astrology.calculation.chart;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.example.astrology.calculation.calendar.EarthlyBranch;
import com.example.astrology.calculation.calendar.HeavenlyStem;
import com.example.astrology.calculation.calendar.HourBranch;

class TracuutuviAuxiliaryStarsCalculatorTests {

	private final TracuutuviMonthHourStarsCalculator calculator =
			new TracuutuviMonthHourStarsCalculator(new StarPlacementService());

	@Test
	void placesLiteraryStarsAtTheirPublishedHourAnchors() {
		var chart = calculator.calculate(StarChart.empty(), input(1, HourBranch.ZI));

		assertEquals(EarthlyBranch.TUAT, chart.branchOf(StarId.VAN_XUONG).orElseThrow());
		assertEquals(EarthlyBranch.THIN, chart.branchOf(StarId.VAN_KHUC).orElseThrow());
		assertEquals(EarthlyBranch.THIN, chart.branchOf(StarId.TA_PHU).orElseThrow());
		assertEquals(EarthlyBranch.TUAT, chart.branchOf(StarId.HUU_BAT).orElseThrow());
	}

	@Test
	void coversAllHourAndMonthInputsWithTheDocumentedAnchorDirections() {
		for (int month = 1; month <= 12; month++) {
			for (HourBranch hour : HourBranch.values()) {
				var chart = calculator.calculate(StarChart.empty(), input(month, hour));
				assertEquals(hour.ordinal() == 0 ? EarthlyBranch.TUAT
								: step(EarthlyBranch.TUAT, -hour.ordinal()),
						chart.branchOf(StarId.VAN_XUONG).orElseThrow());
				assertEquals(step(EarthlyBranch.THIN, hour.ordinal()),
						chart.branchOf(StarId.VAN_KHUC).orElseThrow());
				assertEquals(step(EarthlyBranch.THIN, month - 1),
						chart.branchOf(StarId.TA_PHU).orElseThrow());
				assertEquals(step(EarthlyBranch.TUAT, -(month - 1)),
						chart.branchOf(StarId.HUU_BAT).orElseThrow());
			}
		}
	}

	private LunarChartInput input(int month, HourBranch hour) {
		return new LunarChartInput(2000, HeavenlyStem.JIA, EarthlyBranch.TY,
				month, false, 1, hour, BirthSex.MALE);
	}

	private EarthlyBranch step(EarthlyBranch start, int amount) {
		EarthlyBranch current = start;
		int direction = Integer.signum(amount);
		for (int index = 0; index < Math.abs(amount); index++) {
			int next = current.ordinal() + direction;
			if (next < 0) {
				next = EarthlyBranch.values().length - 1;
			} else if (next == EarthlyBranch.values().length) {
				next = 0;
			}
			current = EarthlyBranch.values()[next];
		}
		return current;
	}
}
