package com.example.astrology.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.astrology.model.CoupleCompatibilityResult;
import com.example.astrology.model.CouplePersonData;

class CoupleCompatibilityServiceTests {

	private CoupleCompatibilityService service;

	@BeforeEach
	void setUp() {
		service = new CoupleCompatibilityService();
	}

	@Test
	void assessReturnsCompleteCompatibilityResult() {
		CouplePersonData person1 = new CouplePersonData(
				"Lê Đức Trung",
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
				"Hoàng Anh Vũ",
				"Nữ",
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

		CoupleCompatibilityResult result = service.assess(person1, person2);

		assertNotNull(result);
		assertTrue(result.totalScore() > 0 && result.totalScore() <= 100);
		assertNotNull(result.menhTitle());
		assertNotNull(result.menhAnalysis());
		assertNotNull(result.canTitle());
		assertNotNull(result.canAnalysis());
		assertNotNull(result.chiTitle());
		assertNotNull(result.chiAnalysis());
		assertNotNull(result.cungPhiTitle());
		assertNotNull(result.cungPhiAnalysis());
		assertNotNull(result.phuTheTitle());
		assertNotNull(result.phuTheAnalysis());
		assertNotNull(result.advice());
		assertNotNull(result.summary());
		assertNotNull(result.rating());
	}

	@Test
	void assessHandlesSameCungPhiWithoutDuplicateElementError() {
		// 2008 Nữ -> Cung Đoài, 2009 Nam -> Cung Đoài
		CouplePersonData person1 = new CouplePersonData(
				"Lê Thị Mỹ Tâm",
				"Nữ",
				"2008-11-14",
				2008,
				11,
				14,
				"00:12",
				"Cần Thơ",
				"Tý",
				null,
				Collections.emptyList(),
				null,
				"Mậu Tý",
				"Tích Lịch Hỏa",
				null
		);

		CouplePersonData person2 = new CouplePersonData(
				"Trần Ngọc Sơn",
				"Nam",
				"2009-11-14",
				2009,
				11,
				14,
				"12:12",
				"Cần Thơ",
				"Sửu",
				null,
				Collections.emptyList(),
				null,
				"Kỷ Sửu",
				"Tích Lịch Hỏa",
				null
		);

		CoupleCompatibilityResult result = service.assess(person1, person2);

		assertNotNull(result);
		assertTrue(result.cungPhiTitle().contains("Đoài"));
		assertTrue(result.cungPhiTitle().contains("Phục Vị"));
		assertTrue(result.totalScore() > 0 && result.totalScore() <= 100);
	}
}
