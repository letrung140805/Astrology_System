package com.example.astrology.calculation.calendar;

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
}
