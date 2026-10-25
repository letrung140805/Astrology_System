package com.example.astrology.model;

import java.util.Objects;

import com.example.astrology.calculation.FiveElement;

public record ChartStarDisplay(String name, FiveElement element) {

	public ChartStarDisplay {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Tên sao không được để trống.");
		}
		Objects.requireNonNull(element, "Ngũ hành sao không được để trống.");
	}
}
