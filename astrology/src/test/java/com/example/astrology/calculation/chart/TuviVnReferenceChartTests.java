package com.example.astrology.calculation.chart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.example.astrology.calculation.calendar.BirthInputNormalizationService;
import com.example.astrology.calculation.calendar.EarthlyBranch;
import com.example.astrology.calculation.calendar.HeavenlyStem;
import com.example.astrology.calculation.calendar.HoNgocDucLunarCalendarProvider;
import com.example.astrology.calculation.calendar.SexagenaryYearCalculator;

class TuviVnReferenceChartTests {

	private static final Set<StarId> FOURTEEN_MAJOR_STARS = EnumSet.of(
			StarId.TU_VI,
			StarId.THIEN_CO,
			StarId.THAI_DUONG,
			StarId.VU_KHUC,
			StarId.THIEN_DONG,
			StarId.LIEM_TRINH,
			StarId.THIEN_PHU,
			StarId.THAI_AM,
			StarId.THAM_LANG,
			StarId.CU_MON,
			StarId.THIEN_TUONG,
			StarId.THIEN_LUONG,
			StarId.THAT_SAT,
			StarId.PHA_QUAN);

	private final TracuutuviChartCalculator chartCalculator = new TracuutuviChartCalculator(
			new TracuutuviPositionsCalculator(new FiveElementsBureauCalculator()),
			new TracuutuviMajorStarsCalculator(new StarPlacementService()),
			new TracuutuviYearStarsCalculator(new StarPlacementService()),
			new TracuutuviMonthHourStarsCalculator(new StarPlacementService()),
			new TracuutuviFourTransformationsCalculator(new StarPlacementService()),
			new TracuutuviChangShengCalculator(new StarPlacementService()));

	@Test
	void normalizesSolarBirthDataToTheLunarDateShownByTuviVn() {
		var lunarDate = new HoNgocDucLunarCalendarProvider().toLunar(
				LocalDate.of(2005, 8, 14),
				ZoneId.of("Asia/Ho_Chi_Minh"));

		assertEquals(2005, lunarDate.year());
		assertEquals(7, lunarDate.month());
		assertEquals(10, lunarDate.day());
		assertFalse(lunarDate.leapMonth());

		var year = new SexagenaryYearCalculator().calculate(lunarDate.year());
		assertEquals(HeavenlyStem.YI, year.stem());
		assertEquals(EarthlyBranch.DAU, year.branch());
	}

	@Test
	void matchesSupportedChartFieldsFromTheTuviVnReference() {
		var fixture = TuviVnReferenceFixtures.birthChart();
		var input = TuviVnReferenceFixtures.lunarChartInput();
		var chart = chartCalculator.calculate(input);
		var actual = snapshotForMajorStars(chart);
		var normalized = fixture.input().normalize(new BirthInputNormalizationService());

		var result = new ReferenceChartComparator().compare(fixture, actual, normalized);

		assertEquals(35, result.comparedFields());
		assertEquals(35, result.matchingFields());
		assertEquals(0, result.mismatches().size());
		assertEquals(EarthlyBranch.TI, chart.positions().thanPalace());
	}

	private ChartSnapshot snapshotForMajorStars(TracuutuviChart chart) {
		Map<String, EarthlyBranch> majorStars = new LinkedHashMap<>();
		FOURTEEN_MAJOR_STARS.forEach(star -> chart.stars().branchOf(star)
				.ifPresent(branch -> majorStars.put(star.name(), branch)));
		return new ChartSnapshot(
				EnumSet.of(
						ChartSection.FIVE_ELEMENTS_BUREAU,
						ChartSection.MING_PALACE,
						ChartSection.THAN_PALACE,
						ChartSection.TWELVE_PALACES,
						ChartSection.STARS),
				Map.of(),
				null,
				chart.positions().bureau().vietnameseName(),
				chart.positions().mingPalace(),
				chart.positions().thanPalace(),
				chart.positions().palaces().positions(),
				majorStars,
				Map.of(),
				Map.of());
	}
}
