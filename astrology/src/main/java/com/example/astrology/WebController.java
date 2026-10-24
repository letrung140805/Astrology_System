package com.example.astrology;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.example.astrology.calculation.AstrologyCalculationInput;
import com.example.astrology.calculation.AstrologyCalculationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController {

	private final AstrologyCalculationService astrologyCalculationService;

	public WebController(AstrologyCalculationService astrologyCalculationService) {
		this.astrologyCalculationService = astrologyCalculationService;
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
		var input = new AstrologyCalculationInput(name, gender, birthDate, birthTime, birthplace);
		var assessment = astrologyCalculationService.assess(input);

		model.addAttribute("name", name);
		model.addAttribute("gender", gender);
		model.addAttribute("birthDate", birthDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
		model.addAttribute("birthTime", birthTime == null ? null : birthTime.format(DateTimeFormatter.ofPattern("HH:mm")));
		model.addAttribute("birthplace", birthplace);
		model.addAttribute("calculation", assessment);
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
