package com.example.astrology.service;

import java.time.DateTimeException;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.astrology.calculation.AstrologyCalculationInput;
import com.example.astrology.calculation.AstrologyCalculationService;
import com.example.astrology.calculation.CalculationAssessment;
import com.example.astrology.calculation.CalculationGap;
import com.example.astrology.calculation.calendar.EarthlyBranch;
import com.example.astrology.calculation.calendar.HoNgocDucLunarCalendarProvider;
import com.example.astrology.calculation.calendar.NayinElementCalculator;
import com.example.astrology.calculation.calendar.SexagenaryYearCalculator;
import com.example.astrology.calculation.calendar.VietnameseLunarCalendarProvider;
import com.example.astrology.calculation.chart.BirthSex;
import com.example.astrology.calculation.chart.LunarChartInput;
import com.example.astrology.calculation.chart.PalaceFunction;
import com.example.astrology.calculation.chart.StarId;
import com.example.astrology.calculation.chart.TracuutuviChart;
import com.example.astrology.calculation.chart.TracuutuviChartCalculator;
import com.example.astrology.model.BirthChartPageData;
import com.example.astrology.model.ChartPalaceDisplay;
import com.example.astrology.model.ChartStarDisplay;

@Service
public class BirthChartApplicationService {

	private static final ZoneOffset VIETNAM_STANDARD_OFFSET = ZoneOffset.ofHours(7);
	private static final List<String> CHART_LIMITATIONS = List.of(
			"Can Chi tháng/ngày/giờ chưa được tính; Can Chi ngày tại giờ Tý dùng quy ước đổi ngày lúc 00:00.",
			"Bảng hiện chỉ cho biết ngũ hành Nạp Âm năm, chưa xuất tên nạp âm đầy đủ.",
			"Chưa tính Chủ Mệnh/Chủ Thân, độ sáng sao và các nhóm phụ tinh khác.");

	private final AstrologyCalculationService astrologyCalculationService;
	private final TracuutuviChartCalculator chartCalculator;
	private final HoNgocDucLunarCalendarProvider lunarCalendarProvider;
	private final SexagenaryYearCalculator sexagenaryYearCalculator;
	private final NayinElementCalculator nayinElementCalculator;

	public BirthChartApplicationService(
			AstrologyCalculationService astrologyCalculationService,
			TracuutuviChartCalculator chartCalculator,
			HoNgocDucLunarCalendarProvider lunarCalendarProvider,
			SexagenaryYearCalculator sexagenaryYearCalculator,
			NayinElementCalculator nayinElementCalculator) {
		this.astrologyCalculationService = astrologyCalculationService;
		this.chartCalculator = chartCalculator;
		this.lunarCalendarProvider = lunarCalendarProvider;
		this.sexagenaryYearCalculator = sexagenaryYearCalculator;
		this.nayinElementCalculator = nayinElementCalculator;
	}

	public BirthChartPageData calculate(AstrologyCalculationInput input) {
		CalculationAssessment assessment = astrologyCalculationService.assess(input);
		TracuutuviChart chart = null;
		String chartInputMessage = null;
		VietnameseLunarCalendarProvider.LunarDate lunarDate = null;
		String yearCanChi = null;
		String yearNayinElement = null;

		try {
			lunarDate = lunarCalendarProvider.toLunar(input.birthDate(), VIETNAM_STANDARD_OFFSET);
			var yearPillar = sexagenaryYearCalculator.calculate(lunarDate.year());
			yearCanChi = yearPillar.stem().vietnameseName() + " " + yearPillar.branch().vietnameseName();
			yearNayinElement = nayinElementCalculator.calculate(lunarDate.year()).vietnameseName();
			if (input.birthTime() == null) {
				chartInputMessage = "Cần có giờ sinh để xác định địa chi giờ và an cung Mệnh, cung Thân.";
			} else if (!"Nam".equals(input.gender()) && !"Nữ".equals(input.gender())) {
				chartInputMessage = "Giới tính cần chọn Nam hoặc Nữ để áp dụng quy ước của profile.";
			} else {
				var lunarInput = new LunarChartInput(
						lunarDate.year(),
						yearPillar.stem(),
						yearPillar.branch(),
						lunarDate.month(),
						lunarDate.leapMonth(),
						lunarDate.day(),
						assessment.normalizedBirthInput().hourBranch(),
						"Nam".equals(input.gender()) ? BirthSex.MALE : BirthSex.FEMALE);
				chart = chartCalculator.calculate(lunarInput);
			}
		} catch (DateTimeException exception) {
			chartInputMessage = exception.getMessage();
		}

		return new BirthChartPageData(
				assessment,
				chart,
				chartInputMessage,
				lunarDate,
				lunarCalendarProvider.implementationName(),
				lunarCalendarProvider.implementationVersion(),
				yearCanChi,
				yearNayinElement,
				NayinElementCalculator.SOURCE_URL,
				chart == null ? List.of() : toDisplayPalaces(chart),
				remainingGaps(assessment, chart, lunarDate, yearNayinElement),
				CHART_LIMITATIONS);
	}

	private List<CalculationGap> remainingGaps(
			CalculationAssessment assessment,
			TracuutuviChart chart,
			VietnameseLunarCalendarProvider.LunarDate lunarDate,
			String yearNayinElement) {
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

	private List<ChartPalaceDisplay> toDisplayPalaces(TracuutuviChart chart) {
		List<ChartPalaceDisplay> palaces = new ArrayList<>();
		for (PalaceFunction function : PalaceFunction.values()) {
			EarthlyBranch branch = chart.positions().palaces().positions().get(function);
			List<ChartStarDisplay> stars = new ArrayList<>();
			for (StarId star : StarId.values()) {
				if (chart.stars().branchOf(star).filter(branch::equals).isPresent()) {
					stars.add(new ChartStarDisplay(star.vietnameseName(), star.element()));
				}
			}
			palaces.add(new ChartPalaceDisplay(function, branch, stars));
		}
		return List.copyOf(palaces);
	}
}
