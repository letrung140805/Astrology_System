package com.example.astrology.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public record ChartHistoryItem(
		String resultUrl,
		String fullName,
		LocalDate birthDate,
		LocalDateTime generatedAt,
		String menhName,
		String cucName) {

	public ChartHistoryItem {
		Objects.requireNonNull(resultUrl, "Đường dẫn lá số không được để trống.");
		Objects.requireNonNull(fullName, "Tên hồ sơ không được để trống.");
		Objects.requireNonNull(birthDate, "Ngày sinh không được để trống.");
		Objects.requireNonNull(generatedAt, "Thời điểm tạo lá số không được để trống.");
	}
}
