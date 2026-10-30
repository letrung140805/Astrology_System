package com.example.astrology.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record ConversationHistoryItem(
		Long id,
		String title,
		LocalDateTime createdAt,
		LocalDateTime updatedAt,
		int messageCount) {

	private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("HH:mm · dd/MM/yyyy");

	public String formattedTime() {
		LocalDateTime time = updatedAt != null ? updatedAt : createdAt;
		return time != null ? time.format(DISPLAY_FORMATTER) : "";
	}
}

