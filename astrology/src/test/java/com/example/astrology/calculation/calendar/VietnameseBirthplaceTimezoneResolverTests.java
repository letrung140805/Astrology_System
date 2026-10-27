package com.example.astrology.calculation.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.ZoneId;

import org.junit.jupiter.api.Test;

class VietnameseBirthplaceTimezoneResolverTests {

	private final VietnameseBirthplaceTimezoneResolver resolver = new VietnameseBirthplaceTimezoneResolver();

	@Test
	void resolvesVietnameseProvinceAndCityNamesWithOrWithoutDiacritics() {
		assertEquals(ZoneId.of("Asia/Ho_Chi_Minh"), resolver.resolve("Hà Nội, Việt Nam"));
		assertEquals(ZoneId.of("Asia/Ho_Chi_Minh"), resolver.resolve("TP. Hồ Chí Minh"));
		assertEquals(ZoneId.of("Asia/Ho_Chi_Minh"), resolver.resolve("Da Lat, Lam Dong"));
	}

	@Test
	void rejectsMissingOrUnrecognizedPlacesInsteadOfAssumingATimezone() {
		assertThrows(IllegalArgumentException.class, () -> resolver.resolve(" "));
		assertThrows(IllegalArgumentException.class, () -> resolver.resolve("Bangkok, Thailand"));
	}
}
