package com.example.astrology.calculation.calendar;

public enum HeavenlyStem {
	JIA("Giáp", true),
	YI("Ất", false),
	BING("Bính", true),
	DING("Đinh", false),
	WU("Mậu", true),
	JI("Kỷ", false),
	GENG("Canh", true),
	XIN("Tân", false),
	REN("Nhâm", true),
	GUI("Quý", false);

	private final String vietnameseName;
	private final boolean yang;

	HeavenlyStem(String vietnameseName, boolean yang) {
		this.vietnameseName = vietnameseName;
		this.yang = yang;
	}

	public String vietnameseName() {
		return vietnameseName;
	}

	public boolean isYang() {
		return yang;
	}
}
