package com.example.astrology.controller;

import java.security.Principal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.astrology.calculation.AstrologyCalculationInput;
import com.example.astrology.model.BirthChartPageData;
import com.example.astrology.model.CoupleCompatibilityResult;
import com.example.astrology.model.CouplePersonData;
import com.example.astrology.service.BirthChartApplicationService;
import com.example.astrology.service.CoupleCompatibilityService;

@Controller
@RequestMapping("/tuong-hop")
public class CoupleController {

	private static final Logger logger = LoggerFactory.getLogger(CoupleController.class);
	private static final DateTimeFormatter BIRTH_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final DateTimeFormatter BIRTH_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

	private final BirthChartApplicationService birthChartApplicationService;
	private final CoupleCompatibilityService coupleCompatibilityService;

	public CoupleController(
			BirthChartApplicationService birthChartApplicationService,
			CoupleCompatibilityService coupleCompatibilityService) {
		this.birthChartApplicationService = birthChartApplicationService;
		this.coupleCompatibilityService = coupleCompatibilityService;
	}

	@GetMapping
	public String couplePage(Model model) {
		model.addAttribute("activePage", "tuong-hop");
		model.addAttribute("submitted", false);
		return "couple";
	}

	public String calculateCouple(
			String name1, String gender1, LocalDate birthDate1, Integer birthDay1, Integer birthMonth1, Integer birthYear1, LocalTime birthTime1, String birthplace1,
			String name2, String gender2, LocalDate birthDate2, Integer birthDay2, Integer birthMonth2, Integer birthYear2, LocalTime birthTime2, String birthplace2,
			Integer viewYear, Model model) {
		return calculateCouple(name1, gender1, birthDate1, birthDay1, birthMonth1, birthYear1, birthTime1, birthplace1,
				name2, gender2, birthDate2, birthDay2, birthMonth2, birthYear2, birthTime2, birthplace2,
				viewYear, () -> "testUser", model);
	}

	@PostMapping
	public String calculateCouple(
			@RequestParam String name1,
			@RequestParam(defaultValue = "Nam") String gender1,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate1,
			@RequestParam(required = false) Integer birthDay1,
			@RequestParam(required = false) Integer birthMonth1,
			@RequestParam(required = false) Integer birthYear1,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime birthTime1,
			@RequestParam(required = false) String birthplace1,

			@RequestParam String name2,
			@RequestParam(defaultValue = "Nữ") String gender2,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate2,
			@RequestParam(required = false) Integer birthDay2,
			@RequestParam(required = false) Integer birthMonth2,
			@RequestParam(required = false) Integer birthYear2,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime birthTime2,
			@RequestParam(required = false) String birthplace2,

			@RequestParam(required = false, defaultValue = "2026") Integer viewYear,
			Principal principal,
			Model model) {

		if (principalName(principal) == null) {
			return "redirect:/login?required=true";
		}

		model.addAttribute("activePage", "tuong-hop");

		// Resolve birthDate1
		if (birthDate1 == null && birthDay1 != null && birthMonth1 != null && birthYear1 != null) {
			try {
				birthDate1 = LocalDate.of(birthYear1, birthMonth1, birthDay1);
			} catch (DateTimeException ex) {
				model.addAttribute("errorMessage", "Ngày sinh của người thứ nhất không hợp lệ.");
				model.addAttribute("submitted", false);
				preserveFormInputs(model, name1, gender1, birthDay1, birthMonth1, birthYear1, birthTime1, birthplace1,
						name2, gender2, birthDay2, birthMonth2, birthYear2, birthTime2, birthplace2, viewYear);
				return "couple";
			}
		}

		// Resolve birthDate2
		if (birthDate2 == null && birthDay2 != null && birthMonth2 != null && birthYear2 != null) {
			try {
				birthDate2 = LocalDate.of(birthYear2, birthMonth2, birthDay2);
			} catch (DateTimeException ex) {
				model.addAttribute("errorMessage", "Ngày sinh của người thứ hai không hợp lệ.");
				model.addAttribute("submitted", false);
				preserveFormInputs(model, name1, gender1, birthDay1, birthMonth1, birthYear1, birthTime1, birthplace1,
						name2, gender2, birthDay2, birthMonth2, birthYear2, birthTime2, birthplace2, viewYear);
				return "couple";
			}
		}

		if (birthDate1 == null || birthDate2 == null) {
			model.addAttribute("errorMessage", "Vui lòng nhập đầy đủ ngày tháng năm sinh của cả hai người.");
			model.addAttribute("submitted", false);
			preserveFormInputs(model, name1, gender1, birthDay1, birthMonth1, birthYear1, birthTime1, birthplace1,
					name2, gender2, birthDay2, birthMonth2, birthYear2, birthTime2, birthplace2, viewYear);
			return "couple";
		}

		try {
			AstrologyCalculationInput input1 = new AstrologyCalculationInput(
					name1.trim(), gender1, birthDate1, birthTime1, birthplace1);
			BirthChartPageData pageData1 = birthChartApplicationService.calculate(input1);

			AstrologyCalculationInput input2 = new AstrologyCalculationInput(
					name2.trim(), gender2, birthDate2, birthTime2, birthplace2);
			BirthChartPageData pageData2 = birthChartApplicationService.calculate(input2);

			if (pageData1.chart() == null || pageData2.chart() == null) {
				model.addAttribute("errorMessage", "Chưa đủ dữ liệu giờ sinh hoặc thông số để lập đủ 12 cung cho cả hai lá số.");
				model.addAttribute("submitted", false);
				preserveFormInputs(model, name1, gender1, birthDay1, birthMonth1, birthYear1, birthTime1, birthplace1,
						name2, gender2, birthDay2, birthMonth2, birthYear2, birthTime2, birthplace2, viewYear);
				return "couple";
			}

			CouplePersonData person1 = toPersonData(input1, pageData1, birthDay1, birthMonth1, birthYear1);
			CouplePersonData person2 = toPersonData(input2, pageData2, birthDay2, birthMonth2, birthYear2);

			CoupleCompatibilityResult compatibility = coupleCompatibilityService.assess(person1, person2);

			model.addAttribute("person1", person1);
			model.addAttribute("person2", person2);
			model.addAttribute("compatibility", compatibility);
			model.addAttribute("viewYear", viewYear != null ? viewYear : 2026);
			model.addAttribute("submitted", true);

			preserveFormInputs(model, name1, gender1, birthDay1, birthMonth1, birthYear1, birthTime1, birthplace1,
					name2, gender2, birthDay2, birthMonth2, birthYear2, birthTime2, birthplace2, viewYear);

			return "couple";
		} catch (Exception ex) {
			logger.warn("Could not calculate couple chart compatibility", ex);
			model.addAttribute("errorMessage", "Đã xảy ra lỗi trong quá trình tính toán lá số cặp đôi: " + ex.getMessage());
			model.addAttribute("submitted", false);
			preserveFormInputs(model, name1, gender1, birthDay1, birthMonth1, birthYear1, birthTime1, birthplace1,
					name2, gender2, birthDay2, birthMonth2, birthYear2, birthTime2, birthplace2, viewYear);
			return "couple";
		}
	}

	private CouplePersonData toPersonData(AstrologyCalculationInput input, BirthChartPageData pageData, Integer day, Integer month, Integer year) {
		String hourBranch = (pageData.assessment() != null && pageData.assessment().normalizedBirthInput() != null && pageData.assessment().normalizedBirthInput().hourBranch() != null)
				? pageData.assessment().normalizedBirthInput().hourBranch().earthlyBranch().vietnameseName()
				: null;

		return new CouplePersonData(
				input.name(),
				input.gender(),
				input.birthDate().format(BIRTH_DATE_FORMAT),
				year != null ? year : input.birthDate().getYear(),
				month != null ? month : input.birthDate().getMonthValue(),
				day != null ? day : input.birthDate().getDayOfMonth(),
				input.birthTime() != null ? input.birthTime().format(BIRTH_TIME_FORMAT) : null,
				input.birthplace(),
				hourBranch,
				pageData.chart(),
				pageData.chartPalaces(),
				pageData.lunarDate(),
				pageData.yearCanChi(),
				pageData.yearNayinElement(),
				pageData.fullDetails()
		);
	}

	private void preserveFormInputs(Model model,
			String name1, String gender1, Integer birthDay1, Integer birthMonth1, Integer birthYear1, LocalTime birthTime1, String birthplace1,
			String name2, String gender2, Integer birthDay2, Integer birthMonth2, Integer birthYear2, LocalTime birthTime2, String birthplace2,
			Integer viewYear) {
		model.addAttribute("formName1", name1);
		model.addAttribute("formGender1", gender1);
		model.addAttribute("formBirthDay1", birthDay1);
		model.addAttribute("formBirthMonth1", birthMonth1);
		model.addAttribute("formBirthYear1", birthYear1);
		model.addAttribute("formBirthTime1", birthTime1 != null ? birthTime1.format(BIRTH_TIME_FORMAT) : "");
		model.addAttribute("formBirthplace1", birthplace1);

		model.addAttribute("formName2", name2);
		model.addAttribute("formGender2", gender2);
		model.addAttribute("formBirthDay2", birthDay2);
		model.addAttribute("formBirthMonth2", birthMonth2);
		model.addAttribute("formBirthYear2", birthYear2);
		model.addAttribute("formBirthTime2", birthTime2 != null ? birthTime2.format(BIRTH_TIME_FORMAT) : "");
		model.addAttribute("formBirthplace2", birthplace2);

		model.addAttribute("formViewYear", viewYear != null ? viewYear : 2026);
	}

	private static String principalName(Principal principal) {
		if (principal == null || "anonymousUser".equals(principal.getName())) {
			return null;
		}
		return principal.getName();
	}
}

