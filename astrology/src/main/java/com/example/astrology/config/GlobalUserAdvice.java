package com.example.astrology.config;

import java.security.Principal;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.astrology.model.UserSummaryDto;
import com.example.astrology.service.BirthChartPersistenceService;
import com.example.astrology.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * ControllerAdvice chuyên trách cung cấp dữ liệu người dùng toàn cục cho View (Thymeleaf templates).
 * Đảm bảo:
 * 1. Không gọi trực tiếp tầng Repository (tuân thủ phân tầng Controller -> Service).
 * 2. Không trả về trực tiếp thực thể JPA AppUser chứa passwordHash ra ngoài View (bảo vệ đóng gói & bảo mật).
 * 3. Tận dụng Request Cache để tránh query lặp lại trên cùng một request HTTP.
 */
@ControllerAdvice
public class GlobalUserAdvice {

	private static final String CURRENT_USER_REQ_ATTR = "GLOBAL_CURRENT_USER_DTO";

	private final UserService userService;
	private final BirthChartPersistenceService birthChartPersistenceService;

	public GlobalUserAdvice(
			UserService userService,
			BirthChartPersistenceService birthChartPersistenceService) {
		this.userService = userService;
		this.birthChartPersistenceService = birthChartPersistenceService;
	}

	@ModelAttribute("currentUser")
	public UserSummaryDto currentUser(Principal principal, HttpServletRequest request) {
		if (principal == null || principal.getName() == null || "anonymousUser".equals(principal.getName())) {
			return null;
		}

		Object cached = request.getAttribute(CURRENT_USER_REQ_ATTR);
		if (cached instanceof UserSummaryDto summary) {
			return summary;
		}

		UserSummaryDto summary = userService.findSummaryByPrincipal(principal.getName()).orElse(null);
		if (summary != null) {
			request.setAttribute(CURRENT_USER_REQ_ATTR, summary);
		}
		return summary;
	}

	@ModelAttribute("userChartCount")
	public long userChartCount(Principal principal) {
		if (principal == null || principal.getName() == null || "anonymousUser".equals(principal.getName())) {
			return 0L;
		}
		return birthChartPersistenceService != null
				? birthChartPersistenceService.countCharts(principal.getName())
				: 0L;
	}
}
