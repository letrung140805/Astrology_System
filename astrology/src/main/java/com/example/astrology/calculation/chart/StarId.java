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
	HUU_BAT("Hữu Bật"),
	HOA_LOC("Hóa Lộc"),
	HOA_QUYEN("Hóa Quyền"),
	HOA_KHOA("Hóa Khoa"),
	HOA_KY("Hóa Kỵ"),
	BAC_SI("Bác Sĩ"),
	LUC_SI("Lực Sĩ"),
	THANH_LONG("Thanh Long"),
	TIEU_HAO("Tiểu Hao"),
	TUONG_QUAN("Tướng Quân"),
	TAU_THU("Tấu Thư"),
	PHI_LIEM("Phi Liêm"),
	HY_THAN("Hỷ Thần"),
	BENH_PHU("Bệnh Phù"),
	DAI_HAO("Đại Hao"),
	PHUC_BINH("Phục Binh"),
	QUAN_PHU_BAC_SI("Quan Phủ"),
	THAI_TUE("Thái Tuế"),
	THIEU_DUONG("Thiếu Dương"),
	THIEN_KHONG("Thiên Không"),
	TANG_MON("Tang Môn"),
	THIEU_AM("Thiếu Âm"),
	QUAN_PHU_THAI_TUE("Quan Phù"),
	TU_PHU("Tử Phù"),
	TUE_PHA("Tuế Phá"),
	LONG_DUC("Long Đức"),
	BACH_HO("Bạch Hổ"),
	PHUC_DUC_STAR("Phúc Đức"),
	DIEU_KHACH("Điếu Khách"),
	TRUC_PHU("Trực Phù"),
	TRUONG_SINH("Trường Sinh"),
	MOC_DUC("Mộc Dục"),
	QUAN_DOI("Quan Đới"),
	LAM_QUAN("Lâm Quan"),
	DE_VUONG("Đế Vượng"),
	SUY("Suy"),
	BENH("Bệnh"),
	TU("Tử"),
	MO("Mộ"),
	TUYET("Tuyệt"),
	THAI("Thai"),
	DUONG("Dưỡng");

	private final String vietnameseName;

	StarId(String vietnameseName) {
		this.vietnameseName = vietnameseName;
	}

	public String vietnameseName() {
		return vietnameseName;
	}
}
