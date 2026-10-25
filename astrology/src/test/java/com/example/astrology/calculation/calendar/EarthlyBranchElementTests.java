package com.example.astrology.calculation.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.example.astrology.calculation.FiveElement;

class EarthlyBranchElementTests {

	@Test
	void mapsEveryBranchToItsElement() {
		assertEquals(FiveElement.WATER, EarthlyBranch.TY.element());
		assertEquals(FiveElement.EARTH, EarthlyBranch.SUU.element());
		assertEquals(FiveElement.WOOD, EarthlyBranch.DAN.element());
		assertEquals(FiveElement.WOOD, EarthlyBranch.MAO.element());
		assertEquals(FiveElement.EARTH, EarthlyBranch.THIN.element());
		assertEquals(FiveElement.FIRE, EarthlyBranch.TI.element());
		assertEquals(FiveElement.FIRE, EarthlyBranch.NGO.element());
		assertEquals(FiveElement.EARTH, EarthlyBranch.MUI.element());
		assertEquals(FiveElement.METAL, EarthlyBranch.THAN.element());
		assertEquals(FiveElement.METAL, EarthlyBranch.DAU.element());
		assertEquals(FiveElement.EARTH, EarthlyBranch.TUAT.element());
		assertEquals(FiveElement.WATER, EarthlyBranch.HOI.element());
	}
}
