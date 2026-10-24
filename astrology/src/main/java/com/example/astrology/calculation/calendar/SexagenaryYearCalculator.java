package com.example.astrology.calculation.calendar;

import org.springframework.stereotype.Service;

@Service
public class SexagenaryYearCalculator {

	private static final int GIAP_TY_YEAR = 1984;

	public CanChiYear calculate(int lunarYear) {
		int cyclePosition = cyclePosition(lunarYear);
		HeavenlyStem stem = HeavenlyStem.values()[cyclePosition % HeavenlyStem.values().length];
		EarthlyBranch branch = EarthlyBranch.values()[cyclePosition % EarthlyBranch.values().length];
		return new CanChiYear(stem, branch);
	}

	public int cyclePosition(int lunarYear) {
		if (lunarYear < 1) {
			throw new IllegalArgumentException("Năm âm lịch phải lớn hơn 0.");
		}
		return Math.floorMod(lunarYear - GIAP_TY_YEAR, 60);
	}

	public record CanChiYear(HeavenlyStem stem, EarthlyBranch branch) {
	}
}
