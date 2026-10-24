package com.example.astrology.calculation.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class BirthDateInputTests {

	@Test
	void acceptsGregorianLeapDay() {
		assertEquals(LocalDate.of(2024, 2, 29),
				LocalDate.of(BirthDateInput.solar(LocalDate.of(2024, 2, 29)).year(),
						BirthDateInput.solar(LocalDate.of(2024, 2, 29)).month(),
						BirthDateInput.solar(LocalDate.of(2024, 2, 29)).day()));
	}

	@Test
	void rejectsInvalidGregorianDate() {
		assertThrows(IllegalArgumentException.class,
				() -> new BirthDateInput(BirthCalendarSystem.SOLAR, 2023, 2, 29, false));
	}

	@Test
	void keepsLunarLeapMonthExplicitWithoutConvertingIt() {
		var input = new BirthDateInput(BirthCalendarSystem.LUNAR, 2023, 2, 1, true);

		assertEquals(BirthCalendarSystem.LUNAR, input.calendarSystem());
		assertEquals(2, input.month());
		assertEquals(1, input.day());
		org.junit.jupiter.api.Assertions.assertTrue(input.leapMonth());
	}

	@Test
	void rejectsThirtyFirstDayForLunarInput() {
		assertThrows(IllegalArgumentException.class,
				() -> new BirthDateInput(BirthCalendarSystem.LUNAR, 2023, 2, 31, false));
	}

	@Test
	void rejectsLeapMonthFlagForSolarInput() {
		assertThrows(IllegalArgumentException.class,
				() -> new BirthDateInput(BirthCalendarSystem.SOLAR, 2024, 2, 29, true));
	}
}
