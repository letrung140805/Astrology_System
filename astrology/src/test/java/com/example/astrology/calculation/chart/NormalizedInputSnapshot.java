package com.example.astrology.calculation.chart;

import java.time.LocalTime;

import com.example.astrology.calculation.calendar.BirthDateInput;
import com.example.astrology.calculation.calendar.CycleDateAdjustment;
import com.example.astrology.calculation.calendar.HourBranch;
import com.example.astrology.calculation.calendar.NormalizedBirthInput;

public record NormalizedInputSnapshot(
		BirthDateInput birthDate,
		LocalTime localBirthTime,
		String timeZoneId,
		String resolvedUtcOffset,
		HourBranch hourBranch,
		CycleDateAdjustment cycleDateAdjustment) {

	public static NormalizedInputSnapshot from(NormalizedBirthInput input) {
		return new NormalizedInputSnapshot(
				input.birthDate(),
				input.localBirthTime(),
				input.zoneId() == null ? null : input.zoneId().getId(),
				input.resolvedOffset() == null ? null : input.resolvedOffset().getId(),
				input.hourBranch(),
				input.cycleDateAdjustment());
	}
}
