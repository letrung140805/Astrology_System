package com.example.astrology.service;

import java.time.DateTimeException;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.astrology.calculation.AstrologyCalculationInput;
import com.example.astrology.calculation.AstrologyCalculationService;
import com.example.astrology.calculation.CanLuongCalculator;
import com.example.astrology.calculation.CalculationAssessment;
import com.example.astrology.calculation.TuViDetailService;
import com.example.astrology.calculation.calendar.HoNgocDucLunarCalendarProvider;
import com.example.astrology.calculation.calendar.NayinElementCalculator;
import com.example.astrology.calculation.calendar.SexagenaryYearCalculator;
import com.example.astrology.calculation.calendar.VietnameseLunarCalendarProvider;
import com.example.astrology.calculation.chart.BirthSex;
import com.example.astrology.calculation.chart.LunarChartInput;
import com.example.astrology.calculation.chart.TracuutuviChart;
import com.example.astrology.calculation.chart.TracuutuviChartCalculator;
import com.example.astrology.model.BirthChartPageData;
import com.example.astrology.model.ChartPalaceDisplay;

/**
 * Service điều phối tính toán lá số Tử Vi cốt lõi (Domain Calculation Orchestrator).
 * Phụ trách kết hợp: chuẩn hóa ngày sinh, chuyển đổi âm lịch, tính Can Chi, Nạp Âm và an 12 cung + sao.
 * Việc chuẩn bị dữ liệu view được ủy quyền cho BirthChartDisplayService.
 */
@Service
public class BirthChartApplicationService {

	private static final ZoneOffset VIETNAM_STANDARD_OFFSET = ZoneOffset.ofHours(7);

	private final AstrologyCalculationService astrologyCalculationService;
	private final TracuutuviChartCalculator chartCalculator;
	private final HoNgocDucLunarCalendarProvider lunarCalendarProvider;
	private final SexagenaryYearCalculator sexagenaryYearCalculator;
	private final NayinElementCalculator nayinElementCalculator;
	private final BirthChartDisplayService displayService;

	@org.springframework.beans.factory.annotation.Autowired
	public BirthChartApplicationService(
			AstrologyCalculationService astrologyCalculationService,
			TracuutuviChartCalculator chartCalculator,
			HoNgocDucLunarCalendarProvider lunarCalendarProvider,
			SexagenaryYearCalculator sexagenaryYearCalculator,
			NayinElementCalculator nayinElementCalculator,
			BirthChartDisplayService displayService) {
		this.astrologyCalculationService = astrologyCalculationService;
		this.chartCalculator = chartCalculator;
		this.lunarCalendarProvider = lunarCalendarProvider;
		this.sexagenaryYearCalculator = sexagenaryYearCalculator;
		this.nayinElementCalculator = nayinElementCalculator;
		this.displayService = displayService;
	}

	public BirthChartApplicationService(
			AstrologyCalculationService astrologyCalculationService,
			TracuutuviChartCalculator chartCalculator,
			HoNgocDucLunarCalendarProvider lunarCalendarProvider,
			SexagenaryYearCalculator sexagenaryYearCalculator,
			NayinElementCalculator nayinElementCalculator,
			TuViDetailService tuViDetailService) {
		this(astrologyCalculationService, chartCalculator, lunarCalendarProvider,
				sexagenaryYearCalculator, nayinElementCalculator,
				new BirthChartDisplayService(tuViDetailService));
	}

	public BirthChartApplicationService(
			AstrologyCalculationService astrologyCalculationService,
			TracuutuviChartCalculator chartCalculator,
			HoNgocDucLunarCalendarProvider lunarCalendarProvider,
			SexagenaryYearCalculator sexagenaryYearCalculator,
			NayinElementCalculator nayinElementCalculator) {
		this(astrologyCalculationService, chartCalculator, lunarCalendarProvider,
				sexagenaryYearCalculator, nayinElementCalculator,
				new TuViDetailService(new CanLuongCalculator(), nayinElementCalculator));
	}

	public BirthChartPageData calculate(AstrologyCalculationInput input) {
		CalculationAssessment assessment = astrologyCalculationService.assess(input);
		TracuutuviChart chart = null;
		String chartInputMessage = null;
		VietnameseLunarCalendarProvider.LunarDate lunarDate = null;
		String yearCanChi = null;
		String yearNayinElement = null;
		TuViDetailService.FullChartDetails fullDetails = null;
		List<ChartPalaceDisplay> displayPalaces = List.of();

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
				BirthSex birthSex = "Nam".equals(input.gender()) ? BirthSex.MALE : BirthSex.FEMALE;
				var lunarInput = new LunarChartInput(
						lunarDate.year(),
						yearPillar.stem(),
						yearPillar.branch(),
						lunarDate.month(),
						lunarDate.leapMonth(),
						lunarDate.day(),
						assessment.normalizedBirthInput().hourBranch(),
						birthSex);
				chart = chartCalculator.calculate(lunarInput);
				if (chart != null) {
					fullDetails = displayService.buildDetails(chart, input.birthDate(), birthSex);
					if (fullDetails != null) {
						displayPalaces = fullDetails.gridPalaces();
					}
				}
			}
		} catch (DateTimeException | IllegalArgumentException | IllegalStateException exception) {
			chartInputMessage = exception.getMessage();
		}

		return displayService.createPageData(
				assessment,
				chart,
				chartInputMessage,
				lunarDate,
				lunarCalendarProvider.implementationName(),
				lunarCalendarProvider.implementationVersion(),
				yearCanChi,
				yearNayinElement,
				displayPalaces,
				fullDetails);
	}
}
