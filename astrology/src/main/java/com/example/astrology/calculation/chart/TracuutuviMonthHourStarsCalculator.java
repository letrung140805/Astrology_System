package com.example.astrology.calculation.chart;

import org.springframework.stereotype.Service;

import com.example.astrology.calculation.calendar.EarthlyBranch;

@Service
public class TracuutuviMonthHourStarsCalculator {

	private final StarPlacementService placementService;
	private final StarRuleEvidence evidence;

	public TracuutuviMonthHourStarsCalculator(StarPlacementService placementService) {
		this.placementService = placementService;
		this.evidence = StarRuleSet.tracuutuviPublicCandidate()
				.evidence().get(StarRuleGroup.TIME_AND_MONTH_AUXILIARY_STARS);
	}

	public StarChart calculate(StarChart initial, LunarChartInput input) {
		int effectiveMonth = input.effectiveMonthForTracuutuviProfile();
		int hourIndex = input.birthHour().ordinal();
		StarChart chart = initial;
		chart = place(chart, StarId.VAN_XUONG,
				TracuutuviPositionsCalculator.step(EarthlyBranch.TUAT, -hourIndex));
		chart = place(chart, StarId.VAN_KHUC,
				TracuutuviPositionsCalculator.step(EarthlyBranch.THIN, hourIndex));
		chart = place(chart, StarId.TA_PHU,
				TracuutuviPositionsCalculator.step(EarthlyBranch.THIN, effectiveMonth - 1));
		return place(chart, StarId.HUU_BAT,
				TracuutuviPositionsCalculator.step(EarthlyBranch.TUAT, -(effectiveMonth - 1)));
	}

	private StarChart place(StarChart chart, StarId star, EarthlyBranch branch) {
		return placementService.addDocumentedPlacement(chart, star, branch, evidence);
	}
}
