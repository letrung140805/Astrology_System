package com.example.astrology.calculation.chart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.astrology.calculation.calendar.EarthlyBranch;

class PalaceLayoutTests {

	@Test
	void acceptsAnyCompleteBijectionWithoutImplyingAPlacementRule() {
		var positions = new EnumMap<PalaceFunction, EarthlyBranch>(PalaceFunction.class);
		var palaces = PalaceFunction.values();
		var branches = EarthlyBranch.values();
		for (int index = 0; index < palaces.length; index++) {
			positions.put(palaces[index], branches[index]);
		}

		var layout = new PalaceLayout(positions);

		assertEquals(PalaceFunction.values().length, layout.positions().size());
		for (int index = 0; index < palaces.length; index++) {
			assertEquals(palaces[index], layout.palaceAt(branches[index]).orElseThrow());
		}
		assertThrows(UnsupportedOperationException.class,
				() -> layout.positions().put(PalaceFunction.MENH, EarthlyBranch.TY));
	}

	@Test
	void rejectsMissingPalace() {
		var positions = new EnumMap<PalaceFunction, EarthlyBranch>(PalaceFunction.class);
		positions.put(PalaceFunction.MENH, EarthlyBranch.TY);

		assertThrows(IllegalArgumentException.class, () -> new PalaceLayout(positions));
	}

	@Test
	void rejectsTwoPalacesAtTheSameBranch() {
		var positions = new EnumMap<PalaceFunction, EarthlyBranch>(PalaceFunction.class);
		for (PalaceFunction palace : PalaceFunction.values()) {
			positions.put(palace, EarthlyBranch.TY);
		}

		assertThrows(IllegalArgumentException.class, () -> new PalaceLayout(positions));
	}

	@Test
	void hasTwelveDistinctBranchAndPalaceTypes() {
		assertEquals(12, EarthlyBranch.values().length);
		assertEquals(12, PalaceFunction.values().length);
		var names = Map.ofEntries(
				Map.entry(EarthlyBranch.TY, "Tý"),
				Map.entry(EarthlyBranch.SUU, "Sửu"),
				Map.entry(EarthlyBranch.DAN, "Dần"),
				Map.entry(EarthlyBranch.MAO, "Mão"),
				Map.entry(EarthlyBranch.THIN, "Thìn"),
				Map.entry(EarthlyBranch.TI, "Tỵ"),
				Map.entry(EarthlyBranch.NGO, "Ngọ"),
				Map.entry(EarthlyBranch.MUI, "Mùi"),
				Map.entry(EarthlyBranch.THAN, "Thân"),
				Map.entry(EarthlyBranch.DAU, "Dậu"),
				Map.entry(EarthlyBranch.TUAT, "Tuất"),
				Map.entry(EarthlyBranch.HOI, "Hợi"));
		assertEquals(EarthlyBranch.values().length, names.size());
		names.forEach((branch, name) -> assertEquals(name, branch.vietnameseName()));
	}
}
