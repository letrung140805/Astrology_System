package com.example.astrology.calculation.chart;

import com.example.astrology.calculation.calendar.EarthlyBranch;

public enum FiveElementsBureau {
	WATER_TWO("Thủy Nhị Cục", 2, EarthlyBranch.THAN),
	WOOD_THREE("Mộc Tam Cục", 3, EarthlyBranch.HOI),
	METAL_FOUR("Kim Tứ Cục", 4, EarthlyBranch.TI),
	EARTH_FIVE("Thổ Ngũ Cục", 5, EarthlyBranch.THAN),
	FIRE_SIX("Hỏa Lục Cục", 6, EarthlyBranch.DAN);

	private final String vietnameseName;
	private final int divisor;
	private final EarthlyBranch changShengStart;

	FiveElementsBureau(String vietnameseName, int divisor, EarthlyBranch changShengStart) {
		this.vietnameseName = vietnameseName;
		this.divisor = divisor;
		this.changShengStart = changShengStart;
	}

	public String vietnameseName() {
		return vietnameseName;
	}

	public int divisor() {
		return divisor;
	}

	public EarthlyBranch changShengStart() {
		return changShengStart;
	}
}
