package com.example.astrology.calculation.chart;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.astrology.calculation.calendar.EarthlyBranch;
import com.example.astrology.calculation.calendar.HeavenlyStem;

@Service
public class TracuutuviYearStarsCalculator {

	private static final List<StarId> LOC_TON_CYCLE = List.of(
			StarId.BAC_SI,
			StarId.LUC_SI,
			StarId.THANH_LONG,
			StarId.TIEU_HAO,
			StarId.TUONG_QUAN,
			StarId.TAU_THU,
			StarId.PHI_LIEM,
			StarId.HY_THAN,
			StarId.BENH_PHU,
			StarId.DAI_HAO,
			StarId.PHUC_BINH,
			StarId.QUAN_PHU_BAC_SI);
	private static final List<StarId> THAI_TUE_CYCLE = List.of(
			StarId.THAI_TUE,
			StarId.THIEU_DUONG,
			StarId.TANG_MON,
			StarId.THIEU_AM,
			StarId.QUAN_PHU_THAI_TUE,
			StarId.TU_PHU,
			StarId.TUE_PHA,
			StarId.LONG_DUC,
			StarId.BACH_HO,
			StarId.PHUC_DUC_STAR,
			StarId.DIEU_KHACH,
			StarId.TRUC_PHU);
	private static final Map<HeavenlyStem, EarthlyBranch> LOC_TON_BRANCH = Map.ofEntries(
			Map.entry(HeavenlyStem.JIA, EarthlyBranch.DAN),
			Map.entry(HeavenlyStem.YI, EarthlyBranch.MAO),
			Map.entry(HeavenlyStem.BING, EarthlyBranch.TI),
			Map.entry(HeavenlyStem.DING, EarthlyBranch.NGO),
			Map.entry(HeavenlyStem.WU, EarthlyBranch.TI),
			Map.entry(HeavenlyStem.JI, EarthlyBranch.NGO),
			Map.entry(HeavenlyStem.GENG, EarthlyBranch.THAN),
			Map.entry(HeavenlyStem.XIN, EarthlyBranch.DAU),
			Map.entry(HeavenlyStem.REN, EarthlyBranch.HOI),
			Map.entry(HeavenlyStem.GUI, EarthlyBranch.TY));

	private final StarPlacementService placementService;
	private final StarRuleEvidence evidence;

	public TracuutuviYearStarsCalculator(StarPlacementService placementService) {
		this.placementService = placementService;
		this.evidence = StarRuleSet.tracuutuviPublicCandidate()
				.evidence().get(StarRuleGroup.LOC_TON_AND_YEAR_STEM_STARS);
	}

	public StarChart calculate(StarChart initial, LunarChartInput input) {
		EarthlyBranch locTon = LOC_TON_BRANCH.get(input.yearStem());
		StarChart chart = placementService.addDocumentedPlacement(initial, StarId.LOC_TON, locTon, evidence);
		int direction = cycleDirection(input.yearStem(), input.birthSex());
		chart = placeCycle(chart, LOC_TON_CYCLE, locTon, direction);

		chart = placeCycle(chart, THAI_TUE_CYCLE, input.yearBranch(), 1);
		EarthlyBranch thienKhong = TracuutuviPositionsCalculator.step(input.yearBranch(), 1);
		return placementService.addDocumentedPlacement(chart, StarId.THIEN_KHONG, thienKhong, evidence);
	}

	static int cycleDirection(HeavenlyStem stem, BirthSex sex) {
		boolean yangMale = stem.isYang() && sex == BirthSex.MALE;
		boolean yinFemale = !stem.isYang() && sex == BirthSex.FEMALE;
		return yangMale || yinFemale ? 1 : -1;
	}

	private StarChart placeCycle(StarChart chart, List<StarId> cycle, EarthlyBranch start, int direction) {
		for (int index = 0; index < cycle.size(); index++) {
			EarthlyBranch branch = TracuutuviPositionsCalculator.step(start, direction * index);
			chart = placementService.addDocumentedPlacement(chart, cycle.get(index), branch, evidence);
		}
		return chart;
	}
}
