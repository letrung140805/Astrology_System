package com.example.astrology.calculation.chart;

import java.util.Map;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.astrology.calculation.calendar.EarthlyBranch;
import com.example.astrology.calculation.calendar.HeavenlyStem;

@Service
public class FiveElementsBureauCalculator {

	private static final Map<HeavenlyStem, List<FiveElementsBureau>> TABLE = Map.of(
			HeavenlyStem.JIA, List.of(FiveElementsBureau.WATER_TWO, FiveElementsBureau.FIRE_SIX,
					FiveElementsBureau.WOOD_THREE, FiveElementsBureau.EARTH_FIVE,
					FiveElementsBureau.METAL_FOUR, FiveElementsBureau.FIRE_SIX),
			HeavenlyStem.JI, List.of(FiveElementsBureau.WATER_TWO, FiveElementsBureau.FIRE_SIX,
					FiveElementsBureau.WOOD_THREE, FiveElementsBureau.EARTH_FIVE,
					FiveElementsBureau.METAL_FOUR, FiveElementsBureau.FIRE_SIX),
			HeavenlyStem.YI, List.of(FiveElementsBureau.FIRE_SIX, FiveElementsBureau.EARTH_FIVE,
					FiveElementsBureau.METAL_FOUR, FiveElementsBureau.WOOD_THREE,
					FiveElementsBureau.WATER_TWO, FiveElementsBureau.EARTH_FIVE),
			HeavenlyStem.GENG, List.of(FiveElementsBureau.FIRE_SIX, FiveElementsBureau.EARTH_FIVE,
					FiveElementsBureau.METAL_FOUR, FiveElementsBureau.WOOD_THREE,
					FiveElementsBureau.WATER_TWO, FiveElementsBureau.EARTH_FIVE),
			HeavenlyStem.BING, List.of(FiveElementsBureau.EARTH_FIVE, FiveElementsBureau.WOOD_THREE,
					FiveElementsBureau.WATER_TWO, FiveElementsBureau.METAL_FOUR,
					FiveElementsBureau.FIRE_SIX, FiveElementsBureau.WOOD_THREE),
			HeavenlyStem.XIN, List.of(FiveElementsBureau.EARTH_FIVE, FiveElementsBureau.WOOD_THREE,
					FiveElementsBureau.WATER_TWO, FiveElementsBureau.METAL_FOUR,
					FiveElementsBureau.FIRE_SIX, FiveElementsBureau.WOOD_THREE),
			HeavenlyStem.DING, List.of(FiveElementsBureau.WOOD_THREE, FiveElementsBureau.METAL_FOUR,
					FiveElementsBureau.FIRE_SIX, FiveElementsBureau.WATER_TWO,
					FiveElementsBureau.EARTH_FIVE, FiveElementsBureau.METAL_FOUR),
			HeavenlyStem.REN, List.of(FiveElementsBureau.WOOD_THREE, FiveElementsBureau.METAL_FOUR,
					FiveElementsBureau.FIRE_SIX, FiveElementsBureau.WATER_TWO,
					FiveElementsBureau.EARTH_FIVE, FiveElementsBureau.METAL_FOUR),
			HeavenlyStem.WU, List.of(FiveElementsBureau.METAL_FOUR, FiveElementsBureau.WATER_TWO,
					FiveElementsBureau.EARTH_FIVE, FiveElementsBureau.FIRE_SIX,
					FiveElementsBureau.WOOD_THREE, FiveElementsBureau.WATER_TWO),
			HeavenlyStem.GUI, List.of(FiveElementsBureau.METAL_FOUR, FiveElementsBureau.WATER_TWO,
					FiveElementsBureau.EARTH_FIVE, FiveElementsBureau.FIRE_SIX,
					FiveElementsBureau.WOOD_THREE, FiveElementsBureau.WATER_TWO));

	public FiveElementsBureau calculate(HeavenlyStem yearStem, EarthlyBranch mingPalace) {
		int group = branchGroup(mingPalace);
		return TABLE.get(yearStem).stream().skip(group).findFirst()
				.orElseThrow(() -> new IllegalStateException("Không tra được Ngũ hành Cục."));
	}

	private static int branchGroup(EarthlyBranch branch) {
		return switch (branch) {
			case TY, SUU -> 0;
			case DAN, MAO -> 1;
			case THIN, TI -> 2;
			case NGO, MUI -> 3;
			case THAN, DAU -> 4;
			case TUAT, HOI -> 5;
		};
	}
}
