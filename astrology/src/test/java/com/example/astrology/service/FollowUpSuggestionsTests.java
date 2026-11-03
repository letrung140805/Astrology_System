package com.example.astrology.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class FollowUpSuggestionsTests {

	private OpenAiAstrologyAnswerGenerator generator;

	@BeforeEach
	void setUp() {
		generator = new OpenAiAstrologyAnswerGenerator(
				null,
				"gemini-3.5-flash-lite",
				"", // unconfigured api key triggers fallback
				null,
				new ObjectMapper()
		);
	}

	@Test
	void fallbackSuggestionsForPhuThe() {
		List<String> suggestions = generator.generateFollowUpSuggestions(
				"Lá số mẫu",
				List.of(),
				"Cung Phu Thê của tôi có đào hoa hay trục trặc không?",
				"Cung Phu Thê của bạn có các sao tốt..."
		);

		assertNotNull(suggestions);
		assertEquals(3, suggestions.size());
		for (String s : suggestions) {
			assertNotNull(s);
			assertFalse(s.isBlank());
			assertTrue(s.endsWith("?"), "Mỗi câu hỏi phải kết thúc bằng dấu chấm hỏi");
		}
	}

	@Test
	void fallbackSuggestionsForCareer() {
		List<String> suggestions = generator.generateFollowUpSuggestions(
				"Lá số mẫu",
				List.of(),
				"Đường công danh sự nghiệp và thăng tiến năm nay ra sao?",
				"Cung Quan Lộc của bạn có Thiên Tướng..."
		);

		assertNotNull(suggestions);
		assertEquals(3, suggestions.size());
		assertTrue(suggestions.stream().anyMatch(s -> s.contains("độc lập") || s.contains("sự nghiệp") || s.contains("công danh")));
	}

	@Test
	void fallbackSuggestionsForWealth() {
		List<String> suggestions = generator.generateFollowUpSuggestions(
				"Lá số mẫu",
				List.of(),
				"Tài chính và tích lũy tiền bạc trong đại vận này thế nào?",
				"Cung Tài Bạch có Vũ Khúc..."
		);

		assertNotNull(suggestions);
		assertEquals(3, suggestions.size());
		assertTrue(suggestions.stream().anyMatch(s -> s.contains("tài") || s.contains("tiền") || s.contains("Lộc")));
	}

	@Test
	void fallbackSuggestionsForHealth() {
		List<String> suggestions = generator.generateFollowUpSuggestions(
				"Lá số mẫu",
				List.of(),
				"Sức khỏe và cung tật ách năm nay cần lưu ý bệnh tật gì?",
				"Cung Tật Ách có các sao..."
		);

		assertNotNull(suggestions);
		assertEquals(3, suggestions.size());
		assertTrue(suggestions.stream().anyMatch(s -> s.contains("Tật Ách") || s.contains("sức khỏe") || s.contains("cứu giải")));
	}

	@Test
	void fallbackSuggestionsDefault() {
		List<String> suggestions = generator.generateFollowUpSuggestions(
				"Lá số mẫu",
				List.of(),
				"Tổng quan vận mệnh cuộc đời tôi ra sao?",
				"Lá số có mệnh Thủy..."
		);

		assertNotNull(suggestions);
		assertEquals(3, suggestions.size());
		for (String s : suggestions) {
			assertTrue(s.endsWith("?"));
		}
	}

	@Test
	void avoidsDuplicateQuestionsFromHistory() {
		List<AstrologyChatPersistenceService.ChatTurn> history = List.of(
				new AstrologyChatPersistenceService.ChatTurn("USER", "Cung Phu Thê có đào hoa hay trắc trở nào cần lưu tâm?"),
				new AstrologyChatPersistenceService.ChatTurn("ASSISTANT", "Phân tích cung Phu Thê...")
		);

		List<String> suggestions = generator.generateFollowUpSuggestions(
				"Lá số mẫu",
				history,
				"Tình duyên của tôi thế nào?",
				"Luận giải tình duyên..."
		);

		assertNotNull(suggestions);
		assertEquals(3, suggestions.size());
		// Verify that the exact question from history is avoided
		assertFalse(suggestions.contains("Cung Phu Thê có đào hoa hay trắc trở nào cần lưu tâm?"));
	}
}
