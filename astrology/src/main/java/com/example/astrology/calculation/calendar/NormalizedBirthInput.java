package com.example.astrology.calculation.calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public record NormalizedBirthInput(
		BirthDateInput birthDate,
		LocalTime localBirthTime,
		ZoneId zoneId,
		ZoneOffset resolvedOffset,
		HourBranch hourBranch,
		CycleDateAdjustment cycleDateAdjustment) {

	public LocalDateTime solarLocalDateTime() {
		if (birthDate.calendarSystem() != BirthCalendarSystem.SOLAR || localBirthTime == null) {
			throw new IllegalStateException("Chỉ ngày dương lịch đã có giờ mới tạo LocalDateTime.");
		}
		return LocalDate.of(birthDate.year(), birthDate.month(), birthDate.day()).atTime(localBirthTime);
	}
}
