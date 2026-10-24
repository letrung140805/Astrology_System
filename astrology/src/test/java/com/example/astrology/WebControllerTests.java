package com.example.astrology;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import com.example.astrology.calculation.AstrologyCalculationService;
import com.example.astrology.calculation.CalculationGap;
import com.example.astrology.calculation.calendar.BirthInputNormalizationService;
import com.example.astrology.calculation.calendar.EarthlyBranch;
import com.example.astrology.calculation.calendar.HeavenlyStem;
import com.example.astrology.calculation.chart.FiveElementsBureauCalculator;
import com.example.astrology.calculation.chart.StarPlacementService;
import com.example.astrology.calculation.chart.TracuutuviChangShengCalculator;
import com.example.astrology.calculation.chart.TracuutuviChartCalculator;
import com.example.astrology.calculation.chart.TracuutuviFourTransformationsCalculator;
import com.example.astrology.calculation.chart.TracuutuviMajorStarsCalculator;
import com.example.astrology.calculation.chart.TracuutuviMonthHourStarsCalculator;
import com.example.astrology.calculation.chart.TracuutuviPositionsCalculator;
import com.example.astrology.calculation.chart.TracuutuviYearStarsCalculator;

class WebControllerTests {

	private final WebController controller = new WebController(
			new AstrologyCalculationService(new BirthInputNormalizationService()),
			new TracuutuviChartCalculator(
					new TracuutuviPositionsCalculator(new FiveElementsBureauCalculator()),
					new TracuutuviMajorStarsCalculator(new StarPlacementService()),
					new TracuutuviYearStarsCalculator(new StarPlacementService()),
					new TracuutuviMonthHourStarsCalculator(new StarPlacementService()),
					new TracuutuviFourTransformationsCalculator(new StarPlacementService()),
					new TracuutuviChangShengCalculator(new StarPlacementService())));

	@Test
	void showsCalculatedHourBranchEvenWhenNoLunarChartDataWasProvided() {
		var model = new ExtendedModelMap();

		String view = controller.result(
				"Lan", "Nữ", LocalDate.of(1999, 6, 12), LocalTime.of(10, 30),
				"Hà Nội", null, null, null, null, null, null, model);

		assertEquals("result", view);
		assertEquals(EarthlyBranch.TI, ((com.example.astrology.calculation.calendar.HourBranch)
				model.get("hourBranch")).earthlyBranch());
		assertNull(model.get("chart"));
		assertFalse(((java.util.List<?>) model.get("remainingGaps")).isEmpty());
	}

	@Test
	void displaysStructuredPalacesAndStarsWhenCompleteManualLunarInputsAreProvided() {
		var model = new ExtendedModelMap();

		controller.result(
				"Lan", "Nữ", LocalDate.of(1999, 6, 12), LocalTime.of(5, 30),
				"Hà Nội", 1999, 5, 15, false, HeavenlyStem.JI, EarthlyBranch.MAO, model);

		assertNotNull(model.get("chart"));
		var palaces = (java.util.List<?>) model.get("chartPalaces");
		assertEquals(12, palaces.size());
		assertTrue(palaces.stream().anyMatch(palace ->
				((com.example.astrology.calculation.chart.ChartPalaceDisplay) palace).function()
						.vietnameseName().equals("Mệnh")));
		assertEquals(3, ((java.util.List<?>) model.get("chartLimitations")).size());
		assertFalse(((java.util.List<?>) model.get("remainingGaps")).contains(CalculationGap.MENH_AND_CUC));
	}

	@Test
	void reportsIncompleteManualLunarInputsInsteadOfReturningAnEmptyChart() {
		var model = new ExtendedModelMap();

		controller.result(
				"Lan", "Nữ", LocalDate.of(1999, 6, 12), LocalTime.of(5, 30),
				"Hà Nội", 1999, null, null, false, null, null, model);

		assertNull(model.get("chart"));
		assertTrue(((String) model.get("chartInputMessage")).contains("nhập đủ ngày âm"));
	}

	@Test
	void requiresBirthTimeForChartPlacement() {
		var model = new ExtendedModelMap();

		controller.result(
				"Lan", "Nữ", LocalDate.of(1999, 6, 12), null,
				"Hà Nội", 1999, 5, 15, false, HeavenlyStem.JI, EarthlyBranch.MAO, model);

		assertNull(model.get("chart"));
		assertTrue(((String) model.get("chartInputMessage")).contains("Cần có giờ sinh"));
	}

	@Test
	void rejectsOutOfRangeManualLunarDates() {
		var model = new ExtendedModelMap();

		controller.result(
				"Lan", "Nữ", LocalDate.of(1999, 6, 12), LocalTime.of(5, 30),
				"Hà Nội", 1999, 13, 15, false, HeavenlyStem.JI, EarthlyBranch.MAO, model);

		assertNull(model.get("chart"));
		assertTrue(((String) model.get("chartInputMessage")).contains("tháng 1-12"));
	}
}
