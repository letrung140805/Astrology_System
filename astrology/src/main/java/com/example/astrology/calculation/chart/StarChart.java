package com.example.astrology.calculation.chart;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;
import java.util.Set;

import com.example.astrology.calculation.calendar.EarthlyBranch;

public record StarChart(Map<EarthlyBranch, Set<StarId>> placements) {

	public StarChart {
		Objects.requireNonNull(placements, "Vị trí sao không được để trống.");

		var copy = new EnumMap<EarthlyBranch, Set<StarId>>(EarthlyBranch.class);
		var seenStars = EnumSet.noneOf(StarId.class);
		placements.forEach((branch, stars) -> {
			Objects.requireNonNull(branch, "Địa chi chứa sao không được để trống.");
			Objects.requireNonNull(stars, "Tập sao không được để trống.");
			if (stars.isEmpty()) {
				throw new IllegalArgumentException("Không lưu cung không có sao.");
			}
			var starCopy = EnumSet.copyOf(stars);
			for (StarId star : starCopy) {
				Objects.requireNonNull(star, "Mã sao không được để trống.");
				if (!seenStars.add(star)) {
					throw new IllegalArgumentException("Một sao không thể nằm ở nhiều địa chi.");
				}
			}
			copy.put(branch, Collections.unmodifiableSet(starCopy));
		});
		placements = Collections.unmodifiableMap(copy);
	}

	public static StarChart empty() {
		return new StarChart(Map.of());
	}

	public StarChart withPlacement(StarId star, EarthlyBranch branch) {
		Objects.requireNonNull(star, "Mã sao không được để trống.");
		Objects.requireNonNull(branch, "Địa chi không được để trống.");

		Optional<EarthlyBranch> existingBranch = branchOf(star);
		if (existingBranch.isPresent() && existingBranch.get() != branch) {
			throw new IllegalArgumentException(
					"Sao " + star.vietnameseName() + " đã được an tại "
							+ existingBranch.get().vietnameseName() + ".");
		}
		if (existingBranch.isPresent()) {
			return this;
		}

		var updated = new EnumMap<EarthlyBranch, Set<StarId>>(EarthlyBranch.class);
		placements.forEach((position, stars) -> updated.put(position, EnumSet.copyOf(stars)));
		updated.computeIfAbsent(branch, ignored -> EnumSet.noneOf(StarId.class)).add(star);
		return new StarChart(updated);
	}

	public Set<StarId> starsAt(EarthlyBranch branch) {
		Objects.requireNonNull(branch, "Địa chi không được để trống.");
		return placements.getOrDefault(branch, Set.of());
	}

	public Optional<EarthlyBranch> branchOf(StarId star) {
		Objects.requireNonNull(star, "Mã sao không được để trống.");
		for (Map.Entry<EarthlyBranch, Set<StarId>> entry : placements.entrySet()) {
			if (entry.getValue().contains(star)) {
				return Optional.of(entry.getKey());
			}
		}
		return Optional.empty();
	}
}
