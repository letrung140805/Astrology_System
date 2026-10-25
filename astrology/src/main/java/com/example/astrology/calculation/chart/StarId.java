package com.example.astrology.calculation.chart;

import com.example.astrology.calculation.FiveElement;

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
	THIEN_KHOI("Thiên Khôi"),
	THIEN_VIET("Thiên Việt"),
	THIEN_MA("Thiên Mã"),
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

	public FiveElement element() {
		return switch (this) {
			case TU_VI, THIEN_PHU, CU_MON, LOC_TON, TA_PHU, PHUC_DUC_STAR,
					BENH, MO -> FiveElement.EARTH;
			case THIEN_CO, THAM_LANG, THIEN_LUONG, TUONG_QUAN, TANG_MON,
					MOC_DUC, DUONG -> FiveElement.WOOD;
			case THIEN_DONG, THAI_AM, THIEN_TUONG, PHA_QUAN, VAN_KHUC,
					HUU_BAT, BAC_SI, THANH_LONG, BENH_PHU, DAI_HAO,
					THIEU_AM, LONG_DUC, SUY, TU, TUYET, THAI -> FiveElement.WATER;
			case THAI_DUONG, LIEM_TRINH, THIEN_KHOI, THIEN_VIET, THIEN_MA,
					HOA_LOC, HOA_QUYEN, LUC_SI, TIEU_HAO, PHI_LIEM, HY_THAN,
					PHUC_BINH, QUAN_PHU_BAC_SI, THAI_TUE, THIEU_DUONG,
					THIEN_KHONG, QUAN_PHU_THAI_TUE, TU_PHU, TUE_PHA,
					DIEU_KHACH, TRUC_PHU, LAM_QUAN -> FiveElement.FIRE;
			case VU_KHUC, THAT_SAT, VAN_XUONG, HOA_KHOA, HOA_KY, TAU_THU,
					BACH_HO, QUAN_DOI -> FiveElement.METAL;
			case TRUONG_SINH, DE_VUONG -> FiveElement.WOOD;
		};
	}
}
