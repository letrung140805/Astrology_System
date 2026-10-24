package com.example.astrology.calculation.chart;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.astrology.calculation.calendar.EarthlyBranch;

@Service
public class TracuutuviChangShengCalculator {

	private static final List<StarId> CYCLE = List.of(
			StarId.TRUONG_SINH,
			StarId.MOC_DUC,
			StarId.QUAN_DOI,
			StarId.LAM_QUAN,
			StarId.DE_VUONG,
			StarId.SUY,
			StarId.BENH,
			StarId.TU,
			StarId.MO,
			StarId.TUYET,
			StarId.THAI,
			StarId.DUONG);

	private final StarPlacementService placementService;
	private final StarRuleEvidence evidence;

	public TracuutuviChangShengCalculator(StarPlacementService placementService) {
		this.placementService = placementService;
		this.evidence = StarRuleSet.tracuutuviPublicCandidate()
				.evidence().get(StarRuleGroup.CHANG_SHENG_CYCLE);
	}

	public StarChart calculate(StarChart initial, AstrologyPositions positions, LunarChartInput input) {
		int direction = TracuutuviYearStarsCalculator.cycleDirection(input.yearStem(), input.birthSex());
		StarChart chart = initial;
		for (int index = 0; index < CYCLE.size(); index++) {
			EarthlyBranch branch = TracuutuviPositionsCalculator.step(
					positions.bureau().changShengStart(), direction * index);
			chart = placementService.addDocumentedPlacement(chart, CYCLE.get(index), branch, evidence);
		}
		return chart;
	}
}
