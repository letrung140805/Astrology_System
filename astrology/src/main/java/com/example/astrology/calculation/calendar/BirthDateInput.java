package com.example.astrology.calculation.calendar;

import java.time.LocalDate;
import java.util.Objects;

public record BirthDateInput(
		BirthCalendarSystem calendarSystem,
		int year,
		int month,
		int day,
		boolean leapMonth) {

	public BirthDateInput {
		Objects.requireNonNull(calendarSystem, "Loại lịch là bắt buộc.");
		if (year < 1) {
			throw new IllegalArgumentException("Năm sinh phải lớn hơn 0.");
		}
		if (month < 1 || month > 12) {
			throw new IllegalArgumentException("Tháng sinh phải nằm trong khoảng 1-12.");
		}
		if (day < 1 || day > 31) {
			throw new IllegalArgumentException("Ngày sinh phải nằm trong khoảng 1-31.");
		}
		if (calendarSystem == BirthCalendarSystem.SOLAR && leapMonth) {
			throw new IllegalArgumentException("Lịch dương không có tháng nhuận âm lịch.");
		}
		if (calendarSystem == BirthCalendarSystem.LUNAR && day > 30) {
			throw new IllegalArgumentException("Ngày âm lịch phải nằm trong khoảng 1-30.");
		}
		if (calendarSystem == BirthCalendarSystem.SOLAR) {
			try {
				LocalDate.of(year, month, day);
			} catch (java.time.DateTimeException exception) {
				throw new IllegalArgumentException("Ngày dương lịch không hợp lệ.", exception);
			}
		}
	}

	public static BirthDateInput solar(LocalDate date) {
		Objects.requireNonNull(date, "Ngày sinh không được để trống.");
		return new BirthDateInput(BirthCalendarSystem.SOLAR,
				date.getYear(), date.getMonthValue(), date.getDayOfMonth(), false);
	}
}
