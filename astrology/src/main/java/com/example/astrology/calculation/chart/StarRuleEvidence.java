package com.example.astrology.calculation.chart;

import java.util.List;
import java.util.Objects;

public record StarRuleEvidence(
		StarRuleGroup group,
		RuleStatus status,
		String ruleSetVersion,
		List<RuleSource> sources) {

	public StarRuleEvidence {
		Objects.requireNonNull(group, "Nhóm quy tắc không được để trống.");
		Objects.requireNonNull(status, "Trạng thái quy tắc không được để trống.");
		if (ruleSetVersion == null || ruleSetVersion.isBlank()) {
			throw new IllegalArgumentException("Phiên bản bộ quy tắc không được để trống.");
		}
		sources = List.copyOf(Objects.requireNonNull(sources, "Danh sách nguồn không được để trống."));
		if (status == RuleStatus.VERIFIED && sources.isEmpty()) {
			throw new IllegalArgumentException("Quy tắc đã xác minh phải có ít nhất một nguồn.");
		}
	}

	public static StarRuleEvidence pending(StarRuleGroup group, String ruleSetVersion) {
		return new StarRuleEvidence(group, RuleStatus.PENDING_CONFIRMATION, ruleSetVersion, List.of());
	}
}
