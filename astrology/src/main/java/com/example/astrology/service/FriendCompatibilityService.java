package com.example.astrology.service;

import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.stereotype.Service;

import com.example.astrology.model.CouplePersonData;
import com.example.astrology.model.FriendCompatibilityResult;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class FriendCompatibilityService {

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

	private static final Set<Set<String>> CHI_LUC_XUNG = Set.of(
			Set.of("Tý", "Ngọ"),
			Set.of("Sửu", "Mùi"),
			Set.of("Dần", "Thân"),
			Set.of("Mão", "Dậu"),
			Set.of("Thìn", "Tuất"),
			Set.of("Tỵ", "Hợi")
	);

	private static final Set<Set<String>> CHI_LUC_HAI = Set.of(
			Set.of("Tý", "Mùi"),
			Set.of("Sửu", "Ngọ"),
			Set.of("Dần", "Tỵ"),
			Set.of("Mão", "Thìn"),
			Set.of("Thân", "Hợi"),
			Set.of("Dậu", "Tuất")
	);

	private static final Set<String> CHI_TU_HINH = Set.of("Thìn", "Ngọ", "Dậu", "Hợi");

	private final FriendRuleEvaluator ruleEvaluator;
	private final FriendAiInterpretationService aiInterpretationService;

	@Autowired
	public FriendCompatibilityService(
			FriendRuleEvaluator ruleEvaluator,
			FriendAiInterpretationService aiInterpretationService) {
		this.ruleEvaluator = ruleEvaluator != null ? ruleEvaluator : new FriendRuleEvaluator(new DefaultResourceLoader(), new ObjectMapper());
		this.aiInterpretationService = aiInterpretationService != null ? aiInterpretationService : new FriendAiInterpretationService(null, "gemini-3.5-flash-lite", "");
	}

	public FriendCompatibilityService() {
		FriendRuleEvaluator eval = new FriendRuleEvaluator(new DefaultResourceLoader(), new ObjectMapper());
		eval.init();
		this.ruleEvaluator = eval;
		this.aiInterpretationService = new FriendAiInterpretationService(null, "gemini-3.5-flash-lite", "");
	}

	public FriendCompatibilityResult assess(CouplePersonData p1, CouplePersonData p2) {
		String elem1 = extractElement(p1.yearNayinElement());
		String elem2 = extractElement(p2.yearNayinElement());

		boolean isMenhSinh = isGenerating(elem1, elem2) || isGenerating(elem2, elem1);
		boolean isMenhKhac = isOvercoming(elem1, elem2) || isOvercoming(elem2, elem1);

		String can1 = extractStem(p1.yearCanChi());
		String can2 = extractStem(p2.yearCanChi());
		boolean isCanHopVal = isCanHop(can1, can2);
		boolean isCanXungVal = isCanXung(can1, can2);

		String chi1 = extractBranch(p1.yearCanChi());
		String chi2 = extractBranch(p2.yearCanChi());
		boolean isChiHopVal = isLucHop(chi1, chi2) || isTamHop(chi1, chi2);
		boolean isChiXungVal = isChiXung(chi1, chi2);

		// Rule Engine Evaluation (Chuẩn hóa từ JSON ruleset 5 yếu tố bạn bè / cộng sự)
		FriendRuleEvaluator.EvaluationResult evalResult = ruleEvaluator.evaluate(
				p1, p2, isMenhSinh, isMenhKhac, isCanHopVal, isChiHopVal, isCanXungVal, isChiXungVal);

		int totalScore = evalResult.totalScore();

		String rating;
		String ratingColor;
		if (totalScore >= 85) {
			rating = "Rất Hòa Hợp (Cộng Sự Đắc Lực - Tri Kỷ)";
			ratingColor = "badge-success";
		} else if (totalScore >= 70) {
			rating = "Hòa Hợp (Đồng Hành Thuận Lợi)";
			ratingColor = "badge-primary";
		} else if (totalScore >= 55) {
			rating = "Bình Hòa (Khá - Cần Phân Quyền Rõ)";
			ratingColor = "badge-info";
		} else {
			rating = "Cần Thận Trọng & Rõ Ràng Thỏa Thuận";
			ratingColor = "badge-warning";
		}

		String summary = "Hai bạn đạt chỉ số tương hợp bạn bè / cộng sự " + totalScore + "/100 điểm (" + rating + "). Mối quan hệ giữa " + p1.name() + " và " + p2.name() + " có nhiều cơ hội bổ trợ và gắn kết bền lâu.";

		// Generate AI Interpretation via Gemini API (or structured rule-based fallback)
		FriendAiInterpretationService.AiInterpretationResult aiResult = aiInterpretationService.generate(
				p1, p2,
				totalScore, rating,
				evalResult.partnerScore(),
				evalResult.communicationScore(),
				evalResult.visionScore(),
				evalResult.valuesScore(),
				evalResult.moneyScore(),
				evalResult.triggeredRules()
		);

		return new FriendCompatibilityResult(
				totalScore,
				rating,
				ratingColor,
				summary,
				evalResult.partnerScore(),
				evalResult.communicationScore(),
				evalResult.visionScore(),
				evalResult.valuesScore(),
				evalResult.moneyScore(),
				aiResult.partner(),
				aiResult.communication(),
				aiResult.vision(),
				aiResult.values(),
				aiResult.money(),
				aiResult.conclusion(),
				aiResult.advice(),
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
		if (c1 == null || c2 == null || c1.equalsIgnoreCase(c2)) return false;
		for (Set<String> pair : CAN_HOP) {
			if (pair.contains(c1) && pair.contains(c2)) return true;
		}
		return false;
	}

	private boolean isCanXung(String c1, String c2) {
		if (c1 == null || c2 == null || c1.equalsIgnoreCase(c2)) return false;
		for (Set<String> pair : CAN_XUNG) {
			if (pair.contains(c1) && pair.contains(c2)) return true;
		}
		return false;
	}

	private boolean isLucHop(String b1, String b2) {
		if (b1 == null || b2 == null || b1.equalsIgnoreCase(b2)) return false;
		for (Set<String> pair : CHI_LUC_HOP) {
			if (pair.contains(b1) && pair.contains(b2)) return true;
		}
		return false;
	}

	private boolean isTamHop(String b1, String b2) {
		if (b1 == null || b2 == null || b1.equalsIgnoreCase(b2)) return false;
		for (Set<String> triple : CHI_TAM_HOP) {
			if (triple.contains(b1) && triple.contains(b2)) return true;
		}
		return false;
	}

	private boolean isChiXung(String b1, String b2) {
		if (b1 == null || b2 == null) return false;
		if (b1.equalsIgnoreCase(b2)) {
			return CHI_TU_HINH.contains(b1);
		}
		for (Set<String> pair : CHI_LUC_XUNG) {
			if (pair.contains(b1) && pair.contains(b2)) return true;
		}
		for (Set<String> pair : CHI_LUC_HAI) {
			if (pair.contains(b1) && pair.contains(b2)) return true;
		}
		return false;
	}
}
