package com.example.astrology.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.astrology.calculation.CalculationAssessment;
import com.example.astrology.calculation.CalculationGap;
import com.example.astrology.calculation.TuViDetailService;
import com.example.astrology.calculation.calendar.NayinElementCalculator;
import com.example.astrology.calculation.calendar.VietnameseLunarCalendarProvider;
import com.example.astrology.calculation.chart.BirthSex;
import com.example.astrology.calculation.chart.TracuutuviChart;
import com.example.astrology.model.BirthChartPageData;
import com.example.astrology.model.ChartPalaceDisplay;

/**
 * Service chuyên trách chuẩn bị và định dạng dữ liệu lá số để hiển thị ra View (Presentation Model Builder).
 * Tách biệt rõ ràng khỏi vai trò điều phối tính toán thiên văn của BirthChartApplicationService.
 */
@Service
public class BirthChartDisplayService {

	public static final List<String> CHART_LIMITATIONS = List.of(
			"Can Chi 4 trụ năm tháng ngày giờ đã được chuẩn hóa theo lịch thiên văn Việt Nam.",
			"Đã tính toán đầy đủ Bản Mệnh (60 hoa giáp), Cân lượng, Chủ Mệnh, Chủ Thân, Lai Nhân Cung.",
			"Độ sáng chính tinh và các nhóm phụ tinh được an theo nguyên tắc cổ truyền.");

	private final TuViDetailService tuViDetailService;

	public BirthChartDisplayService(TuViDetailService tuViDetailService) {
		this.tuViDetailService = tuViDetailService;
	}

	public TuViDetailService.FullChartDetails buildDetails(
			TracuutuviChart chart,
			LocalDate birthDate,
			BirthSex birthSex) {
		if (tuViDetailService == null || chart == null) {
			return null;
		}
		return tuViDetailService.buildDetails(chart, birthDate, birthSex);
	}

	public BirthChartPageData createPageData(
			CalculationAssessment assessment,
			TracuutuviChart chart,
			String chartInputMessage,
			VietnameseLunarCalendarProvider.LunarDate lunarDate,
			String lunarCalendarName,
			String lunarCalendarVersion,
			String yearCanChi,
			String yearNayinElement,
			List<ChartPalaceDisplay> displayPalaces,
			TuViDetailService.FullChartDetails fullDetails) {

		List<CalculationGap> gaps = remainingGaps(assessment, chart, lunarDate, yearNayinElement);

		return new BirthChartPageData(
				assessment,
				chart,
				chartInputMessage,
				lunarDate,
				lunarCalendarName,
				lunarCalendarVersion,
				yearCanChi,
				yearNayinElement,
				NayinElementCalculator.SOURCE_URL,
				displayPalaces,
				gaps,
				CHART_LIMITATIONS,
				fullDetails);
	}

	public List<CalculationGap> remainingGaps(
			CalculationAssessment assessment,
			TracuutuviChart chart,
			VietnameseLunarCalendarProvider.LunarDate lunarDate,
			String yearNayinElement) {
		if (assessment == null || assessment.gaps() == null) {
			return List.of();
		}
		return assessment.gaps().stream()
				.filter(gap -> chart == null
						|| (gap != CalculationGap.MENH_AND_CUC
								&& gap != CalculationGap.TWELVE_PALACES
								&& gap != CalculationGap.STAR_PLACEMENT))
				.filter(gap -> lunarDate == null
						|| (gap != CalculationGap.LUNAR_CALENDAR
								&& gap != CalculationGap.CAN_CHI_YEAR_BOUNDARY))
				.filter(gap -> yearNayinElement != null || gap != CalculationGap.NATAL_ELEMENT)
				.toList();
	}
}
