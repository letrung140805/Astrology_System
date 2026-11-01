package com.example.astrology.model;

import java.util.Objects;

import com.example.astrology.calculation.FiveElement;

public record ChartStarDisplay(
		String name,
		FiveElement element,
		String brightness,
		boolean isMajor,
		boolean isGood) {

	public ChartStarDisplay {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Tên sao không được để trống.");
		}
		Objects.requireNonNull(element, "Ngũ hành sao không được để trống.");
		if (brightness == null) {
			brightness = "";
		}
	}

	public ChartStarDisplay(String name, FiveElement element) {
		this(name, element, "", false, true);
	}

	public ChartStarDisplay(String name, FiveElement element, String brightness, boolean isMajor) {
		this(name, element, brightness, isMajor, true);
	}
}
