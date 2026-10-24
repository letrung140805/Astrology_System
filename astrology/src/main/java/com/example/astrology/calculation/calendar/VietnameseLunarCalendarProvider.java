package com.example.astrology.calculation.calendar;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Adapter boundary for an independently verified Vietnamese lunar calendar.
 * No implementation is registered until a usable library and its supported
 * date range, leap-month behavior, timezone, and reference fixtures are verified.
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
