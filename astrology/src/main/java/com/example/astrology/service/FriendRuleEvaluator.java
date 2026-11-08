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
public class FriendRuleEvaluator {

	private static final Logger logger = LoggerFactory.getLogger(FriendRuleEvaluator.class);

	private static final Set<String> SAT_PHA_THAM = Set.of("Thất Sát", "Phá Quân", "Tham Lang");
	private static final Set<String> TU_PHU_VU_TUONG = Set.of("Tử Vi", "Thiên Phủ", "Vũ Khúc", "Thiên Tướng");
	private static final Set<String> CO_NGUYET_DONG_LUONG = Set.of("Thiên Cơ", "Thái Âm", "Thiên Đồng", "Thiên Lương");
	private static final Set<String> CU_NHAT = Set.of("Cự Môn", "Thái Dương");

	private final ResourceLoader resourceLoader;
	private final ObjectMapper objectMapper;
	private RulesetConfig rulesetConfig;

	public FriendRuleEvaluator(ResourceLoader resourceLoader, ObjectMapper objectMapper) {
		this.resourceLoader = resourceLoader;
		this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
	}

	@PostConstruct
	public void init() {
		try {
			Resource resource = resourceLoader.getResource("classpath:rules/friend_compatibility_rules.json");
			if (resource.exists()) {
				try (InputStream is = resource.getInputStream()) {
					rulesetConfig = objectMapper.readValue(is, RulesetConfig.class);
					logger.info("Loaded {} friend compatibility rules from 5-factor JSON ruleset",
							rulesetConfig != null && rulesetConfig.rules != null ? rulesetConfig.rules.size() : 0);
				}
			} else {
				logger.warn("friend_compatibility_rules.json not found on classpath");
			}
		} catch (Exception ex) {
			logger.error("Failed to load friend compatibility rules JSON", ex);
		}
	}

	public EvaluationResult evaluate(
			CouplePersonData p1,
			CouplePersonData p2,
			boolean isMenhSinh,
			boolean isMenhKhac,
			boolean isCanHop,
			boolean isChiHop) {

		List<TriggeredRule> triggered = new ArrayList<>();

		Set<String> menh1 = extractStarNames(p1, PalaceFunction.MENH);
		Set<String> menh2 = extractStarNames(p2, PalaceFunction.MENH);

		Set<String> noBoc1 = extractStarNames(p1, PalaceFunction.NO_BOC);
		Set<String> noBoc2 = extractStarNames(p2, PalaceFunction.NO_BOC);

		Set<String> huynhDe1 = extractStarNames(p1, PalaceFunction.HUYNH_DE);
		Set<String> huynhDe2 = extractStarNames(p2, PalaceFunction.HUYNH_DE);

		Set<String> thienDi1 = extractStarNames(p1, PalaceFunction.THIEN_DI);
		Set<String> thienDi2 = extractStarNames(p2, PalaceFunction.THIEN_DI);

		Set<String> quanLoc1 = extractStarNames(p1, PalaceFunction.QUAN_LOC);
		Set<String> quanLoc2 = extractStarNames(p2, PalaceFunction.QUAN_LOC);

		Set<String> phucDuc1 = extractStarNames(p1, PalaceFunction.PHUC_DUC);
		Set<String> phucDuc2 = extractStarNames(p2, PalaceFunction.PHUC_DUC);

		Set<String> taiBach1 = extractStarNames(p1, PalaceFunction.TAI_BACH);
		Set<String> taiBach2 = extractStarNames(p2, PalaceFunction.TAI_BACH);

		if (rulesetConfig != null && rulesetConfig.rules != null) {
			for (RuleDefinition rule : rulesetConfig.rules) {
				boolean active = matchRule(rule, p1, p2,
						menh1, menh2, noBoc1, noBoc2, huynhDe1, huynhDe2,
						thienDi1, thienDi2, quanLoc1, quanLoc2,
						phucDuc1, phucDuc2, taiBach1, taiBach2,
						isMenhSinh, isMenhKhac, isCanHop, isChiHop);
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

		// Calculate Sub-scores for 5 Core Friend/Partner Factors (Base scores)
		double partner = computeDimensionScore(triggered, "FRIEND_PARTNER", 7.0);
		double communication = computeDimensionScore(triggered, "COMMUNICATION", 6.8);
		double vision = computeDimensionScore(triggered, "VISION_WORK", 7.0);
		double values = computeDimensionScore(triggered, "VALUES_PRINCIPLES", 7.0);
		double money = computeDimensionScore(triggered, "FINANCIAL_BENEFIT", 6.8);

		// Total score (weighted out of 100):
		// 1. Friend/Partner: 25%
		// 2. Communication: 20%
		// 3. Vision & Work: 20%
		// 4. Values & Principles: 15%
		// 5. Money & Benefit: 20%
		int totalScore = (int) Math.round(
				partner * 2.5 +
				communication * 2.0 +
				vision * 2.0 +
				values * 1.5 +
				money * 2.0
		);
		totalScore = Math.max(35, Math.min(98, totalScore));

		return new EvaluationResult(
				totalScore,
				partner,
				communication,
				vision,
				values,
				money,
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
			Set<String> noBoc1, Set<String> noBoc2,
			Set<String> huynhDe1, Set<String> huynhDe2,
			Set<String> thienDi1, Set<String> thienDi2,
			Set<String> quanLoc1, Set<String> quanLoc2,
			Set<String> phucDuc1, Set<String> phucDuc2,
			Set<String> taiBach1, Set<String> taiBach2,
			boolean isMenhSinh, boolean isMenhKhac,
			boolean isCanHop, boolean isChiHop) {

		String type = rule.conditionType != null ? rule.conditionType : "";
		return switch (type) {
			case "CROSS_MENH_NOBOC" -> hasOverlap(menh1, noBoc2) || hasOverlap(menh2, noBoc1);
			case "PALACE_STARS_CONTAIN", "PALACE_GOOD_STARS" -> checkPalaceContains(rule.palace, rule.stars, p1, p2);
			case "PALACE_TUAN_TRIET" -> checkTuanTriet(rule.palace, p1, p2);
			case "MENH_POLARITY_COMPLEMENT" -> isPolarityComplement(menh1, menh2) || isPolarityComplement(quanLoc1, quanLoc2);
			case "POWER_CLASH" -> isPowerClash(menh1, menh2, quanLoc1, quanLoc2);
			case "FINANCE_WIN_WIN" -> checkFinanceWinWin(taiBach1, taiBach2);
			case "CU_MON_GOOD" -> (containsAny(noBoc1, "Cự Môn") || containsAny(menh1, "Cự Môn") || containsAny(thienDi1, "Cự Môn") || containsAny(noBoc2, "Cự Môn") || containsAny(menh2, "Cự Môn") || containsAny(thienDi2, "Cự Môn"))
					&& (containsAny(noBoc1, "Hóa Khoa", "Hóa Lộc", "Tấu Thư") || containsAny(noBoc2, "Hóa Khoa", "Hóa Lộc", "Tấu Thư")
					|| containsAny(thienDi1, "Hóa Khoa", "Hóa Lộc", "Tấu Thư") || containsAny(thienDi2, "Hóa Khoa", "Hóa Lộc", "Tấu Thư")
					|| containsAny(menh1, "Hóa Khoa", "Hóa Lộc", "Tấu Thư") || containsAny(menh2, "Hóa Khoa", "Hóa Lộc", "Tấu Thư"));
			case "CU_MON_CLASH" -> (containsAny(noBoc1, "Cự Môn") && containsAny(noBoc2, "Cự Môn"))
					|| ((containsAny(noBoc1, "Cự Môn") || containsAny(noBoc2, "Cự Môn") || containsAny(thienDi1, "Cự Môn") || containsAny(thienDi2, "Cự Môn") || containsAny(menh1, "Cự Môn") || containsAny(menh2, "Cự Môn"))
					&& (containsAny(noBoc1, "Kình Dương", "Đà La", "Hóa Kỵ") || containsAny(noBoc2, "Kình Dương", "Đà La", "Hóa Kỵ")
					|| containsAny(thienDi1, "Kình Dương", "Đà La", "Hóa Kỵ") || containsAny(thienDi2, "Kình Dương", "Đà La", "Hóa Kỵ")
					|| containsAny(menh1, "Kình Dương", "Đà La", "Hóa Kỵ") || containsAny(menh2, "Kình Dương", "Đà La", "Hóa Kỵ")));
			case "TRADITIONAL_MENH_SINH" -> isMenhSinh;
			case "TRADITIONAL_MENH_KHAC" -> isMenhKhac;
			case "TRADITIONAL_CAN_HOP" -> isCanHop;
			case "TRADITIONAL_CHI_HOP" -> isChiHop;
			default -> false;
		};
	}

	private boolean isPolarityComplement(Set<String> s1, Set<String> s2) {
		boolean c1 = containsAny(s1, SAT_PHA_THAM) || containsAny(s1, TU_PHU_VU_TUONG);
		boolean c2 = containsAny(s2, SAT_PHA_THAM) || containsAny(s2, TU_PHU_VU_TUONG);
		boolean n1 = containsAny(s1, CO_NGUYET_DONG_LUONG);
		boolean n2 = containsAny(s2, CO_NGUYET_DONG_LUONG);
		return (c1 && n2) || (n1 && c2);
	}

	private boolean isPowerClash(Set<String> m1, Set<String> m2, Set<String> q1, Set<String> q2) {
		Set<String> p1Comb = new HashSet<>(m1);
		p1Comb.addAll(q1);
		Set<String> p2Comb = new HashSet<>(m2);
		p2Comb.addAll(q2);

		boolean p1High = containsAny(p1Comb, "Thất Sát", "Phá Quân", "Tử Vi");
		boolean p2High = containsAny(p2Comb, "Thất Sát", "Phá Quân", "Tử Vi");
		return p1High && p2High;
	}

	private boolean checkFinanceWinWin(Set<String> tb1, Set<String> tb2) {
		boolean make1 = containsAny(tb1, "Hóa Lộc", "Vũ Khúc", "Tham Lang");
		boolean keep2 = containsAny(tb2, "Thiên Phủ", "Lộc Tồn");
		boolean make2 = containsAny(tb2, "Hóa Lộc", "Vũ Khúc", "Tham Lang");
		boolean keep1 = containsAny(tb1, "Thiên Phủ", "Lộc Tồn");
		return (make1 && keep2) || (make2 && keep1);
	}

	private boolean hasOverlap(Set<String> s1, Set<String> s2) {
		for (String s : s1) {
			if (s2.contains(s)) return true;
		}
		return false;
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
			double partnerScore,
			double communicationScore,
			double visionScore,
			double valuesScore,
			double moneyScore,
			List<TriggeredRule> triggeredRules
	) {
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
