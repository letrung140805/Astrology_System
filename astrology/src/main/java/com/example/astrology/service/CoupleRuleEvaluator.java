package com.example.astrology.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.example.astrology.calculation.chart.PalaceFunction;
import com.example.astrology.model.ChartPalaceDisplay;
import com.example.astrology.model.ChartStarDisplay;
import com.example.astrology.model.CouplePersonData;
import com.example.astrology.model.TriggeredRule;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Component
public class CoupleRuleEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(CoupleRuleEvaluator.class);

	private static final Set<String> SAT_PHA_THAM = Set.of("Thất Sát", "Phá Quân", "Tham Lang");
	private static final Set<String> TU_PHU_VU_TUONG = Set.of("Tử Vi", "Thiên Phủ", "Vũ Khúc", "Thiên Tướng");
	private static final Set<String> CO_NGUYET_DONG_LUONG = Set.of("Thiên Cơ", "Thái Âm", "Thiên Đồng", "Thiên Lương");
	private static final Set<String> CU_NHAT = Set.of("Cự Môn", "Thái Dương");

	private final ResourceLoader resourceLoader;
	private final ObjectMapper objectMapper;
	private RulesetConfig rulesetConfig;

	public CoupleRuleEvaluator(ResourceLoader resourceLoader, ObjectMapper objectMapper) {
		this.resourceLoader = resourceLoader;
		this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
	}

	@PostConstruct
	public void init() {
		try {
			Resource resource = resourceLoader.getResource("classpath:rules/couple_compatibility_rules.json");
			if (resource.exists()) {
				try (InputStream is = resource.getInputStream()) {
					rulesetConfig = objectMapper.readValue(is, RulesetConfig.class);
					logger.info("Loaded {} couple compatibility rules from 5-factor JSON ruleset",
							rulesetConfig != null && rulesetConfig.rules != null ? rulesetConfig.rules.size() : 0);
				}
			} else {
				logger.warn("couple_compatibility_rules.json not found on classpath");
			}
		} catch (Exception ex) {
			logger.error("Failed to load couple compatibility rules JSON", ex);
		}
	}

	public EvaluationResult evaluate(
			CouplePersonData p1,
			CouplePersonData p2,
			boolean isMenhSinh,
			boolean isMenhKhac,
			boolean isCanHop,
			boolean isChiHop,
			boolean isBatTrachGood) {

		List<TriggeredRule> triggered = new ArrayList<>();

		Set<String> menh1 = extractStarNames(p1, PalaceFunction.MENH);
		Set<String> menh2 = extractStarNames(p2, PalaceFunction.MENH);

		Set<String> phuThe1 = extractStarNames(p1, PalaceFunction.PHU_THE);
		Set<String> phuThe2 = extractStarNames(p2, PalaceFunction.PHU_THE);

		Set<String> phucDuc1 = extractStarNames(p1, PalaceFunction.PHUC_DUC);
		Set<String> phucDuc2 = extractStarNames(p2, PalaceFunction.PHUC_DUC);

		Set<String> noBoc1 = extractStarNames(p1, PalaceFunction.NO_BOC);
		Set<String> noBoc2 = extractStarNames(p2, PalaceFunction.NO_BOC);

		Set<String> thienDi1 = extractStarNames(p1, PalaceFunction.THIEN_DI);
		Set<String> thienDi2 = extractStarNames(p2, PalaceFunction.THIEN_DI);

		Set<String> dienTrach1 = extractStarNames(p1, PalaceFunction.DIEN_TRACH);
		Set<String> dienTrach2 = extractStarNames(p2, PalaceFunction.DIEN_TRACH);

		Set<String> taiBach1 = extractStarNames(p1, PalaceFunction.TAI_BACH);
		Set<String> taiBach2 = extractStarNames(p2, PalaceFunction.TAI_BACH);

		Set<String> quanLoc1 = extractStarNames(p1, PalaceFunction.QUAN_LOC);
		Set<String> quanLoc2 = extractStarNames(p2, PalaceFunction.QUAN_LOC);

		if (rulesetConfig != null && rulesetConfig.rules != null) {
			for (RuleDefinition rule : rulesetConfig.rules) {
				boolean active = matchRule(rule, p1, p2,
						menh1, menh2, phuThe1, phuThe2, phucDuc1, phucDuc2,
						noBoc1, noBoc2, thienDi1, thienDi2, dienTrach1, dienTrach2,
						taiBach1, taiBach2, quanLoc1, quanLoc2,
						isMenhSinh, isMenhKhac, isCanHop, isChiHop, isBatTrachGood);
				if (active) {
					triggered.add(new TriggeredRule(
							rule.id,
							rule.name,
							rule.dimension,
							rule.category,
							rule.weight,
							rule.description,
							rule.advice
					));
				}
			}
		}

		// Calculate Sub-scores for 5 Core Factors (Base: 7.0 for each factor)
		double emotional = computeDimensionScore(triggered, "EMOTIONAL", 7.0);
		double communication = computeDimensionScore(triggered, "COMMUNICATION", 6.8);
		double financial = computeDimensionScore(triggered, "FINANCIAL", 7.0);
		double stability = computeDimensionScore(triggered, "STABILITY", 6.5);
		double livingTogether = computeDimensionScore(triggered, "LIVING_TOGETHER", 6.8);

		// Total score (weighted out of 100):
		// Emotional: 25%, Communication: 20%, Financial: 15%, Stability: 20%, Living Together: 20%
		int totalScore = (int) Math.round(
				emotional * 2.5 +
				communication * 2.0 +
				financial * 1.5 +
				stability * 2.0 +
				livingTogether * 2.0
		);
		totalScore = Math.max(35, Math.min(98, totalScore));

		return new EvaluationResult(
				totalScore,
				emotional,
				communication,
				financial,
				stability,
				livingTogether,
				triggered
		);
	}

	private double computeDimensionScore(List<TriggeredRule> rules, String dimension, double base) {
		double sum = base;
		for (TriggeredRule r : rules) {
			if (dimension.equalsIgnoreCase(r.dimension())) {
				sum += (r.weight() * 0.4);
			}
		}
		sum = Math.max(3.0, Math.min(10.0, sum));
		return Math.round(sum * 10.0) / 10.0;
	}

	private boolean matchRule(
			RuleDefinition rule,
			CouplePersonData p1,
			CouplePersonData p2,
			Set<String> menh1, Set<String> menh2,
			Set<String> phuThe1, Set<String> phuThe2,
			Set<String> phucDuc1, Set<String> phucDuc2,
			Set<String> noBoc1, Set<String> noBoc2,
			Set<String> thienDi1, Set<String> thienDi2,
			Set<String> dienTrach1, Set<String> dienTrach2,
			Set<String> taiBach1, Set<String> taiBach2,
			Set<String> quanLoc1, Set<String> quanLoc2,
			boolean isMenhSinh, boolean isMenhKhac,
			boolean isCanHop, boolean isChiHop, boolean isBatTrachGood) {

		String type = rule.conditionType != null ? rule.conditionType : "";
		return switch (type) {
			case "CROSS_MENH_PHUTHE_ANY" -> hasOverlap(menh1, phuThe2) || hasOverlap(menh2, phuThe1);
			case "CROSS_DUAL_RESONANCE" -> hasOverlap(menh1, phuThe2) && hasOverlap(menh2, phuThe1);
			case "SHARED_STAR_MENH_OR_PHUC" -> hasSharedMajorStar(menh1, menh2) || hasSharedMajorStar(phucDuc1, phucDuc2);
			case "MENH_POLARITY_COMPLEMENT" -> isPolarityComplement(menh1, menh2);
			case "ROMANTIC_STARS_PRESENT" -> hasAnyRomantic(menh1, phuThe1, menh2, phuThe2);
			case "PALACE_STARS_CONTAIN", "PALACE_GOOD_STARS" -> checkPalaceContains(rule.palace, rule.stars, p1, p2);
			case "PALACE_TUAN_TRIET" -> checkTuanTriet(rule.palace, p1, p2);
			case "CU_MON_GOOD" -> (containsAny(noBoc1, "Cự Môn") || containsAny(menh1, "Cự Môn") || containsAny(noBoc2, "Cự Môn") || containsAny(menh2, "Cự Môn"))
					&& (containsAny(noBoc1, "Hóa Khoa", "Hóa Lộc", "Tấu Thư") || containsAny(noBoc2, "Hóa Khoa", "Hóa Lộc", "Tấu Thư"));
			case "CU_MON_CLASH" -> (containsAny(noBoc1, "Cự Môn") && containsAny(noBoc2, "Cự Môn"))
					|| ((containsAny(noBoc1, "Cự Môn") || containsAny(noBoc2, "Cự Môn")) && (containsAny(noBoc1, "Kình Dương", "Hóa Kỵ") || containsAny(noBoc2, "Kình Dương", "Hóa Kỵ")));
			case "SATS_STARS_PRESENT" -> hasSatsPresent(menh1, phuThe1, menh2, phuThe2, rule.stars);
			case "RESCUE_STARS_PRESENT" -> hasRescueStars(menh1, phuThe1, phucDuc1, menh2, phuThe2, phucDuc2, rule.stars);
			case "TRADITIONAL_MENH_SINH" -> isMenhSinh;
			case "TRADITIONAL_MENH_KHAC" -> isMenhKhac;
			case "TRADITIONAL_CHI_HOP" -> isChiHop;
			case "TRADITIONAL_BAT_TRACH_GOOD" -> isBatTrachGood;
			default -> false;
		};
	}

	private boolean isPolarityComplement(Set<String> m1, Set<String> m2) {
		boolean m1Cuong = containsAny(m1, SAT_PHA_THAM) || containsAny(m1, TU_PHU_VU_TUONG);
		boolean m2Cuong = containsAny(m2, SAT_PHA_THAM) || containsAny(m2, TU_PHU_VU_TUONG);
		boolean m1Nhu = containsAny(m1, CO_NGUYET_DONG_LUONG);
		boolean m2Nhu = containsAny(m2, CO_NGUYET_DONG_LUONG);
		return (m1Cuong && m2Nhu) || (m1Nhu && m2Cuong);
	}

	private boolean hasSharedMajorStar(Set<String> s1, Set<String> s2) {
		Set<String> major14 = new HashSet<>(SAT_PHA_THAM);
		major14.addAll(TU_PHU_VU_TUONG);
		major14.addAll(CO_NGUYET_DONG_LUONG);
		major14.addAll(CU_NHAT);
		for (String star : s1) {
			if (major14.contains(star) && s2.contains(star)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasOverlap(Set<String> s1, Set<String> s2) {
		for (String s : s1) {
			if (s2.contains(s)) return true;
		}
		return false;
	}

	private boolean hasAnyRomantic(Set<String> m1, Set<String> pt1, Set<String> m2, Set<String> pt2) {
		Set<String> rom = Set.of("Đào Hoa", "Hồng Loan", "Thiên Hỷ");
		return containsAny(m1, rom) || containsAny(pt1, rom) || containsAny(m2, rom) || containsAny(pt2, rom);
	}

	private boolean checkPalaceContains(String palaceName, List<String> targetStars, CouplePersonData p1, CouplePersonData p2) {
		if (palaceName == null || targetStars == null || targetStars.isEmpty()) return false;
		PalaceFunction func = parsePalaceFunction(palaceName);
		if (func == null) return false;
		Set<String> stars1 = extractStarNames(p1, func);
		Set<String> stars2 = extractStarNames(p2, func);
		return containsAny(stars1, targetStars) || containsAny(stars2, targetStars);
	}

	private boolean checkTuanTriet(String palaceName, CouplePersonData p1, CouplePersonData p2) {
		PalaceFunction func = parsePalaceFunction(palaceName);
		if (func == null) return false;
		ChartPalaceDisplay pal1 = findPalace(p1, func);
		ChartPalaceDisplay pal2 = findPalace(p2, func);
		return (pal1 != null && (pal1.hasTuan() || pal1.hasTriet()))
				|| (pal2 != null && (pal2.hasTuan() || pal2.hasTriet()));
	}

	private boolean hasSatsPresent(Set<String> m1, Set<String> pt1, Set<String> m2, Set<String> pt2, List<String> targets) {
		if (targets == null) targets = List.of("Kình Dương", "Đà La", "Hỏa Tinh", "Linh Tinh", "Địa Không", "Địa Kiếp");
		return containsAny(m1, targets) || containsAny(pt1, targets) || containsAny(m2, targets) || containsAny(pt2, targets);
	}

	private boolean hasRescueStars(Set<String> m1, Set<String> pt1, Set<String> pd1, Set<String> m2, Set<String> pt2, Set<String> pd2, List<String> targets) {
		if (targets == null) targets = List.of("Hóa Khoa", "Thiên Khôi", "Thiên Việt", "Giải Thần", "Long Đức");
		return containsAny(m1, targets) || containsAny(pt1, targets) || containsAny(pd1, targets)
				|| containsAny(m2, targets) || containsAny(pt2, targets) || containsAny(pd2, targets);
	}

	private boolean containsAny(Set<String> source, Iterable<String> targets) {
		if (source == null || targets == null) return false;
		for (String target : targets) {
			if (source.contains(target)) return true;
		}
		return false;
	}

	private boolean containsAny(Set<String> source, String... targets) {
		if (source == null || targets == null) return false;
		for (String target : targets) {
			if (source.contains(target)) return true;
		}
		return false;
	}

	private Set<String> extractStarNames(CouplePersonData person, PalaceFunction func) {
		ChartPalaceDisplay palace = findPalace(person, func);
		if (palace == null || palace.stars() == null) return Collections.emptySet();
		return palace.stars().stream()
				.map(ChartStarDisplay::name)
				.collect(Collectors.toSet());
	}

	private ChartPalaceDisplay findPalace(CouplePersonData person, PalaceFunction func) {
		if (person.chartPalaces() == null) return null;
		for (ChartPalaceDisplay p : person.chartPalaces()) {
			if (p.function() == func) return p;
		}
		return null;
	}

	private PalaceFunction parsePalaceFunction(String name) {
		try {
			return PalaceFunction.valueOf(name);
		} catch (Exception ex) {
			for (PalaceFunction pf : PalaceFunction.values()) {
				if (pf.vietnameseName().equalsIgnoreCase(name)) return pf;
			}
			return null;
		}
	}

	public record EvaluationResult(
			int totalScore,
			double emotionalScore,
			double communicationScore,
			double financialScore,
			double stabilityScore,
			double livingTogetherScore,
			List<TriggeredRule> triggeredRules
	) {
		public double attractionScore() { return emotionalScore; }
		public double personalityScore() { return emotionalScore; }
		public double partnerFitScore() { return emotionalScore; }
		public double dailyLifeScore() { return livingTogetherScore; }
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class RulesetConfig {
		public String version;
		public String name;
		public String description;
		public List<RuleDefinition> rules;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class RuleDefinition {
		public String id;
		public String dimension;
		public String name;
		public String category;
		public int weight;
		public String conditionType;
		public String palace;
		public List<String> stars;
		public String description;
		public String advice;
	}
}
