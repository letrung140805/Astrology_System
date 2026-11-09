package com.example.astrology.service;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.astrology.calculation.calendar.EarthlyBranch;
import com.example.astrology.calculation.chart.PalaceFunction;
import com.example.astrology.model.ChartPalaceDisplay;
import com.example.astrology.model.ChartStarDisplay;
import com.example.astrology.model.CouplePersonData;
import com.example.astrology.model.FriendCompatibilityResult;

class FriendCompatibilityServiceTests {

	private FriendCompatibilityService service;

	@BeforeEach
	void setUp() {
		service = new FriendCompatibilityService();
	}

	@Test
	void assessReturnsCompleteFriendCompatibilityResult() {
		CouplePersonData person1 = new CouplePersonData(
				"Nguyễn Văn A",
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
				"Trần Văn B",
				"Nam",
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

		FriendCompatibilityResult result = service.assess(person1, person2);

		assertNotNull(result);
		assertTrue(result.totalScore() >= 35 && result.totalScore() <= 100);
		assertTrue(result.partnerScore() >= 3.0 && result.partnerScore() <= 10.0);
		assertTrue(result.communicationScore() >= 3.0 && result.communicationScore() <= 10.0);
		assertTrue(result.visionScore() >= 3.0 && result.visionScore() <= 10.0);
		assertTrue(result.valuesScore() >= 3.0 && result.valuesScore() <= 10.0);
		assertTrue(result.moneyScore() >= 3.0 && result.moneyScore() <= 10.0);
		assertNotNull(result.partnerInterpretation());
		assertNotNull(result.communicationInterpretation());
		assertNotNull(result.visionInterpretation());
		assertNotNull(result.valuesInterpretation());
		assertNotNull(result.moneyInterpretation());
		assertNotNull(result.conclusion());
		assertNotNull(result.advice());
		assertNotNull(result.summary());
		assertNotNull(result.rating());
	}

	@Test
	void assessScoresVaryDynamicallyWhenRulesAreTriggered() {
		// Person 1 with strong friend/partner, work, and finance stars
		ChartStarDisplay taPhu = new ChartStarDisplay("Tả Phụ", com.example.astrology.calculation.FiveElement.EARTH);
		ChartStarDisplay huuBat = new ChartStarDisplay("Hữu Bật", com.example.astrology.calculation.FiveElement.EARTH);
		ChartPalaceDisplay noBoc1 = new ChartPalaceDisplay(
				PalaceFunction.NO_BOC, EarthlyBranch.NGO, List.of(taPhu, huuBat));

		ChartStarDisplay tuVi = new ChartStarDisplay("Tử Vi", com.example.astrology.calculation.FiveElement.EARTH);
		ChartStarDisplay thienPhu = new ChartStarDisplay("Thiên Phủ", com.example.astrology.calculation.FiveElement.EARTH);
		ChartPalaceDisplay quanLoc1 = new ChartPalaceDisplay(
				PalaceFunction.QUAN_LOC, EarthlyBranch.THAN, List.of(tuVi, thienPhu));

		ChartStarDisplay vuKhuc = new ChartStarDisplay("Vũ Khúc", com.example.astrology.calculation.FiveElement.METAL);
		ChartStarDisplay hoaLoc = new ChartStarDisplay("Hóa Lộc", com.example.astrology.calculation.FiveElement.WOOD);
		ChartPalaceDisplay taiBach1 = new ChartPalaceDisplay(
				PalaceFunction.TAI_BACH, EarthlyBranch.THIN, List.of(vuKhuc, hoaLoc));

		CouplePersonData person1 = new CouplePersonData(
				"Lê Đức Trung",
				"Nam",
				"2005-08-14",
				2005,
				8,
				14,
				"18:30",
				"Thanh Hóa",
				"Dậu",
				null,
				List.of(noBoc1, quanLoc1, taiBach1),
				null,
				"Ất Dậu",
				"Tuyền Trung Thủy",
				null
		);

		CouplePersonData person2 = new CouplePersonData(
				"Lê Thị Mỹ Tâm",
				"Nữ",
				"2005-12-30",
				2005,
				12,
				30,
				"09:50",
				"Đắk Lắk",
				"Tỵ",
				null,
				Collections.emptyList(),
				null,
				"Ất Dậu",
				"Tuyền Trung Thủy",
				null
		);

		FriendCompatibilityResult result = service.assess(person1, person2);

		assertNotNull(result);
		// With Tả Phụ / Hữu Bật in Nô Bộc, partnerScore should exceed default base 7.0
		assertTrue(result.partnerScore() > 7.0, "Partner score should be higher than 7.0 when assistant stars present: " + result.partnerScore());
		// With Tử Vi / Thiên Phủ in Quan Lộc, visionScore should exceed default base 7.0
		assertTrue(result.visionScore() > 7.0, "Vision score should be higher than 7.0 when imperial stars present: " + result.visionScore());
		// With Vũ Khúc / Hóa Lộc in Tài Bạch, moneyScore should exceed default base 6.8
		assertTrue(result.moneyScore() > 6.8, "Money score should be higher than 6.8 when wealth stars present: " + result.moneyScore());
		// Two Ất Dậu people share same Can/Chi (Tự Hình), valuesScore should not be artificially maxed at 10.0
		assertTrue(result.valuesScore() < 10.0, "Values score should not be 10.0 for two same year Ất Dậu: " + result.valuesScore());
	}

	@Test
	void assessValuesScoreVariesBasedOnCanChiAndPhucDuc() {
		// Pair 1: Ất Dậu & Canh Thìn (Can hợp: Ất-Canh, Chi hợp: Thìn-Dậu, Mệnh sinh: Kim sinh Thủy)
		CouplePersonData person1 = new CouplePersonData(
				"Lê Đức Trung", "Nam", "2005-08-14", 2005, 8, 14, "18:30", "Thanh Hóa", "Dậu",
				null, Collections.emptyList(), null, "Ất Dậu", "Tuyền Trung Thủy", null);

		CouplePersonData personHarmonious = new CouplePersonData(
				"Trần Thị B", "Nữ", "2000-05-10", 2000, 5, 10, "10:00", "Hà Nội", "Thìn",
				null, Collections.emptyList(), null, "Canh Thìn", "Bạch Lạp Kim", null);

		FriendCompatibilityResult resultHarmonious = service.assess(person1, personHarmonious);
		assertTrue(resultHarmonious.valuesScore() >= 9.0, "Harmonious pair should have high values score: " + resultHarmonious.valuesScore());

		// Pair 2: Ất Dậu & Tân Mão (Can xung: Ất-Tân, Chi xung: Mão-Dậu Tứ Hành Xung)
		ChartStarDisplay hoaKy = new ChartStarDisplay("Hóa Kỵ", com.example.astrology.calculation.FiveElement.WATER);
		ChartPalaceDisplay phucDucBad = new ChartPalaceDisplay(
				PalaceFunction.PHUC_DUC, EarthlyBranch.HOI, List.of(hoaKy));

		CouplePersonData personClashing = new CouplePersonData(
				"Nguyễn Văn C", "Nam", "2011-03-15", 2011, 3, 15, "14:00", "Đà Nẵng", "Mão",
				null, List.of(phucDucBad), null, "Tân Mão", "Tùng Bách Mộc", null);

		FriendCompatibilityResult resultClashing = service.assess(person1, personClashing);
		assertTrue(resultClashing.valuesScore() <= 6.0, "Clashing pair should have low values score: " + resultClashing.valuesScore());
	}
}
