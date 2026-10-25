package com.example.astrology.calculation.calendar;

import com.example.astrology.calculation.FiveElement;

public enum EarthlyBranch {
	TY("Tý"),
	SUU("Sửu"),
	DAN("Dần"),
	MAO("Mão"),
	THIN("Thìn"),
	TI("Tỵ"),
	NGO("Ngọ"),
	MUI("Mùi"),
	THAN("Thân"),
	DAU("Dậu"),
	TUAT("Tuất"),
	HOI("Hợi");

	private final String vietnameseName;

	EarthlyBranch(String vietnameseName) {
		this.vietnameseName = vietnameseName;
	}

	public String vietnameseName() {
		return vietnameseName;
	}

	public FiveElement element() {
		return switch (this) {
			case TY, HOI -> FiveElement.WATER;
			case SUU, THIN, MUI, TUAT -> FiveElement.EARTH;
			case DAN, MAO -> FiveElement.WOOD;
			case TI, NGO -> FiveElement.FIRE;
			case THAN, DAU -> FiveElement.METAL;
		};
	}
}
