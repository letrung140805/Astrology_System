package com.example.astrology.service;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.example.astrology.calculation.chart.PalaceFunction;
import com.example.astrology.model.ChartPalaceDisplay;
import com.example.astrology.model.ChartStarDisplay;
import com.example.astrology.model.CouplePersonData;
import com.example.astrology.model.TriggeredRule;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Lớp trừu tượng nền tảng cho bộ đánh giá tương hợp (Compatibility Rule Evaluator).
 * Chia sẻ các thành phần dùng chung:
 * 1. Nạp và phân giải cấu hình JSON tập luật (Ruleset).
 * 2. Các nhóm chính tinh cơ bản trong Tử Vi (Sát Phá Tham, Tử Phủ Vũ Tướng, Cơ Nguyệt Đồng Lương, Cự Nhật).
 * 3. Các hàm trợ giúp kiểm tra sao, cung vị và tính điểm từng khía cạnh.
 *
 * Lưu ý kiến trúc: Giữ nguyên các quy tắc chuyên biệt và kết quả đặc thù riêng cho
 * Tương hợp Tình Duyên (Couple) và Tương hợp Bạn Bè & Cộng Sự (Friend).
 */
public abstract class AbstractCompatibilityRuleEvaluator {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected static final Set<String> SAT_PHA_THAM = Set.of("Thất Sát", "Phá Quân", "Tham Lang");
	protected static final Set<String> TU_PHU_VU_TUONG = Set.of("Tử Vi", "Thiên Phủ", "Vũ Khúc", "Thiên Tướng");
	protected static final Set<String> CO_NGUYET_DONG_LUONG = Set.of("Thiên Cơ", "Thái Âm", "Thiên Đồng", "Thiên Lương");
	protected static final Set<String> CU_NHAT = Set.of("Cự Môn", "Thái Dương");

	protected final ResourceLoader resourceLoader;
	protected final ObjectMapper objectMapper;
	protected RulesetConfig rulesetConfig;

	protected AbstractCompatibilityRuleEvaluator(ResourceLoader resourceLoader, ObjectMapper objectMapper) {
		this.resourceLoader = resourceLoader;
		this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
	}

	protected void loadRules(String resourcePath, String rulesetName) {
		try {
			Resource resource = resourceLoader.getResource(resourcePath);
			if (resource.exists()) {
				try (InputStream is = resource.getInputStream()) {
					rulesetConfig = objectMapper.readValue(is, RulesetConfig.class);
					logger.info("Loaded {} {} rules from 5-factor JSON ruleset",
							rulesetConfig != null && rulesetConfig.rules != null ? rulesetConfig.rules.size() : 0,
							rulesetName);
				}
			} else {
				logger.warn("{} not found on classpath: {}", rulesetName, resourcePath);
			}
		} catch (Exception ex) {
			logger.error("Failed to load {} rules JSON from {}", rulesetName, resourcePath, ex);
		}
	}

	protected double computeDimensionScore(List<TriggeredRule> rules, String dimension, double base) {
		return computeDimensionScore(rules, dimension, base, 0.4);
	}

	protected double computeDimensionScore(List<TriggeredRule> rules, String dimension, double base, double weightMultiplier) {
		double sum = base;
		for (TriggeredRule r : rules) {
			if (dimension.equalsIgnoreCase(r.dimension())) {
				sum += (r.weight() * weightMultiplier);
			}
		}
		sum = Math.max(3.0, Math.min(10.0, sum));
		return Math.round(sum * 10.0) / 10.0;
	}

	protected boolean containsAny(Set<String> source, Iterable<String> targets) {
		if (source == null || targets == null) return false;
		for (String target : targets) {
			if (source.contains(target)) return true;
		}
		return false;
	}

	protected boolean containsAny(Set<String> source, String... targets) {
		if (source == null || targets == null) return false;
		for (String target : targets) {
			if (source.contains(target)) return true;
		}
		return false;
	}

	protected Set<String> extractStarNames(CouplePersonData person, PalaceFunction func) {
		ChartPalaceDisplay palace = findPalace(person, func);
		if (palace == null || palace.stars() == null) return Collections.emptySet();
		return palace.stars().stream()
				.map(ChartStarDisplay::name)
				.collect(Collectors.toSet());
	}

	protected ChartPalaceDisplay findPalace(CouplePersonData person, PalaceFunction func) {
		if (person.chartPalaces() == null) return null;
		for (ChartPalaceDisplay p : person.chartPalaces()) {
			if (p.function() == func) return p;
		}
		return null;
	}

	protected PalaceFunction parsePalaceFunction(String name) {
		try {
			return PalaceFunction.valueOf(name);
		} catch (Exception ex) {
			for (PalaceFunction pf : PalaceFunction.values()) {
				if (pf.vietnameseName().equalsIgnoreCase(name)) return pf;
			}
			return null;
		}
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
