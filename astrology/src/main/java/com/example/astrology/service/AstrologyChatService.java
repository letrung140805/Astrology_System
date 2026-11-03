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
		List<String> suggestions = answerGenerator.generateFollowUpSuggestions(
				session.chartContext(), session.messages(), cleanQuestion, answer);
		try {
			Long savedConversationId = persistenceService.saveExchange(
					accessToken, conversationId, principalName, cleanQuestion, answer, answerGenerator.modelName());
			return new ChatReply(savedConversationId, answer, true, suggestions);
		} catch (DataAccessException exception) {
			logger.warn("AI response was generated but the conversation could not be saved.", exception);
			return new ChatReply(null, answer, false, suggestions);
		}
	}

	public List<String> generateFollowUpSuggestions(
			UUID accessToken,
			Long conversationId,
			String principalName,
			String question,
			String answer) {
		try {
			var session = persistenceService.load(accessToken, conversationId, principalName);
			return answerGenerator.generateFollowUpSuggestions(
					session.chartContext(), session.messages(), question, answer);
		} catch (Exception ex) {
			logger.warn("Could not generate dynamic follow-up suggestions: {}", ex.getMessage());
			return List.of(
					"Cung Phu Thê có sao đào hoa hay trắc trở nào cần lưu tâm?",
					"Nên làm việc độc lập hay hợp tác chung thì tốt hơn?",
					"Đại vận này có cơ hội đột phá tài lộc hay nên thận trọng?"
			);
		}
	}

	public List<String> getLatestSuggestions(
			UUID accessToken,
			Long conversationId,
			String principalName) {
		try {
			var session = persistenceService.load(accessToken, conversationId, principalName);
			if (session.messages() != null && !session.messages().isEmpty()) {
				String lastQuestion = null;
				String lastAnswer = null;
				for (int i = session.messages().size() - 1; i >= 0; i--) {
					var turn = session.messages().get(i);
					if (lastAnswer == null && "ASSISTANT".equals(turn.senderType())) {
						lastAnswer = turn.content();
					} else if (lastQuestion == null && "USER".equals(turn.senderType())) {
						lastQuestion = turn.content();
					}
					if (lastQuestion != null && lastAnswer != null) break;
				}
				if (lastQuestion != null && lastAnswer != null) {
					return answerGenerator.generateFollowUpSuggestions(
							session.chartContext(), session.messages(), lastQuestion, lastAnswer);
				}
			}
		} catch (Exception ex) {
			logger.debug("Could not compute latest suggestions: {}", ex.getMessage());
		}
		return List.of(
				"Cung Phu Thê có đào hoa hay trắc trở không?",
				"Nên làm việc độc lập hay hợp tác chung thì tốt hơn?",
				"Tài vận năm nay có cơ hội đột phá tích lũy tài sản không?"
		);
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

	public record ChatReply(Long conversationId, String answer, boolean persisted, List<String> suggestions) {
		public ChatReply(Long conversationId, String answer, boolean persisted) {
			this(conversationId, answer, persisted, List.of());
		}
	}
}
