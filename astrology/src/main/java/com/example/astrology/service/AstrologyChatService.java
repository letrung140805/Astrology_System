package com.example.astrology.service;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.example.astrology.model.ConversationHistoryItem;

import reactor.core.publisher.Flux;

@Service
public class AstrologyChatService {

	private static final Logger logger = LoggerFactory.getLogger(AstrologyChatService.class);
	private static final int MAX_QUESTION_LENGTH = 2000;

	private final AstrologyChatPersistenceService persistenceService;
	private final AstrologyAnswerGenerator answerGenerator;

	public AstrologyChatService(
			AstrologyChatPersistenceService persistenceService,
			AstrologyAnswerGenerator answerGenerator) {
		this.persistenceService = persistenceService;
		this.answerGenerator = answerGenerator;
	}

	public List<ConversationHistoryItem> listConversations(UUID accessToken, String principalName) {
		return persistenceService.listConversations(accessToken, principalName);
	}

	public void deleteConversation(UUID accessToken, Long conversationId, String principalName) {
		persistenceService.deleteConversation(accessToken, conversationId, principalName);
	}

	public AstrologyChatPersistenceService.ChatSession load(
			UUID accessToken,
			Long conversationId,
			String principalName) {
		return persistenceService.load(accessToken, conversationId, principalName);
	}

	public ChatReply send(
			UUID accessToken,
			Long conversationId,
			String principalName,
			String question) {
		if (question == null || question.isBlank()) {
			throw new IllegalArgumentException("Hãy nhập câu hỏi trước khi gửi.");
		}
		String cleanQuestion = question.trim();
		if (cleanQuestion.length() > MAX_QUESTION_LENGTH) {
			throw new IllegalArgumentException("Câu hỏi không được dài quá 2.000 ký tự.");
		}

		var session = persistenceService.load(accessToken, conversationId, principalName);
		String answer = answerGenerator.generate(session.chartContext(), session.messages(), cleanQuestion);
		if (answer == null || answer.isBlank()) {
			throw new IllegalStateException("Mô hình AI chưa trả về nội dung.");
		}
		try {
			Long savedConversationId = persistenceService.saveExchange(
					accessToken, conversationId, principalName, cleanQuestion, answer, answerGenerator.modelName());
			return new ChatReply(savedConversationId, answer, true);
		} catch (DataAccessException exception) {
			logger.warn("AI response was generated but the conversation could not be saved.", exception);
			return new ChatReply(null, answer, false);
		}
	}

	public Flux<String> stream(
			UUID accessToken,
			Long conversationId,
			String principalName,
			String question) {
		if (question == null || question.isBlank()) {
			throw new IllegalArgumentException("Hãy nhập câu hỏi trước khi gửi.");
		}
		String cleanQuestion = question.trim();
		if (cleanQuestion.length() > MAX_QUESTION_LENGTH) {
			throw new IllegalArgumentException("Câu hỏi không được dài quá 2.000 ký tự.");
		}

		var session = persistenceService.load(accessToken, conversationId, principalName);
		return answerGenerator.stream(session.chartContext(), session.messages(), cleanQuestion);
	}

	public Long saveExchange(
			UUID accessToken,
			Long conversationId,
			String principalName,
			String question,
			String answer) {
		return persistenceService.saveExchange(
				accessToken, conversationId, principalName, question, answer, answerGenerator.modelName());
	}

	public String modelName() {
		return answerGenerator.modelName();
	}

	public record ChatReply(Long conversationId, String answer, boolean persisted) {
	}
}
