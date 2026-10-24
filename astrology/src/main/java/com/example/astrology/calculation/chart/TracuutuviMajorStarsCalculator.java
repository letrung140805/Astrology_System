package com.example.astrology.calculation.chart;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.astrology.calculation.calendar.EarthlyBranch;

@Service
public class TracuutuviMajorStarsCalculator {

	private static final List<StarId> ZIWEI_GROUP = List.of(
			StarId.TU_VI,
			StarId.THIEN_CO,
			StarId.THAI_DUONG,
			StarId.VU_KHUC,
			StarId.THIEN_DONG,
			StarId.LIEM_TRINH);
	private static final List<Integer> ZIWEI_OFFSETS = List.of(0, -1, -3, -4, -5, -8);
	private static final List<StarId> TIANFU_GROUP = List.of(
			StarId.THIEN_PHU,
			StarId.THAI_AM,
			StarId.THAM_LANG,
			StarId.CU_MON,
			StarId.THIEN_TUONG,
			StarId.THIEN_LUONG,
			StarId.THAT_SAT,
			StarId.PHA_QUAN);
	private static final List<Integer> TIANFU_OFFSETS = List.of(0, 1, 2, 3, 4, 5, 6, 10);

	private final StarPlacementService placementService;
	private final StarRuleEvidence evidence;

	public TracuutuviMajorStarsCalculator(StarPlacementService placementService) {
		this.placementService = placementService;
		this.evidence = StarRuleSet.tracuutuviPublicCandidate()
				.evidence().get(StarRuleGroup.FOURTEEN_MAJOR_STARS);
	}

	public StarChart calculate(int lunarDay, FiveElementsBureau bureau) {
		if (lunarDay < 1 || lunarDay > 30) {
			throw new IllegalArgumentException("Ngày âm lịch phải nằm trong khoảng 1-30.");
		}
		if (bureau == null) {
			throw new IllegalArgumentException("Ngũ hành Cục không được để trống.");
		}

		EarthlyBranch ziwei = ziweiBranch(lunarDay, bureau);
		EarthlyBranch tianfu = reflectAroundYin(ziwei);
		StarChart chart = StarChart.empty();
		for (int index = 0; index < ZIWEI_GROUP.size(); index++) {
			chart = placementService.addDocumentedPlacement(
					chart,
					ZIWEI_GROUP.get(index),
					TracuutuviPositionsCalculator.step(ziwei, ZIWEI_OFFSETS.get(index)),
					evidence);
		}
		for (int index = 0; index < TIANFU_GROUP.size(); index++) {
			chart = placementService.addDocumentedPlacement(
					chart,
					TIANFU_GROUP.get(index),
					TracuutuviPositionsCalculator.step(tianfu, TIANFU_OFFSETS.get(index)),
					evidence);
		}
		return chart;
	}

	EarthlyBranch ziweiBranch(int lunarDay, FiveElementsBureau bureau) {
		int remainder = lunarDay % bureau.divisor();
		int borrowed = remainder == 0 ? 0 : bureau.divisor() - remainder;
		int countFromYin = (lunarDay + borrowed) / bureau.divisor();
		EarthlyBranch position = TracuutuviPositionsCalculator.step(EarthlyBranch.DAN, countFromYin - 1);
		if (borrowed != 0) {
			int adjustment = borrowed % 2 == 0 ? borrowed : -borrowed;
			position = TracuutuviPositionsCalculator.step(position, adjustment);
		}
		return position;
	}

	private EarthlyBranch reflectAroundYin(EarthlyBranch branch) {
		int relativeToYin = branch.ordinal() - EarthlyBranch.DAN.ordinal();
		return TracuutuviPositionsCalculator.step(EarthlyBranch.DAN, -relativeToYin);
	}
}
