package com.example.astrology.calculation.chart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.astrology.calculation.calendar.EarthlyBranch;
import com.example.astrology.calculation.calendar.HeavenlyStem;

class TracuutuviFourTransformationsCalculatorTests {

	private final TracuutuviFourTransformationsCalculator calculator =
			new TracuutuviFourTransformationsCalculator(new StarPlacementService());
	private final Map<HeavenlyStem, List<StarId>> expectedSources = Map.ofEntries(
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
	private final List<StarId> transformations =
			List.of(StarId.HOA_LOC, StarId.HOA_QUYEN, StarId.HOA_KHOA, StarId.HOA_KY);

	@Test
	void appliesThePublishedTenStemTableToTheAssociatedStarLocations() {
		StarChart base = baseChart();
		for (HeavenlyStem stem : HeavenlyStem.values()) {
			var chart = calculator.calculate(base, stem);
			for (int index = 0; index < transformations.size(); index++) {
				assertEquals(base.branchOf(expectedSources.get(stem).get(index)).orElseThrow(),
						chart.branchOf(transformations.get(index)).orElseThrow());
			}
		}
	}

	@Test
	void rejectsMissingBaseStarInsteadOfInventingTransformationLocation() {
		assertThrows(IllegalStateException.class,
				() -> calculator.calculate(StarChart.empty(), HeavenlyStem.JIA));
	}

	private StarChart baseChart() {
		StarChart chart = StarChart.empty();
		StarId[] baseStars = {
				StarId.TU_VI, StarId.THIEN_CO, StarId.THAI_DUONG, StarId.VU_KHUC,
				StarId.THIEN_DONG, StarId.LIEM_TRINH, StarId.THIEN_PHU, StarId.THAI_AM,
				StarId.THAM_LANG, StarId.CU_MON, StarId.THIEN_TUONG, StarId.THIEN_LUONG,
				StarId.THAT_SAT, StarId.PHA_QUAN, StarId.VAN_XUONG, StarId.VAN_KHUC,
				StarId.TA_PHU, StarId.HUU_BAT
		};
		for (int index = 0; index < baseStars.length; index++) {
			chart = chart.withPlacement(baseStars[index], EarthlyBranch.values()[index % 12]);
		}
		return chart;
	}
}
