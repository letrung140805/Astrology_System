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
public class FriendAiInterpretationService {

	private static final Logger logger = LoggerFactory.getLogger(FriendAiInterpretationService.class);

	private static final String SYSTEM_INSTRUCTION = """
			QUY TẮC BẮT BUỘC VỀ VAI TRÒ VÀ BẢO TOÀN DỮ LIỆU:
			1. Bạn là Chuyên gia Chiêm tinh học & Tử Vi Đẩu Số cao cấp, chuyên sâu về Thuật Phối Hợp Bằng Hữu, Đồng Nghiệp và Cộng Sự Kinh Doanh.
			2. BẢO TOÀN TOÀN DIỆN DỮ LIỆU ĐÃ TÍNH TOÁN: Hệ thống Java đã tính toán chính xác 100% điểm số và các quy tắc kích hoạt cho đúng 5 YẾU TỐ CỐT LÕI BẠN BÈ / ĐỐI TÁC:
			   - 1. Khả năng làm bạn/cộng sự
			   - 2. Giao tiếp & tương tác xã hội
			   - 3. Tầm nhìn & cách làm việc
			   - 4. Giá trị sống & nguyên tắc
			   - 5. Tiền bạc & lợi ích
			   Bạn TUYỆT ĐỐI KHÔNG ĐƯỢC TỰ Ý THAY ĐỔI ĐIỂM SỐ, không tự bịa đặt các sao hoặc cách cục không có trong dữ liệu được cung cấp.
			3. Luận giải hoàn toàn bằng TIẾNG VIỆT chuẩn xác, văn phong sắc bén, thực tế, chuyên nghiệp và hướng tới mục tiêu hợp tác bền vững, cùng thắng (win-win).
			4. TRIẾT LÝ HỢP TÁC: "Đồng thanh tương ứng, đồng khí tương cầu" — minh bạch quyền lợi, phân định thẩm quyền rõ ràng và lấy chữ Tín làm gốc.

			CẤU TRÚC ĐỊNH DẠNG BẮT BUỘC (MARKDOWN):
			Bài luận giải BẮT BUỘC PHẢI DIỄN GIẢI CHI TIẾT ĐÚNG 5 YẾU TỐ VÀ 1 PHẦN LỜI KẾT VỚI 6 ĐỀ MỤC CẤP 3:
			### 1. Khả năng làm bạn/cộng sự
			(Diễn giải chi tiết mức độ tin cậy, sự tương trợ phò tá, khí chất kết nối qua Cung Nô Bộc, Huynh Đệ và đối chiếu chéo Mệnh - Nô)

			### 2. Giao tiếp & tương tác xã hội
			(Diễn giải chi tiết cách hai người đối thoại, phản biện, đàm phán, phong thái ngoại giao qua Cung Thiên Di, Nô Bộc, sao Cự Môn, Xương Khúc, Thái Dương)

			### 3. Tầm nhìn & cách làm việc
			(Diễn giải chi tiết phương thức hành động, phong cách làm việc, chiến lược và nguy cơ tranh chấp quyền lực qua Cung Quan Lộc các bộ sao Sát Phá Tham, Tử Phủ Vũ Tướng, Cơ Nguyệt Đồng Lương)

			### 4. Giá trị sống & nguyên tắc
			(Diễn giải chi tiết nền tảng đạo đức, chữ Tín, triết lý nhân sinh và mức độ đồng thuận nguyên tắc qua Cung Mệnh/Thân, Cung Phúc Đức, ngũ hành nạp âm và can chi)

			### 5. Tiền bạc & lợi ích
			(Diễn giải chi tiết về cơ chế kiếm tiền, quản lý dòng tiền, tích lũy tài sản và phân chia quyền lợi qua Cung Tài Bạch, các tài tinh Vũ Khúc, Thiên Phủ, Lộc Tồn hoặc hao tinh)

			### 6. Lời kết
			(Tổng kết súc tích mức độ phù hợp làm bạn hay làm đối tác kinh doanh, lời khuyên thực tế để giữ mối quan hệ lâu dài, phân quyền và cơ chế hợp tác tối ưu)
			""";

	private final ChatClient chatClient;
	private final String modelName;
	private final String apiKey;

	public FriendAiInterpretationService(
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
			double partner,
			double communication,
			double vision,
			double values,
			double money,
			List<TriggeredRule> triggeredRules) {

		if (!isKeyConfigured() || chatClient == null) {
			logger.info("Gemini API key not configured or ChatClient absent. Using structured rule-based interpretation fallback for friendship.");
			return generateFallbackInterpretation(p1, p2, totalScore, rating, partner, communication, vision, values, money, triggeredRules);
		}

		try {
			String userPrompt = buildUserPrompt(p1, p2, totalScore, rating, partner, communication, vision, values, money, triggeredRules);
			List<Message> messages = List.of(
					new SystemMessage(SYSTEM_INSTRUCTION),
					new UserMessage(userPrompt)
			);

			var options = OpenAiChatOptions.builder()
					.model(modelName)
					.maxTokens(8192);

			String aiResponse = chatClient.prompt().messages(messages).options(options).call().content();
			if (aiResponse == null || aiResponse.isBlank()) {
				return generateFallbackInterpretation(p1, p2, totalScore, rating, partner, communication, vision, values, money, triggeredRules);
			}

			return parseAiSections(aiResponse, p1, p2, totalScore, rating);
		} catch (Exception ex) {
			logger.warn("Could not generate friend interpretation via Gemini API: {}. Falling back to rule-based generation.", ex.getMessage());
			return generateFallbackInterpretation(p1, p2, totalScore, rating, partner, communication, vision, values, money, triggeredRules);
		}
	}

	private String buildUserPrompt(
			CouplePersonData p1, CouplePersonData p2,
			int totalScore, String rating,
			double partner, double communication, double vision, double values, double money,
			List<TriggeredRule> triggeredRules) {

		StringBuilder sb = new StringBuilder();
		sb.append("DỮ LIỆU ĐÃ TÍNH TOÁN BỞI HỆ THỐNG JAVA CHO MỐI QUAN HỆ BẠN BÈ / CỘNG SỰ (KHÔNG ĐƯỢC THAY ĐỔI ĐIỂM SỐ HOẶC TỰ BỊA SAO):\n\n");

		sb.append("1. ĐIỂM SỐ 5 YẾU TỐ CỐT LÕI:\n");
		sb.append(String.format("- Điểm tương hợp tổng thể: %d/100 (%s)\n", totalScore, rating));
		sb.append(String.format("- 1. Khả năng làm bạn/cộng sự: %.1f/10\n", partner));
		sb.append(String.format("- 2. Giao tiếp & tương tác xã hội: %.1f/10\n", communication));
		sb.append(String.format("- 3. Tầm nhìn & cách làm việc: %.1f/10\n", vision));
		sb.append(String.format("- 4. Giá trị sống & nguyên tắc: %.1f/10\n", values));
		sb.append(String.format("- 5. Tiền bạc & lợi ích: %.1f/10\n\n", money));

		sb.append("2. THÔNG TIN LÁ SỐ HAI BẢN MỆNH:\n");
		sb.append(String.format("Người thứ nhất: %s (%s, sinh %s, Bản mệnh: %s, Can Chi: %s)\n",
				p1.name(), p1.gender(), p1.birthDate(), p1.banMenhFull(), p1.yearCanChi()));
		sb.append("  + Cung Mệnh: ").append(formatPalaceStars(p1, PalaceFunction.MENH)).append("\n");
		sb.append("  + Cung Nô Bộc: ").append(formatPalaceStars(p1, PalaceFunction.NO_BOC)).append("\n");
		sb.append("  + Cung Huynh Đệ: ").append(formatPalaceStars(p1, PalaceFunction.HUYNH_DE)).append("\n");
		sb.append("  + Cung Thiên Di: ").append(formatPalaceStars(p1, PalaceFunction.THIEN_DI)).append("\n");
		sb.append("  + Cung Quan Lộc: ").append(formatPalaceStars(p1, PalaceFunction.QUAN_LOC)).append("\n");
		sb.append("  + Cung Tài Bạch: ").append(formatPalaceStars(p1, PalaceFunction.TAI_BACH)).append("\n");
		sb.append("  + Cung Phúc Đức: ").append(formatPalaceStars(p1, PalaceFunction.PHUC_DUC)).append("\n\n");

		sb.append(String.format("Người thứ hai (bạn bè / cộng sự): %s (%s, sinh %s, Bản mệnh: %s, Can Chi: %s)\n",
				p2.name(), p2.gender(), p2.birthDate(), p2.banMenhFull(), p2.yearCanChi()));
		sb.append("  + Cung Mệnh: ").append(formatPalaceStars(p2, PalaceFunction.MENH)).append("\n");
		sb.append("  + Cung Nô Bộc: ").append(formatPalaceStars(p2, PalaceFunction.NO_BOC)).append("\n");
		sb.append("  + Cung Huynh Đệ: ").append(formatPalaceStars(p2, PalaceFunction.HUYNH_DE)).append("\n");
		sb.append("  + Cung Thiên Di: ").append(formatPalaceStars(p2, PalaceFunction.THIEN_DI)).append("\n");
		sb.append("  + Cung Quan Lộc: ").append(formatPalaceStars(p2, PalaceFunction.QUAN_LOC)).append("\n");
		sb.append("  + Cung Tài Bạch: ").append(formatPalaceStars(p2, PalaceFunction.TAI_BACH)).append("\n");
		sb.append("  + Cung Phúc Đức: ").append(formatPalaceStars(p2, PalaceFunction.PHUC_DUC)).append("\n\n");

		sb.append("3. CÁC QUY TẮC ĐƯỢC JAVA KÍCH HOẠT THEO TỪNG YẾU TỐ:\n");
		for (TriggeredRule r : triggeredRules) {
			sb.append(String.format("- [%s] %s (%+dđ): %s | Lời khuyên: %s\n",
					r.dimension(), r.name(), r.weight(), r.description(), r.advice()));
		}

		sb.append("\nHÃY DỰA HOÀN TOÀN VÀO DỮ LIỆU TRÊN ĐỂ VIẾT BÀI LUẬN GIẢI CHI TIẾT THEO ĐÚNG 5 YẾU TỐ VÀ LỜI KẾT.");
		return sb.toString();
	}

	private String formatPalaceStars(CouplePersonData person, PalaceFunction func) {
		if (person.chartPalaces() == null) return "Chưa an sao";
		for (ChartPalaceDisplay p : person.chartPalaces()) {
			if (p.function() == func) {
				if (p.stars() == null || p.stars().isEmpty()) return "Vô chính diệu";
				return p.stars().stream().map(ChartStarDisplay::name).collect(Collectors.joining(", "));
			}
		}
		return "Chưa an sao";
	}

	private AiInterpretationResult parseAiSections(String fullMarkdown, CouplePersonData p1, CouplePersonData p2, int totalScore, String rating) {
		String partner = extractSection(fullMarkdown, "1. Khả năng làm bạn/cộng sự", "### 2.");
		String comm = extractSection(fullMarkdown, "2. Giao tiếp & tương tác xã hội", "### 3.");
		String vision = extractSection(fullMarkdown, "3. Tầm nhìn & cách làm việc", "### 4.");
		String values = extractSection(fullMarkdown, "4. Giá trị sống & nguyên tắc", "### 5.");
		String money = extractSection(fullMarkdown, "5. Tiền bạc & lợi ích", "### 6.");
		String conclusion = extractSection(fullMarkdown, "6. Lời kết", null);

		if (partner.isBlank() && comm.isBlank() && vision.isBlank()) {
			return generateFallbackInterpretation(p1, p2, totalScore, rating, 7.5, 7.0, 7.0, 7.0, 7.0, List.of());
		}

		String advice = "Trong quan hệ bằng hữu và hợp tác kinh doanh: 'Đồng thanh tương ứng, đồng khí tương cầu'. Sự minh bạch về quyền lợi, tôn trọng thẩm quyền độc lập và giữ vững chữ Tín chính là chìa khóa kiến tạo thành công bền vững.";

		return new AiInterpretationResult(
				partner,
				comm,
				vision,
				values,
				money,
				conclusion,
				advice,
				fullMarkdown
		);
	}

	private String extractSection(String content, String startHeader, String endHeader) {
		if (content == null) return "";
		int startIdx = content.indexOf(startHeader);
		if (startIdx == -1) {
			// Thử tìm không phân biệt chữ hoa thường hoặc cắt bớt
			String lower = content.toLowerCase();
			String lowerStart = startHeader.toLowerCase();
			startIdx = lower.indexOf(lowerStart);
			if (startIdx == -1) return "";
		}

		// Nhảy qua dòng tiêu đề
		int lineEnd = content.indexOf('\n', startIdx);
		if (lineEnd == -1) lineEnd = startIdx + startHeader.length();
		int actualStart = lineEnd + 1;

		int endIdx = content.length();
		if (endHeader != null) {
			int foundEnd = content.indexOf(endHeader, actualStart);
			if (foundEnd != -1) {
				endIdx = foundEnd;
			}
		}

		return content.substring(actualStart, endIdx).trim();
	}

	private AiInterpretationResult generateFallbackInterpretation(
			CouplePersonData p1, CouplePersonData p2,
			int totalScore, String rating,
			double partner, double communication, double vision, double values, double money,
			List<TriggeredRule> triggeredRules) {

		StringBuilder partnerText = new StringBuilder();
		partnerText.append(String.format("Xét tương quan Cung Nô Bộc và Cung Mệnh của hai bên, chỉ số khả năng làm bạn/cộng sự đạt **%.1f/10 điểm**. ", partner));
		partnerText.append(String.format("Năng lượng kết nối giữa %s và %s phản ánh sự tương hỗ rõ nét giữa hai bản mệnh. ", p1.name(), p2.name()));
		appendRulesToText(partnerText, triggeredRules, "FRIEND_PARTNER");
		if (partner >= 7.5) {
			partnerText.append("Đối phương là người có thể tin cậy, sẵn sàng chia sẻ khó khăn và đồng hành lâu dài trong công việc lẫn cuộc sống.");
		} else {
			partnerText.append("Đôi bên cần thêm thời gian thử thách trong các dự án nhỏ trước khi cam kết những thỏa thuận cộng tác quy mô lớn.");
		}

		StringBuilder commText = new StringBuilder();
		commText.append(String.format("Chỉ số giao tiếp & tương tác xã hội đạt **%.1f/10 điểm**. ", communication));
		commText.append(String.format("Khảo sát qua Cung Thiên Di và các sao chủ về ngôn từ khẩu khí, %s và %s có cách tiếp cận và đối thoại riêng biệt. ", p1.name(), p2.name()));
		appendRulesToText(commText, triggeredRules, "COMMUNICATION");
		commText.append("Khi bàn luận các chủ đề mang tính quyết định, việc duy trì sự tôn trọng, lắng nghe trọn vẹn luận điểm của đối tác trước khi phản hồi sẽ giúp tránh được những va chạm khẩu thiệt không đáng có.");

		StringBuilder visionText = new StringBuilder();
		visionText.append(String.format("Về tầm nhìn & cách làm việc, mức độ hòa hợp đạt **%.1f/10 điểm**. ", vision));
		visionText.append(String.format("Cung Quan Lộc của hai bạn thể hiện hai phong cách làm việc rõ ràng. ", p1.name(), p2.name()));
		appendRulesToText(visionText, triggeredRules, "VISION_WORK");
		visionText.append("Sự kết hợp giữa tư duy chiến lược và năng lực thực thi hành động là điểm tựa then chốt. Cần phân định rạch ròi phạm vi phụ trách: người định hướng thị trường và người quản trị vận hành để tránh xung đột cái tôi.");

		StringBuilder valuesText = new StringBuilder();
		valuesText.append(String.format("Nền tảng giá trị sống & nguyên tắc đạt **%.1f/10 điểm**. ", values));
		valuesText.append(String.format("Đối chiếu Cung Mệnh, Thân, Phúc Đức và Ngũ hành nạp âm (%s - %s) cho thấy sự tương thích căn bản về nhân sinh quan. ", p1.yearNayinElement(), p2.yearNayinElement()));
		appendRulesToText(valuesText, triggeredRules, "VALUES_PRINCIPLES");
		valuesText.append("Chữ Tín và sự trung thực luôn là sợi dây liên kết bền chặt nhất giữa hai người. Giữ vững cam kết ban đầu sẽ giúp tình bằng hữu ngày càng keo sơn.");

		StringBuilder moneyText = new StringBuilder();
		moneyText.append(String.format("Chỉ số tiền bạc & lợi ích hợp tác đạt **%.1f/10 điểm**. ", money));
		moneyText.append(String.format("Cung Tài Bạch của hai bên chỉ ra quan điểm về tiền tài, cơ chế phân chia lợi nhuận và quản lý vốn. ", p1.name(), p2.name()));
		appendRulesToText(moneyText, triggeredRules, "FINANCIAL_BENEFIT");
		moneyText.append("Để hợp tác kinh doanh dài lâu, nguyên tắc vàng là: 'Tiền bạc phân minh, ái tình dứt khoát'. Mọi nguồn thu, chi phí vận hành và tỷ lệ chia sẻ lợi nhuận cần được ghi nhận minh bạch bằng văn bản từ trước.");

		String conclusion = String.format("Tổng quan mức độ tương hợp bạn bè/cộng sự giữa **%s** và **%s** đạt **%d/100 điểm** (%s). Mối quan hệ mang nhiều tiềm năng phát triển vượt bậc nếu đôi bên cùng phát huy tinh thần trách nhiệm, tôn trọng chuyên môn của nhau và lấy chữ Tín làm kim chỉ nam hành động.",
				p1.name(), p2.name(), totalScore, rating);

		String advice = "Trong quan hệ bằng hữu và hợp tác kinh doanh: 'Đồng thanh tương ứng, đồng khí tương cầu'. Sự minh bạch về quyền lợi, tôn trọng thẩm quyền độc lập và giữ vững chữ Tín chính là chìa khóa kiến tạo thành công bền vững.";

		return new AiInterpretationResult(
				partnerText.toString(),
				commText.toString(),
				visionText.toString(),
				valuesText.toString(),
				moneyText.toString(),
				conclusion,
				advice,
				null
		);
	}

	private void appendRulesToText(StringBuilder sb, List<TriggeredRule> rules, String dimension) {
		if (rules == null || rules.isEmpty()) return;
		for (TriggeredRule r : rules) {
			if (dimension.equalsIgnoreCase(r.dimension())) {
				sb.append("\n- ").append(r.description()).append(" ");
			}
		}
	}

	public record AiInterpretationResult(
			String partner,
			String communication,
			String vision,
			String values,
			String money,
			String conclusion,
			String advice,
			String fullMarkdown
	) {
	}
}
