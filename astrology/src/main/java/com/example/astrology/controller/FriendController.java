package com.example.astrology.controller;

import java.security.Principal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.astrology.calculation.AstrologyCalculationInput;
import com.example.astrology.entity.FriendChart;
import com.example.astrology.model.BirthChartPageData;
import com.example.astrology.model.CouplePersonData;
import com.example.astrology.model.FriendCompatibilityResult;
import com.example.astrology.service.BirthChartApplicationService;
import com.example.astrology.service.FriendChartPersistenceService;
import com.example.astrology.service.FriendCompatibilityService;

@Controller
@RequestMapping("/tuong-hop-ban-be")
public class FriendController {

	private static final Logger logger = LoggerFactory.getLogger(FriendController.class);
	private static final DateTimeFormatter BIRTH_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final DateTimeFormatter BIRTH_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

	private final BirthChartApplicationService birthChartApplicationService;
	private final FriendCompatibilityService friendCompatibilityService;
	private final FriendChartPersistenceService friendChartPersistenceService;

	@Autowired
	public FriendController(
			BirthChartApplicationService birthChartApplicationService,
			FriendCompatibilityService friendCompatibilityService,
			FriendChartPersistenceService friendChartPersistenceService) {
		this.birthChartApplicationService = birthChartApplicationService;
		this.friendCompatibilityService = friendCompatibilityService;
		this.friendChartPersistenceService = friendChartPersistenceService;
	}

	public FriendController(
			BirthChartApplicationService birthChartApplicationService,
			FriendCompatibilityService friendCompatibilityService) {
		this(birthChartApplicationService, friendCompatibilityService, null);
	}

	@GetMapping
	public String friendPage(Model model) {
		model.addAttribute("activePage", "tuong-hop-ban-be");
		model.addAttribute("submitted", false);
		return "friend";
	}

	public String calculateFriend(
			String name1, String gender1, LocalDate birthDate1, Integer birthDay1, Integer birthMonth1, Integer birthYear1, LocalTime birthTime1, String birthplace1,
			String name2, String gender2, LocalDate birthDate2, Integer birthDay2, Integer birthMonth2, Integer birthYear2, LocalTime birthTime2, String birthplace2,
			Integer viewYear, Model model) {
		return calculateFriend(name1, gender1, birthDate1, birthDay1, birthMonth1, birthYear1, birthTime1, birthplace1,
				name2, gender2, birthDate2, birthDay2, birthMonth2, birthYear2, birthTime2, birthplace2,
				viewYear, () -> "testUser", model);
	}

	@PostMapping
	public String calculateFriend(
			@RequestParam String name1,
			@RequestParam(defaultValue = "Nam") String gender1,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate1,
			@RequestParam(required = false) Integer birthDay1,
			@RequestParam(required = false) Integer birthMonth1,
			@RequestParam(required = false) Integer birthYear1,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime birthTime1,
			@RequestParam(required = false) String birthplace1,

			@RequestParam String name2,
			@RequestParam(defaultValue = "Nam") String gender2,
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

		model.addAttribute("activePage", "tuong-hop-ban-be");

		// Resolve birthDate1
		if (birthDate1 == null && birthDay1 != null && birthMonth1 != null && birthYear1 != null) {
			try {
				birthDate1 = LocalDate.of(birthYear1, birthMonth1, birthDay1);
			} catch (DateTimeException ex) {
				model.addAttribute("errorMessage", "Ngày sinh của bạn không hợp lệ.");
				model.addAttribute("submitted", false);
				preserveFormInputs(model, name1, gender1, birthDay1, birthMonth1, birthYear1, birthTime1, birthplace1,
						name2, gender2, birthDay2, birthMonth2, birthYear2, birthTime2, birthplace2, viewYear);
				return "friend";
			}
		}

		// Resolve birthDate2
		if (birthDate2 == null && birthDay2 != null && birthMonth2 != null && birthYear2 != null) {
			try {
				birthDate2 = LocalDate.of(birthYear2, birthMonth2, birthDay2);
			} catch (DateTimeException ex) {
				model.addAttribute("errorMessage", "Ngày sinh của bạn bè/cộng sự không hợp lệ.");
				model.addAttribute("submitted", false);
				preserveFormInputs(model, name1, gender1, birthDay1, birthMonth1, birthYear1, birthTime1, birthplace1,
						name2, gender2, birthDay2, birthMonth2, birthYear2, birthTime2, birthplace2, viewYear);
				return "friend";
			}
		}

		if (birthDate1 == null || birthDate2 == null) {
			model.addAttribute("errorMessage", "Vui lòng nhập đầy đủ ngày tháng năm sinh của cả hai người.");
			model.addAttribute("submitted", false);
			preserveFormInputs(model, name1, gender1, birthDay1, birthMonth1, birthYear1, birthTime1, birthplace1,
					name2, gender2, birthDay2, birthMonth2, birthYear2, birthTime2, birthplace2, viewYear);
			return "friend";
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
				return "friend";
			}

			CouplePersonData person1 = toPersonData(input1, pageData1, birthDay1, birthMonth1, birthYear1);
			CouplePersonData person2 = toPersonData(input2, pageData2, birthDay2, birthMonth2, birthYear2);

			FriendCompatibilityResult compatibility = friendCompatibilityService.assess(person1, person2);

			// Lưu kết quả 2 lá số và bài luận giải tương hợp vào database/lịch sử
			if (friendChartPersistenceService != null && principalName(principal) != null) {
				try {
					FriendChart savedChart = friendChartPersistenceService.save(
							person1, person2, compatibility, principalName(principal));
					model.addAttribute("savedId", savedChart.getId());
					model.addAttribute("savedSuccess", true);
				} catch (Exception ex) {
					logger.warn("Could not save friend chart to history: {}", ex.getMessage());
				}
			}

			model.addAttribute("person1", person1);
			model.addAttribute("person2", person2);
			model.addAttribute("compatibility", compatibility);
			model.addAttribute("viewYear", viewYear != null ? viewYear : 2026);
			model.addAttribute("submitted", true);

			preserveFormInputs(model, name1, gender1, birthDay1, birthMonth1, birthYear1, birthTime1, birthplace1,
					name2, gender2, birthDay2, birthMonth2, birthYear2, birthTime2, birthplace2, viewYear);

			return "friend";
		} catch (Exception ex) {
			logger.warn("Could not calculate friend chart compatibility", ex);
			model.addAttribute("errorMessage", "Đã xảy ra lỗi trong quá trình tính toán lá số bạn bè: " + ex.getMessage());
			model.addAttribute("submitted", false);
			preserveFormInputs(model, name1, gender1, birthDay1, birthMonth1, birthYear1, birthTime1, birthplace1,
					name2, gender2, birthDay2, birthMonth2, birthYear2, birthTime2, birthplace2, viewYear);
			return "friend";
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

	@GetMapping("/id/{id}")
	public String viewSavedFriendChart(
			@PathVariable Long id,
			Principal principal,
			Model model) {

		String principalName = principalName(principal);
		if (principalName == null) {
			return "redirect:/login?required=true";
		}
		if (friendChartPersistenceService == null) {
			return "redirect:/tuong-hop-ban-be";
		}

		FriendChart saved = friendChartPersistenceService.loadOwned(id, principalName);

		AstrologyCalculationInput input1 = new AstrologyCalculationInput(
				saved.getName1(), saved.getGender1(), saved.getBirthDate1(), saved.getBirthTime1(), saved.getBirthplace1());
		BirthChartPageData pageData1 = birthChartApplicationService.calculate(input1);

		AstrologyCalculationInput input2 = new AstrologyCalculationInput(
				saved.getName2(), saved.getGender2(), saved.getBirthDate2(), saved.getBirthTime2(), saved.getBirthplace2());
		BirthChartPageData pageData2 = birthChartApplicationService.calculate(input2);

		CouplePersonData person1 = toPersonData(input1, pageData1, saved.getBirthDate1().getDayOfMonth(), saved.getBirthDate1().getMonthValue(), saved.getBirthDate1().getYear());
		CouplePersonData person2 = toPersonData(input2, pageData2, saved.getBirthDate2().getDayOfMonth(), saved.getBirthDate2().getMonthValue(), saved.getBirthDate2().getYear());

		FriendCompatibilityResult compatibility = FriendCompatibilityResult.fromSaved(
				saved.getTotalScore() != null ? saved.getTotalScore() : 0,
				saved.getRating(),
				saved.getRatingColor(),
				"Kết quả đối chiếu tương hợp bạn bè & cộng sự giữa " + saved.getName1() + " và " + saved.getName2() + ".",
				saved.getAdvice(),
				saved.getPartnerScore(),
				saved.getCommunicationScore(),
				saved.getVisionScore(),
				saved.getValuesScore(),
				saved.getMoneyScore(),
				saved.getPartnerInterpretation(),
				saved.getCommunicationInterpretation(),
				saved.getVisionInterpretation(),
				saved.getValuesInterpretation(),
				saved.getMoneyInterpretation(),
				saved.getConclusion()
		);

		model.addAttribute("activePage", "tuong-hop-ban-be");
		model.addAttribute("person1", person1);
		model.addAttribute("person2", person2);
		model.addAttribute("compatibility", compatibility);
		model.addAttribute("viewYear", 2026);
		model.addAttribute("submitted", true);
		model.addAttribute("savedId", saved.getId());
		model.addAttribute("isFromHistory", true);

		preserveFormInputs(model, saved.getName1(), saved.getGender1(), saved.getBirthDate1().getDayOfMonth(), saved.getBirthDate1().getMonthValue(), saved.getBirthDate1().getYear(), saved.getBirthTime1(), saved.getBirthplace1(),
				saved.getName2(), saved.getGender2(), saved.getBirthDate2().getDayOfMonth(), saved.getBirthDate2().getMonthValue(), saved.getBirthDate2().getYear(), saved.getBirthTime2(), saved.getBirthplace2(), 2026);

		return "friend";
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
