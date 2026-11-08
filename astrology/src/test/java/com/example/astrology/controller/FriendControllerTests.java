package com.example.astrology.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import com.example.astrology.calculation.AstrologyCalculationService;
import com.example.astrology.calculation.calendar.BirthInputNormalizationService;
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
import com.example.astrology.model.CouplePersonData;
import com.example.astrology.model.FriendCompatibilityResult;
import com.example.astrology.service.BirthChartApplicationService;
import com.example.astrology.service.FriendCompatibilityService;

class FriendControllerTests {

	private FriendController controller;

	@BeforeEach
	void setUp() {
		BirthChartApplicationService chartApplicationService = new BirthChartApplicationService(
				new AstrologyCalculationService(new BirthInputNormalizationService()),
				new TracuutuviChartCalculator(
						new TracuutuviPositionsCalculator(new FiveElementsBureauCalculator()),
						new TracuutuviMajorStarsCalculator(new StarPlacementService()),
						new TracuutuviYearStarsCalculator(new StarPlacementService()),
						new TracuutuviMonthHourStarsCalculator(new StarPlacementService()),
						new TracuutuviFourTransformationsCalculator(new StarPlacementService()),
						new TracuutuviChangShengCalculator(new StarPlacementService())
				),
				new HoNgocDucLunarCalendarProvider(),
				new SexagenaryYearCalculator(),
				new NayinElementCalculator(new SexagenaryYearCalculator())
		);

		FriendCompatibilityService compatibilityService = new FriendCompatibilityService();
		controller = new FriendController(chartApplicationService, compatibilityService);
	}

	@Test
	void showFriendPageReturnsFriendView() {
		ExtendedModelMap model = new ExtendedModelMap();
		String viewName = controller.friendPage(model);

		assertEquals("friend", viewName);
		assertEquals(false, model.get("submitted"));
		assertEquals("tuong-hop-ban-be", model.get("activePage"));
	}

	@Test
	void calculateFriendProducesBothChartsAndCompatibility() {
		ExtendedModelMap model = new ExtendedModelMap();

		String viewName = controller.calculateFriend(
				"Nguyễn Văn A",
				"Nam",
				LocalDate.of(2005, 8, 14),
				14,
				8,
				2005,
				LocalTime.of(17, 30),
				"Hà Nội",
				"Trần Văn B",
				"Nam",
				LocalDate.of(2005, 4, 4),
				4,
				4,
				2005,
				LocalTime.of(18, 0),
				"Hà Nội",
				2026,
				model
		);

		assertEquals("friend", viewName);
		assertEquals(true, model.get("submitted"));

		CouplePersonData person1 = (CouplePersonData) model.get("person1");
		CouplePersonData person2 = (CouplePersonData) model.get("person2");
		FriendCompatibilityResult compatibility = (FriendCompatibilityResult) model.get("compatibility");

		assertNotNull(person1);
		assertNotNull(person2);
		assertNotNull(compatibility);
		assertTrue(compatibility.totalScore() >= 35 && compatibility.totalScore() <= 100);
		assertTrue(compatibility.partnerScore() >= 3.0);
		assertTrue(compatibility.communicationScore() >= 3.0);
		assertTrue(compatibility.visionScore() >= 3.0);
		assertTrue(compatibility.valuesScore() >= 3.0);
		assertTrue(compatibility.moneyScore() >= 3.0);
	}
}
