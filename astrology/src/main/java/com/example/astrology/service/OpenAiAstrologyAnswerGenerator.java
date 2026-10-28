package com.example.astrology.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;

@Component
public class OpenAiAstrologyAnswerGenerator implements AstrologyAnswerGenerator {

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
			""";

	private final ChatClient chatClient;
	private final String modelName;
	private final String apiKey;
	private final AstrologyRagService ragService;

	public OpenAiAstrologyAnswerGenerator(
			ChatClient.Builder chatClientBuilder,
			@Value("${spring.ai.openai.chat.options.model:${spring.ai.openai.chat.model:openrouter/free}}") String modelName,
			@Value("${spring.ai.openai.api-key:}") String apiKey,
			AstrologyRagService ragService) {
		this.chatClient = chatClientBuilder.build();
		this.modelName = modelName;
		this.apiKey = apiKey;
		this.ragService = ragService;
	}

	private boolean isKeyConfigured() {
		return apiKey != null && !apiKey.isBlank() && !apiKey.contains("demo-replace-with-your-key");
	}

	@Override
	public String generate(
			String chartContext,
			List<AstrologyChatPersistenceService.ChatTurn> history,
			String question) {
		if (!isKeyConfigured()) {
			throw new IllegalArgumentException(
					"Chưa cấu hình API Key OpenRouter. Vui lòng thiết lập biến môi trường OPENROUTER_API_KEY trước khi hỏi AI.");
		}
		List<Message> promptMessages = buildPromptMessages(chartContext, history, question);
		return chatClient.prompt().messages(promptMessages).call().content();
	}

	@Override
	public Flux<String> stream(
			String chartContext,
			List<AstrologyChatPersistenceService.ChatTurn> history,
			String question) {
		if (!isKeyConfigured()) {
			return Flux.error(new IllegalArgumentException(
					"Chưa cấu hình API Key OpenRouter. Vui lòng thiết lập biến môi trường OPENROUTER_API_KEY trước khi hỏi AI."));
		}
		List<Message> promptMessages = buildPromptMessages(chartContext, history, question);
		return chatClient.prompt().messages(promptMessages).stream().content();
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
			// Limit to the most recent 6 turns (3 Q&A pairs) to prevent prompt token bloat on free OpenRouter models
			int startIndex = Math.max(0, history.size() - 6);
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

		promptMessages.add(new UserMessage(question + "\n\n(Yêu cầu: Luôn trả lời hoàn toàn bằng tiếng Việt và định dạng Markdown phân đoạn rõ ràng như ChatGPT)"));
		return promptMessages;
	}

	@Override
	public String modelName() {
		return modelName;
	}
}
