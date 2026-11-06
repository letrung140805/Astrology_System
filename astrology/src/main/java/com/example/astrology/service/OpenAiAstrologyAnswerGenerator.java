package com.example.astrology.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;

@Component
public class OpenAiAstrologyAnswerGenerator implements AstrologyAnswerGenerator {

	private static final Logger logger = LoggerFactory.getLogger(OpenAiAstrologyAnswerGenerator.class);

	private static final String SYSTEM_INSTRUCTIONS = """
			QUY TẮC BẮT BUỘC VỀ NGÔN NGỮ:
			1. Bạn PHẢI LUÔN LUÔN trả lời 100% bằng TIẾNG VIỆT chuẩn xác trong mọi tình huống.
			2. TUYỆT ĐỐI KHÔNG dùng tiếng Anh. Mọi tên cung vị, tên sao, cách cục đều phải giữ nguyên hoặc dịch chuẩn thuật ngữ tiếng Việt (Ví dụ: "Cung Tài Bạch", không dùng "Wealth Palace" hay "Tai Bach Palace"; "Cung Quan Lộc", không dùng "Career Palace"; "Sao Tử Vi, Thiên Phủ, Thất Sát", v.v.).

			PHONG CÁCH LUẬN GIẢI CHUYÊN NGHIỆP:
			1. Bạn là Trợ lý AI Tử Vi Đẩu Số thông thái, phân tích sâu sắc, tự nhiên, đĩnh đạc và thấu hiểu.
			2. TUYỆT ĐỐI KHÔNG trích dẫn nguồn, KHÔNG nhắc đến "RAG", "cơ sở dữ liệu", "phiên bản quy tắc", "ruleset", "ứng viên chưa xác minh" hay các câu cảnh báo kỹ thuật. Hãy luận giải tự nhiên như một chuyên gia chiêm tinh học thực thụ.
			3. Luận giải trực tiếp theo lá số của người dùng, phân tích cung vị, sao tọa thủ, tam phương tứ chính và đưa ra lời khuyên thực tế, định hướng tích cực.

			QUY TẮC ĐỊNH DẠNG CHUẨN MARKDOWN NHƯ CHATGPT:
			1. Trình bày câu trả lời chuyên nghiệp, cấu trúc rõ ràng, ngắt đoạn thoáng mắt và thẩm mỹ.
			2. Sử dụng đề mục cấp 3 (`###`) hoặc gạch đầu dòng rõ ràng:
			   - `### 1. Phân tích cung vị & Các sao tọa thủ`
			   - `### 2. Ý nghĩa cách cục & Vận trình`
			   - `### 3. Đánh giá thuận - nghịch`
			   - `📍 Kim chỉ nam hành động từ lá số:`
			3. Dùng danh sách gạch đầu dòng (`- `) hoặc đánh số thứ tự (`1. `, `2. `) để làm rõ từng ý phân tích.
			4. In đậm (`**...**`) các sao, cung, vị trí đắc/hãm và từ khóa trọng tâm để làm nổi bật thông tin.
			5. Ngắt dòng đôi giữa các đoạn để người đọc dễ theo dõi, không gom thành một khối chữ dày đặc.
			6. YÊU CẦU HOÀN THIỆN TOÀN DIỆN: Luôn hoàn thành trọn vẹn 100% câu trả lời đến dấu kết thúc, kết luận rõ ràng, tuyệt đối không bao giờ ngắt quãng dở dang giữa câu hay giữa chừng luận giải.
			""";

	private final ChatClient chatClient;
	private final String modelName;
	private final String apiKey;
	private final AstrologyRagService ragService;
	private final ObjectMapper objectMapper;

	public OpenAiAstrologyAnswerGenerator(
			ChatClient.Builder chatClientBuilder,
			@Value("${spring.ai.openai.chat.options.model:${spring.ai.openai.chat.model:gemini-3.5-flash-lite}}") String modelName,
			@Value("${spring.ai.openai.api-key:}") String apiKey,
			AstrologyRagService ragService,
			ObjectMapper objectMapper) {
		this.chatClient = chatClientBuilder != null ? chatClientBuilder.build() : null;
		this.modelName = modelName;
		this.apiKey = apiKey;
		this.ragService = ragService;
		this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
	}

	private boolean isKeyConfigured() {
		return apiKey != null && !apiKey.isBlank() && !apiKey.contains("demo-replace-with-your-key");
	}

	private OpenAiChatOptions.Builder buildChatOptions() {
		return OpenAiChatOptions.builder()
				.model(modelName)
				.maxTokens(8192);
	}

	@Override
	public String generate(
			String chartContext,
			List<AstrologyChatPersistenceService.ChatTurn> history,
			String question) {
		if (!isKeyConfigured()) {
			throw new IllegalArgumentException(
					"Chưa cấu hình API Key Google Gemini (AI Studio). Vui lòng thiết lập api-key trong application.properties trước khi hỏi AI.");
		}
		List<Message> promptMessages = buildPromptMessages(chartContext, history, question);
		return chatClient.prompt().messages(promptMessages).options(buildChatOptions()).call().content();
	}

	@Override
	public Flux<String> stream(
			String chartContext,
			List<AstrologyChatPersistenceService.ChatTurn> history,
			String question) {
		if (!isKeyConfigured()) {
			return Flux.error(new IllegalArgumentException(
					"Chưa cấu hình API Key Google Gemini (AI Studio). Vui lòng thiết lập api-key trong application.properties trước khi hỏi AI."));
		}
		List<Message> promptMessages = buildPromptMessages(chartContext, history, question);
		return chatClient.prompt().messages(promptMessages).options(buildChatOptions()).stream().content();
	}

	private List<Message> buildPromptMessages(
			String chartContext,
			List<AstrologyChatPersistenceService.ChatTurn> history,
			String question) {
		List<Message> promptMessages = new ArrayList<>();

		StringBuilder systemContent = new StringBuilder(SYSTEM_INSTRUCTIONS);
		systemContent.append("\n\nNGỮ CẢNH LÁ SỐ:\n").append(chartContext);

		if (ragService != null) {
			String ragKnowledge = ragService.retrieveRelevantKnowledge(chartContext, question);
			if (ragKnowledge != null && !ragKnowledge.isBlank()) {
				systemContent.append("\n\nTRI THỨC THAM KHẢO TỪ CƠ SỞ DỮ LIỆU TỬ VI (RAG):\n").append(ragKnowledge);
			}
		}

		promptMessages.add(new SystemMessage(systemContent.toString()));

		if (history != null && !history.isEmpty()) {
			// Limit to the most recent 10 turns (5 Q&A pairs) to provide good context for Gemini
			int startIndex = Math.max(0, history.size() - 10);
			for (int i = startIndex; i < history.size(); i++) {
				AstrologyChatPersistenceService.ChatTurn turn = history.get(i);
				String content = turn.content();
				if (content == null || content.isBlank()) {
					continue;
				}
				// Truncate overly long past assistant messages to conserve context window
				if ("ASSISTANT".equals(turn.senderType()) && content.length() > 800) {
					content = content.substring(0, 800) + "\n... [tóm gọn nội dung trước]";
				}
				if ("USER".equals(turn.senderType())) {
					promptMessages.add(new UserMessage(content));
				} else if ("ASSISTANT".equals(turn.senderType())) {
					promptMessages.add(new AssistantMessage(content));
				}
			}
		}

		promptMessages.add(new UserMessage(question + "\n\n(Yêu cầu: Luôn trả lời hoàn toàn bằng tiếng Việt, phân đoạn rõ ràng và hoàn thành trọn vẹn toàn bộ câu trả lời đến kết luận cuối cùng, không dừng dở dang giữa câu hay giữa đoạn)"));
		return promptMessages;
	}

	@Override
	public List<String> generateFollowUpSuggestions(
			String chartContext,
			List<AstrologyChatPersistenceService.ChatTurn> history,
			String question,
			String answer) {
		if (!isKeyConfigured()) {
			return filterFallbackSuggestions(generateFallbackSuggestions(question), history);
		}
		try {
			List<Message> promptMessages = buildSuggestionsPromptMessages(chartContext, history, question, answer);
			String rawResponse = chatClient.prompt().messages(promptMessages).call().content();
			List<String> parsed = parseSuggestions(rawResponse, history);
			if (parsed.size() == 3) {
				return parsed;
			}
			// If fewer than 3 were parsed, supplement with context-aware fallbacks
			List<String> result = new ArrayList<>(parsed);
			List<String> pastQuestions = extractUserQuestions(history);
			for (String fallback : generateFallbackSuggestions(question)) {
				if (!result.contains(fallback) && isValidSuggestion(fallback, result, pastQuestions)) {
					result.add(fallback);
					if (result.size() == 3) break;
				}
			}
			return result.size() > 3 ? result.subList(0, 3) : result;
		} catch (Exception ex) {
			logger.warn("Could not generate AI follow-up suggestions: {}", ex.getMessage());
			return filterFallbackSuggestions(generateFallbackSuggestions(question), history);
		}
	}

	private List<Message> buildSuggestionsPromptMessages(
			String chartContext,
			List<AstrologyChatPersistenceService.ChatTurn> history,
			String question,
			String answer) {
		List<Message> messages = new ArrayList<>();

		String systemPrompt = """
				Bạn là Chuyên gia Tử Vi Đẩu Số cao cấp. Nhiệm vụ của bạn là tạo ra đúng 3 CÂU HỎI GỢI Ý TIẾP THEO (Dynamic Follow-up Suggestions) để người dùng tiếp tục khám phá sâu hơn về lá số của mình.

				QUY TẮC BẮT BUỘC:
				1. Tạo đúng 3 câu hỏi bằng TIẾNG VIỆT tự nhiên, sâu sắc, ngắn gọn (tối đa 15 từ mỗi câu).
				2. Bám sát chủ đề vừa trao đổi (ví dụ: công danh sự nghiệp, tình duyên hôn nhân, tài lộc, sức khỏe, hạn năm, cung vị tam hợp, xung chiếu).
				3. Tuyệt đối KHÔNG lặp lại hoặc hỏi câu tương tự với các câu hỏi người dùng đã từng hỏi trong lịch sử cuộc trò chuyện.
				4. Định dạng trả về: Bắt buộc trả về duy nhất một mảng JSON chuẩn gồm 3 chuỗi ký tự, không kèm văn bản giải thích hay mã markdown.
				Ví dụ định dạng:
				["Cung Phu Thê có sao cứu giải nào trợ lực không?", "Đại vận này có thời điểm nào dễ thăng tiến?", "Cung Tài Bạch phối chiếu mang lại nguồn tiền từ đâu?"]
				""";
		messages.add(new SystemMessage(systemPrompt));

		StringBuilder userPrompt = new StringBuilder();
		userPrompt.append("TÓM TẮT LÁ SỐ CỦA BẢN MỆNH:\n");
		if (chartContext != null && !chartContext.isBlank()) {
			userPrompt.append(chartContext.length() > 800 ? chartContext.substring(0, 800) : chartContext);
		} else {
			userPrompt.append("Lá số Tử Vi cá nhân.");
		}
		userPrompt.append("\n\n");

		List<String> pastQuestions = extractUserQuestions(history);
		if (!pastQuestions.isEmpty()) {
			userPrompt.append("CÁC CÂU HỎI NGƯỜI DÙNG ĐÃ HỎI TRƯỚC ĐÓ (TUYỆT ĐỐI KHÔNG TRÙNG LẶP):\n");
			for (String q : pastQuestions) {
				userPrompt.append("- ").append(q).append("\n");
			}
			userPrompt.append("\n");
		}

		userPrompt.append("LƯỢT TRAO ĐỔI VỪA RỒI:\n");
		userPrompt.append("Người dùng hỏi: ").append(question).append("\n");
		if (answer != null && !answer.isBlank()) {
			String truncatedAnswer = answer.length() > 500 ? answer.substring(0, 500) + "..." : answer;
			userPrompt.append("AI trả lời: ").append(truncatedAnswer).append("\n");
		}
		userPrompt.append("\nHãy tạo đúng 3 câu hỏi gợi ý tiếp theo dưới dạng JSON Array [\"...\", \"...\", \"...\"]:");

		messages.add(new UserMessage(userPrompt.toString()));
		return messages;
	}

	private List<String> parseSuggestions(
			String rawResponse,
			List<AstrologyChatPersistenceService.ChatTurn> history) {
		if (rawResponse == null || rawResponse.isBlank()) {
			return List.of();
		}
		String text = rawResponse.trim();
		if (text.startsWith("```json")) {
			text = text.substring(7);
		} else if (text.startsWith("```")) {
			text = text.substring(3);
		}
		if (text.endsWith("```")) {
			text = text.substring(0, text.length() - 3);
		}
		text = text.trim();

		List<String> pastQuestions = extractUserQuestions(history);
		List<String> candidates = new ArrayList<>();

		try {
			int start = text.indexOf('[');
			int end = text.lastIndexOf(']');
			if (start != -1 && end != -1 && end > start) {
				String jsonArray = text.substring(start, end + 1);
				List<String> parsed = objectMapper.readValue(jsonArray, new TypeReference<List<String>>() {});
				if (parsed != null) {
					for (String item : parsed) {
						if (item != null && !item.isBlank()) {
							String clean = cleanSuggestionText(item);
							if (isValidSuggestion(clean, candidates, pastQuestions)) {
								candidates.add(clean);
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.debug("Could not parse JSON array for suggestions: {}", ex.getMessage());
		}

		if (candidates.size() < 3) {
			Pattern pattern = Pattern.compile("\"([^\"]{5,100})\"");
			Matcher matcher = pattern.matcher(text);
			while (matcher.find() && candidates.size() < 3) {
				String match = cleanSuggestionText(matcher.group(1));
				if (isValidSuggestion(match, candidates, pastQuestions)) {
					candidates.add(match);
				}
			}
		}

		if (candidates.size() < 3) {
			for (String line : text.split("\\r?\\n")) {
				String clean = cleanSuggestionText(line);
				if (isValidSuggestion(clean, candidates, pastQuestions)) {
					candidates.add(clean);
					if (candidates.size() == 3) break;
				}
			}
		}

		return candidates.size() > 3 ? candidates.subList(0, 3) : candidates;
	}

	private String cleanSuggestionText(String text) {
		if (text == null) return "";
		String clean = text.trim();
		clean = clean.replaceAll("^[\\d\\-\\.\\*\\s\\[\\]\"]+", "").replaceAll("[\"\\]]+$", "").trim();
		if (!clean.endsWith("?") && clean.length() > 5) {
			clean += "?";
		}
		return clean;
	}

	private boolean isValidSuggestion(String suggestion, List<String> currentList, List<String> pastQuestions) {
		if (suggestion == null || suggestion.length() < 6 || suggestion.length() > 120) {
			return false;
		}
		String lower = suggestion.toLowerCase(Locale.ROOT);
		if (lower.contains("json") || lower.contains("câu hỏi gợi ý") || lower.contains("lựa chọn")) {
			return false;
		}
		for (String existing : currentList) {
			if (existing.equalsIgnoreCase(suggestion)) return false;
		}
		for (String past : pastQuestions) {
			if (past.equalsIgnoreCase(suggestion) || (past.length() > 10 && lower.contains(past.toLowerCase(Locale.ROOT)))) {
				return false;
			}
		}
		return true;
	}

	private List<String> extractUserQuestions(List<AstrologyChatPersistenceService.ChatTurn> history) {
		if (history == null) return List.of();
		List<String> list = new ArrayList<>();
		for (var turn : history) {
			if ("USER".equals(turn.senderType()) && turn.content() != null && !turn.content().isBlank()) {
				list.add(turn.content().trim());
			}
		}
		return list;
	}


	private List<String> filterFallbackSuggestions(
			List<String> rawFallbacks,
			List<AstrologyChatPersistenceService.ChatTurn> history) {
		List<String> pastQuestions = extractUserQuestions(history);
		List<String> result = new ArrayList<>();
		if (rawFallbacks != null) {
			for (String item : rawFallbacks) {
				if (isValidSuggestion(item, result, pastQuestions)) {
					result.add(item);
				}
			}
		}
		if (result.size() < 3) {
			for (String general : List.of(
					"Cung Mệnh và Cung Thân của tôi có những ưu thế nổi bật nào?",
					"Đại vận nào trong đời là giai đoạn hoàng kim thuận lợi nhất?",
					"Cung Thiên Di có thuận lợi cho việc xuất hành hay phát triển ở xa?",
					"Cách tích lũy tài sản và giữ tiền tốt nhất theo cung Tài Bạch?",
					"Thời điểm nào trong đại vận dễ có hỷ sự kết duyên?"
			)) {
				if (isValidSuggestion(general, result, pastQuestions)) {
					result.add(general);
					if (result.size() == 3) break;
				}
			}
		}
		return result.size() > 3 ? result.subList(0, 3) : result;
	}

	private List<String> generateFallbackSuggestions(String question) {
		String q = question != null ? question.toLowerCase(Locale.ROOT) : "";
		if (q.contains("phu thê") || q.contains("vợ") || q.contains("chồng") || q.contains("tình") || q.contains("hôn nhân") || q.contains("đào hoa")) {
			return List.of(
					"Cung Phu Thê có đào hoa hay trắc trở nào cần lưu tâm?",
					"Thời điểm nào trong đại vận dễ có hỷ sự kết duyên?",
					"Cách hóa giải xung khắc trong cung Phu Thê theo lá số?"
			);
		}
		if (q.contains("quan lộc") || q.contains("sự nghiệp") || q.contains("công việc") || q.contains("kinh doanh") || q.contains("làm ăn") || q.contains("thăng tiến")) {
			return List.of(
					"Nên làm việc độc lập hay hợp tác chung thì tốt hơn?",
					"Đại vận này có cơ hội đột phá sự nghiệp hay nên thận trọng?",
					"Cung Tài Bạch và Thiên Di tương chiếu hỗ trợ công danh ra sao?"
			);
		}
		if (q.contains("tài") || q.contains("tiền") || q.contains("lộc") || q.contains("tài chính") || q.contains("đầu tư")) {
			return List.of(
					"Cách tích lũy tài sản và giữ tiền tốt nhất theo cung Tài Bạch?",
					"Những năm nào cần đề phòng hao tài tán của?",
					"Có sao Hóa Lộc hay Lộc Tồn trợ lực cho đường tài vận không?"
			);
		}
		if (q.contains("sức khỏe") || q.contains("tật ách") || q.contains("bệnh")) {
			return List.of(
					"Cung Tật Ách nhắc nhở điều gì về sức khỏe và thói quen sinh hoạt?",
					"Năm nay cần lưu ý những tháng nào về sức khỏe và đi lại?",
					"Các sao cứu giải trong lá số giúp tiêu trừ tai ương như thế nào?"
			);
		}
		return List.of(
				"Cung Mệnh và Cung Thân của tôi có những ưu thế nổi bật nào?",
				"Đại vận nào trong đời là giai đoạn hoàng kim thuận lợi nhất?",
				"Cung Thiên Di có thuận lợi cho việc xuất hành hay phát triển ở xa?"
		);
	}

	@Override
	public String modelName() {
		return modelName;
	}
}
