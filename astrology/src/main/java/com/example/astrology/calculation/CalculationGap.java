package com.example.astrology.calculation;

public enum CalculationGap {

	LUNAR_CALENDAR("Ngày dương chưa đổi được sang âm lịch trong phạm vi nguồn lịch hiện hỗ trợ."),
	CAN_CHI_YEAR_BOUNDARY("Chưa xác định được năm Can Chi theo quy ước đổi năm đã chọn."),
	CAN_CHI_RULES("Can Chi tháng, ngày và giờ chưa được tính từ quy tắc có nguồn đối chiếu."),
	NATAL_ELEMENT("Chưa xác định được ngũ hành Nạp Âm năm theo bảng đã chọn."),
	MENH_AND_CUC("Chưa có quy tắc an Mệnh, an Thân và xác định Cục được chọn, kèm nguồn."),
	TWELVE_PALACES("Chưa có quy tắc an 12 cung, cung khởi và chiều an cung."),
	STAR_PLACEMENT("Chưa có bảng/quy tắc an sao, miếu vượng hãm và tứ hóa có nguồn kiểm chứng."),
	TIMEZONE_AND_LOCATION("Chưa có tọa độ, múi giờ lịch sử hoặc chính sách hiệu chỉnh giờ mặt trời; giữ nguyên giờ địa phương được nhập."),
	BIRTHPLACE("Chưa cung cấp địa điểm sinh để xác định vị trí địa lý."),
	UNKNOWN_BIRTH_TIME("Thiếu giờ sinh; chưa xác định quy tắc lập lá số một phần khi không biết giờ.");

	private final String description;

	CalculationGap(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
