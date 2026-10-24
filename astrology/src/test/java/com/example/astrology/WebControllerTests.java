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
import com.example.astrology.calculation.calendar.HoNgocDucLunarCalendarProvider;
import com.example.astrology.calculation.calendar.NayinElementCalculator;
import com.example.astrology.calculation.calendar.SexagenaryYearCalculator;
import com.example.astrology.calculation.chart.FiveElementsBureauCalculator;
import com.example.astrology.calculation.chart.StarPlacementService;
import com.example.astrology.calculation.chart.TracuutuviChangShengCalculator;
import com.example.astrology.calculation.chart.TracuutuviChartCalculator;
import com.example.astrology.calculation.chart.TracuutuviFourTransformationsCalculator;
import com.example.astrology.calculation.chart.TracuutuviMajorStarsCalculator;
import com.example.astrology.calculation.chart.TracuutuviMonthHourStarsCalculator;
import com.example.astrology.calculation.chart.TracuutuviPositionsCalculator;
import com.example.astrology.calculation.chart.TracuutuviYearStarsCalculator;
import com.example.astrology.controller.WebController;
import com.example.astrology.model.ChartPalaceDisplay;
import com.example.astrology.service.BirthChartApplicationService;

class WebControllerTests {

	private final WebController controller = new WebController(new BirthChartApplicationService(
			new AstrologyCalculationService(new BirthInputNormalizationService()),
			new TracuutuviChartCalculator(
					new TracuutuviPositionsCalculator(new FiveElementsBureauCalculator()),
					new TracuutuviMajorStarsCalculator(new StarPlacementService()),
					new TracuutuviYearStarsCalculator(new StarPlacementService()),
					new TracuutuviMonthHourStarsCalculator(new StarPlacementService()),
					new TracuutuviFourTransformationsCalculator(new StarPlacementService()),
					new TracuutuviChangShengCalculator(new StarPlacementService())),
			new HoNgocDucLunarCalendarProvider(),
			new SexagenaryYearCalculator(),
			new NayinElementCalculator(new SexagenaryYearCalculator())));

	@Test
	void calculatesChartFromBasicSolarBirthInputWithoutManualLunarFields() {
		var model = new ExtendedModelMap();

		String view = controller.result(
				"Lan", "Nữ", LocalDate.of(1999, 6, 12), LocalTime.of(10, 30),
				"Hà Nội", model);

		assertEquals("result", view);
		assertEquals(EarthlyBranch.TI, ((com.example.astrology.calculation.calendar.HourBranch)
				model.get("hourBranch")).earthlyBranch());
		assertNotNull(model.get("lunarDate"));
		assertNotNull(model.get("yearCanChi"));
		assertNotNull(model.get("yearNayinElement"));
		assertNotNull(model.get("chart"));
		assertFalse(((java.util.List<?>) model.get("remainingGaps")).isEmpty());
	}

	@Test
	void displaysStructuredPalacesAndStarsFromBasicBirthInput() {
		var model = new ExtendedModelMap();

		controller.result(
				"Lan", "Nữ", LocalDate.of(1999, 6, 12), LocalTime.of(5, 30),
				"Hà Nội", model);

		assertNotNull(model.get("chart"));
		var palaces = (java.util.List<?>) model.get("chartPalaces");
		assertEquals(12, palaces.size());
		assertTrue(palaces.stream().anyMatch(palace ->
				((ChartPalaceDisplay) palace).function()
						.vietnameseName().equals("Mệnh")));
		assertEquals(3, ((java.util.List<?>) model.get("chartLimitations")).size());
		assertFalse(((java.util.List<?>) model.get("remainingGaps")).contains(CalculationGap.MENH_AND_CUC));
	}

	@Test
	void convertsLunarDateEvenWhenBirthTimeIsUnknown() {
		var model = new ExtendedModelMap();

		controller.result(
				"Lan", "Nữ", LocalDate.of(1999, 6, 12), null,
				"Hà Nội", model);

		assertNotNull(model.get("lunarDate"));
	}

	@Test
	void requiresBirthTimeForChartPlacement() {
		var model = new ExtendedModelMap();

		controller.result(
				"Lan", "Nữ", LocalDate.of(1999, 6, 12), null,
				"Hà Nội", model);

		assertNull(model.get("chart"));
		assertTrue(((String) model.get("chartInputMessage")).contains("Cần có giờ sinh"));
	}

	@Test
	void reportsWhenGregorianDateIsOutsideCalendarSupportRange() {
		var model = new ExtendedModelMap();

		controller.result(
				"Lan", "Nữ", LocalDate.of(1700, 6, 12), LocalTime.of(5, 30),
				"Hà Nội", model);

		assertNull(model.get("chart"));
		assertTrue(((String) model.get("chartInputMessage")).contains("1800 đến 2199"));
	}
}
