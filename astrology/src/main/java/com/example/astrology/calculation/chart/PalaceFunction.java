package com.example.astrology.calculation.chart;

public enum PalaceFunction {
	MENH("Mệnh"),
	PHU_MAU("Phụ Mẫu"),
	PHUC_DUC("Phúc Đức"),
	DIEN_TRACH("Điền Trạch"),
	QUAN_LOC("Quan Lộc"),
	NO_BOC("Nô Bộc"),
	THIEN_DI("Thiên Di"),
	TAT_ACH("Tật Ách"),
	TAI_BACH("Tài Bạch"),
	TU_TUC("Tử Tức"),
	PHU_THE("Phu Thê"),
	HUYNH_DE("Huynh Đệ");

	private final String vietnameseName;

	PalaceFunction(String vietnameseName) {
		this.vietnameseName = vietnameseName;
	}

	public String vietnameseName() {
		return vietnameseName;
	}
}
