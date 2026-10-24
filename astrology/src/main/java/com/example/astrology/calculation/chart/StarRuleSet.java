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

	public boolean isFullyVerified() {
		return evidence.values().stream().allMatch(item -> item.status() == RuleStatus.VERIFIED);
	}
}
