package com.example.astrology.config;

import java.security.Principal;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.astrology.entity.AppUser;
import com.example.astrology.repository.BirthChartRepository;
import com.example.astrology.service.UserService;

@ControllerAdvice
public class GlobalUserAdvice {

	private final UserService userService;
	private final BirthChartRepository birthChartRepository;

	public GlobalUserAdvice(UserService userService, BirthChartRepository birthChartRepository) {
		this.userService = userService;
		this.birthChartRepository = birthChartRepository;
	}

	@ModelAttribute("currentUser")
	public AppUser currentUser(Principal principal) {
		if (principal == null || principal.getName() == null) {
			return null;
		}
		return userService.findByEmail(principal.getName())
				.or(() -> userService.findByUsername(principal.getName()))
				.orElse(null);
	}

	@ModelAttribute("userChartCount")
	public long userChartCount(Principal principal) {
		if (principal == null || principal.getName() == null) {
			return 0L;
		}
		AppUser user = currentUser(principal);
		if (user == null) {
			return 0L;
		}
		return birthChartRepository.countByUser_Id(user.getId());
	}
}

