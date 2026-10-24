package com.example.astrology;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.example.astrology.calculation.AstrologyCalculationInput;
import com.example.astrology.calculation.AstrologyCalculationService;
import com.example.astrology.calculation.CalculationGap;
import com.example.astrology.calculation.calendar.EarthlyBranch;
import com.example.astrology.calculation.calendar.HeavenlyStem;
import com.example.astrology.calculation.chart.BirthSex;
import com.example.astrology.calculation.chart.ChartPalaceDisplay;
import com.example.astrology.calculation.chart.LunarChartInput;
import com.example.astrology.calculation.chart.PalaceFunction;
import com.example.astrology.calculation.chart.StarId;
import com.example.astrology.calculation.chart.TracuutuviChart;
import com.example.astrology.calculation.chart.TracuutuviChartCalculator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController {

	private final AstrologyCalculationService astrologyCalculationService;
	private final TracuutuviChartCalculator chartCalculator;

	public WebController(
			AstrologyCalculationService astrologyCalculationService,
			TracuutuviChartCalculator chartCalculator) {
		this.astrologyCalculationService = astrologyCalculationService;
		this.chartCalculator = chartCalculator;
	}

	@GetMapping("/")
	public String home() {
		return "home";
	}

	@GetMapping("/result")
	public String result(
			@RequestParam String name,
			@RequestParam String gender,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime birthTime,
			@RequestParam(required = false) String birthplace,
			@RequestParam(required = false) Integer lunarYear,
			@RequestParam(required = false) Integer lunarMonth,
			@RequestParam(required = false) Integer lunarDay,
			@RequestParam(required = false) Boolean leapMonth,
			@RequestParam(required = false) HeavenlyStem yearStem,
			@RequestParam(required = false) EarthlyBranch yearBranch,
			Model model) {
		var input = new AstrologyCalculationInput(name, gender, birthDate, birthTime, birthplace);
		var assessment = astrologyCalculationService.assess(input);

		model.addAttribute("name", name);
		model.addAttribute("gender", gender);
		model.addAttribute("birthDate", birthDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
		model.addAttribute("birthTime", birthTime == null ? null : birthTime.format(DateTimeFormatter.ofPattern("HH:mm")));
		model.addAttribute("birthplace", birthplace);
		model.addAttribute("calculation", assessment);
		model.addAttribute("hourBranch", assessment.normalizedBirthInput().hourBranch());

		boolean anyLunarInput = lunarYear != null || lunarMonth != null || lunarDay != null
				|| yearStem != null || yearBranch != null || Boolean.TRUE.equals(leapMonth);
		TracuutuviChart chart = null;
		String chartInputMessage = null;
		if (anyLunarInput) {
			if (lunarYear == null || lunarMonth == null || lunarDay == null
					|| yearStem == null || yearBranch == null) {
				chartInputMessage = "Để tính các phần của lá số, hãy nhập đủ ngày âm, tháng âm, năm âm "
						+ "và Can Chi năm sinh.";
			} else if (birthTime == null) {
				chartInputMessage = "Cần có giờ sinh để tính cung Mệnh, cung Thân và vị trí sao.";
			} else if (!"Nam".equals(gender) && !"Nữ".equals(gender)) {
				chartInputMessage = "Giới tính cần chọn Nam hoặc Nữ để áp dụng quy ước của profile.";
			} else if (lunarYear < 1 || lunarMonth < 1 || lunarMonth > 12 || lunarDay < 1 || lunarDay > 30) {
				chartInputMessage = "Ngày âm cần nằm trong khoảng ngày 1-30, tháng 1-12 và năm lớn hơn 0.";
			} else {
				var lunarInput = new LunarChartInput(
						lunarYear,
						yearStem,
						yearBranch,
						lunarMonth,
						Boolean.TRUE.equals(leapMonth),
						lunarDay,
						assessment.normalizedBirthInput().hourBranch(),
						"Nam".equals(gender) ? BirthSex.MALE : BirthSex.FEMALE);
				chart = chartCalculator.calculate(lunarInput);
			}
		}
		model.addAttribute("chart", chart);
		model.addAttribute("chartInputMessage", chartInputMessage);
		model.addAttribute("chartPalaces", chart == null ? List.of() : toDisplayPalaces(chart));
		model.addAttribute("remainingGaps", chart == null
				? assessment.gaps()
				: assessment.gaps().stream()
						.filter(gap -> gap != CalculationGap.MENH_AND_CUC
								&& gap != CalculationGap.TWELVE_PALACES
								&& gap != CalculationGap.STAR_PLACEMENT)
						.toList());
		model.addAttribute("chartLimitations", List.of(
				"Chưa có chuyển đổi ngày dương sang âm; dữ liệu âm lịch/Can Chi năm trên chart được nhập thủ công.",
				"Chưa tính đủ Can Chi năm, tháng, ngày, giờ từ ngày giờ sinh.",
				"Chưa tính Nạp Âm, Chủ Mệnh/Chủ Thân, độ sáng sao và các nhóm phụ tinh khác."));
		return "result";
	}

	private List<ChartPalaceDisplay> toDisplayPalaces(TracuutuviChart chart) {
		List<ChartPalaceDisplay> palaces = new ArrayList<>();
		for (PalaceFunction function : PalaceFunction.values()) {
			EarthlyBranch branch = chart.positions().palaces().positions().get(function);
			List<String> stars = new ArrayList<>();
			for (StarId star : StarId.values()) {
				if (chart.stars().branchOf(star).filter(branch::equals).isPresent()) {
					stars.add(star.vietnameseName());
				}
			}
			palaces.add(new ChartPalaceDisplay(function, branch, stars));
		}
		return List.copyOf(palaces);
	}

	@GetMapping("/history")
	public String history() {
		return "history";
	}

	@GetMapping("/account")
	public String account() {
		return "account";
	}

	@GetMapping("/about")
	public String about() {
		return "about";
	}

	@GetMapping("/chat")
	public String chat() {
		return "chat";
	}

	@GetMapping("/login")
	public String login() {
		return "login";
	}
}
