package com.example.astrology.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.astrology.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	List<ChatMessage> findAllByConversation_IdAndConversation_User_IdOrderByCreatedAtAsc(
			Long conversationId, Long userId);

	List<ChatMessage> findAllByConversation_IdOrderByCreatedAtAsc(Long conversationId);

	int countByConversation_Id(Long conversationId);

	void deleteAllByConversation_Id(Long conversationId);
}
