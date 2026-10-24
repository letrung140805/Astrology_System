package com.example.astrology.calculation.chart;

import java.net.URI;
import java.util.Objects;

public record RuleSource(
		String title,
		URI url,
		String versionOrEdition,
		String section) {

	public RuleSource {
		if (title == null || title.isBlank()) {
			throw new IllegalArgumentException("Tên nguồn không được để trống.");
		}
		Objects.requireNonNull(url, "URL nguồn không được để trống.");
		if (!"https".equalsIgnoreCase(url.getScheme())) {
			throw new IllegalArgumentException("Nguồn quy tắc phải dùng HTTPS.");
		}
		if (versionOrEdition == null || versionOrEdition.isBlank()) {
			throw new IllegalArgumentException("Phiên bản hoặc ấn bản nguồn là bắt buộc.");
		}
		if (section == null || section.isBlank()) {
			throw new IllegalArgumentException("Mục hoặc trang nguồn là bắt buộc.");
		}
	}
}
