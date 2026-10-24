package com.example.astrology.calculation;

import java.time.LocalDate;
import java.time.LocalTime;

public record AstrologyCalculationInput(
		String name,
		String gender,
		LocalDate birthDate,
		LocalTime birthTime,
		String birthplace) {
}
