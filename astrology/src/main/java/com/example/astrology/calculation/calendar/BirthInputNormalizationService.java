package com.example.astrology.calculation.calendar;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.zone.ZoneRules;
import java.util.List;
import java.util.Objects;

import com.example.astrology.calculation.AstrologyCalculationInput;
import org.springframework.stereotype.Service;

@Service
public class BirthInputNormalizationService {

	public NormalizedBirthInput normalize(AstrologyCalculationInput input) {
		Objects.requireNonNull(input, "Thông tin đầu vào không được để trống.");
		return normalizeSolar(input.birthDate(), input.birthTime(), null, null,
				ZiHourDayBoundary.UNSPECIFIED);
	}

	public NormalizedBirthInput normalize(
			BirthDateInput birthDate,
			LocalTime localBirthTime,
			ZoneId zoneId,
			ZoneOffset explicitOffset,
			ZiHourDayBoundary ziHourDayBoundary) {
		Objects.requireNonNull(birthDate, "Ngày sinh không được để trống.");
		Objects.requireNonNull(ziHourDayBoundary, "Chính sách đổi ngày giờ Tý không được để trống.");
		if (explicitOffset != null && zoneId == null) {
			throw new IllegalArgumentException("Cần có múi giờ IANA khi cung cấp UTC offset.");
		}

		ZoneOffset resolvedOffset = null;
		if (birthDate.calendarSystem() == BirthCalendarSystem.SOLAR
				&& localBirthTime != null && zoneId != null) {
			resolvedOffset = resolveOffset(birthDate, localBirthTime, zoneId, explicitOffset);
		}

		HourBranch hourBranch = localBirthTime == null ? null : HourBranch.fromLocalTime(localBirthTime);
		CycleDateAdjustment adjustment = determineCycleDateAdjustment(localBirthTime, ziHourDayBoundary);

		return new NormalizedBirthInput(
				birthDate, localBirthTime, zoneId, resolvedOffset, hourBranch, adjustment);
	}

	public NormalizedBirthInput normalizeSolar(
			LocalDate date,
			LocalTime time,
			ZoneId zoneId,
			ZoneOffset explicitOffset,
			ZiHourDayBoundary ziHourDayBoundary) {
		return normalize(BirthDateInput.solar(date), time, zoneId, explicitOffset, ziHourDayBoundary);
	}

	private ZoneOffset resolveOffset(
			BirthDateInput birthDate,
			LocalTime localBirthTime,
			ZoneId zoneId,
			ZoneOffset explicitOffset) {
		LocalDateTime localDateTime = LocalDate.of(
				birthDate.year(), birthDate.month(), birthDate.day()).atTime(localBirthTime);
		ZoneRules rules = zoneId.getRules();
		List<ZoneOffset> validOffsets = rules.getValidOffsets(localDateTime);

		if (validOffsets.isEmpty()) {
			throw new DateTimeException("Giờ sinh địa phương không tồn tại trong múi giờ " + zoneId + ".");
		}
		if (explicitOffset != null) {
			if (!validOffsets.contains(explicitOffset)) {
				throw new DateTimeException("UTC offset không hợp lệ cho ngày giờ và múi giờ đã nhập.");
			}
			return explicitOffset;
		}
		if (validOffsets.size() > 1) {
			throw new DateTimeException("Giờ sinh bị lặp do đổi giờ; cần chỉ định UTC offset.");
		}
		return validOffsets.get(0);
	}

	private CycleDateAdjustment determineCycleDateAdjustment(
			LocalTime localBirthTime,
			ZiHourDayBoundary ziHourDayBoundary) {
		if (localBirthTime == null || localBirthTime.isBefore(LocalTime.of(23, 0))) {
			return CycleDateAdjustment.NONE;
		}
		return switch (ziHourDayBoundary) {
			case CHANGE_AT_23 -> CycleDateAdjustment.NEXT_DAY;
			case CHANGE_AT_MIDNIGHT -> CycleDateAdjustment.NONE;
			case UNSPECIFIED -> CycleDateAdjustment.UNRESOLVED;
		};
	}
}
