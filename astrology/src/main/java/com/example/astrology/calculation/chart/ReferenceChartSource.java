package com.example.astrology.calculation.chart;

import java.net.URI;
import java.time.LocalDate;
import java.util.Objects;

public record ReferenceChartSource(
		String title,
		URI sourceUrl,
		LocalDate capturedOn,
		String captureMethod,
		String conventionNotes,
		String evidenceArtifact) {

	public ReferenceChartSource {
		if (title == null || title.isBlank()) {
			throw new IllegalArgumentException("Tên nguồn tham chiếu không được để trống.");
		}
		Objects.requireNonNull(sourceUrl, "URL nguồn tham chiếu không được để trống.");
		if (!"https".equalsIgnoreCase(sourceUrl.getScheme())) {
			throw new IllegalArgumentException("Nguồn tham chiếu phải dùng HTTPS.");
		}
		Objects.requireNonNull(capturedOn, "Ngày ghi nhận lá số tham chiếu là bắt buộc.");
		if (captureMethod == null || captureMethod.isBlank()) {
			throw new IllegalArgumentException("Cách ghi nhận dữ liệu tham chiếu là bắt buộc.");
		}
		if (conventionNotes == null || conventionNotes.isBlank()) {
			throw new IllegalArgumentException("Quy ước dùng khi lập tham chiếu là bắt buộc.");
		}
	}
}
