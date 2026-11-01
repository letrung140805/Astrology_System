package com.example.astrology.service;

import java.time.DateTimeException;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.astrology.calculation.AstrologyCalculationInput;
import com.example.astrology.calculation.AstrologyCalculationService;
import com.example.astrology.calculation.CanLuongCalculator;
import com.example.astrology.calculation.CalculationAssessment;
import com.example.astrology.calculation.CalculationGap;
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

@Service
public class BirthChartApplicationService {

	private static final ZoneOffset VIETNAM_STANDARD_OFFSET = ZoneOffset.ofHours(7);
	private static final List<String> CHART_LIMITATIONS = List.of(
			"Can Chi 4 trụ năm tháng ngày giờ đã được chuẩn hóa theo lịch thiên văn Việt Nam.",
			"Đã tính toán đầy đủ Bản Mệnh (60 hoa giáp), Cân lượng, Chủ Mệnh, Chủ Thân, Lai Nhân Cung.",
			"Độ sáng chính tinh và các nhóm phụ tinh được an theo nguyên tắc cổ truyền.");

	private final AstrologyCalculationService astrologyCalculationService;
	private final TracuutuviChartCalculator chartCalculator;
	private final HoNgocDucLunarCalendarProvider lunarCalendarProvider;
	private final SexagenaryYearCalculator sexagenaryYearCalculator;
	private final NayinElementCalculator nayinElementCalculator;
	private final TuViDetailService tuViDetailService;

	@org.springframework.beans.factory.annotation.Autowired
	public BirthChartApplicationService(
			AstrologyCalculationService astrologyCalculationService,
			TracuutuviChartCalculator chartCalculator,
			HoNgocDucLunarCalendarProvider lunarCalendarProvider,
			SexagenaryYearCalculator sexagenaryYearCalculator,
			NayinElementCalculator nayinElementCalculator,
			TuViDetailService tuViDetailService) {
		this.astrologyCalculationService = astrologyCalculationService;
		this.chartCalculator = chartCalculator;
		this.lunarCalendarProvider = lunarCalendarProvider;
		this.sexagenaryYearCalculator = sexagenaryYearCalculator;
		this.nayinElementCalculator = nayinElementCalculator;
		this.tuViDetailService = tuViDetailService;
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
					fullDetails = tuViDetailService.buildDetails(chart, input.birthDate(), birthSex);
					displayPalaces = fullDetails.gridPalaces();
				}
			}
		} catch (DateTimeException | IllegalArgumentException | IllegalStateException exception) {
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
				displayPalaces,
				remainingGaps(assessment, chart, lunarDate, yearNayinElement),
				CHART_LIMITATIONS,
				fullDetails);
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
}
