package com.example.astrology.calculation.chart;

import java.util.Objects;

import com.example.astrology.calculation.calendar.EarthlyBranch;
import com.example.astrology.calculation.calendar.HeavenlyStem;
import com.example.astrology.calculation.calendar.HourBranch;

public record LunarChartInput(
		int lunarYear,
		HeavenlyStem yearStem,
		EarthlyBranch yearBranch,
		int lunarMonth,
		boolean leapMonth,
		int lunarDay,
		HourBranch birthHour,
		BirthSex birthSex) {

	public LunarChartInput {
		Objects.requireNonNull(yearStem, "Thiên can năm sinh không được để trống.");
		Objects.requireNonNull(yearBranch, "Địa chi năm sinh không được để trống.");
		Objects.requireNonNull(birthHour, "Địa chi giờ sinh không được để trống.");
		Objects.requireNonNull(birthSex, "Giới tính theo quy ước an sao không được để trống.");
		if (lunarYear < 1) {
			throw new IllegalArgumentException("Năm âm lịch phải lớn hơn 0.");
		}
		if (lunarMonth < 1 || lunarMonth > 12) {
			throw new IllegalArgumentException("Tháng âm lịch phải nằm trong khoảng 1-12.");
		}
		if (lunarDay < 1 || lunarDay > 30) {
			throw new IllegalArgumentException("Ngày âm lịch phải nằm trong khoảng 1-30.");
		}
	}

	public int effectiveMonthForTracuutuviProfile() {
		return leapMonth ? lunarMonth == 12 ? 1 : lunarMonth + 1 : lunarMonth;
	}
}
