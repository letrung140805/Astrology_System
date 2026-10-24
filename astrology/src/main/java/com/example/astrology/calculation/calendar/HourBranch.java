package com.example.astrology.calculation.calendar;

import java.time.LocalTime;
import java.util.Objects;

public enum HourBranch {
	ZI(EarthlyBranch.TY, LocalTime.of(23, 0), LocalTime.of(1, 0), true),
	CHOU(EarthlyBranch.SUU, LocalTime.of(1, 0), LocalTime.of(3, 0), false),
	YIN(EarthlyBranch.DAN, LocalTime.of(3, 0), LocalTime.of(5, 0), false),
	MAO(EarthlyBranch.MAO, LocalTime.of(5, 0), LocalTime.of(7, 0), false),
	CHEN(EarthlyBranch.THIN, LocalTime.of(7, 0), LocalTime.of(9, 0), false),
	SI(EarthlyBranch.TI, LocalTime.of(9, 0), LocalTime.of(11, 0), false),
	WU(EarthlyBranch.NGO, LocalTime.of(11, 0), LocalTime.of(13, 0), false),
	WEI(EarthlyBranch.MUI, LocalTime.of(13, 0), LocalTime.of(15, 0), false),
	SHEN(EarthlyBranch.THAN, LocalTime.of(15, 0), LocalTime.of(17, 0), false),
	YOU(EarthlyBranch.DAU, LocalTime.of(17, 0), LocalTime.of(19, 0), false),
	XU(EarthlyBranch.TUAT, LocalTime.of(19, 0), LocalTime.of(21, 0), false),
	HAI(EarthlyBranch.HOI, LocalTime.of(21, 0), LocalTime.of(23, 0), false);

	private final EarthlyBranch earthlyBranch;
	private final LocalTime startInclusive;
	private final LocalTime endExclusive;
	private final boolean crossesMidnight;

	HourBranch(EarthlyBranch earthlyBranch, LocalTime startInclusive, LocalTime endExclusive, boolean crossesMidnight) {
		this.earthlyBranch = earthlyBranch;
		this.startInclusive = startInclusive;
		this.endExclusive = endExclusive;
		this.crossesMidnight = crossesMidnight;
	}

	public String vietnameseName() {
		return earthlyBranch.vietnameseName();
	}

	public EarthlyBranch earthlyBranch() {
		return earthlyBranch;
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
