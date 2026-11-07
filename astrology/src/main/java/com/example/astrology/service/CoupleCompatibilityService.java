package com.example.astrology.service;

import java.util.Locale;
import java.util.Set;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.stereotype.Service;

import com.example.astrology.model.ChartPalaceDisplay;
import com.example.astrology.model.CoupleCompatibilityResult;
import com.example.astrology.model.CouplePersonData;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CoupleCompatibilityService {

	private static final Set<Set<String>> CAN_HOP = Set.of(
			Set.of("Giáp", "Kỷ"),
			Set.of("Ất", "Canh"),
			Set.of("Bính", "Tân"),
			Set.of("Đinh", "Nhâm"),
			Set.of("Mậu", "Quý")
	);

	private static final Set<Set<String>> CAN_XUNG = Set.of(
			Set.of("Giáp", "Canh"),
			Set.of("Ất", "Tân"),
			Set.of("Bính", "Nhâm"),
			Set.of("Đinh", "Quý")
	);

	private static final Set<Set<String>> CHI_TAM_HOP = Set.of(
			Set.of("Thân", "Tý", "Thìn"),
			Set.of("Dần", "Ngọ", "Tuất"),
			Set.of("Tỵ", "Dậu", "Sửu"),
			Set.of("Hợi", "Mão", "Mùi")
	);

	private static final Set<Set<String>> CHI_LUC_HOP = Set.of(
			Set.of("Tý", "Sửu"),
			Set.of("Dần", "Hợi"),
			Set.of("Mão", "Tuất"),
			Set.of("Thìn", "Dậu"),
			Set.of("Tỵ", "Thân"),
			Set.of("Ngọ", "Mùi")
	);

	private static final Set<Set<String>> CHI_TU_XUNG = Set.of(
			Set.of("Dần", "Thân", "Tỵ", "Hợi"),
			Set.of("Tý", "Ngọ", "Mão", "Dậu"),
			Set.of("Thìn", "Tuất", "Sửu", "Mùi")
	);

	private static final Set<Set<String>> CHI_LUC_HAI = Set.of(
			Set.of("Tý", "Mùi"),
			Set.of("Sửu", "Ngọ"),
			Set.of("Dần", "Tỵ"),
			Set.of("Mão", "Thìn"),
			Set.of("Thân", "Hợi"),
			Set.of("Dậu", "Tuất")
	);

	private final CoupleRuleEvaluator ruleEvaluator;
	private final CoupleAiInterpretationService aiInterpretationService;

	@org.springframework.beans.factory.annotation.Autowired
	public CoupleCompatibilityService(
			CoupleRuleEvaluator ruleEvaluator,
			CoupleAiInterpretationService aiInterpretationService) {
		this.ruleEvaluator = ruleEvaluator != null ? ruleEvaluator : new CoupleRuleEvaluator(new DefaultResourceLoader(), new ObjectMapper());
		this.aiInterpretationService = aiInterpretationService != null ? aiInterpretationService : new CoupleAiInterpretationService(null, "gemini-3.5-flash-lite", "");
	}

	public CoupleCompatibilityService() {
		CoupleRuleEvaluator eval = new CoupleRuleEvaluator(new DefaultResourceLoader(), new ObjectMapper());
		eval.init();
		this.ruleEvaluator = eval;
		this.aiInterpretationService = new CoupleAiInterpretationService(null, "gemini-3.5-flash-lite", "");
	}

	public CoupleCompatibilityResult assess(CouplePersonData p1, CouplePersonData p2) {
		String elem1 = extractElement(p1.yearNayinElement());
		String elem2 = extractElement(p2.yearNayinElement());

		// 1. Phân tích Ngũ Hành Bản Mệnh (Tối đa 25 điểm)
		int menhScore;
		String menhAnalysis;
		String menhTitle;
		boolean isMenhSinh = false;
		boolean isMenhKhac = false;

		if (isGenerating(elem1, elem2)) {
			menhScore = 25;
			isMenhSinh = true;
			menhTitle = elem1 + " sinh " + elem2 + " (Tương sinh - Rất tốt)";
			menhAnalysis = "Bản mệnh của " + p1.name() + " (" + elem1 + ") tương sinh cho bản mệnh của " + p2.name() + " (" + elem2 + "). Trong phong thủy hôn nhân, đây là mối quan hệ tương sinh đại cát: người này luôn biết chở che, tạo điểm tựa và tiếp thêm sinh khí, may mắn cho người kia.";
		} else if (isGenerating(elem2, elem1)) {
			menhScore = 23;
			isMenhSinh = true;
			menhTitle = elem2 + " sinh " + elem1 + " (Tương sinh - Rất tốt)";
			menhAnalysis = "Bản mệnh của " + p2.name() + " (" + elem2 + ") tương sinh cho bản mệnh của " + p1.name() + " (" + elem1 + "). Cặp đôi có sự nâng đỡ hòa hợp, gia đạo thuận hòa, công danh tài lộc dễ có sự phát triển bền vững.";
		} else if (elem1.equals(elem2)) {
			menhScore = 20;
			menhTitle = "Lưỡng " + elem1 + " (Bình hòa - Đồng điệu)";
			menhAnalysis = "Cả hai đều cùng thuộc hành " + elem1 + ". Tính cách có nhiều nét tương đồng, dễ thấu hiểu suy nghĩ và quan điểm sống của nhau. Cần chú ý giữ hòa khí, tránh những lúc cả hai cùng nóng tính.";
		} else if (isOvercoming(elem1, elem2)) {
			menhScore = 12;
			isMenhKhac = true;
			menhTitle = elem1 + " khắc " + elem2 + " (Tương khắc nhẹ)";
			menhAnalysis = "Bản mệnh " + elem1 + " khắc " + elem2 + ". Theo quan niệm cổ học, quan hệ tương khắc có thể đôi lúc dẫn đến sự bất đồng ý kiến hoặc cá tính mạnh. Tuy nhiên hoàn toàn có thể hóa giải bằng sự bao dung, lắng nghe và tôn trọng không gian riêng.";
		} else {
			menhScore = 10;
			isMenhKhac = true;
			menhTitle = elem2 + " khắc " + elem1 + " (Tương khắc)";
			menhAnalysis = "Bản mệnh " + elem2 + " khắc " + elem1 + ". Đôi bên cần kiên nhẫn hơn trong giao tiếp hàng ngày, tránh đối đầu trực diện khi có bất đồng quan điểm.";
		}

		// 2. Phân tích Thiên Can (Tối đa 20 điểm)
		String can1 = extractStem(p1.yearCanChi());
		String can2 = extractStem(p2.yearCanChi());
		int canScore;
		String canTitle;
		String canAnalysis;
		boolean isCanHopVal = isCanHop(can1, can2);

		if (isCanHopVal) {
			canScore = 20;
			canTitle = can1 + " - " + can2 + " (Thiên Can Tương Hợp - Đại Cát)";
			canAnalysis = "Thiên can năm sinh của hai bạn là " + can1 + " và " + can2 + " thuộc thế ngũ hợp trời ban. Hai người có sự gắn kết tự nhiên về mặt tư tưởng, chí hướng làm ăn và định hướng cuộc sống gia đình.";
		} else if (isCanXung(can1, can2)) {
			canScore = 8;
			canTitle = can1 + " - " + can2 + " (Thiên Can Tương Xung)";
			canAnalysis = "Thiên can " + can1 + " và " + can2 + " thuộc cặp tương xung. Có lúc quan điểm công việc hoặc cách giải quyết vấn đề có sự khác biệt, chỉ cần tôn trọng chính kiến của nhau là mọi việc đều êm đẹp.";
		} else {
			canScore = 15;
			canTitle = can1 + " - " + can2 + " (Thiên Can Bình Hòa)";
			canAnalysis = "Thiên can " + can1 + " và " + can2 + " ở thế bình hòa, không xung không khắc. Mối quan hệ dựa trên sự vun đắp, chia sẻ chân thành từ hai phía.";
		}

		// 3. Phân tích Địa Chi (Tối đa 25 điểm)
		String chi1 = extractBranch(p1.yearCanChi());
		String chi2 = extractBranch(p2.yearCanChi());
		int chiScore;
		String chiTitle;
		String chiAnalysis;
		boolean isChiHopVal = isLucHop(chi1, chi2) || isTamHop(chi1, chi2);

		if (isLucHop(chi1, chi2)) {
			chiScore = 25;
			chiTitle = chi1 + " - " + chi2 + " (Lục Hợp - Tuyệt Vời)";
			chiAnalysis = "Địa chi " + chi1 + " và " + chi2 + " thuộc thế Lục Hợp cát lợi bậc nhất. Hai bạn có sự gắn bó sâu sắc về tâm hồn, cảm xúc và duyên nợ bền chặt theo năm tháng.";
		} else if (isTamHop(chi1, chi2)) {
			chiScore = 24;
			chiTitle = chi1 + " - " + chi2 + " (Tam Hợp - May Mắn & Cát Tường)";
			chiAnalysis = "Địa chi " + chi1 + " và " + chi2 + " nằm trong vòng Tam Hợp. Vợ chồng đồng lòng, hỗ trợ nhau rất tốt trên con đường gây dựng sự nghiệp và xây tổ ấm.";
		} else if (isLucHai(chi1, chi2)) {
			chiScore = 8;
			chiTitle = chi1 + " - " + chi2 + " (Lục Hại - Cần Kiên Nhẫn)";
			chiAnalysis = "Địa chi " + chi1 + " và " + chi2 + " phạm thế Lục Hại. Trong đời sống thường nhật nên tránh xét nét tiểu tiết, đặt niềm tin trọn vẹn vào đối phương.";
		} else if (isTuXung(chi1, chi2)) {
			chiScore = 7;
			chiTitle = chi1 + " - " + chi2 + " (Tứ Hành Xung)";
			chiAnalysis = "Địa chi " + chi1 + " và " + chi2 + " nằm trong nhóm Tứ Hành Xung. Cần tránh tranh cãi những lúc căng thẳng, sự điềm đạm và lắng nghe sẽ là chìa khóa vàng gìn giữ hạnh phúc.";
		} else {
			chiScore = 16;
			chiTitle = chi1 + " - " + chi2 + " (Địa Chi Bình Hòa)";
			chiAnalysis = "Địa chi " + chi1 + " và " + chi2 + " không xung không phá, tạo thế cân bằng ổn định cho mối quan hệ.";
		}

		// 4. Phân tích Cung Phi Bát Tự (Tối đa 20 điểm)
		String cungPhi1 = calculateCungPhi(p1.lunarDate() != null ? p1.lunarDate().year() : (p1.solarYear() != null ? p1.solarYear() : 2000), "Nam".equalsIgnoreCase(p1.gender()));
		String cungPhi2 = calculateCungPhi(p2.lunarDate() != null ? p2.lunarDate().year() : (p2.solarYear() != null ? p2.solarYear() : 2000), "Nam".equalsIgnoreCase(p2.gender()));
		BatTrachRelation batTrach = evaluateBatTrach(cungPhi1, cungPhi2);
		int cungPhiScore = batTrach.score();
		boolean isBatTrachGood = batTrach.score() >= 18;
		String cungPhiTitle = cungPhi1 + " phối " + cungPhi2 + " (" + batTrach.name() + ")";
		String cungPhiAnalysis = "Cung phi Bát Tự của " + p1.name() + " là " + cungPhi1 + ", của " + p2.name() + " là " + cungPhi2 + ". Khi phối ngẫu tạo nên quẻ " + batTrach.name() + ": " + batTrach.description();

		// 5. Cung Phu Thê hai lá số (Tối đa 10 điểm)
		String phuThe1Stars = extractPalaceStars(p1, "PHU THÊ");
		String phuThe2Stars = extractPalaceStars(p2, "PHU THÊ");
		int phuTheScore = 8;
		String phuTheTitle = "Sao chủ quản Cung Phu Thê trên hai lá số";
		String phuTheAnalysis = "Cung Phu Thê của " + p1.name() + " tọa thủ các sao: " + phuThe1Stars + ". Cung Phu Thê của " + p2.name() + " tọa thủ các sao: " + phuThe2Stars + ". Sự phối chiếu giữa cung vị Phu Thê và cung Mệnh cho thấy mối liên kết nhân duyên sâu sắc, hướng tới sự ổn định và bổ trợ lẫn nhau.";

		// Rule Engine Evaluation (Chuẩn hóa từ JSON ruleset & DeepTuVi)
		CoupleRuleEvaluator.EvaluationResult evalResult = ruleEvaluator.evaluate(
				p1, p2, isMenhSinh, isMenhKhac, isCanHopVal, isChiHopVal, isBatTrachGood);

		int totalScore = evalResult.totalScore();

		String rating;
		String ratingColor;
		if (totalScore >= 85) {
			rating = "Rất Hòa Hợp (Đại Cát Hợp Hôn)";
			ratingColor = "badge-success";
		} else if (totalScore >= 70) {
			rating = "Hòa Hợp (Cát Tường Đồng Thuận)";
			ratingColor = "badge-primary";
		} else if (totalScore >= 55) {
			rating = "Bình Hòa (Khá - Cần Vun Đắp)";
			ratingColor = "badge-info";
		} else {
			rating = "Cần Thấu Hiểu & Cải Mệnh";
			ratingColor = "badge-warning";
		}

		String summary = "Hai bạn đạt chỉ số tương hợp " + totalScore + "/100 điểm (" + rating + "). Duyên nợ giữa " + p1.name() + " và " + p2.name() + " có nhiều điểm tương sinh, tương hỗ đắc lực trong cuộc sống.";

		// Generate AI Interpretation via Gemini API (or structured rule-based fallback)
		CoupleAiInterpretationService.AiInterpretationResult aiResult = aiInterpretationService.generate(
				p1, p2,
				totalScore, rating,
				evalResult.emotionalScore(),
				evalResult.communicationScore(),
				evalResult.financialScore(),
				evalResult.stabilityScore(),
				evalResult.livingTogetherScore(),
				evalResult.triggeredRules()
		);

		return new CoupleCompatibilityResult(
				totalScore,
				rating,
				ratingColor,
				summary,
				menhTitle,
				menhScore,
				menhAnalysis,
				canTitle,
				canScore,
				canAnalysis,
				chiTitle,
				chiScore,
				chiAnalysis,
				cungPhiTitle,
				cungPhiScore,
				cungPhiAnalysis,
				phuTheTitle,
				phuTheAnalysis,
				aiResult.advice(),
				evalResult.emotionalScore(),
				evalResult.communicationScore(),
				evalResult.financialScore(),
				evalResult.stabilityScore(),
				evalResult.livingTogetherScore(),
				aiResult.emotional(),
				aiResult.communication(),
				aiResult.financial(),
				aiResult.stability(),
				aiResult.livingTogether(),
				aiResult.conclusion(),
				aiResult.fullMarkdown(),
				evalResult.triggeredRules()
		);
	}

	private String extractElement(String nayin) {
		if (nayin == null) return "Kim";
		String s = nayin.toLowerCase(Locale.ROOT);
		if (s.contains("kim")) return "Kim";
		if (s.contains("mộc") || s.contains("moc")) return "Mộc";
		if (s.contains("thủy") || s.contains("thuy")) return "Thủy";
		if (s.contains("hỏa") || s.contains("hoa")) return "Hỏa";
		if (s.contains("thổ") || s.contains("tho")) return "Thổ";
		return "Kim";
	}

	private boolean isGenerating(String e1, String e2) {
		return ("Kim".equals(e1) && "Thủy".equals(e2))
				|| ("Thủy".equals(e1) && "Mộc".equals(e2))
				|| ("Mộc".equals(e1) && "Hỏa".equals(e2))
				|| ("Hỏa".equals(e1) && "Thổ".equals(e2))
				|| ("Thổ".equals(e1) && "Kim".equals(e2));
	}

	private boolean isOvercoming(String e1, String e2) {
		return ("Kim".equals(e1) && "Mộc".equals(e2))
				|| ("Mộc".equals(e1) && "Thổ".equals(e2))
				|| ("Thổ".equals(e1) && "Thủy".equals(e2))
				|| ("Thủy".equals(e1) && "Hỏa".equals(e2))
				|| ("Hỏa".equals(e1) && "Kim".equals(e2));
	}

	private String extractStem(String canChi) {
		if (canChi == null || canChi.isBlank()) return "Giáp";
		String[] parts = canChi.trim().split("\\s+");
		return parts[0];
	}

	private String extractBranch(String canChi) {
		if (canChi == null || canChi.isBlank()) return "Tý";
		String[] parts = canChi.trim().split("\\s+");
		return parts.length > 1 ? parts[1] : "Tý";
	}

	private boolean isCanHop(String c1, String c2) {
		for (Set<String> pair : CAN_HOP) {
			if (pair.contains(c1) && pair.contains(c2)) return true;
		}
		return false;
	}

	private boolean isCanXung(String c1, String c2) {
		for (Set<String> pair : CAN_XUNG) {
			if (pair.contains(c1) && pair.contains(c2)) return true;
		}
		return false;
	}

	private boolean isTamHop(String b1, String b2) {
		for (Set<String> set : CHI_TAM_HOP) {
			if (set.contains(b1) && set.contains(b2)) return true;
		}
		return false;
	}

	private boolean isLucHop(String b1, String b2) {
		for (Set<String> pair : CHI_LUC_HOP) {
			if (pair.contains(b1) && pair.contains(b2)) return true;
		}
		return false;
	}

	private boolean isLucHai(String b1, String b2) {
		for (Set<String> pair : CHI_LUC_HAI) {
			if (pair.contains(b1) && pair.contains(b2)) return true;
		}
		return false;
	}

	private boolean isTuXung(String b1, String b2) {
		for (Set<String> set : CHI_TU_XUNG) {
			if (set.contains(b1) && set.contains(b2)) return true;
		}
		return false;
	}

	private String calculateCungPhi(int year, boolean isMale) {
		int sum = 0;
		int temp = year;
		while (temp > 0) {
			sum += temp % 10;
			temp /= 10;
		}
		while (sum > 9) {
			int s = 0;
			while (sum > 0) {
				s += sum % 10;
				sum /= 10;
			}
			sum = s;
		}

		int number;
		if (isMale) {
			number = (year < 2000) ? (10 - sum) : (9 - sum);
			if (number <= 0) number += 9;
		} else {
			number = (year < 2000) ? (sum + 5) % 9 : (sum + 6) % 9;
			if (number == 0) number = 9;
		}

		return switch (number) {
			case 1 -> "Khảm";
			case 2 -> "Khôn";
			case 3 -> "Chấn";
			case 4 -> "Tốn";
			case 5 -> isMale ? "Khôn" : "Cấn";
			case 6 -> "Càn";
			case 7 -> "Đoài";
			case 8 -> "Cấn";
			case 9 -> "Ly";
			default -> "Càn";
		};
	}

	private record BatTrachRelation(String name, int score, String description) {}

	private BatTrachRelation evaluateBatTrach(String q1, String q2) {
		if (q1.equalsIgnoreCase(q2)) {
			return new BatTrachRelation("Phục Vị", 18, "Cung cát, mang lại sự bền vững, gắn bó, ổn định gia đạo.");
		}
		// Sinh Khí
		if (isPair(q1, q2, "Càn", "Đoài") || isPair(q1, q2, "Khảm", "Tốn")
				|| isPair(q1, q2, "Cấn", "Khôn") || isPair(q1, q2, "Chấn", "Ly")) {
			return new BatTrachRelation("Sinh Khí", 20, "Cung thượng cát bậc nhất trong Bát Trạch, chủ về phúc lộc vẹn toàn, con cái ngoan hiền thành đạt.");
		}
		// Diên Niên
		if (isPair(q1, q2, "Càn", "Khôn") || isPair(q1, q2, "Khảm", "Ly")
				|| isPair(q1, q2, "Cấn", "Đoài") || isPair(q1, q2, "Chấn", "Tốn")) {
			return new BatTrachRelation("Diên Niên", 19, "Cung đại cát chủ về hòa khí keo sơn, trăm năm hạnh phúc, tình cảm bền chặt dài lâu.");
		}
		// Thiên Y
		if (isPair(q1, q2, "Càn", "Cấn") || isPair(q1, q2, "Khảm", "Chấn")
				|| isPair(q1, q2, "Khôn", "Đoài") || isPair(q1, q2, "Tốn", "Ly")) {
			return new BatTrachRelation("Thiên Y", 18, "Cung cát chủ về sức khỏe khang kiện, có quý nhân phù trợ trong gia đạo.");
		}
		// Tuyệt Mệnh
		if (isPair(q1, q2, "Càn", "Ly") || isPair(q1, q2, "Khảm", "Khôn")
				|| isPair(q1, q2, "Cấn", "Tốn") || isPair(q1, q2, "Chấn", "Đoài")) {
			return new BatTrachRelation("Tuyệt Mệnh", 7, "Cung cần hóa giải: Nên chú trọng thấu hiểu, bổ trợ phong thủy phòng ngủ và hướng nhà.");
		}
		// Ngũ Quỷ
		if (isPair(q1, q2, "Càn", "Chấn") || isPair(q1, q2, "Khảm", "Cấn")
				|| isPair(q1, q2, "Khôn", "Tốn") || isPair(q1, q2, "Ly", "Đoài")) {
			return new BatTrachRelation("Ngũ Quỷ", 8, "Cung cần hóa giải: Giữ gìn tài chính minh bạch, tránh hiểu lầm trong lời nói.");
		}
		// Lục Sát
		if (isPair(q1, q2, "Càn", "Khảm") || isPair(q1, q2, "Cấn", "Chấn")
				|| isPair(q1, q2, "Khôn", "Ly") || isPair(q1, q2, "Tốn", "Đoài")) {
			return new BatTrachRelation("Lục Sát", 9, "Cung cần hóa giải: Tôn trọng không gian riêng và luôn đồng thuận khi ra quyết định lớn.");
		}
		// Họa Hại
		return new BatTrachRelation("Họa Hại", 10, "Cung cần hóa giải: Bình hòa, nhường nhịn nhau sẽ chuyển hung thành cát.");
	}

	private boolean isPair(String q1, String q2, String a, String b) {
		return (a.equalsIgnoreCase(q1) && b.equalsIgnoreCase(q2))
				|| (a.equalsIgnoreCase(q2) && b.equalsIgnoreCase(q1));
	}

	private String extractPalaceStars(CouplePersonData person, String palaceNameKeyword) {
		if (person.chartPalaces() == null || person.chartPalaces().isEmpty()) {
			return "Các cát tinh phối chiếu";
		}
		for (ChartPalaceDisplay palace : person.chartPalaces()) {
			if (palace.function() != null && palace.function().vietnameseName().equalsIgnoreCase(palaceNameKeyword)) {
				StringBuilder sb = new StringBuilder();
				if (palace.majorStars() != null) {
					for (var s : palace.majorStars()) {
						if (!sb.isEmpty()) sb.append(", ");
						sb.append(s.name()).append(" (chính tinh)");
					}
				}
				if (palace.goodStars() != null && !palace.goodStars().isEmpty()) {
					for (int i = 0; i < Math.min(3, palace.goodStars().size()); i++) {
						if (!sb.isEmpty()) sb.append(", ");
						sb.append(palace.goodStars().get(i).name());
					}
				}
				return sb.isEmpty() ? "Hội tụ cát tinh" : sb.toString();
			}
		}
		return "Các cát tinh trợ lực";
	}
}
