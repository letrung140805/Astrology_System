package com.example.astrology.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.astrology.service.BirthChartPersistenceService;
import com.example.astrology.service.UserService;

@Controller
@RequestMapping("/account")
public class AccountController {

	private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

	private final UserService userService;
	private final BirthChartPersistenceService birthChartPersistenceService;

	public AccountController(
			UserService userService,
			BirthChartPersistenceService birthChartPersistenceService) {
		this.userService = userService;
		this.birthChartPersistenceService = birthChartPersistenceService;
	}

	@GetMapping
	public String account(
			@RequestParam(required = false) String pwdSuccess,
			@RequestParam(required = false) String pwdError,
			@RequestParam(required = false) String profileSuccess,
			@RequestParam(required = false) String profileError,
			Principal principal,
			Model model) {
		if (pwdSuccess != null) {
			model.addAttribute("pwdSuccessMessage", "Đổi mật khẩu thành công! Mật khẩu mới của bạn đã có hiệu lực.");
		}
		if (pwdError != null) {
			model.addAttribute("pwdErrorMessage", pwdError);
		}
		if (profileSuccess != null) {
			model.addAttribute("profileSuccessMessage", "Cập nhật thông tin tài khoản thành công!");
		}
		if (profileError != null) {
			model.addAttribute("profileErrorMessage", profileError);
		}
		String userPrincipal = principalName(principal);
		long count = birthChartPersistenceService != null
				? birthChartPersistenceService.countCharts(userPrincipal)
				: 0L;
		model.addAttribute("chartCount", count);
		return "account";
	}

	@PostMapping("/update-profile")
	public String updateProfile(
			@RequestParam(required = false) String fullName,
			@RequestParam(required = false) String username,
			Principal principal) {
		String userPrincipal = principalName(principal);
		if (userPrincipal == null) {
			return "redirect:/login";
		}
		if (userService == null) {
			return "redirect:/account?profileError=" + URLEncoder.encode("Dịch vụ người dùng chưa sẵn sàng.", StandardCharsets.UTF_8);
		}
		try {
			userService.updateProfile(userPrincipal, fullName, username);
			return "redirect:/account?profileSuccess=true";
		} catch (IllegalArgumentException ex) {
			return "redirect:/account?profileError=" + URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8);
		} catch (Exception ex) {
			logger.error("Lỗi khi cập nhật thông tin tài khoản cho {}", userPrincipal, ex);
			return "redirect:/account?profileError=" + URLEncoder.encode("Đã xảy ra lỗi khi cập nhật thông tin. Vui lòng thử lại sau.", StandardCharsets.UTF_8);
		}
	}

	@PostMapping("/change-password")
	public String changePassword(
			@RequestParam String currentPassword,
			@RequestParam String newPassword,
			@RequestParam String confirmPassword,
			Principal principal) {
		String userPrincipal = principalName(principal);
		if (userPrincipal == null) {
			return "redirect:/login";
		}
		if (userService == null) {
			return "redirect:/account?pwdError=" + URLEncoder.encode("Dịch vụ người dùng chưa sẵn sàng.", StandardCharsets.UTF_8);
		}
		try {
			userService.changePassword(userPrincipal, currentPassword, newPassword, confirmPassword);
			return "redirect:/account?pwdSuccess=true";
		} catch (IllegalArgumentException ex) {
			return "redirect:/account?pwdError=" + URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8);
		} catch (Exception ex) {
			logger.error("Lỗi khi đổi mật khẩu cho {}", userPrincipal, ex);
			return "redirect:/account?pwdError=" + URLEncoder.encode("Đã xảy ra lỗi khi đổi mật khẩu. Vui lòng thử lại sau.", StandardCharsets.UTF_8);
		}
	}

	private static String principalName(Principal principal) {
		if (principal == null || "anonymousUser".equals(principal.getName())) {
			return null;
		}
		return principal.getName();
	}
}
