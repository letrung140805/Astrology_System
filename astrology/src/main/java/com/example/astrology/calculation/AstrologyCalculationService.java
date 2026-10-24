package com.example.astrology.calculation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.example.astrology.calculation.calendar.BirthInputNormalizationService;
import org.springframework.stereotype.Service;

@Service
public class AstrologyCalculationService {

	private final BirthInputNormalizationService birthInputNormalizationService;

	public AstrologyCalculationService(BirthInputNormalizationService birthInputNormalizationService) {
		this.birthInputNormalizationService = birthInputNormalizationService;
	}

	private static final List<CalculationGap> UNIMPLEMENTED_RULES = List.of(
			CalculationGap.LUNAR_CALENDAR,
			CalculationGap.CAN_CHI_YEAR_BOUNDARY,
			CalculationGap.CAN_CHI_RULES,
			CalculationGap.MENH_AND_CUC,
			CalculationGap.TWELVE_PALACES,
			CalculationGap.STAR_PLACEMENT,
			CalculationGap.TIMEZONE_AND_LOCATION);

	public CalculationAssessment assess(AstrologyCalculationInput input) {
		Objects.requireNonNull(input, "Thông tin đầu vào không được để trống.");
		var normalizedBirthInput = birthInputNormalizationService.normalize(input);
		if (input.gender() == null || input.gender().isBlank()) {
			throw new IllegalArgumentException("Giới tính là bắt buộc để lập lá số.");
		}

		List<CalculationGap> gaps = new ArrayList<>(UNIMPLEMENTED_RULES);
		if (input.birthTime() == null) {
			gaps.add(CalculationGap.UNKNOWN_BIRTH_TIME);
		}
		if (input.birthplace() == null || input.birthplace().isBlank()) {
			gaps.add(CalculationGap.BIRTHPLACE);
		}

		return new CalculationAssessment(input, normalizedBirthInput, gaps);
	}
}
