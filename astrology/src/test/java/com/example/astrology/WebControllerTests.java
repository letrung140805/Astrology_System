package com.example.astrology;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
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
import com.example.astrology.model.StoredBirthChartPageData;
import com.example.astrology.service.BirthChartApplicationService;
import com.example.astrology.service.BirthChartPersistenceService;

class WebControllerTests {

	private final BirthChartApplicationService calculationService = new BirthChartApplicationService(
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
			new NayinElementCalculator(new SexagenaryYearCalculator()));
	private final BirthChartPersistenceService persistenceService = mock(BirthChartPersistenceService.class);
	private final WebController controller = new WebController(calculationService, persistenceService);

	@Test
	void savesCalculatedChartAndRedirectsToItsAccessToken() {
		var model = new ExtendedModelMap();
		UUID token = UUID.randomUUID();
		when(persistenceService.save(any(), any(), isNull())).thenReturn(token);

		String view = controller.result(
				"Lan", "Nữ", LocalDate.of(1999, 6, 12), LocalTime.of(10, 30),
				"Hà Nội", null, new MockHttpSession(), model);

		assertEquals("redirect:/result/" + token, view);
		verify(persistenceService).save(any(), any(), isNull());
	}

	@Test
	void usesTheRequestPrincipalAsTheOwnerAndDoesNotAcceptAnOwnerParameter() {
		UUID token = UUID.randomUUID();
		when(persistenceService.save(any(), any(), org.mockito.ArgumentMatchers.eq("signed-in-user")))
				.thenReturn(token);

		String view = controller.result(
				"Lan", "Nữ", LocalDate.of(1999, 6, 12), LocalTime.of(10, 30),
				"Hà Nội", () -> "signed-in-user", new MockHttpSession(), new ExtendedModelMap());

		assertEquals("redirect:/result/" + token, view);
		verify(persistenceService).save(any(), any(), org.mockito.ArgumentMatchers.eq("signed-in-user"));
	}

	@Test
	void displaysStructuredPalacesAndStarsFromBasicBirthInput() {
		var pageData = calculationService.calculate(input(LocalTime.of(5, 30)));

		assertNotNull(pageData.chart());
		var palaces = pageData.chartPalaces();
		assertEquals(12, palaces.size());
		assertTrue(palaces.stream().anyMatch(palace ->
				((ChartPalaceDisplay) palace).function()
						.vietnameseName().equals("Mệnh")));
		assertEquals(3, pageData.chartLimitations().size());
		assertFalse(pageData.remainingGaps().contains(CalculationGap.MENH_AND_CUC));
	}

	@Test
	void convertsLunarDateEvenWhenBirthTimeIsUnknown() {
		var pageData = calculationService.calculate(input(null));

		assertNotNull(pageData.lunarDate());
	}

	@Test
	void incompleteCalculationIsNotSavedAsACompletedChart() {
		var model = new ExtendedModelMap();

		controller.result(
				"Lan", "Nữ", LocalDate.of(1999, 6, 12), null,
				"Hà Nội", null, new MockHttpSession(), model);

		assertNull(model.get("chart"));
		assertTrue(((String) model.get("saveErrorMessage")).contains("chưa được lưu"));
		verify(persistenceService, never()).save(any(), any(), any());
	}

	@Test
	void reportsWhenGregorianDateIsOutsideCalendarSupportRange() {
		var model = new ExtendedModelMap();

		controller.result(
				"Lan", "Nữ", LocalDate.of(1700, 6, 12), LocalTime.of(5, 30),
				"Hà Nội", null, new MockHttpSession(), model);

		assertNull(model.get("chart"));
		assertTrue(((String) model.get("chartInputMessage")).contains("1800 đến 2199"));
		verify(persistenceService, never()).save(any(), any(), any());
	}

	@Test
	void loadsSavedChartDataForARefreshWithoutRecalculating() {
		var calculated = calculationService.calculate(input(LocalTime.of(10, 30)));
		UUID token = UUID.randomUUID();
		var saved = new StoredBirthChartPageData(
				token,
				input(LocalTime.of(10, 30)),
				calculated.chart(),
				calculated.lunarDate(),
				calculated.lunarCalendarName(),
				calculated.lunarCalendarVersion(),
				calculated.yearCanChi(),
				calculated.yearNayinElement(),
				calculated.yearNayinSource(),
				calculated.chartPalaces(),
				calculated.remainingGaps(),
				calculated.chartLimitations());
		when(persistenceService.load(token, null)).thenReturn(saved);
		var model = new ExtendedModelMap();

		String view = controller.savedResult(token, null, model);

		assertEquals("result", view);
		assertEquals(token, model.get("accessToken"));
		assertEquals(12, ((java.util.List<?>) model.get("chartPalaces")).size());
		assertEquals("Lan", model.get("name"));
		verify(persistenceService).load(token, null);
	}

	private com.example.astrology.calculation.AstrologyCalculationInput input(LocalTime birthTime) {
		return new com.example.astrology.calculation.AstrologyCalculationInput(
				"Lan", "Nữ", LocalDate.of(1999, 6, 12), birthTime, "Hà Nội");
	}
}
