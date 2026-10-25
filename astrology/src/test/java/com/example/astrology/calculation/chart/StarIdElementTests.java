package com.example.astrology.calculation.chart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.example.astrology.calculation.FiveElement;

class StarIdElementTests {

	@Test
	void assignsFiveElementsToEveryDisplayedStar() {
		for (StarId star : StarId.values()) {
			assertNotNull(star.element(), star.name());
		}
	}

	@Test
	void mapsFourteenMajorStarsToTheirTraditionalElements() {
		assertEquals(FiveElement.EARTH, StarId.TU_VI.element());
		assertEquals(FiveElement.WOOD, StarId.THIEN_CO.element());
		assertEquals(FiveElement.FIRE, StarId.THAI_DUONG.element());
		assertEquals(FiveElement.METAL, StarId.VU_KHUC.element());
		assertEquals(FiveElement.WATER, StarId.THIEN_DONG.element());
		assertEquals(FiveElement.FIRE, StarId.LIEM_TRINH.element());
		assertEquals(FiveElement.EARTH, StarId.THIEN_PHU.element());
		assertEquals(FiveElement.WATER, StarId.THAI_AM.element());
		assertEquals(FiveElement.WOOD, StarId.THAM_LANG.element());
		assertEquals(FiveElement.EARTH, StarId.CU_MON.element());
		assertEquals(FiveElement.WATER, StarId.THIEN_TUONG.element());
		assertEquals(FiveElement.WOOD, StarId.THIEN_LUONG.element());
		assertEquals(FiveElement.METAL, StarId.THAT_SAT.element());
		assertEquals(FiveElement.WATER, StarId.PHA_QUAN.element());
	}
}
