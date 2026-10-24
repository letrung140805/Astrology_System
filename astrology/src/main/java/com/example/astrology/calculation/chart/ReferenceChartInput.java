package com.example.astrology.calculation.chart;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Objects;

import com.example.astrology.calculation.calendar.BirthDateInput;
import com.example.astrology.calculation.calendar.BirthInputNormalizationService;
import com.example.astrology.calculation.calendar.NormalizedBirthInput;
import com.example.astrology.calculation.calendar.ZiHourDayBoundary;

public record ReferenceChartInput(
		BirthDateInput birthDate,
		String localBirthTime,
		String gender,
		String birthplace,
		String timeZoneId,
		String explicitUtcOffset,
		ZiHourDayBoundary ziHourDayBoundary) {

	public ReferenceChartInput {
		Objects.requireNonNull(birthDate, "Ngày sinh fixture không được để trống.");
		Objects.requireNonNull(ziHourDayBoundary, "Quy ước giờ Tý phải được ghi rõ.");
		if (localBirthTime != null) {
			LocalTime.parse(localBirthTime);
		}
		if (timeZoneId != null) {
			ZoneId.of(timeZoneId);
		}
		if (explicitUtcOffset != null) {
			ZoneOffset.of(explicitUtcOffset);
		}
	}

	public NormalizedBirthInput normalize(BirthInputNormalizationService service) {
		Objects.requireNonNull(service, "Dịch vụ chuẩn hóa không được để trống.");
		return service.normalize(
				birthDate,
				localBirthTime == null ? null : LocalTime.parse(localBirthTime),
				timeZoneId == null ? null : ZoneId.of(timeZoneId),
				explicitUtcOffset == null ? null : ZoneOffset.of(explicitUtcOffset),
				ziHourDayBoundary);
	}
}
