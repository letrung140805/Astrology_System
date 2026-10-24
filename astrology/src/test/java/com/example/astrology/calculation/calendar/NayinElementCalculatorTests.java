package com.example.astrology.calculation.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class NayinElementCalculatorTests {

	private final NayinElementCalculator calculator =
			new NayinElementCalculator(new SexagenaryYearCalculator());

	@Test
	void mapsKnownSexagenaryPairsFromThePinnedReferenceTable() {
		assertEquals(NayinElement.METAL, calculator.calculate(1984));
		assertEquals(NayinElement.METAL, calculator.calculate(1985));
		assertEquals(NayinElement.WATER, calculator.calculate(2005));
		assertEquals(NayinElement.WATER, calculator.calculate(2026));
	}

	@Test
	void rejectsYearsOutsideTheSexagenaryYearCalculatorDomain() {
		assertThrows(IllegalArgumentException.class, () -> calculator.calculate(0));
	}
}
