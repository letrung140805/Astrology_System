package com.example.astrology;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController {

	@GetMapping("/")
	public String home() {
		return "home";
	}

	@GetMapping("/result")
	public String result(
			@RequestParam(defaultValue = "Nguyễn Thị Lan") String name,
			@RequestParam(defaultValue = "Nữ") String gender,
			@RequestParam(defaultValue = "2005-06-12") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate,
			@RequestParam(defaultValue = "10:30") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime birthTime,
			@RequestParam(defaultValue = "Hà Nội, Việt Nam") String birthplace,
			Model model) {
		model.addAttribute("name", name);
		model.addAttribute("gender", gender);
		model.addAttribute("birthDate", birthDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
		model.addAttribute("birthTime", birthTime.format(DateTimeFormatter.ofPattern("HH:mm")));
		model.addAttribute("birthplace", birthplace);
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
