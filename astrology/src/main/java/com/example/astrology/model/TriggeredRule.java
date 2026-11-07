package com.example.astrology.model;

public record TriggeredRule(
		String id,
		String name,
		String dimension,
		String category,
		int weight,
		String description,
		String advice
) {}
