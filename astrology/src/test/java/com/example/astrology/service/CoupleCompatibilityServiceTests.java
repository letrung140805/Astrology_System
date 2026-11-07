package com.example.astrology.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.astrology.model.CoupleCompatibilityResult;
import com.example.astrology.model.CouplePersonData;

class CoupleCompatibilityServiceTests {

	private CoupleCompatibilityService service;

	@BeforeEach
	void setUp() {
		service = new CoupleCompatibilityService();
	}

	@Test
	void assessReturnsCompleteCompatibilityResult() {
		CouplePersonData person1 = new CouplePersonData(
				"Lê Đức Trung",
				"Nam",
				"2005-08-14",
				2005,
				8,
				14,
				"17:30",
				"Hà Nội",
				"Dậu",
				null,
				Collections.emptyList(),
				null,
				"Ất Dậu",
				"Tuyền Trung Thủy",
				null
		);

		CouplePersonData person2 = new CouplePersonData(
				"Hoàng Anh Vũ",
				"Nữ",
				"2005-04-04",
				2005,
				4,
				4,
				"18:00",
				"Hà Nội",
				"Dậu",
				null,
				Collections.emptyList(),
				null,
				"Ất Dậu",
				"Tuyền Trung Thủy",
				null
		);

		CoupleCompatibilityResult result = service.assess(person1, person2);

		assertNotNull(result);
		assertTrue(result.totalScore() > 0 && result.totalScore() <= 100);
		assertNotNull(result.menhTitle());
		assertNotNull(result.menhAnalysis());
		assertNotNull(result.canTitle());
		assertNotNull(result.canAnalysis());
		assertNotNull(result.chiTitle());
		assertNotNull(result.chiAnalysis());
		assertNotNull(result.cungPhiTitle());
		assertNotNull(result.cungPhiAnalysis());
		assertNotNull(result.phuTheTitle());
		assertNotNull(result.phuTheAnalysis());
		assertNotNull(result.advice());
		assertNotNull(result.summary());
		assertNotNull(result.rating());
	}

	@Test
	void assessHandlesSameCungPhiWithoutDuplicateElementError() {
		// 2008 Nữ -> Cung Đoài, 2009 Nam -> Cung Đoài
		CouplePersonData person1 = new CouplePersonData(
				"Lê Thị Mỹ Tâm",
				"Nữ",
				"2008-11-14",
				2008,
				11,
				14,
				"00:12",
				"Cần Thơ",
				"Tý",
				null,
				Collections.emptyList(),
				null,
				"Mậu Tý",
				"Tích Lịch Hỏa",
				null
		);

		CouplePersonData person2 = new CouplePersonData(
				"Trần Ngọc Sơn",
				"Nam",
				"2009-11-14",
				2009,
				11,
				14,
				"12:12",
				"Cần Thơ",
				"Sửu",
				null,
				Collections.emptyList(),
				null,
				"Kỷ Sửu",
				"Tích Lịch Hỏa",
				null
		);

		CoupleCompatibilityResult result = service.assess(person1, person2);

		assertNotNull(result);
		assertTrue(result.cungPhiTitle().contains("Đoài"));
		assertTrue(result.cungPhiTitle().contains("Phục Vị"));
		assertTrue(result.totalScore() > 0 && result.totalScore() <= 100);
	}

	@Test
	void assessWithDetailedPalacesTriggersRulesAndComputesSubscores() {
		com.example.astrology.calculation.FiveElement fire = com.example.astrology.calculation.FiveElement.FIRE;
		com.example.astrology.calculation.FiveElement water = com.example.astrology.calculation.FiveElement.WATER;
		com.example.astrology.calculation.FiveElement earth = com.example.astrology.calculation.FiveElement.EARTH;
		com.example.astrology.calculation.FiveElement metal = com.example.astrology.calculation.FiveElement.METAL;

		var menhPalace1 = new com.example.astrology.model.ChartPalaceDisplay(
				com.example.astrology.calculation.chart.PalaceFunction.MENH,
				com.example.astrology.calculation.calendar.EarthlyBranch.DAU,
				List.of(
						new com.example.astrology.model.ChartStarDisplay("Phá Quân", water, "(H)", true),
						new com.example.astrology.model.ChartStarDisplay("Liêm Trinh", fire, "(V)", true)
				)
		);
		var phuThePalace1 = new com.example.astrology.model.ChartPalaceDisplay(
				com.example.astrology.calculation.chart.PalaceFunction.PHU_THE,
				com.example.astrology.calculation.calendar.EarthlyBranch.MUI,
				List.of(
						new com.example.astrology.model.ChartStarDisplay("Thất Sát", metal, "(M)", true)
				)
		);
		var phucDucPalace1 = new com.example.astrology.model.ChartPalaceDisplay(
				com.example.astrology.calculation.chart.PalaceFunction.PHUC_DUC,
				com.example.astrology.calculation.calendar.EarthlyBranch.TY,
				List.of(
						new com.example.astrology.model.ChartStarDisplay("Thiên Phủ", earth, "(M)", true)
				)
		);

		var menhPalace2 = new com.example.astrology.model.ChartPalaceDisplay(
				com.example.astrology.calculation.chart.PalaceFunction.MENH,
				com.example.astrology.calculation.calendar.EarthlyBranch.THAN,
				List.of(
						new com.example.astrology.model.ChartStarDisplay("Phá Quân", water, "(H)", true)
				)
		);
		var phuThePalace2 = new com.example.astrology.model.ChartPalaceDisplay(
				com.example.astrology.calculation.chart.PalaceFunction.PHU_THE,
				com.example.astrology.calculation.calendar.EarthlyBranch.NGO,
				List.of(
						new com.example.astrology.model.ChartStarDisplay("Vũ Khúc", metal, "(M)", true)
				)
		);
		var phucDucPalace2 = new com.example.astrology.model.ChartPalaceDisplay(
				com.example.astrology.calculation.chart.PalaceFunction.PHUC_DUC,
				com.example.astrology.calculation.calendar.EarthlyBranch.THIN,
				List.of(
						new com.example.astrology.model.ChartStarDisplay("Thiên Phủ", earth, "(M)", true),
						new com.example.astrology.model.ChartStarDisplay("Tử Vi", earth, "(M)", true)
				)
		);

		CouplePersonData person1 = new CouplePersonData(
				"Ngô Đức Anh",
				"Nam",
				"1998-05-10",
				1998,
				5,
				10,
				"18:30",
				"Hà Nội",
				"Dậu",
				null,
				List.of(menhPalace1, phuThePalace1, phucDucPalace1),
				null,
				"Mậu Dần",
				"Thành Đầu Thổ",
				null
		);

		CouplePersonData person2 = new CouplePersonData(
				"Vũ Khánh Linh",
				"Nữ",
				"2000-08-20",
				2000,
				8,
				20,
				"16:45",
				"Hà Nội",
				"Thân",
				null,
				List.of(menhPalace2, phuThePalace2, phucDucPalace2),
				null,
				"Canh Thìn",
				"Bạch Lạp Kim",
				null
		);

		CoupleCompatibilityResult result = service.assess(person1, person2);

		assertNotNull(result);
		assertNotNull(result.triggeredRules());
		assertTrue(!result.triggeredRules().isEmpty(), "Triggered rules should not be empty");

		// 5 factors check
		assertTrue(result.emotionalScore() >= 3.0 && result.emotionalScore() <= 10.0);
		assertTrue(result.communicationScore() >= 3.0 && result.communicationScore() <= 10.0);
		assertTrue(result.financialScore() >= 3.0 && result.financialScore() <= 10.0);
		assertTrue(result.stabilityScore() >= 3.0 && result.stabilityScore() <= 10.0);
		assertTrue(result.livingTogetherScore() >= 3.0 && result.livingTogetherScore() <= 10.0);

		// 5 detailed factor interpretations check
		assertNotNull(result.emotionalInterpretation());
		assertNotNull(result.communicationInterpretation());
		assertNotNull(result.financialInterpretation());
		assertNotNull(result.stabilityInterpretation());
		assertNotNull(result.livingTogetherInterpretation());
		assertNotNull(result.conclusion());
		assertTrue(!result.conclusion().isBlank());
		assertNotNull(result.advice());
		assertNotNull(result.aiFullInterpretation());

		assertTrue(result.aiFullInterpretation().contains("1. Hòa hợp tình cảm"));
		assertTrue(result.aiFullInterpretation().contains("2. Giao tiếp"));
		assertTrue(result.aiFullInterpretation().contains("3. Tài chính"));
		assertTrue(result.aiFullInterpretation().contains("4. Độ bền trước biến cố"));
		assertTrue(result.aiFullInterpretation().contains("5. Sống chung"));
		assertTrue(result.aiFullInterpretation().contains("6. Lời kết"));
	}
}
