package com.example.astrology.calculation.calendar;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Objects;

import org.springframework.stereotype.Component;

/**
 * Vietnamese astronomical lunar-calendar conversion based on Ho Ngoc Duc's
 * published algorithm, using the time zone supplied by the caller.
 */
@Component
public class HoNgocDucLunarCalendarProvider implements VietnameseLunarCalendarProvider {

	private static final double NEW_MOON_CYCLE = 29.530588853;
	private static final double JULIAN_EPOCH = 2415021.076998695;
	private static final double DEGREES_TO_RADIANS = Math.PI / 180.0;

	@Override
	public String implementationName() {
		return "Ho Ngoc Duc Vietnamese lunar calendar algorithm";
	}

	@Override
	public String implementationVersion() {
		return "published-algorithm; supported solar years 1800-2199";
	}

	@Override
	public String sourceUrl() {
		return "https://xemamlich.uhm.vn/calrules.html";
	}

	@Override
	public LunarDate toLunar(LocalDate solarDate, ZoneId zoneId) {
		Objects.requireNonNull(solarDate, "Ngày dương lịch không được để trống.");
		Objects.requireNonNull(zoneId, "Múi giờ lịch không được để trống.");
		requireSupportedYear(solarDate.getYear());

		double timeZone = timeZoneHours(solarDate, zoneId);
		int dayNumber = julianDay(solarDate);
		int k = floorToInt((dayNumber - JULIAN_EPOCH) / NEW_MOON_CYCLE);
		int monthStart = newMoonDay(k + 1, timeZone);
		if (monthStart > dayNumber) {
			monthStart = newMoonDay(k, timeZone);
		}

		int month11Before = lunarMonth11(solarDate.getYear(), timeZone);
		int month11After = month11Before;
		int lunarYear;
		if (month11Before >= monthStart) {
			lunarYear = solarDate.getYear();
			month11Before = lunarMonth11(solarDate.getYear() - 1, timeZone);
		} else {
			lunarYear = solarDate.getYear() + 1;
			month11After = lunarMonth11(solarDate.getYear() + 1, timeZone);
		}

		int lunarDay = dayNumber - monthStart + 1;
		int monthOffset = floorToInt((monthStart - month11Before) / 29.0);
		int lunarMonth = monthOffset + 11;
		boolean leapMonth = false;
		if (month11After - month11Before > 365) {
			int leapMonthOffset = leapMonthOffset(month11Before, timeZone);
			if (monthOffset >= leapMonthOffset) {
				lunarMonth = monthOffset + 10;
				leapMonth = monthOffset == leapMonthOffset;
			}
		}
		if (lunarMonth > 12) {
			lunarMonth -= 12;
		}
		if (lunarMonth >= 11 && monthOffset < 4) {
			lunarYear--;
		}

		return new LunarDate(lunarYear, lunarMonth, lunarDay, leapMonth);
	}

	@Override
	public LocalDate toSolar(LunarDate lunarDate, ZoneId zoneId) {
		Objects.requireNonNull(lunarDate, "Ngày âm lịch không được để trống.");
		Objects.requireNonNull(zoneId, "Múi giờ lịch không được để trống.");
		if (lunarDate.year() < 1800 || lunarDate.year() > 2199
				|| lunarDate.month() < 1 || lunarDate.month() > 12
				|| lunarDate.day() < 1 || lunarDate.day() > 30) {
			throw new IllegalArgumentException("Ngày âm lịch nằm ngoài phạm vi hỗ trợ 1800-2199.");
		}

		double timeZone = timeZoneHours(
				LocalDate.of(lunarDate.year(), 7, 1), zoneId);
		int month11Before;
		int month11After;
		if (lunarDate.month() < 11) {
			month11Before = lunarMonth11(lunarDate.year() - 1, timeZone);
			month11After = lunarMonth11(lunarDate.year(), timeZone);
		} else {
			month11Before = lunarMonth11(lunarDate.year(), timeZone);
			month11After = lunarMonth11(lunarDate.year() + 1, timeZone);
		}

		int monthOffset = lunarDate.month() - 11;
		if (monthOffset < 0) {
			monthOffset += 12;
		}
		if (month11After - month11Before > 365) {
			int leapOffset = leapMonthOffset(month11Before, timeZone);
			int leapMonth = leapOffset - 2;
			if (leapMonth < 0) {
				leapMonth += 12;
			}
			if (lunarDate.leapMonth() && lunarDate.month() != leapMonth) {
				throw new DateTimeException("Tháng nhuận không tồn tại trong năm âm lịch đã chọn.");
			}
			if (lunarDate.leapMonth() || monthOffset >= leapOffset) {
				monthOffset++;
			}
		} else if (lunarDate.leapMonth()) {
			throw new DateTimeException("Năm âm lịch đã chọn không có tháng nhuận.");
		}

		int k = floorToInt(0.5 + (month11Before - JULIAN_EPOCH) / NEW_MOON_CYCLE);
		int monthStart = newMoonDay(k + monthOffset, timeZone);
		LocalDate solarDate = LocalDate.ofEpochDay(monthStart + lunarDate.day() - 1L - 2440588L);
		if (!toLunar(solarDate, zoneId).equals(lunarDate)) {
			throw new DateTimeException("Ngày âm lịch không hợp lệ hoặc không tồn tại.");
		}
		return solarDate;
	}

	private static double timeZoneHours(LocalDate date, ZoneId zoneId) {
		ZoneId zone = zoneId;
		return zone.getRules()
				.getOffset(LocalDateTime.of(date, LocalTime.NOON))
				.getTotalSeconds() / 3600.0;
	}

	private static void requireSupportedYear(int year) {
		if (year < 1800 || year > 2199) {
			throw new DateTimeException("Lịch âm hiện hỗ trợ năm dương lịch từ 1800 đến 2199.");
		}
	}

	private static int julianDay(LocalDate date) {
		int day = date.getDayOfMonth();
		int month = date.getMonthValue();
		int year = date.getYear();
		int a = (14 - month) / 12;
		int y = year + 4800 - a;
		int m = month + 12 * a - 3;
		int jd = day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045;
		if (jd < 2299161) {
			return day + (153 * m + 2) / 5 + 365 * y + y / 4 - 32083;
		}
		return jd;
	}

	private static int newMoonDay(int k, double timeZone) {
		double t = k / 1236.85;
		double t2 = t * t;
		double t3 = t2 * t;
		double jd1 = 2415020.75933 + 29.53058868 * k + 0.0001178 * t2 - 0.000000155 * t3;
		jd1 += 0.00033 * Math.sin((166.56 + 132.87 * t - 0.009173 * t2) * DEGREES_TO_RADIANS);
		double m = 359.2242 + 29.10535608 * k - 0.0000333 * t2 - 0.00000347 * t3;
		double moonAnomaly = 306.0253 + 385.81691806 * k + 0.0107306 * t2 + 0.00001236 * t3;
		double latitudeArgument = 21.2964 + 390.67050646 * k - 0.0016528 * t2 - 0.00000239 * t3;

		double correction = (0.1734 - 0.000393 * t) * Math.sin(m * DEGREES_TO_RADIANS)
				+ 0.0021 * Math.sin(2 * m * DEGREES_TO_RADIANS)
				- 0.4068 * Math.sin(moonAnomaly * DEGREES_TO_RADIANS)
				+ 0.0161 * Math.sin(2 * moonAnomaly * DEGREES_TO_RADIANS)
				- 0.0004 * Math.sin(3 * moonAnomaly * DEGREES_TO_RADIANS)
				+ 0.0104 * Math.sin(2 * latitudeArgument * DEGREES_TO_RADIANS)
				- 0.0051 * Math.sin((m + moonAnomaly) * DEGREES_TO_RADIANS)
				- 0.0074 * Math.sin((m - moonAnomaly) * DEGREES_TO_RADIANS)
				+ 0.0004 * Math.sin((2 * latitudeArgument + m) * DEGREES_TO_RADIANS)
				- 0.0004 * Math.sin((2 * latitudeArgument - m) * DEGREES_TO_RADIANS)
				- 0.0006 * Math.sin((2 * latitudeArgument + moonAnomaly) * DEGREES_TO_RADIANS)
				+ 0.0010 * Math.sin((2 * latitudeArgument - moonAnomaly) * DEGREES_TO_RADIANS)
				+ 0.0005 * Math.sin((2 * moonAnomaly + m) * DEGREES_TO_RADIANS);

		double deltaT = t < -11
				? 0.001 + 0.000839 * t + 0.0002261 * t2 - 0.00000845 * t3 - 0.000000081 * t * t3
				: -0.000278 + 0.000265 * t + 0.000262 * t2;
		return floorToInt(jd1 + correction - deltaT + 0.5 + timeZone / 24.0);
	}

	private static int sunLongitudeSector(int julianDay, double timeZone) {
		double t = (julianDay - 2451545.5 - timeZone / 24.0) / 36525.0;
		double t2 = t * t;
		double meanAnomaly = 357.52910 + 35999.05030 * t - 0.0001559 * t2 - 0.00000048 * t * t2;
		double meanLongitude = 280.46645 + 36000.76983 * t + 0.0003032 * t2;
		double longitudeCorrection = (1.914600 - 0.004817 * t - 0.000014 * t2)
				* Math.sin(meanAnomaly * DEGREES_TO_RADIANS)
				+ (0.019993 - 0.000101 * t) * Math.sin(2 * meanAnomaly * DEGREES_TO_RADIANS)
				+ 0.000290 * Math.sin(3 * meanAnomaly * DEGREES_TO_RADIANS);
		double longitude = Math.toRadians(meanLongitude + longitudeCorrection);
		double circle = 2 * Math.PI;
		longitude -= circle * Math.floor(longitude / circle);
		return floorToInt(longitude * 6 / Math.PI);
	}

	private static int lunarMonth11(int year, double timeZone) {
		int off = julianDay(LocalDate.of(year, 12, 31)) - 2415021;
		int k = floorToInt(off / NEW_MOON_CYCLE);
		int newMoon = newMoonDay(k, timeZone);
		if (sunLongitudeSector(newMoon, timeZone) >= 9) {
			newMoon = newMoonDay(k - 1, timeZone);
		}
		return newMoon;
	}

	private static int leapMonthOffset(int month11, double timeZone) {
		int k = floorToInt((month11 - JULIAN_EPOCH) / NEW_MOON_CYCLE + 0.5);
		int last = 0;
		int i = 1;
		int arc = sunLongitudeSector(newMoonDay(k + i, timeZone), timeZone);
		while (arc != last && i < 14) {
			last = arc;
			i++;
			arc = sunLongitudeSector(newMoonDay(k + i, timeZone), timeZone);
		}
		return i - 1;
	}

	private static int floorToInt(double value) {
		return (int) Math.floor(value);
	}
}
