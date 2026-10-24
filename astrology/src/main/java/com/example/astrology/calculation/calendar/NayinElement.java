package com.example.astrology.calculation.calendar;

public enum NayinElement {
	METAL("Kim"),
	FIRE("Hỏa"),
	WOOD("Mộc"),
	EARTH("Thổ"),
	WATER("Thủy");

	private final String vietnameseName;

	NayinElement(String vietnameseName) {
		this.vietnameseName = vietnameseName;
	}

	public String vietnameseName() {
		return vietnameseName;
	}
}
