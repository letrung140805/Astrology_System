package com.example.astrology.calculation.calendar;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Adapter boundary for Vietnamese lunar-calendar conversions. Implementations
 * must identify their source, version, supported range, and time-zone policy.
 */
public interface VietnameseLunarCalendarProvider {

	String implementationName();

	String implementationVersion();

	String sourceUrl();

	LunarDate toLunar(LocalDate solarDate, ZoneId zoneId);

	LocalDate toSolar(LunarDate lunarDate, ZoneId zoneId);

	record LunarDate(int year, int month, int day, boolean leapMonth) {
	}
}
