package com.example.astrology.calculation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

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
import com.example.astrology.service.BirthChartApplicationService;

class TuViDetailServiceTests {

	private final BirthChartApplicationService applicationService = new BirthChartApplicationService(
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
			new NayinElementCalculator(new SexagenaryYearCalculator()),
			new TuViDetailService(
					new CanLuongCalculator(),
					new NayinElementCalculator(new SexagenaryYearCalculator())));

	@Test
	void calculatesSampleChartAccurately() {
		// Test subject: Lê Hồ Phước Thịnh, 24/04/2003, 18:00, Nam
		var input = new AstrologyCalculationInput(
				"Lê Hồ Phước Thịnh",
				"Nam",
				LocalDate.of(2003, 4, 24),
				LocalTime.of(18, 0),
				"TP. Hồ Chí Minh");

		var pageData = applicationService.calculate(input);
		assertNotNull(pageData);
		assertNotNull(pageData.chart());

		// 1. Can Chi 4 Trụ
		var details = pageData.fullDetails();
		assertNotNull(details);
		assertEquals("Quý Mùi", pageData.yearCanChi());
		assertEquals("Bính Thìn", details.monthCanChi());
		assertEquals("Đinh Mão", details.dayCanChi());
		assertEquals("Kỷ Dậu", details.hourCanChi());

		// 2. Cân lượng
		assertEquals("4 lượng 2 chỉ", details.canLuong());

		// 3. Chủ Mệnh, Chủ Thân, Lai Nhân Cung
		assertEquals("Vũ Khúc", details.chuMenh());
		assertEquals("Thiên Tướng", details.chuThan());
		assertEquals("Quan Lộc", details.laiNhanCung());

		// 4. Âm Dương & Bản Mệnh
		assertEquals("Âm Nam", details.amDuongNamNu());
		assertEquals("Âm Dương thuận lý", details.amDuongLy());
		assertEquals("Dương Liễu Mộc - Hỏa Lục Cục", details.banMenhFull());
		assertEquals("(Mệnh Mộc sinh Cục Hỏa)", details.menhCucSinhKhac());

		// 5. Cung Mệnh tại Mùi, Thân cư Sửu (Thiên Di)
		assertEquals(EarthlyBranch.MUI, pageData.chart().positions().mingPalace());
		assertEquals(EarthlyBranch.SUU, pageData.chart().positions().thanPalace());

		// 6. 12 Cung on Grid (physical perimeter order)
		var grid = details.gridPalaces();
		assertEquals(12, grid.size());

		// First palace is TI (Tỵ) - Phu Thê
		var tiPalace = grid.get(0);
		assertEquals(EarthlyBranch.TI, tiPalace.branch());
		assertEquals("Đ.Tỵ", tiPalace.stemBranchCode());
		assertEquals("-Hoả", tiPalace.palaceSignElement());
		assertEquals(26, tiPalace.daiHan());
		assertEquals("Th.11", tiPalace.nguyetHan());
		assertEquals("Tuyệt", tiPalace.trangSinh());
		assertEquals("ĐV.HUYNH", tiPalace.dvLabel());
		assertEquals("LN.PHỐI", tiPalace.lnLabel());

		// Mùi (Mệnh)
		var muiPalace = grid.stream().filter(p -> p.branch() == EarthlyBranch.MUI).findFirst().orElseThrow();
		assertEquals("K.Mùi", muiPalace.stemBranchCode());
		assertEquals("-Thổ", muiPalace.palaceSignElement());
		assertEquals(6, muiPalace.daiHan());
		assertEquals("Th.1", muiPalace.nguyetHan());
		assertEquals("Tử", muiPalace.trangSinh());
		assertEquals("ĐV.PHỤ", muiPalace.dvLabel());
		assertEquals("LN.MỆNH", muiPalace.lnLabel());

		// Tuần at Thân, Dậu; Triệt at Tý, Sửu
		var thanPalace = grid.stream().filter(p -> p.branch() == EarthlyBranch.THAN).findFirst().orElseThrow();
		var dauPalace = grid.stream().filter(p -> p.branch() == EarthlyBranch.DAU).findFirst().orElseThrow();
		var tyPalace = grid.stream().filter(p -> p.branch() == EarthlyBranch.TY).findFirst().orElseThrow();
		var suuPalace = grid.stream().filter(p -> p.branch() == EarthlyBranch.SUU).findFirst().orElseThrow();

		assertTrue(thanPalace.hasTuan());
		assertTrue(dauPalace.hasTuan());
		assertTrue(tyPalace.hasTriet());
		assertTrue(suuPalace.hasTriet());
		assertEquals("than-dau", details.tuanMarkerPosition());
		assertEquals("ty-suu", details.trietMarkerPosition());

		// 7. Verify major stars and brightness
		// Tị: Thiên Lương (H)
		assertTrue(tiPalace.majorStars().stream().anyMatch(s -> s.name().equals("Thiên Lương") && s.brightness().equals("(H)")));
		// Ngọ: Thất Sát (M)
		var ngoPalace = grid.stream().filter(p -> p.branch() == EarthlyBranch.NGO).findFirst().orElseThrow();
		assertTrue(ngoPalace.majorStars().stream().anyMatch(s -> s.name().equals("Thất Sát") && s.brightness().equals("(M)")));
		// Thân: Liêm Trinh (V)
		assertTrue(thanPalace.majorStars().stream().anyMatch(s -> s.name().equals("Liêm Trinh") && s.brightness().equals("(V)")));
		// Thìn: Tử Vi (V), Thiên Tướng (V)
		var thinPalace = grid.stream().filter(p -> p.branch() == EarthlyBranch.THIN).findFirst().orElseThrow();
		assertTrue(thinPalace.majorStars().stream().anyMatch(s -> s.name().equals("Tử Vi") && s.brightness().equals("(V)")));
		assertTrue(thinPalace.majorStars().stream().anyMatch(s -> s.name().equals("Thiên Tướng") && s.brightness().equals("(V)")));

		// 8. Verify auxiliary and Lưu Niên stars (L.Sao)
		// Mùi: Linh Tinh (H), Thái Tuế, Hoa Cái, Tấu Thư
		assertTrue(muiPalace.badStars().stream().anyMatch(s -> s.name().equals("Linh Tinh")));
		assertTrue(muiPalace.badStars().stream().anyMatch(s -> s.name().equals("Thái Tuế")));
		assertTrue(muiPalace.goodStars().stream().anyMatch(s -> s.name().equals("Hoa Cái")));
		assertTrue(muiPalace.goodStars().stream().anyMatch(s -> s.name().equals("Tấu Thư")));

		// Tị: Đường Phù and L.Lộc Tồn
		assertTrue(tiPalace.goodStars().stream().anyMatch(s -> s.name().equals("Đường Phù")));
		assertTrue(tiPalace.goodStars().stream().anyMatch(s -> s.name().equals("L.Lộc Tồn")));

		// Ngọ: L.Thái Tuế, L.Kình Dương
		assertTrue(ngoPalace.badStars().stream().anyMatch(s -> s.name().equals("L.Thái Tuế")));
		assertTrue(ngoPalace.badStars().stream().anyMatch(s -> s.name().equals("L.Kình Dương")));

		// Hợi: L.Hóa Lộc, L.Thiên Khôi
		var hoiPalace = grid.stream().filter(p -> p.branch() == EarthlyBranch.HOI).findFirst().orElseThrow();
		assertTrue(hoiPalace.goodStars().stream().anyMatch(s -> s.name().equals("L.Hóa Lộc")));
		assertTrue(hoiPalace.goodStars().stream().anyMatch(s -> s.name().equals("L.Thiên Khôi")));

		// Mão: L.Hóa Quyền, L.Đào Hoa
		var maoPalace = grid.stream().filter(p -> p.branch() == EarthlyBranch.MAO).findFirst().orElseThrow();
		assertTrue(maoPalace.goodStars().stream().anyMatch(s -> s.name().equals("L.Hóa Quyền")));
		assertTrue(maoPalace.goodStars().stream().anyMatch(s -> s.name().equals("L.Đào Hoa")));

		// Sửu: L.Hóa Khoa
		assertTrue(suuPalace.goodStars().stream().anyMatch(s -> s.name().equals("L.Hóa Khoa")));

		// Thân: L.Hóa Kỵ, L.Tang Môn, L.Thiên Mã
		assertTrue(thanPalace.badStars().stream().anyMatch(s -> s.name().equals("L.Hóa Kỵ")));
		assertTrue(thanPalace.badStars().stream().anyMatch(s -> s.name().equals("L.Tang Môn")));
		assertTrue(thanPalace.goodStars().stream().anyMatch(s -> s.name().equals("L.Thiên Mã")));
	}
}
