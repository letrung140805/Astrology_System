package com.example.astrology.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.astrology.model.CouplePersonData;
import com.example.astrology.model.FriendCompatibilityResult;

class FriendCompatibilityServiceTests {

	private FriendCompatibilityService service;

	@BeforeEach
	void setUp() {
		service = new FriendCompatibilityService();
	}

	@Test
	void assessReturnsCompleteFriendCompatibilityResult() {
		CouplePersonData person1 = new CouplePersonData(
				"Nguyễn Văn A",
				"Nam",
				"2005-08-14",
				2005,
				8,
				14,
				"17:30",
				"Hà Nội",
				"Dậu",
				null,
				Collections.emptyList(),
				null,
				"Ất Dậu",
				"Tuyền Trung Thủy",
				null
		);

		CouplePersonData person2 = new CouplePersonData(
				"Trần Văn B",
				"Nam",
				"2005-04-04",
				2005,
				4,
				4,
				"18:00",
				"Hà Nội",
				"Dậu",
				null,
				Collections.emptyList(),
				null,
				"Ất Dậu",
				"Tuyền Trung Thủy",
				null
		);

		FriendCompatibilityResult result = service.assess(person1, person2);

		assertNotNull(result);
		assertTrue(result.totalScore() >= 35 && result.totalScore() <= 100);
		assertTrue(result.partnerScore() >= 3.0 && result.partnerScore() <= 10.0);
		assertTrue(result.communicationScore() >= 3.0 && result.communicationScore() <= 10.0);
		assertTrue(result.visionScore() >= 3.0 && result.visionScore() <= 10.0);
		assertTrue(result.valuesScore() >= 3.0 && result.valuesScore() <= 10.0);
		assertTrue(result.moneyScore() >= 3.0 && result.moneyScore() <= 10.0);
		assertNotNull(result.partnerInterpretation());
		assertNotNull(result.communicationInterpretation());
		assertNotNull(result.visionInterpretation());
		assertNotNull(result.valuesInterpretation());
		assertNotNull(result.moneyInterpretation());
		assertNotNull(result.conclusion());
		assertNotNull(result.advice());
		assertNotNull(result.summary());
		assertNotNull(result.rating());
	}
}
