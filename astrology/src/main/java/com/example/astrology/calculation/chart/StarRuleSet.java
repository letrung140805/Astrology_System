package com.example.astrology.calculation.chart;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public record StarRuleSet(String version, Map<StarRuleGroup, StarRuleEvidence> evidence) {

	public StarRuleSet {
		if (version == null || version.isBlank()) {
			throw new IllegalArgumentException("Phiên bản bộ quy tắc không được để trống.");
		}
		Objects.requireNonNull(evidence, "Bằng chứng bộ quy tắc không được để trống.");

		var copy = new EnumMap<StarRuleGroup, StarRuleEvidence>(StarRuleGroup.class);
		copy.putAll(evidence);
		if (!copy.keySet().equals(java.util.EnumSet.allOf(StarRuleGroup.class))) {
			throw new IllegalArgumentException("Bộ quy tắc phải khai báo trạng thái cho từng nhóm sao.");
		}
		copy.forEach((group, groupEvidence) -> {
			if (groupEvidence.group() != group || !groupEvidence.ruleSetVersion().equals(version)) {
				throw new IllegalArgumentException("Bằng chứng phải khớp nhóm và phiên bản bộ quy tắc.");
			}
		});
		evidence = Collections.unmodifiableMap(copy);
	}

	public static StarRuleSet pendingInitial() {
		String version = "vietnamese-tu-vi-pending-1";
		var evidence = new EnumMap<StarRuleGroup, StarRuleEvidence>(StarRuleGroup.class);
		for (StarRuleGroup group : StarRuleGroup.values()) {
			evidence.put(group, StarRuleEvidence.pending(group, version));
		}
		return new StarRuleSet(version, evidence);
	}

	public static StarRuleSet tracuutuviPublicCandidate() {
		String version = "tracuutuvi-public-rules-2026-10-24+quanshu-supplement-1+cestu-79ebf331";
		var site = new RuleSource(
				"Tra Cứu Tử Vi - An sao tử vi",
				java.net.URI.create("https://tracuutuvi.com/an-sao-tu-vi.html"),
				"Public article accessed 2026-10-24",
				"sections 2.2-3.4 and 4.1");
		var classic = new RuleSource(
				"紫微斗數全書",
				java.net.URI.create("https://zh.wikisource.org/wiki/紫微斗數全書/卷二"),
				"Wikisource electronic transcription accessed 2026-10-24",
				"安身命例; 安十二宮例; 安南北斗諸星訣");
		var fourTransformations = new RuleSource(
				"Tra Cứu Tử Vi - Tứ Hóa",
				java.net.URI.create("https://tracuutuvi.com/tu-hoa.html"),
				"Public article accessed 2026-10-24",
				"section 2; Nam phái table");
		var ceStu = new RuleSource(
				"HAHVAC/CEStu-vi-ai",
				java.net.URI.create(
						"https://github.com/HAHVAC/CEStu-vi-ai/blob/79ebf331e95e8f2005a0c8ef62b82a2a845fc893/lib/ziwei/constants.ts"),
				"Git revision 79ebf331e95e8f2005a0c8ef62b82a2a845fc893",
				"TIANKUI_TABLE, TIANMA_TABLE, and NAYIN_ELEMENTS");
		var evidence = new EnumMap<StarRuleGroup, StarRuleEvidence>(StarRuleGroup.class);
		evidence.put(StarRuleGroup.FOURTEEN_MAJOR_STARS,
				StarRuleEvidence.documented(StarRuleGroup.FOURTEEN_MAJOR_STARS, version, java.util.List.of(site, classic)));
		evidence.put(StarRuleGroup.LOC_TON_AND_YEAR_STEM_STARS,
				StarRuleEvidence.documented(
						StarRuleGroup.LOC_TON_AND_YEAR_STEM_STARS, version, java.util.List.of(site, ceStu)));
		evidence.put(StarRuleGroup.TIME_AND_MONTH_AUXILIARY_STARS,
				StarRuleEvidence.documented(StarRuleGroup.TIME_AND_MONTH_AUXILIARY_STARS, version, java.util.List.of(site)));
		evidence.put(StarRuleGroup.FOUR_TRANSFORMATIONS,
				StarRuleEvidence.documented(
						StarRuleGroup.FOUR_TRANSFORMATIONS, version, java.util.List.of(site, fourTransformations)));
		evidence.put(StarRuleGroup.CHANG_SHENG_CYCLE,
				StarRuleEvidence.documented(StarRuleGroup.CHANG_SHENG_CYCLE, version, java.util.List.of(site)));
		evidence.put(StarRuleGroup.STAR_BRIGHTNESS,
				StarRuleEvidence.pending(StarRuleGroup.STAR_BRIGHTNESS, version));
		return new StarRuleSet(version, evidence);
	}

	public boolean isFullyVerified() {
		return evidence.values().stream().allMatch(item -> item.status() == RuleStatus.VERIFIED);
	}
}
