package com.example.astrology.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.astrology.calculation.AstrologyCalculationInput;
import com.example.astrology.model.BirthChartPageData;
import com.example.astrology.service.BirthChartApplicationService;

@Controller
public class WebController {

	private final BirthChartApplicationService birthChartApplicationService;

	public WebController(BirthChartApplicationService birthChartApplicationService) {
		this.birthChartApplicationService = birthChartApplicationService;
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
			Model model) {
		BirthChartPageData pageData = birthChartApplicationService.calculate(
				new AstrologyCalculationInput(name, gender, birthDate, birthTime, birthplace));

		model.addAttribute("name", name);
		model.addAttribute("gender", gender);
		model.addAttribute("birthDate", birthDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
		model.addAttribute("birthTime", birthTime == null
				? null
				: birthTime.format(DateTimeFormatter.ofPattern("HH:mm")));
		model.addAttribute("birthplace", birthplace);
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
		return "result";
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
