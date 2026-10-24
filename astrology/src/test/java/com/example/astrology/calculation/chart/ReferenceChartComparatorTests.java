package com.example.astrology.calculation.chart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import com.example.astrology.calculation.calendar.BirthDateInput;
import com.example.astrology.calculation.calendar.BirthInputNormalizationService;
import com.example.astrology.calculation.calendar.CycleDateAdjustment;
import com.example.astrology.calculation.calendar.EarthlyBranch;
import com.example.astrology.calculation.calendar.HourBranch;
import com.example.astrology.calculation.calendar.ZiHourDayBoundary;

class ReferenceChartComparatorTests {

	private final ReferenceChartComparator comparator = new ReferenceChartComparator();

	@Test
	void comparesAllCoveredStructuredFieldsExactly() {
		var expected = completeSnapshot();

		var result = comparator.compare("case-001", expected, completeSnapshot());

		assertEquals(22, result.comparedFields());
		assertEquals(22, result.matchingFields());
		assertTrue(result.mismatches().isEmpty());
		assertTrue(result.matches());
		assertEquals(1.0, result.matchRate().orElseThrow());
	}

	@Test
	void reportsFieldLevelDiscrepanciesWithCategories() {
		var expected = completeSnapshot();
		var actual = new ChartSnapshot(
				expected.coveredSections(),
				expected.canChi(),
				expected.natalElement(),
				expected.fiveElementsBureau(),
				EarthlyBranch.SUU,
				expected.thanPalace(),
				expected.twelvePalaces(),
				Map.of("TU_VI", EarthlyBranch.SUU, "THIEN_CO", EarthlyBranch.DAN),
				expected.starBrightness(),
				expected.rulingStars());

		var result = comparator.compare("case-002", expected, actual);

		assertEquals(22, result.comparedFields());
		assertEquals(3, result.mismatches().size());
		assertEquals(List.of(
				new ChartMismatch("mingPalace", MismatchCategory.PALACE_PLACEMENT,
						EarthlyBranch.TY, EarthlyBranch.SUU),
				new ChartMismatch("stars.THIEN_CO", MismatchCategory.STAR_PLACEMENT,
						EarthlyBranch.SUU, EarthlyBranch.DAN),
				new ChartMismatch("stars.TU_VI", MismatchCategory.STAR_PLACEMENT,
						EarthlyBranch.TY, EarthlyBranch.SUU)), result.mismatches());
		assertEquals(19, result.matchingFields());
	}

	@Test
	void reportsCanChiNatalElementAndBureauSeparately() {
		var expected = completeSnapshot();
		var alteredCanChi = new EnumMap<ChartSnapshot.CanChiPeriod, CanChiValue>(expected.canChi());
		alteredCanChi.put(ChartSnapshot.CanChiPeriod.YEAR,
				new CanChiValue("Ất", EarthlyBranch.SUU));
		var actual = new ChartSnapshot(
				expected.coveredSections(),
				alteredCanChi,
				"Kim",
				"Hỏa Lục Cục",
				expected.mingPalace(),
				expected.thanPalace(),
				expected.twelvePalaces(),
				expected.stars(),
				expected.starBrightness(),
				expected.rulingStars());

		var result = comparator.compare("case-003", expected, actual);

		assertTrue(result.mismatches().stream().anyMatch(mismatch ->
				mismatch.field().equals("canChi.YEAR") && mismatch.category() == MismatchCategory.CAN_CHI));
		assertTrue(result.mismatches().stream().anyMatch(mismatch ->
				mismatch.field().equals("natalElement") && mismatch.category() == MismatchCategory.NATAL_ELEMENT));
		assertTrue(result.mismatches().stream().anyMatch(mismatch ->
				mismatch.field().equals("fiveElementsBureau") && mismatch.category() == MismatchCategory.RULE_DATA));
	}

	@Test
	void returnsNoMatchRateForAnEmptyReferenceSet() {
		var summary = ComparisonSummary.from(List.of());

		assertEquals(0, summary.fixtureCount());
		assertEquals(0, summary.comparedFields());
		assertFalse(summary.matchRate().isPresent());
	}

	@Test
	void fixtureCannotBeMarkedReadyWithoutStructuredReferenceEvidence() {
		assertTrue(ReferenceChartFixture.pending("pending-case").status()
				== ReferenceFixtureStatus.PENDING_REFERENCE);
	}

	@Test
	void regressionRunnerRejectsPendingReferenceFixtures() {
		var runner = new ReferenceChartRegressionRunner(comparator);
		var pending = ReferenceChartFixture.pending("pending-case");

		org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
				() -> runner.compare(pending, completeSnapshot()));
	}

	@Test
	void reportsCalendarAndZiHourBoundaryNormalizationDifferencesSeparately() {
		var birthDate = BirthDateInput.solar(LocalDate.of(2000, 1, 1));
		var input = new ReferenceChartInput(
				birthDate, "23:30", "Nữ", "Hà Nội", "Asia/Ho_Chi_Minh",
				null, ZiHourDayBoundary.UNSPECIFIED);
		var expectedNormalization = new NormalizedInputSnapshot(
				birthDate, LocalTime.of(23, 30), "Asia/Ho_Chi_Minh",
				ZoneOffset.ofHours(7).getId(), HourBranch.ZI, CycleDateAdjustment.UNRESOLVED);
		var expectedChart = new ChartSnapshot(
				EnumSet.of(ChartSection.MING_PALACE), Map.of(), null, null,
				EarthlyBranch.TY, null, Map.of(), Map.of(), Map.of(), Map.of());
		var fixture = new ReferenceChartFixture(
				"case-normalization",
				ReferenceFixtureStatus.READY_FOR_COMPARISON,
				input,
				expectedNormalization,
				new ReferenceChartSource(
						"Saved reference",
						URI.create("https://tracuutuvi.com/lap-la-so-tu-vi"),
						LocalDate.of(2026, 10, 24),
						"Manual transcription",
						"Solar date, local time, unspecified Zi day-boundary",
						null),
				expectedChart);
		var actualNormalization = new BirthInputNormalizationService().normalizeSolar(
				LocalDate.of(2000, 1, 2), LocalTime.of(23, 30),
				ZoneId.of("Asia/Ho_Chi_Minh"), null, ZiHourDayBoundary.CHANGE_AT_23);

		var result = comparator.compare(fixture, expectedChart, actualNormalization);

		assertEquals(7, result.comparedFields());
		assertTrue(result.mismatches().stream().anyMatch(mismatch ->
				mismatch.field().equals("normalized.birthDate")
						&& mismatch.category() == MismatchCategory.CALENDAR));
		assertTrue(result.mismatches().stream().anyMatch(mismatch ->
				mismatch.field().equals("normalized.cycleDateAdjustment")
						&& mismatch.category() == MismatchCategory.DAY_BOUNDARY_CONVENTION));
	}

	private ChartSnapshot completeSnapshot() {
		var canChi = new EnumMap<ChartSnapshot.CanChiPeriod, CanChiValue>(ChartSnapshot.CanChiPeriod.class);
		canChi.put(ChartSnapshot.CanChiPeriod.YEAR, new CanChiValue("Giáp", EarthlyBranch.TY));
		canChi.put(ChartSnapshot.CanChiPeriod.MONTH, new CanChiValue("Bính", EarthlyBranch.DAN));
		canChi.put(ChartSnapshot.CanChiPeriod.DAY, new CanChiValue("Mậu", EarthlyBranch.THIN));
		canChi.put(ChartSnapshot.CanChiPeriod.HOUR, new CanChiValue("Canh", EarthlyBranch.NGO));

		var palaces = new EnumMap<PalaceFunction, EarthlyBranch>(PalaceFunction.class);
		var palaceFunctions = PalaceFunction.values();
		var branches = EarthlyBranch.values();
		for (int index = 0; index < palaceFunctions.length; index++) {
			palaces.put(palaceFunctions[index], branches[index]);
		}

		return new ChartSnapshot(
				EnumSet.of(ChartSection.CAN_CHI, ChartSection.NATAL_ELEMENT,
						ChartSection.FIVE_ELEMENTS_BUREAU, ChartSection.MING_PALACE,
						ChartSection.THAN_PALACE, ChartSection.TWELVE_PALACES, ChartSection.STARS),
				canChi,
				"Mộc",
				"Thủy Nhị Cục",
				EarthlyBranch.TY,
				EarthlyBranch.MAO,
				palaces,
				Map.of("TU_VI", EarthlyBranch.TY, "THIEN_CO", EarthlyBranch.SUU),
				Map.of(),
				Map.of());
	}
}
