package com.example.astrology.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.astrology.calculation.chart.PalaceFunction;
import com.example.astrology.model.ChartPalaceDisplay;
import com.example.astrology.model.ChartStarDisplay;
import com.example.astrology.model.CouplePersonData;
import com.example.astrology.model.TriggeredRule;

@Service
public class CoupleAiInterpretationService {

	private static final Logger logger = LoggerFactory.getLogger(CoupleAiInterpretationService.class);

	private static final String SYSTEM_INSTRUCTION = """
			QUY TẮC BẮT BUỘC VỀ VAI TRÒ VÀ BẢO TOÀN DỮ LIỆU:
			1. Bạn là Chuyên gia Chiêm tinh học & Tử Vi Đẩu Số cao cấp, chuyên sâu về Hợp Hôn Thuật và Đối chiếu Song Bàn cặp đôi.
			2. BẢO TOÀN TOÀN DIỆN DỮ LIỆU ĐÃ TÍNH TOÁN: Hệ thống Java đã tính toán chính xác 100% điểm số và các quy tắc kích hoạt cho đúng 5 YẾU TỐ CỐT LÕI. Bạn TUYỆT ĐỐI KHÔNG ĐƯỢC TỰ Ý THAY ĐỔI ĐIỂM SỐ, không tự bịa đặt các sao hoặc cách cục không có trong dữ liệu được cung cấp.
			3. Luận giải hoàn toàn bằng TIẾNG VIỆT chuẩn xác, văn phong tinh tế, sâu sắc, thấu hiểu tâm lý và định hướng tích cực.
			4. TRIẾT LÝ HÓA GIẢI CỔ HỌC: Luôn thấm nhuần tinh thần "Đức năng thắng số" — mọi xung khắc tinh bàn đều là chỉ dẫn để đôi bên cùng hoàn thiện, thấu hiểu và bao dung cho nhau.

			CẤU TRÚC ĐỊNH DẠNG BẮT BUỘC (MARKDOWN):
			Bài luận giải BẮT BUỘC PHẢI DIỄN GIẢI CHI TIẾT ĐÚNG 5 YẾU TỐ VÀ 1 PHẦN LỜI KẾT TỔNG QUAN VỚI 6 ĐỀ MỤC CẤP 3:
			### 1. Hòa hợp tình cảm
			(Diễn giải chi tiết mức độ gắn kết cảm xúc, duyên nợ, thấu hiểu tâm hồn qua Cung Mệnh, Phu Thê, Phúc Đức, các sao đào hoa duyên nợ và ngũ hành bản mệnh)

			### 2. Giao tiếp
			(Diễn giải chi tiết cách hai người trò chuyện, lắng nghe, chia sẻ quan điểm; phân tích sao Cự Môn, Cung Nô Bộc, Thiên Di và cách hóa giải nguy cơ tranh cãi khẩu thiệt)

			### 3. Tài chính
			(Diễn giải chi tiết về quan niệm tiền bạc, sự tương trợ kinh tế, khả năng tích lũy hay nguy cơ hao tán qua Cung Tài Bạch, Quan Lộc)

			### 4. Độ bền trước biến cố
			(Diễn giải chi tiết về sức đề kháng của mối quan hệ khi đối diện với thử thách, nghịch cảnh qua Lục sát tinh, Tuần Triệt, sao cứu giải và phong thủy Bát Trạch)

			### 5. Sống chung
			(Diễn giải chi tiết về đời sống thường nhật, sự đồng thuận xây dựng tổ ấm, thói quen sinh hoạt và nếp sống gia đình qua Cung Điền Trạch và địa chi)

			### 6. Lời kết
			(Tổng kết súc tích nhân duyên cặp đôi, động viên tinh thần, định hướng xây dựng tình cảm chân thành, bền vững và hạnh phúc dài lâu)
			""";

	private final ChatClient chatClient;
	private final String modelName;
	private final String apiKey;

	public CoupleAiInterpretationService(
			ChatClient.Builder chatClientBuilder,
			@Value("${spring.ai.openai.chat.options.model:${spring.ai.openai.chat.model:gemini-3.5-flash-lite}}") String modelName,
			@Value("${spring.ai.openai.api-key:}") String apiKey) {
		this.chatClient = chatClientBuilder != null ? chatClientBuilder.build() : null;
		this.modelName = modelName;
		this.apiKey = apiKey;
	}

	private boolean isKeyConfigured() {
		return apiKey != null && !apiKey.isBlank() && !apiKey.contains("demo-replace-with-your-key");
	}

	public AiInterpretationResult generate(
			CouplePersonData p1,
			CouplePersonData p2,
			int totalScore,
			String rating,
			double emotional,
			double communication,
			double financial,
			double stability,
			double livingTogether,
			List<TriggeredRule> triggeredRules) {

		if (!isKeyConfigured() || chatClient == null) {
			logger.info("Gemini API key not configured or ChatClient absent. Using structured rule-based interpretation fallback.");
			return generateFallbackInterpretation(p1, p2, totalScore, rating, emotional, communication, financial, stability, livingTogether, triggeredRules);
		}

		try {
			String userPrompt = buildUserPrompt(p1, p2, totalScore, rating, emotional, communication, financial, stability, livingTogether, triggeredRules);
			List<Message> messages = List.of(
					new SystemMessage(SYSTEM_INSTRUCTION),
					new UserMessage(userPrompt)
			);

			var options = OpenAiChatOptions.builder()
					.model(modelName)
					.maxTokens(8192);

			String aiResponse = chatClient.prompt().messages(messages).options(options).call().content();
			if (aiResponse == null || aiResponse.isBlank()) {
				return generateFallbackInterpretation(p1, p2, totalScore, rating, emotional, communication, financial, stability, livingTogether, triggeredRules);
			}

			return parseAiSections(aiResponse, p1, p2, totalScore, rating);
		} catch (Exception ex) {
			logger.warn("Could not generate couple interpretation via Gemini API: {}. Falling back to rule-based generation.", ex.getMessage());
			return generateFallbackInterpretation(p1, p2, totalScore, rating, emotional, communication, financial, stability, livingTogether, triggeredRules);
		}
	}

	private String buildUserPrompt(
			CouplePersonData p1, CouplePersonData p2,
			int totalScore, String rating,
			double emotional, double communication, double financial, double stability, double livingTogether,
			List<TriggeredRule> triggeredRules) {

		StringBuilder sb = new StringBuilder();
		sb.append("DỮ LIỆU ĐÃ TÍNH TOÁN BỞI HỆ THỐNG JAVA (KHÔNG ĐƯỢC THAY ĐỔI ĐIỂM SỐ HOẶC TỰ BỊA SAO):\n\n");

		sb.append("1. ĐIỂM SỐ 5 YẾU TỐ CỐT LÕI:\n");
		sb.append(String.format("- Điểm tương hợp tổng thể: %d/100 (%s)\n", totalScore, rating));
		sb.append(String.format("- 1. Hòa hợp tình cảm: %.1f/10\n", emotional));
		sb.append(String.format("- 2. Giao tiếp: %.1f/10\n", communication));
		sb.append(String.format("- 3. Tài chính: %.1f/10\n", financial));
		sb.append(String.format("- 4. Độ bền trước biến cố: %.1f/10\n", stability));
		sb.append(String.format("- 5. Sống chung: %.1f/10\n\n", livingTogether));

		sb.append("2. THÔNG TIN LÁ SỐ HAI BẢN MỆNH:\n");
		sb.append(String.format("Người thứ nhất: %s (%s, sinh %s, Bản mệnh: %s, Can Chi: %s)\n",
				p1.name(), p1.gender(), p1.birthDate(), p1.banMenhFull(), p1.yearCanChi()));
		sb.append("  + Cung Mệnh: ").append(formatPalaceStars(p1, PalaceFunction.MENH)).append("\n");
		sb.append("  + Cung Phu Thê: ").append(formatPalaceStars(p1, PalaceFunction.PHU_THE)).append("\n");
		sb.append("  + Cung Phúc Đức: ").append(formatPalaceStars(p1, PalaceFunction.PHUC_DUC)).append("\n");
		sb.append("  + Cung Nô Bộc: ").append(formatPalaceStars(p1, PalaceFunction.NO_BOC)).append("\n");
		sb.append("  + Cung Điền Trạch: ").append(formatPalaceStars(p1, PalaceFunction.DIEN_TRACH)).append("\n");
		sb.append("  + Cung Tài Bạch: ").append(formatPalaceStars(p1, PalaceFunction.TAI_BACH)).append("\n\n");

		sb.append(String.format("Người thứ hai: %s (%s, sinh %s, Bản mệnh: %s, Can Chi: %s)\n",
				p2.name(), p2.gender(), p2.birthDate(), p2.banMenhFull(), p2.yearCanChi()));
		sb.append("  + Cung Mệnh: ").append(formatPalaceStars(p2, PalaceFunction.MENH)).append("\n");
		sb.append("  + Cung Phu Thê: ").append(formatPalaceStars(p2, PalaceFunction.PHU_THE)).append("\n");
		sb.append("  + Cung Phúc Đức: ").append(formatPalaceStars(p2, PalaceFunction.PHUC_DUC)).append("\n");
		sb.append("  + Cung Nô Bộc: ").append(formatPalaceStars(p2, PalaceFunction.NO_BOC)).append("\n");
		sb.append("  + Cung Điền Trạch: ").append(formatPalaceStars(p2, PalaceFunction.DIEN_TRACH)).append("\n");
		sb.append("  + Cung Tài Bạch: ").append(formatPalaceStars(p2, PalaceFunction.TAI_BACH)).append("\n\n");

		sb.append("3. CÁC QUY TẮC ĐƯỢC JAVA KÍCH HOẠT THEO TỪNG YẾU TỐ:\n");
		for (TriggeredRule r : triggeredRules) {
			sb.append(String.format("- [%s] %s (%+dđ): %s | Lời khuyên: %s\n",
					r.dimension(), r.name(), r.weight(), r.description(), r.advice()));
		}

		sb.append("\nHÃY DỰA HOÀN TOÀN VÀO DỮ LIỆU TRÊN ĐỂ VIẾT BÀI LUẬN GIẢI CHI TIẾT THEO ĐÚNG 5 YẾU TỐ.");
		return sb.toString();
	}

	private String formatPalaceStars(CouplePersonData person, PalaceFunction func) {
		if (person.chartPalaces() == null) return "Chưa an sao";
		for (ChartPalaceDisplay p : person.chartPalaces()) {
			if (p.function() == func) {
				return p.stars() != null
						? p.stars().stream().map(ChartStarDisplay::name).collect(Collectors.joining(", "))
						: "Không có sao";
			}
		}
		return "Chưa an sao";
	}

	private AiInterpretationResult parseAiSections(String fullText, CouplePersonData p1, CouplePersonData p2, int totalScore, String rating) {
		String emotional = extractSection(fullText, "1. Hòa hợp tình cảm", "2. Giao tiếp");
		String communication = extractSection(fullText, "2. Giao tiếp", "3. Tài chính");
		String financial = extractSection(fullText, "3. Tài chính", "4. Độ bền trước biến cố");
		String stability = extractSection(fullText, "4. Độ bền trước biến cố", "5. Sống chung");

		int concIdx = findConclusionHeaderIndex(fullText);
		String livingTogether;
		String conclusion = "";
		if (concIdx != -1) {
			int start5 = findHeaderIndex(fullText, "5. Sống chung");
			if (start5 != -1 && start5 < concIdx) {
				int nl = fullText.indexOf('\n', start5);
				int contentStart = nl != -1 ? nl + 1 : start5;
				livingTogether = fullText.substring(contentStart, concIdx).trim();
			} else {
				livingTogether = extractSection(fullText, "5. Sống chung", null);
			}
			int nlConc = fullText.indexOf('\n', concIdx);
			int concContentStart = nlConc != -1 ? nlConc + 1 : concIdx;
			conclusion = fullText.substring(concContentStart).trim();
		} else {
			livingTogether = extractSection(fullText, "5. Sống chung", null);
		}

		// Strip inline "Lời kết" from livingTogether if present
		int inlineIdx = findInlineConclusionIndex(livingTogether);
		if (inlineIdx != -1) {
			String inlineConc = livingTogether.substring(inlineIdx)
					.replaceAll("(?i)^(\\*{0,2}###?\\s*)?(\\*{0,2}6?\\.?\\s*Lời kết:?\\*{0,2})", "")
					.trim();
			livingTogether = livingTogether.substring(0, inlineIdx).trim();
			if (conclusion.isBlank() && !inlineConc.isBlank()) {
				conclusion = inlineConc;
			}
		}

		if (conclusion.isBlank()) {
			conclusion = String.format("%s và %s sở hữu một nền tảng nhân duyên sâu đậm với điểm số tổng thể %d/100 (%s). Những khác biệt về ngũ hành hay tính cách hoàn toàn chỉ là những nốt trầm cần thiết để bản giao hưởng tình yêu của hai bạn thêm phần phong phú. Bằng sự thấu hiểu, lòng bao dung và tình yêu chân thành, hai bạn hoàn toàn đủ khả năng xây dựng một cuộc hôn nhân viên mãn, bền chặt và hạnh phúc dài lâu.",
					p1.name(), p2.name(), totalScore, rating);
		}

		String advice = "Trong đạo vợ chồng theo triết lý Đông phương, 'Đức năng thắng số'. Sự hòa hợp của lá số là nền tảng ban đầu, sự thấu hiểu và tôn trọng nhau mới là yếu tố quyết định hôn nhân viên mãn.";

		return new AiInterpretationResult(
				emotional.isBlank() ? "Mức độ gắn kết cảm xúc và duyên nợ tương đối hài hòa." : emotional,
				communication.isBlank() ? "Giao tiếp chân thành, cùng nhau chia sẻ thẳng thắn." : communication,
				financial.isBlank() ? "Tài chính gia đình có sự tương trợ, hướng tới sự ổn định." : financial,
				stability.isBlank() ? "Mối quan hệ có khả năng vượt qua thử thách nhờ sự kiên nhẫn." : stability,
				livingTogether.isBlank() ? "Nếp sống gia đình và tổ ấm thuận hòa, êm ấm." : livingTogether,
				conclusion,
				advice,
				fullText
		);
	}

	private int findConclusionHeaderIndex(String text) {
		String[] patterns = {"### 6. Lời kết", "6. Lời kết", "### Lời kết", "## Lời kết", "**Lời kết:**", "**Lời kết**", "Lời kết:"};
		for (String p : patterns) {
			int idx = text.indexOf(p);
			if (idx != -1) return idx;
		}
		return -1;
	}

	private int findInlineConclusionIndex(String text) {
		String[] patterns = {"\n### 6. Lời kết", "\n6. Lời kết", "\n### Lời kết", "\n## Lời kết", "\n**Lời kết:**", "\n**Lời kết**", "\nLời kết:"};
		for (String p : patterns) {
			int idx = text.indexOf(p);
			if (idx != -1) return idx + 1;
		}
		if (text.startsWith("Lời kết:") || text.startsWith("**Lời kết:**") || text.startsWith("### Lời kết")) {
			return 0;
		}
		return -1;
	}

	private String extractSection(String content, String startHeader, String endHeader) {
		int startIndex = findHeaderIndex(content, startHeader);
		if (startIndex == -1) return "";

		int newline = content.indexOf('\n', startIndex);
		if (newline != -1) {
			startIndex = newline + 1;
		}

		int endIndex = endHeader != null ? findHeaderIndex(content, endHeader) : content.length();
		if (endIndex == -1 || endIndex <= startIndex) {
			endIndex = content.length();
		}

		return content.substring(startIndex, endIndex).trim();
	}

	private int findHeaderIndex(String text, String headerKeyword) {
		int idx = text.indexOf(headerKeyword);
		if (idx != -1) return idx;
		String simplified = headerKeyword.replace("### ", "").trim();
		return text.indexOf(simplified);
	}

	private AiInterpretationResult generateFallbackInterpretation(
			CouplePersonData p1, CouplePersonData p2,
			int totalScore, String rating,
			double emotional, double communication, double financial, double stability, double livingTogether,
			List<TriggeredRule> triggeredRules) {

		String emotionalText = buildDimensionFallback(triggeredRules, "EMOTIONAL",
				String.format("Chỉ số hòa hợp tình cảm giữa %s và %s đạt %.1f/10. Sự phối chiếu giữa Cung Mệnh, Cung Phu Thê và Cung Phúc Đức cho thấy mối liên kết nhân duyên sâu đậm, có sự thu hút tự nhiên về tâm hồn.", p1.name(), p2.name(), emotional));

		String commText = buildDimensionFallback(triggeredRules, "COMMUNICATION",
				String.format("Chỉ số giao tiếp đạt %.1f/10. Hai bạn có xu hướng chia sẻ cởi mở, tuy nhiên cần chú ý lắng nghe nhiều hơn để tránh những lúc tranh luận gay gắt do cái tôi cá nhân.", communication));

		String financeText = buildDimensionFallback(triggeredRules, "FINANCIAL",
				String.format("Chỉ số tài chính đạt %.1f/10. Sự tương trợ về mặt kinh tế và sự nghiệp giúp hai bạn có thêm động lực xây dựng nguồn lực vững vàng cho tương lai gia đình.", financial));

		String stabilityText = buildDimensionFallback(triggeredRules, "STABILITY",
				String.format("Chỉ số độ bền trước biến cố đạt %.1f/10. Khi đứng trước các giai đoạn thử thách hay thay đổi hoàn cảnh, sự bình tĩnh, kiên định và tin tưởng lẫn nhau chính là chìa khóa gìn giữ hạnh phúc.", stability));

		String livingText = buildDimensionFallback(triggeredRules, "LIVING_TOGETHER",
				String.format("Chỉ số sống chung đạt %.1f/10. Cung Điền Trạch và địa chi cho thấy sự đồng thuận trong việc xây dựng tổ ấm, cùng nhau vun vén không gian sống an yên.", livingTogether));

		String conclusion = String.format("%s và %s sở hữu một nền tảng nhân duyên sâu đậm với điểm số tổng thể %d/100 (%s). Những khác biệt về ngũ hành hay tính cách hoàn toàn chỉ là những nốt trầm cần thiết để bản giao hưởng tình yêu của hai bạn thêm phần phong phú. Bằng sự thấu hiểu, lòng bao dung và tình yêu chân thành, hai bạn hoàn toàn đủ khả năng xây dựng một cuộc hôn nhân viên mãn, bền chặt và hạnh phúc dài lâu.",
				p1.name(), p2.name(), totalScore, rating);

		String advice = "Trong đạo vợ chồng theo triết lý Đông phương, 'Đức năng thắng số'. Sự hòa hợp của lá số là nền tảng phước lành ban đầu, nhưng sự thủy chung, tôn trọng, thấu hiểu và cùng nhau vượt qua thử thách mới chính là yếu tố quyết định hôn nhân viên mãn trọn đời.";

		String fullMarkdown = String.format("""
				### 1. Hòa hợp tình cảm
				%s

				### 2. Giao tiếp
				%s

				### 3. Tài chính
				%s

				### 4. Độ bền trước biến cố
				%s

				### 5. Sống chung
				%s

				### 6. Lời kết
				%s
				""", emotionalText, commText, financeText, stabilityText, livingText, conclusion);

		return new AiInterpretationResult(
				emotionalText,
				commText,
				financeText,
				stabilityText,
				livingText,
				conclusion,
				advice,
				fullMarkdown
		);
	}

	private String buildDimensionFallback(List<TriggeredRule> rules, String dimension, String defaultIntro) {
		StringBuilder sb = new StringBuilder(defaultIntro).append("\n\n");
		boolean hasRules = false;
		for (TriggeredRule r : rules) {
			if (dimension.equalsIgnoreCase(r.dimension())) {
				hasRules = true;
				sb.append("- **").append(r.name()).append("**: ").append(r.description());
				if (r.advice() != null && !r.advice().isBlank()) {
					sb.append(" *(Chỉ dẫn: ").append(r.advice()).append(")*");
				}
				sb.append("\n\n");
			}
		}
		return hasRules ? sb.toString().trim() : defaultIntro;
	}

	public record AiInterpretationResult(
			String emotional,
			String communication,
			String financial,
			String stability,
			String livingTogether,
			String conclusion,
			String advice,
			String fullMarkdown
	) {}
}
