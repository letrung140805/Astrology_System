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
import com.example.astrology.model.CoupleCompatibilityResult;
import com.example.astrology.model.CouplePersonData;
import com.example.astrology.service.BirthChartApplicationService;
import com.example.astrology.service.CoupleCompatibilityService;

class CoupleControllerTests {

	private CoupleController controller;

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

		CoupleCompatibilityService compatibilityService = new CoupleCompatibilityService();
		controller = new CoupleController(chartApplicationService, compatibilityService);
	}

	@Test
	void showCouplePageReturnsCoupleView() {
		ExtendedModelMap model = new ExtendedModelMap();
		String viewName = controller.couplePage(model);

		assertEquals("couple", viewName);
		assertEquals(false, model.get("submitted"));
	}

	@Test
	void calculateCoupleProducesBothChartsAndCompatibility() {
		ExtendedModelMap model = new ExtendedModelMap();

		String viewName = controller.calculateCouple(
				"Lê Đức Trung",
				"Nam",
				LocalDate.of(2005, 8, 14),
				14,
				8,
				2005,
				LocalTime.of(17, 30),
				"Hà Nội",
				"Hoàng Anh Vũ",
				"Nữ",
				LocalDate.of(2005, 4, 4),
				4,
				4,
				2005,
				LocalTime.of(18, 0),
				"Hà Nội",
				2026,
				model
		);

		assertEquals("couple", viewName);
		assertEquals(true, model.get("submitted"));

		CouplePersonData person1 = (CouplePersonData) model.get("person1");
		CouplePersonData person2 = (CouplePersonData) model.get("person2");
		CoupleCompatibilityResult compatibility = (CoupleCompatibilityResult) model.get("compatibility");

		assertNotNull(person1);
		assertNotNull(person2);
		assertNotNull(compatibility);

		assertEquals("Lê Đức Trung", person1.name());
		assertEquals("Hoàng Anh Vũ", person2.name());
		assertEquals("Âm Nam", person1.amDuongNamNu());
		assertEquals("Âm Nữ", person2.amDuongNamNu());

		// 12 palaces should be created for both
		assertEquals(12, person1.chartPalaces().size());
		assertEquals(12, person2.chartPalaces().size());

		// Compatibility score should be between 0 and 100
		assertTrue(compatibility.totalScore() > 0 && compatibility.totalScore() <= 100);
	}

	@Test
	void calculateCoupleWithDMYDropdownFallback() {
		ExtendedModelMap model = new ExtendedModelMap();

		// Case where birthDate string/object is null, but day, month, year dropdowns are selected
		String viewName = controller.calculateCouple(
				"Nguyễn Văn A",
				"Nam",
				null,
				10,
				10,
				2000,
				LocalTime.of(8, 0),
				"Đà Nẵng",
				"Trần Thị B",
				"Nữ",
				null,
				15,
				5,
				2002,
				LocalTime.of(9, 30),
				"TP.HCM",
				2026,
				model
		);

		assertEquals("couple", viewName);
		assertEquals(true, model.get("submitted"));

		CouplePersonData person1 = (CouplePersonData) model.get("person1");
		CouplePersonData person2 = (CouplePersonData) model.get("person2");

		assertNotNull(person1);
		assertNotNull(person2);
		assertEquals("Nguyễn Văn A", person1.name());
		assertEquals("Trần Thị B", person2.name());
	}

	@Test
	void calculateCoupleWherePerson1IsFemaleAndPerson2IsMale() {
		ExtendedModelMap model = new ExtendedModelMap();

		String viewName = controller.calculateCouple(
				"Trần Thị Hoa",
				"Nữ",
				LocalDate.of(2003, 3, 15),
				15,
				3,
				2003,
				LocalTime.of(10, 15),
				"Hà Nội",
				"Phạm Văn Nam",
				"Nam",
				LocalDate.of(2001, 7, 20),
				20,
				7,
				2001,
				LocalTime.of(14, 45),
				"Hải Phòng",
				2026,
				model
		);

		assertEquals("couple", viewName);
		assertEquals(true, model.get("submitted"));

		CouplePersonData person1 = (CouplePersonData) model.get("person1");
		CouplePersonData person2 = (CouplePersonData) model.get("person2");
		CoupleCompatibilityResult compatibility = (CoupleCompatibilityResult) model.get("compatibility");

		assertNotNull(person1);
		assertNotNull(person2);
		assertNotNull(compatibility);
		assertEquals("Trần Thị Hoa", person1.name());
		assertEquals("Nữ", person1.gender());
		assertEquals("Phạm Văn Nam", person2.name());
		assertEquals("Nam", person2.gender());
		assertEquals("Âm Nữ", person1.amDuongNamNu());
		assertEquals("Âm Nam", person2.amDuongNamNu());
		assertEquals(12, person1.chartPalaces().size());
		assertEquals(12, person2.chartPalaces().size());
		assertTrue(compatibility.totalScore() > 0 && compatibility.totalScore() <= 100);
	}

	@Test
	void calculateCoupleWithSameCungPhiDoaiGeneratesSuccessfully() {
		// Exact case from user's screenshot: Lê Thị Mỹ Tâm 2008 & Trần Ngọc Sơn 2009
		ExtendedModelMap model = new ExtendedModelMap();

		String viewName = controller.calculateCouple(
				"Lê Thị Mỹ Tâm",
				"Nữ",
				LocalDate.of(2008, 11, 14),
				14,
				11,
				2008,
				LocalTime.of(0, 12),
				"Cần Thơ",
				"Trần Ngọc Sơn",
				"Nam",
				LocalDate.of(2009, 11, 14),
				14,
				11,
				2009,
				LocalTime.of(12, 12),
				"Cần Thơ",
				2026,
				model
		);

		assertEquals("couple", viewName);
		assertEquals(true, model.get("submitted"));

		CouplePersonData person1 = (CouplePersonData) model.get("person1");
		CouplePersonData person2 = (CouplePersonData) model.get("person2");
		CoupleCompatibilityResult compatibility = (CoupleCompatibilityResult) model.get("compatibility");

		assertNotNull(person1);
		assertNotNull(person2);
		assertNotNull(compatibility);
		assertEquals("Lê Thị Mỹ Tâm", person1.name());
		assertEquals("Trần Ngọc Sơn", person2.name());
		assertEquals(12, person1.chartPalaces().size());
		assertEquals(12, person2.chartPalaces().size());
		assertTrue(compatibility.cungPhiTitle().contains("Đoài"));
		assertTrue(compatibility.cungPhiTitle().contains("Phục Vị"));
	}
}
