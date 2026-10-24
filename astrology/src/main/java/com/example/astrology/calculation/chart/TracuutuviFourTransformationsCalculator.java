package com.example.astrology.calculation.chart;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.astrology.calculation.calendar.EarthlyBranch;
import com.example.astrology.calculation.calendar.HeavenlyStem;

@Service
public class TracuutuviFourTransformationsCalculator {

	private static final List<StarId> TRANSFORMATIONS = List.of(
			StarId.HOA_LOC,
			StarId.HOA_QUYEN,
			StarId.HOA_KHOA,
			StarId.HOA_KY);
	private static final Map<HeavenlyStem, List<StarId>> TABLE = Map.ofEntries(
			Map.entry(HeavenlyStem.JIA, List.of(StarId.LIEM_TRINH, StarId.PHA_QUAN, StarId.VU_KHUC, StarId.THAI_DUONG)),
			Map.entry(HeavenlyStem.YI, List.of(StarId.THIEN_CO, StarId.THIEN_LUONG, StarId.TU_VI, StarId.THAI_AM)),
			Map.entry(HeavenlyStem.BING, List.of(StarId.THIEN_DONG, StarId.THIEN_CO, StarId.VAN_XUONG, StarId.LIEM_TRINH)),
			Map.entry(HeavenlyStem.DING, List.of(StarId.THAI_AM, StarId.THIEN_DONG, StarId.THIEN_CO, StarId.CU_MON)),
			Map.entry(HeavenlyStem.WU, List.of(StarId.THAM_LANG, StarId.THAI_AM, StarId.HUU_BAT, StarId.THIEN_CO)),
			Map.entry(HeavenlyStem.JI, List.of(StarId.VU_KHUC, StarId.THAM_LANG, StarId.THIEN_LUONG, StarId.VAN_KHUC)),
			Map.entry(HeavenlyStem.GENG, List.of(StarId.THAI_DUONG, StarId.VU_KHUC, StarId.THAI_AM, StarId.THIEN_DONG)),
			Map.entry(HeavenlyStem.XIN, List.of(StarId.CU_MON, StarId.THAI_DUONG, StarId.VAN_KHUC, StarId.VAN_XUONG)),
			Map.entry(HeavenlyStem.REN, List.of(StarId.THIEN_LUONG, StarId.TU_VI, StarId.TA_PHU, StarId.VU_KHUC)),
			Map.entry(HeavenlyStem.GUI, List.of(StarId.PHA_QUAN, StarId.CU_MON, StarId.THAI_AM, StarId.THAM_LANG)));

	private final StarPlacementService placementService;
	private final StarRuleEvidence evidence;

	public TracuutuviFourTransformationsCalculator(StarPlacementService placementService) {
		this.placementService = placementService;
		this.evidence = StarRuleSet.tracuutuviPublicCandidate()
				.evidence().get(StarRuleGroup.FOUR_TRANSFORMATIONS);
	}

	public StarChart calculate(StarChart initial, HeavenlyStem yearStem) {
		List<StarId> transformedStars = TABLE.get(yearStem);
		if (transformedStars == null) {
			throw new IllegalArgumentException("Thiên can năm sinh không được để trống.");
		}
		StarChart chart = initial;
		if (evidence.status() == RuleStatus.PENDING_CONFIRMATION) {
			throw new IllegalStateException("Bảng Tứ Hóa chưa được xác nhận.");
		}
		var positions = new EnumMap<StarId, EarthlyBranch>(StarId.class);
		for (int index = 0; index < TRANSFORMATIONS.size(); index++) {
			StarId transformation = TRANSFORMATIONS.get(index);
			StarId sourceStar = transformedStars.get(index);
			EarthlyBranch branch = initial.branchOf(sourceStar).orElseThrow(() ->
					new IllegalStateException("Chưa an sao nguồn của " + transformation.vietnameseName() + "."));
			positions.put(transformation, branch);
		}
		for (Map.Entry<StarId, EarthlyBranch> entry : positions.entrySet()) {
			chart = placementService.addDocumentedPlacement(
					chart, entry.getKey(), entry.getValue(), evidence);
		}
		return chart;
	}
}
