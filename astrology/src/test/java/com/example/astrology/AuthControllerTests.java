package com.example.astrology;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;

import com.example.astrology.controller.AuthController;
import com.example.astrology.entity.AppUser;
import com.example.astrology.service.UserService;

class AuthControllerTests {

	private UserService userService;
	private AuthController controller;

	@BeforeEach
	void setUp() {
		userService = mock(UserService.class);
		controller = new AuthController(userService);
	}

	@Test
	void loginDisplaysRequiredMessageWhenRedirectedFromChartCreation() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		ExtendedModelMap model = new ExtendedModelMap();
		String view = controller.login(null, null, null, "true", null, null, request, model);

		assertEquals("login", view);
		assertTrue(model.containsKey("requiredMessage"));
	}

	@Test
	void loginDisplaysSuccessMessageWhenRegistered() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		ExtendedModelMap model = new ExtendedModelMap();
		String view = controller.login(null, "true", null, null, null, "test@gmail.com", request, model);

		assertEquals("login", view);
		assertTrue(model.containsKey("successMessage"));
		assertEquals("test@gmail.com", model.get("loginEmail"));
	}

	@Test
	void registerCreatesUserDirectlyAndRedirectsToLoginWithRegistered() {
		AppUser user = new AppUser();
		user.setEmail("test@gmail.com");

		when(userService.existsByEmail("test@gmail.com")).thenReturn(false);
		when(userService.registerUser(eq("test@gmail.com"), eq("password123"), any(), any())).thenReturn(user);

		String view = controller.register("test@gmail.com", "password123", "Người Dùng Test", null);

		assertEquals("redirect:/login?registered=true&email=test%40gmail.com", view);
		verify(userService).registerUser(eq("test@gmail.com"), eq("password123"), eq("Người Dùng Test"), isNull());
	}

	@Test
	void registerRejectsExistingEmail() {
		when(userService.existsByEmail("existing@gmail.com")).thenReturn(true);

		String view = controller.register("existing@gmail.com", "password123", "Người Dùng", null);

		assertEquals("redirect:/login?tab=login&error=exists&email=existing%40gmail.com", view);
	}

	@Test
	void registerRejectsInvalidInput() {
		String view1 = controller.register("not-an-email", "123456", null, null);
		assertEquals("redirect:/login?tab=register&error=invalid", view1);

		String view2 = controller.register("test@gmail.com", "123", null, null);
		assertEquals("redirect:/login?tab=register&error=invalid", view2);
	}
}
