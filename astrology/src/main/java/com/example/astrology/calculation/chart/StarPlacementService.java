package com.example.astrology.calculation.chart;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.example.astrology.calculation.calendar.EarthlyBranch;

@Service
public class StarPlacementService {

	public StarChart addVerifiedPlacement(
			StarChart chart,
			StarId star,
			EarthlyBranch branch,
			StarRuleEvidence evidence) {
		Objects.requireNonNull(chart, "Lá số sao không được để trống.");
		Objects.requireNonNull(evidence, "Bằng chứng quy tắc không được để trống.");
		if (evidence.status() != RuleStatus.VERIFIED) {
			throw new IllegalStateException(
					"Không thể an sao như quy tắc đã xác minh khi trạng thái là "
							+ evidence.status() + ": " + evidence.group() + ".");
		}
		return chart.withPlacement(star, branch);
	}

	public StarChart addDocumentedPlacement(
			StarChart chart,
			StarId star,
			EarthlyBranch branch,
			StarRuleEvidence evidence) {
		Objects.requireNonNull(chart, "Lá số sao không được để trống.");
		Objects.requireNonNull(evidence, "Bằng chứng quy tắc không được để trống.");
		if (evidence.status() == RuleStatus.PENDING_CONFIRMATION) {
			throw new IllegalStateException(
					"Không thể an sao bằng quy tắc đang chờ xác nhận: " + evidence.group() + ".");
		}
		return chart.withPlacement(star, branch);
	}
}
