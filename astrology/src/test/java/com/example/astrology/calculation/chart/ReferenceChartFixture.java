package com.example.astrology.calculation.chart;

import java.util.Objects;

public record ReferenceChartFixture(
		String fixtureId,
		ReferenceFixtureStatus status,
		ReferenceChartInput input,
		NormalizedInputSnapshot expectedNormalization,
		ReferenceChartSource reference,
		ChartSnapshot expected) {

	public ReferenceChartFixture {
		if (fixtureId == null || fixtureId.isBlank()) {
			throw new IllegalArgumentException("Mã fixture không được để trống.");
		}
		Objects.requireNonNull(status, "Trạng thái fixture không được để trống.");
		if (status == ReferenceFixtureStatus.READY_FOR_COMPARISON) {
			Objects.requireNonNull(input, "Fixture sẵn sàng phải có đầu vào.");
			Objects.requireNonNull(expectedNormalization, "Fixture sẵn sàng phải có normalization đối chiếu.");
			Objects.requireNonNull(reference, "Fixture sẵn sàng phải có nguồn tham chiếu.");
			Objects.requireNonNull(expected, "Fixture sẵn sàng phải có kết quả có cấu trúc.");
			if (expected.coveredSections().isEmpty()) {
				throw new IllegalArgumentException("Fixture sẵn sàng phải khai báo trường cần đối chiếu.");
			}
		}
	}

	public static ReferenceChartFixture pending(String fixtureId) {
		return new ReferenceChartFixture(fixtureId, ReferenceFixtureStatus.PENDING_REFERENCE,
				null, null, null, null);
	}
}
