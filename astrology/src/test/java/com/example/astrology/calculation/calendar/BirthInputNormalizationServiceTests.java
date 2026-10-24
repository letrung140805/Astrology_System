package com.example.astrology.calculation.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class BirthInputNormalizationServiceTests {

	private final BirthInputNormalizationService service = new BirthInputNormalizationService();

	@Test
	void mapsPublishedHourIntervalsAtTheirBoundaries() {
		assertEquals(HourBranch.ZI, branchAt("23:00"));
		assertEquals(HourBranch.ZI, branchAt("00:59"));
		assertEquals(HourBranch.CHOU, branchAt("01:00"));
		assertEquals(HourBranch.CHOU, branchAt("02:59"));
		assertEquals(HourBranch.YIN, branchAt("03:00"));
		assertEquals(HourBranch.HAI, branchAt("22:59"));
	}

	@Test
	void keepsHourBranchDistinctFromTheEarthlyBranchPositionType() {
		assertEquals(EarthlyBranch.TY, HourBranch.ZI.earthlyBranch());
		assertEquals("Tý", HourBranch.ZI.vietnameseName());
	}

	@Test
	void recordsUnspecifiedZiHourDateBoundaryInsteadOfGuessing() {
		var normalized = service.normalizeSolar(
				LocalDate.of(2000, 1, 1), LocalTime.of(23, 0),
				null, null, ZiHourDayBoundary.UNSPECIFIED);

		assertEquals(CycleDateAdjustment.UNRESOLVED, normalized.cycleDateAdjustment());
		assertEquals(LocalDate.of(2000, 1, 1), normalized.solarLocalDateTime().toLocalDate());
	}

	@Test
	void appliesExplicitZiHourDatePolicyAsAnAdjustmentWithoutChangingBirthInput() {
		var nextDay = service.normalizeSolar(
				LocalDate.of(2000, 1, 1), LocalTime.of(23, 0),
				null, null, ZiHourDayBoundary.CHANGE_AT_23);
		var sameDay = service.normalizeSolar(
				LocalDate.of(2000, 1, 1), LocalTime.of(23, 0),
				null, null, ZiHourDayBoundary.CHANGE_AT_MIDNIGHT);

		assertEquals(CycleDateAdjustment.NEXT_DAY, nextDay.cycleDateAdjustment());
		assertEquals(CycleDateAdjustment.NONE, sameDay.cycleDateAdjustment());
		assertEquals(LocalDate.of(2000, 1, 1), nextDay.solarLocalDateTime().toLocalDate());
	}

	@Test
	void resolvesHistoricalZoneOffsetForUnambiguousLocalTime() {
		var normalized = service.normalizeSolar(
				LocalDate.of(2024, 1, 15), LocalTime.NOON,
				ZoneId.of("Asia/Ho_Chi_Minh"), null, ZiHourDayBoundary.UNSPECIFIED);

		assertEquals(ZoneOffset.ofHours(7), normalized.resolvedOffset());
	}

	@Test
	void rejectsLocalTimeInsideDaylightSavingGap() {
		assertThrows(DateTimeException.class, () -> service.normalizeSolar(
				LocalDate.of(2024, 3, 10), LocalTime.of(2, 30),
				ZoneId.of("America/New_York"), null, ZiHourDayBoundary.UNSPECIFIED));
	}

	@Test
	void requiresExplicitOffsetForAmbiguousLocalTime() {
		assertThrows(DateTimeException.class, () -> service.normalizeSolar(
				LocalDate.of(2024, 11, 3), LocalTime.of(1, 30),
				ZoneId.of("America/New_York"), null, ZiHourDayBoundary.UNSPECIFIED));
	}

	@Test
	void acceptsAnExplicitOffsetWhenItIsValidForAnAmbiguousLocalTime() {
		var normalized = service.normalizeSolar(
				LocalDate.of(2024, 11, 3), LocalTime.of(1, 30),
				ZoneId.of("America/New_York"), ZoneOffset.ofHours(-5),
				ZiHourDayBoundary.UNSPECIFIED);

		assertEquals(ZoneOffset.ofHours(-5), normalized.resolvedOffset());
	}

	@Test
	void doesNotTryGregorianDstRulesForUnconvertedLunarDate() {
		var normalized = service.normalize(
				new BirthDateInput(BirthCalendarSystem.LUNAR, 2024, 2, 1, true),
				LocalTime.of(12, 0), ZoneId.of("America/New_York"), null,
				ZiHourDayBoundary.UNSPECIFIED);

		org.junit.jupiter.api.Assertions.assertNull(normalized.resolvedOffset());
	}

	private HourBranch branchAt(String time) {
		return service.normalizeSolar(
				LocalDate.of(2000, 1, 1), LocalTime.parse(time),
				null, null, ZiHourDayBoundary.UNSPECIFIED).hourBranch();
	}
}
