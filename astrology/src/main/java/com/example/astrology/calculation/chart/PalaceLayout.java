package com.example.astrology.calculation.chart;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.example.astrology.calculation.calendar.EarthlyBranch;

public record PalaceLayout(Map<PalaceFunction, EarthlyBranch> positions) {

	public PalaceLayout {
		Objects.requireNonNull(positions, "Vị trí các cung không được để trống.");

		var copy = new EnumMap<PalaceFunction, EarthlyBranch>(PalaceFunction.class);
		copy.putAll(positions);
		if (!copy.keySet().equals(EnumSet.allOf(PalaceFunction.class))) {
			throw new IllegalArgumentException("Bố cục phải có đủ đúng 12 cung chức.");
		}
		if (EnumSet.copyOf(copy.values()).size() != EarthlyBranch.values().length) {
			throw new IllegalArgumentException("Mỗi cung chức phải nằm tại một địa chi riêng.");
		}
		positions = Collections.unmodifiableMap(copy);
	}

	public Optional<PalaceFunction> palaceAt(EarthlyBranch branch) {
		Objects.requireNonNull(branch, "Địa chi không được để trống.");
		for (Map.Entry<PalaceFunction, EarthlyBranch> entry : positions.entrySet()) {
			if (entry.getValue() == branch) {
				return Optional.of(entry.getKey());
			}
		}
		return Optional.empty();
	}
}
