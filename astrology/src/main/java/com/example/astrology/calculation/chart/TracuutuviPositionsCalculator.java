package com.example.astrology.calculation.chart;

import java.util.EnumMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.astrology.calculation.calendar.EarthlyBranch;

@Service
public class TracuutuviPositionsCalculator {

	private static final List<PalaceFunction> PALACE_ORDER = List.of(
			PalaceFunction.MENH,
			PalaceFunction.HUYNH_DE,
			PalaceFunction.PHU_THE,
			PalaceFunction.TU_TUC,
			PalaceFunction.TAI_BACH,
			PalaceFunction.TAT_ACH,
			PalaceFunction.THIEN_DI,
			PalaceFunction.NO_BOC,
			PalaceFunction.QUAN_LOC,
			PalaceFunction.DIEN_TRACH,
			PalaceFunction.PHUC_DUC,
			PalaceFunction.PHU_MAU);

	private final FiveElementsBureauCalculator bureauCalculator;

	public TracuutuviPositionsCalculator(FiveElementsBureauCalculator bureauCalculator) {
		this.bureauCalculator = bureauCalculator;
	}

	public AstrologyPositions calculate(LunarChartInput input) {
		int effectiveMonth = input.effectiveMonthForTracuutuviProfile();
		EarthlyBranch monthAnchor = step(EarthlyBranch.DAN, effectiveMonth - 1);
		int hourIndex = input.birthHour().ordinal();
		EarthlyBranch ming = step(monthAnchor, -hourIndex);
		EarthlyBranch than = step(monthAnchor, hourIndex);

		var positions = new EnumMap<PalaceFunction, EarthlyBranch>(PalaceFunction.class);
		for (int index = 0; index < PALACE_ORDER.size(); index++) {
			positions.put(PALACE_ORDER.get(index), step(ming, -index));
		}
		var layout = new PalaceLayout(positions);
		var bureau = bureauCalculator.calculate(input.yearStem(), ming);
		return new AstrologyPositions(ming, than, layout, bureau);
	}

	static EarthlyBranch step(EarthlyBranch start, int steps) {
		EarthlyBranch[] branches = EarthlyBranch.values();
		int index = start.ordinal();
		int remaining = Math.abs(steps);
		int direction = Integer.signum(steps);
		for (int count = 0; count < remaining; count++) {
			index += direction;
			if (index < 0) {
				index = branches.length - 1;
			} else if (index == branches.length) {
				index = 0;
			}
		}
		return branches[index];
	}
}
