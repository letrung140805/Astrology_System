package com.example.astrology.calculation.chart;

public enum StarId {
	TU_VI("Tử Vi"),
	THIEN_CO("Thiên Cơ"),
	THAI_DUONG("Thái Dương"),
	VU_KHUC("Vũ Khúc"),
	THIEN_DONG("Thiên Đồng"),
	LIEM_TRINH("Liêm Trinh"),
	THIEN_PHU("Thiên Phủ"),
	THAI_AM("Thái Âm"),
	THAM_LANG("Tham Lang"),
	CU_MON("Cự Môn"),
	THIEN_TUONG("Thiên Tướng"),
	THIEN_LUONG("Thiên Lương"),
	THAT_SAT("Thất Sát"),
	PHA_QUAN("Phá Quân"),
	LOC_TON("Lộc Tồn"),
	VAN_XUONG("Văn Xương"),
	VAN_KHUC("Văn Khúc"),
	TA_PHU("Tả Phù"),
	HUU_BAT("Hữu Bật");

	private final String vietnameseName;

	StarId(String vietnameseName) {
		this.vietnameseName = vietnameseName;
	}

	public String vietnameseName() {
		return vietnameseName;
	}
}
