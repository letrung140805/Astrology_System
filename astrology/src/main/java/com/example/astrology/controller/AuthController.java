package com.example.astrology.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.astrology.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AuthController {

	private static final Logger log = LoggerFactory.getLogger(AuthController.class);

	private final UserService userService;

	public AuthController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/login")
	public String login(
			@RequestParam(required = false) String error,
			@RequestParam(required = false) String registered,
			@RequestParam(required = false) String logout,
			@RequestParam(required = false) String required,
			@RequestParam(required = false) String tab,
			@RequestParam(required = false) String email,
			HttpServletRequest request,
			Model model) {

		if (logout != null) {
			SecurityContextHolder.clearContext();
			model.addAttribute("infoMessage", "Bạn đã đăng xuất an toàn khỏi hệ thống.");
		}

		if (error != null) {
			if ("exists".equals(error)) {
				model.addAttribute("errorMessage", "Email này đã được đăng ký trên hệ thống. Vui lòng đăng nhập.");
			} else if ("invalid".equals(error)) {
				model.addAttribute("errorMessage", "Vui lòng nhập email hợp lệ và mật khẩu tối thiểu 6 ký tự.");
			} else if ("bad_credentials".equals(error)) {
				model.addAttribute("errorMessage", "Tài khoản không tồn tại hoặc mật khẩu chưa chính xác. Nếu chưa có tài khoản, vui lòng chọn tab 'Đăng ký tài khoản'.");
			} else {
				model.addAttribute("errorMessage", "Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin tài khoản.");
			}
		}

		if (registered != null) {
			model.addAttribute("successMessage", "Tạo tài khoản thành công! Vui lòng nhập mật khẩu để đăng nhập vào trang chủ.");
		}

		if (required != null) {
			model.addAttribute("requiredMessage", "Vui lòng đăng nhập tài khoản Gmail để khởi tạo và lưu trữ lá số Tử Vi.");
		}

		model.addAttribute("loginEmail", email != null ? email.trim() : "");
		model.addAttribute("activeTab", "register".equals(tab) ? "register" : "login");
		return "login";
	}

	@PostMapping("/register")
	public String register(
			@RequestParam String email,
			@RequestParam String password,
			@RequestParam(required = false) String fullName,
			@RequestParam(required = false) String username) {

		log.info(">>> [POST /register] Nhận yêu cầu tạo tài khoản: email={}, fullName={}, username={}", email, fullName, username);

		if (email == null || !email.contains("@") || password == null || password.trim().length() < 6) {
			log.warn(">>> [POST /register] Dữ liệu không hợp lệ: email={}", email);
			return "redirect:/login?tab=register&error=invalid";
		}

		String normalizedEmail = email.trim().toLowerCase();
		if (userService.existsByEmail(normalizedEmail)) {
			log.info(">>> [POST /register] Email đã tồn tại: {}", normalizedEmail);
			return "redirect:/login?tab=login&error=exists&email=" + URLEncoder.encode(normalizedEmail, StandardCharsets.UTF_8);
		}

		try {
			userService.registerUser(normalizedEmail, password, fullName, username);
			log.info(">>> [POST /register] Lưu tài khoản thành công vào CSDL: {}", normalizedEmail);
			return "redirect:/login?registered=true&email=" + URLEncoder.encode(normalizedEmail, StandardCharsets.UTF_8);
		} catch (IllegalArgumentException ex) {
			log.warn(">>> [POST /register] Ngoại lệ IllegalArgumentException: {}", ex.getMessage());
			return "redirect:/login?tab=login&error=exists&email=" + URLEncoder.encode(normalizedEmail, StandardCharsets.UTF_8);
		} catch (Exception ex) {
			log.error(">>> [POST /register] Lỗi khi tạo tài khoản {}: {}", normalizedEmail, ex.getMessage(), ex);
			return "redirect:/login?tab=register&error=invalid";
		}
	}
}
