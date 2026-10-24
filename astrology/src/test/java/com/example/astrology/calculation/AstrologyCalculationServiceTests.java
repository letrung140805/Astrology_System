package com.example.astrology.calculation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.example.astrology.calculation.calendar.BirthInputNormalizationService;
import com.example.astrology.calculation.calendar.CycleDateAdjustment;
import com.example.astrology.calculation.calendar.HourBranch;
import org.junit.jupiter.api.Test;

class AstrologyCalculationServiceTests {

	private final AstrologyCalculationService service =
			new AstrologyCalculationService(new BirthInputNormalizationService());

	@Test
	void acceptsGregorianLeapDayAndPreservesLocalBirthTimeWithoutTimezoneConversion() {
		var input = new AstrologyCalculationInput(
				"Test", "Nữ", LocalDate.of(2000, 2, 29), LocalTime.of(23, 59), "Hà Nội, Việt Nam");

		var assessment = service.assess(input);

		assertEquals(input, assessment.input());
		assertEquals(HourBranch.ZI, assessment.normalizedBirthInput().hourBranch());
		assertEquals(CycleDateAdjustment.UNRESOLVED, assessment.normalizedBirthInput().cycleDateAdjustment());
		assertFalse(assessment.isComplete());
		assertTrue(assessment.gaps().contains(CalculationGap.TIMEZONE_AND_LOCATION));
		assertFalse(assessment.gaps().contains(CalculationGap.UNKNOWN_BIRTH_TIME));
	}

	@Test
	void acceptsMidnightAndLatestRepresentableLocalTimeWithoutInventingAnHourBranch() {
		var midnight = validInput(LocalTime.MIDNIGHT);
		var lastTime = validInput(LocalTime.MAX);

		assertEquals(LocalTime.MIDNIGHT, service.assess(midnight).input().birthTime());
		assertEquals(LocalTime.MAX, service.assess(lastTime).input().birthTime());
	}

	@Test
	void acceptsUnknownBirthTimeButReportsItAsUnresolved() {
		var assessment = service.assess(validInput(null));

		assertTrue(assessment.gaps().contains(CalculationGap.UNKNOWN_BIRTH_TIME));
		assertFalse(assessment.isComplete());
	}

	@Test
	void rejectsMissingBirthDate() {
		var input = new AstrologyCalculationInput("Test", "Nữ", null, LocalTime.NOON, "Hà Nội");

		assertThrows(NullPointerException.class, () -> service.assess(input));
	}

	@Test
	void rejectsMissingGender() {
		var input = new AstrologyCalculationInput("Test", " ", LocalDate.of(2000, 1, 1), LocalTime.NOON, "Hà Nội");

		assertThrows(IllegalArgumentException.class, () -> service.assess(input));
	}

	@Test
	void identifiesMissingBirthplaceWithoutDuplicatingTimezoneGap() {
		var input = new AstrologyCalculationInput(
				"Test", "Nữ", LocalDate.of(2000, 1, 1), LocalTime.NOON, " ");

		var gaps = service.assess(input).gaps();

		assertTrue(gaps.contains(CalculationGap.BIRTHPLACE));
		assertEquals(1, gaps.stream().filter(gap -> gap == CalculationGap.TIMEZONE_AND_LOCATION).count());
	}

	@Test
	void reportsEveryAstrologyRuleThatStillNeedsAnAuthoritativeConvention() {
		var assessment = service.assess(validInput(LocalTime.NOON));

		assertEquals(List.of(
				CalculationGap.LUNAR_CALENDAR,
				CalculationGap.CAN_CHI_YEAR_BOUNDARY,
				CalculationGap.CAN_CHI_RULES,
				CalculationGap.MENH_AND_CUC,
				CalculationGap.TWELVE_PALACES,
				CalculationGap.STAR_PLACEMENT,
				CalculationGap.TIMEZONE_AND_LOCATION), assessment.gaps());
	}

	private AstrologyCalculationInput validInput(LocalTime time) {
		return new AstrologyCalculationInput(
				"Test", "Nữ", LocalDate.of(2024, 2, 29), time, "Hà Nội, Việt Nam");
	}
}
