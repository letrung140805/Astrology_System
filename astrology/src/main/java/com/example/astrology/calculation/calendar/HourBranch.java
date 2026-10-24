package com.example.astrology.calculation.calendar;

import java.time.LocalTime;
import java.util.Objects;

public enum HourBranch {
	ZI("Tý", LocalTime.of(23, 0), LocalTime.of(1, 0), true),
	CHOU("Sửu", LocalTime.of(1, 0), LocalTime.of(3, 0), false),
	YIN("Dần", LocalTime.of(3, 0), LocalTime.of(5, 0), false),
	MAO("Mão", LocalTime.of(5, 0), LocalTime.of(7, 0), false),
	CHEN("Thìn", LocalTime.of(7, 0), LocalTime.of(9, 0), false),
	SI("Tỵ", LocalTime.of(9, 0), LocalTime.of(11, 0), false),
	WU("Ngọ", LocalTime.of(11, 0), LocalTime.of(13, 0), false),
	WEI("Mùi", LocalTime.of(13, 0), LocalTime.of(15, 0), false),
	SHEN("Thân", LocalTime.of(15, 0), LocalTime.of(17, 0), false),
	YOU("Dậu", LocalTime.of(17, 0), LocalTime.of(19, 0), false),
	XU("Tuất", LocalTime.of(19, 0), LocalTime.of(21, 0), false),
	HAI("Hợi", LocalTime.of(21, 0), LocalTime.of(23, 0), false);

	private final String vietnameseName;
	private final LocalTime startInclusive;
	private final LocalTime endExclusive;
	private final boolean crossesMidnight;

	HourBranch(String vietnameseName, LocalTime startInclusive, LocalTime endExclusive, boolean crossesMidnight) {
		this.vietnameseName = vietnameseName;
		this.startInclusive = startInclusive;
		this.endExclusive = endExclusive;
		this.crossesMidnight = crossesMidnight;
	}

	public String vietnameseName() {
		return vietnameseName;
	}

	public static HourBranch fromLocalTime(LocalTime time) {
		Objects.requireNonNull(time, "Giờ sinh không được để trống.");
		for (HourBranch branch : values()) {
			boolean matches = branch.crossesMidnight
					? !time.isBefore(branch.startInclusive) || time.isBefore(branch.endExclusive)
					: !time.isBefore(branch.startInclusive) && time.isBefore(branch.endExclusive);
			if (matches) {
				return branch;
			}
		}
		throw new IllegalStateException("Không tìm thấy địa chi giờ cho " + time + ".");
	}
}
