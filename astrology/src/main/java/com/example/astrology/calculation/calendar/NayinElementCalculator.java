package com.example.astrology.calculation.calendar;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

@Service
public class NayinElementCalculator {

	public static final String SOURCE_REVISION = "79ebf331e95e8f2005a0c8ef62b82a2a845fc893";
	public static final String SOURCE_URL = "https://github.com/HAHVAC/CEStu-vi-ai/blob/"
			+ SOURCE_REVISION + "/lib/ziwei/constants.ts";

	private static final List<NayinElement> ELEMENTS_BY_PAIR = List.of(
			NayinElement.METAL, NayinElement.FIRE, NayinElement.WOOD, NayinElement.EARTH,
			NayinElement.METAL, NayinElement.FIRE, NayinElement.WATER, NayinElement.EARTH,
			NayinElement.METAL, NayinElement.WOOD, NayinElement.WATER, NayinElement.EARTH,
			NayinElement.FIRE, NayinElement.WOOD, NayinElement.WATER, NayinElement.METAL,
			NayinElement.FIRE, NayinElement.WOOD, NayinElement.EARTH, NayinElement.METAL,
			NayinElement.FIRE, NayinElement.WATER, NayinElement.EARTH, NayinElement.METAL,
			NayinElement.WOOD, NayinElement.WATER, NayinElement.EARTH, NayinElement.FIRE,
			NayinElement.WOOD, NayinElement.WATER);

	private final SexagenaryYearCalculator sexagenaryYearCalculator;

	public NayinElementCalculator(SexagenaryYearCalculator sexagenaryYearCalculator) {
		this.sexagenaryYearCalculator = Objects.requireNonNull(
				sexagenaryYearCalculator, "Bộ tính Can Chi năm không được để trống.");
	}

	public NayinElement calculate(int lunarYear) {
		int cyclePosition = sexagenaryYearCalculator.cyclePosition(lunarYear);
		return ELEMENTS_BY_PAIR.get(cyclePosition / 2);
	}
}
