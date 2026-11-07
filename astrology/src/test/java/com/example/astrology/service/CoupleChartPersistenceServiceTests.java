package com.example.astrology.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.web.server.ResponseStatusException;

import com.example.astrology.entity.AppUser;
import com.example.astrology.entity.CoupleChart;
import com.example.astrology.model.CoupleCompatibilityResult;
import com.example.astrology.model.CoupleHistoryItem;
import com.example.astrology.model.CouplePersonData;
import com.example.astrology.repository.AppUserRepository;
import com.example.astrology.repository.CoupleChartRepository;

@DataJpaTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:astroai_couple;MODE=MySQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.jpa.show-sql=false"
})
@Import(CoupleChartPersistenceService.class)
class CoupleChartPersistenceServiceTests {

	@Autowired
	private CoupleChartPersistenceService persistenceService;

	@Autowired
	private AppUserRepository appUserRepository;

	@Autowired
	private CoupleChartRepository coupleChartRepository;

	private AppUser testUser;

	@BeforeEach
	void setUp() {
		testUser = new AppUser();
		testUser.setEmail("tester@example.com");
		testUser.setUsername("tester");
		testUser.setFullName("Nguyễn Văn Test");
		appUserRepository.saveAndFlush(testUser);
	}

	@Test
	void savesAndLoadsCoupleChartSuccessfully() {
		CouplePersonData person1 = new CouplePersonData(
				"Phạm Mạnh", "Nam", "15/08/1995", 1995, 8, 15, "09:30", "Hà Nội", "Tỵ",
				null, List.of(), null, "Ất Hợi", "Sơn Đầu Hỏa", null);

		CouplePersonData person2 = new CouplePersonData(
				"Phạm Huyền Linh", "Nữ", "20/10/1997", 1997, 10, 20, "14:15", "Hải Phòng", "Thân",
				null, List.of(), null, "Đinh Sửu", "Giản Hạ Thủy", null);

		CoupleCompatibilityResult result = new CoupleCompatibilityResult(
				82, "Hòa Hợp", "badge-success", "Hai bạn rất hòa hợp",
				"Hỏa", 12, "Tương khắc nhẹ",
				"Ất - Đinh", 18, "Tương sinh",
				"Hợi - Sửu", 16, "Tam hợp",
				"Cấn - Đoài", 18, "Diên Niên",
				"Thái Âm - Thiên Phủ", "Đắc địa",
				"Cần lắng nghe nhau nhiều hơn",
				8.5, 7.5, 8.0, 9.0, 8.2,
				"Tình cảm thắm thiết", "Giao tiếp cởi mở", "Tài chính ổn định",
				"Vững vàng trước sóng gió", "Hòa hợp trong sinh hoạt",
				"Lời kết: Mối nhân duyên tốt đẹp.",
				"", List.of());

		CoupleChart saved = persistenceService.save(person1, person2, result, "tester@example.com");

		assertNotNull(saved);
		assertNotNull(saved.getId());
		assertEquals("Phạm Mạnh", saved.getName1());
		assertEquals("Phạm Huyền Linh", saved.getName2());
		assertEquals(82, saved.getTotalScore());
		assertEquals(8.5, saved.getEmotionalScore());
		assertEquals("Lời kết: Mối nhân duyên tốt đẹp.", saved.getConclusion());

		// Test loadOwned
		CoupleChart loaded = persistenceService.loadOwned(saved.getId(), "tester@example.com");
		assertNotNull(loaded);
		assertEquals(saved.getId(), loaded.getId());
		assertEquals("Phạm Mạnh", loaded.getName1());

		// Test history
		List<CoupleHistoryItem> history = persistenceService.history("tester@example.com", List.of());
		assertEquals(1, history.size());
		CoupleHistoryItem item = history.get(0);
		assertEquals("Phạm Mạnh", item.name1());
		assertEquals("Phạm Huyền Linh", item.name2());
		assertEquals(82, item.totalScore());
		assertEquals("/tuong-hop/id/" + saved.getId(), item.viewUrl());
	}

	@Test
	void throwsNotFoundWhenChartBelongsToDifferentUser() {
		AppUser anotherUser = new AppUser();
		anotherUser.setEmail("other@example.com");
		anotherUser.setUsername("other");
		anotherUser.setFullName("Trần Khác");
		appUserRepository.saveAndFlush(anotherUser);

		CouplePersonData person1 = new CouplePersonData(
				"Anh A", "Nam", "01/01/1990", 1990, 1, 1, "08:00", "Hà Nội", "Dần",
				null, List.of(), null, "Canh Ngọ", "Lộ Bàng Thổ", null);
		CouplePersonData person2 = new CouplePersonData(
				"Chị B", "Nữ", "02/02/1992", 1992, 2, 2, "10:00", "Hà Nội", "Thìn",
				null, List.of(), null, "Nhâm Thân", "Kiếm Phong Kim", null);

		CoupleCompatibilityResult result = new CoupleCompatibilityResult(
				75, "Hòa Hợp", "badge-success", "Bình hòa",
				"", 0, "", "", 0, "", "", 0, "", "", 0, "",
				"", "", "",
				7.0, 7.0, 7.0, 7.0, 7.0,
				"Tình cảm", "Giao tiếp", "Tài chính", "Độ bền", "Sống chung",
				"Kết luận", "", List.of());

		CoupleChart saved = persistenceService.save(person1, person2, result, "other@example.com");

		assertThrows(ResponseStatusException.class, () ->
				persistenceService.loadOwned(saved.getId(), "tester@example.com"));
	}
}
