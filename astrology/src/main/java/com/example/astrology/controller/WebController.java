package com.example.astrology.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.security.Principal;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.astrology.calculation.AstrologyCalculationInput;
import com.example.astrology.model.BirthChartPageData;
import com.example.astrology.model.StoredBirthChartPageData;
import com.example.astrology.service.BirthChartApplicationService;
import com.example.astrology.service.BirthChartPersistenceService;

@Controller
public class WebController {

	private static final Logger logger = LoggerFactory.getLogger(WebController.class);
	private static final String GUEST_HISTORY_SESSION_KEY = "guestBirthChartHistory";
	private static final DateTimeFormatter BIRTH_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final DateTimeFormatter BIRTH_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

	private final BirthChartApplicationService birthChartApplicationService;
	private final BirthChartPersistenceService birthChartPersistenceService;

	public WebController(
			BirthChartApplicationService birthChartApplicationService,
			BirthChartPersistenceService birthChartPersistenceService) {
		this.birthChartApplicationService = birthChartApplicationService;
		this.birthChartPersistenceService = birthChartPersistenceService;
	}

	@GetMapping("/")
	public String home() {
		return "home";
	}

	@PostMapping("/result")
	public String result(
			@RequestParam String name,
			@RequestParam String gender,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime birthTime,
			@RequestParam(required = false) String birthplace,
			Principal principal,
			HttpSession session,
			Model model) {
		String principalName = principalName(principal);
		AstrologyCalculationInput input = new AstrologyCalculationInput(
				name, gender, birthDate, birthTime, birthplace);
		BirthChartPageData pageData = birthChartApplicationService.calculate(input);
		if (pageData.chart() == null) {
			addCalculatedResult(model, input, pageData);
			model.addAttribute("saveErrorMessage",
					"Lá số chưa được lưu vì chưa thể tính đủ 12 cung và dữ liệu sao.");
			return "result";
		}

		try {
			UUID accessToken = birthChartPersistenceService.save(
					input, pageData, principalName);
			if (principalName == null) {
				rememberGuestChart(session, accessToken);
			}
			return "redirect:/result/" + accessToken;
		} catch (DataAccessException | TransactionException | IllegalArgumentException
				| IllegalStateException | UsernameNotFoundException exception) {
			logger.warn("Could not persist the calculated birth chart.", exception);
			addCalculatedResult(model, input, pageData);
			model.addAttribute("saveErrorMessage",
					"Không thể lưu hồ sơ vào cơ sở dữ liệu. Lá số chưa được lưu; vui lòng thử lại sau.");
			return "result";
		}
	}

	@GetMapping("/result/{accessToken}")
	public String savedResult(
			@PathVariable UUID accessToken,
			Principal principal,
			Model model) {
		StoredBirthChartPageData pageData = birthChartPersistenceService.load(
				accessToken, principalName(principal));
		addStoredResult(model, pageData);
		return "result";
	}

	@GetMapping("/result/id/{chartId}")
	public String ownedSavedResult(
			@PathVariable Long chartId,
			Principal principal,
			Model model) {
		StoredBirthChartPageData pageData = birthChartPersistenceService.loadOwnedChart(
				chartId, principalName(principal));
		addStoredResult(model, pageData);
		return "result";
	}

	private void addCalculatedResult(
			Model model,
			AstrologyCalculationInput input,
			BirthChartPageData pageData) {
		model.addAttribute("name", input.name());
		model.addAttribute("gender", input.gender());
		model.addAttribute("birthDate", input.birthDate().format(BIRTH_DATE_FORMAT));
		model.addAttribute("birthTime", input.birthTime() == null
				? null
				: input.birthTime().format(BIRTH_TIME_FORMAT));
		model.addAttribute("birthplace", input.birthplace());
		model.addAttribute("calculation", pageData.assessment());
		model.addAttribute("hourBranch", pageData.assessment().normalizedBirthInput().hourBranch());
		model.addAttribute("chart", pageData.chart());
		model.addAttribute("chartInputMessage", pageData.chartInputMessage());
		model.addAttribute("lunarDate", pageData.lunarDate());
		model.addAttribute("lunarCalendarName", pageData.lunarCalendarName());
		model.addAttribute("lunarCalendarVersion", pageData.lunarCalendarVersion());
		model.addAttribute("yearCanChi", pageData.yearCanChi());
		model.addAttribute("yearNayinElement", pageData.yearNayinElement());
		model.addAttribute("yearNayinSource", pageData.yearNayinSource());
		model.addAttribute("chartPalaces", pageData.chartPalaces());
		model.addAttribute("remainingGaps", pageData.remainingGaps());
		model.addAttribute("chartLimitations", pageData.chartLimitations());
	}

	private void addStoredResult(Model model, StoredBirthChartPageData pageData) {
		model.addAttribute("name", pageData.input().name());
		model.addAttribute("gender", pageData.input().gender());
		model.addAttribute("birthDate", pageData.input().birthDate().format(BIRTH_DATE_FORMAT));
		model.addAttribute("birthTime", pageData.input().birthTime() == null
				? null
				: pageData.input().birthTime().format(BIRTH_TIME_FORMAT));
		model.addAttribute("birthplace", pageData.input().birthplace());
		model.addAttribute("hourBranch", pageData.chart().input().birthHour());
		model.addAttribute("chart", pageData.chart());
		model.addAttribute("chartInputMessage", null);
		model.addAttribute("lunarDate", pageData.lunarDate());
		model.addAttribute("lunarCalendarName", pageData.lunarCalendarName());
		model.addAttribute("lunarCalendarVersion", pageData.lunarCalendarVersion());
		model.addAttribute("yearCanChi", pageData.yearCanChi());
		model.addAttribute("yearNayinElement", pageData.yearNayinElement());
		model.addAttribute("yearNayinSource", pageData.yearNayinSource());
		model.addAttribute("chartPalaces", pageData.chartPalaces());
		model.addAttribute("remainingGaps", pageData.remainingGaps());
		model.addAttribute("chartLimitations", pageData.chartLimitations());
		model.addAttribute("accessToken", pageData.accessToken());
		model.addAttribute("storedChart", true);
	}

	@GetMapping("/history")
	public String history(Principal principal, HttpSession session, Model model) {
		String principalName = principalName(principal);
		model.addAttribute("historyItems", birthChartPersistenceService.history(
				principalName, principalName == null ? guestChartTokens(session) : List.of()));
		model.addAttribute("guestHistory", principalName == null);
		return "history";
	}

	private static String principalName(Principal principal) {
		if (principal == null || "anonymousUser".equals(principal.getName())) {
			return null;
		}
		return principal.getName();
	}

	private static void rememberGuestChart(HttpSession session, UUID accessToken) {
		List<String> tokens = new ArrayList<>();
		Object stored = session.getAttribute(GUEST_HISTORY_SESSION_KEY);
		if (stored instanceof List<?> existingTokens) {
			for (Object token : existingTokens) {
				if (token instanceof String value) {
					tokens.add(value);
				}
			}
		}
		String value = accessToken.toString();
		if (!tokens.contains(value)) {
			tokens.add(value);
		}
		session.setAttribute(GUEST_HISTORY_SESSION_KEY, List.copyOf(tokens));
	}

	private static List<UUID> guestChartTokens(HttpSession session) {
		Object stored = session.getAttribute(GUEST_HISTORY_SESSION_KEY);
		if (!(stored instanceof List<?> tokens)) {
			return List.of();
		}
		List<UUID> validTokens = new ArrayList<>();
		for (Object token : tokens) {
			if (token instanceof String value) {
				try {
					validTokens.add(UUID.fromString(value));
				} catch (IllegalArgumentException exception) {
					logger.warn("Ignoring malformed guest chart token in HTTP session.");
				}
			}
		}
		return List.copyOf(validTokens);
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
