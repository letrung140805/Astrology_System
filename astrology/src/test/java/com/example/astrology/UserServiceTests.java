package com.example.astrology;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.astrology.entity.AppUser;
import com.example.astrology.repository.AppUserRepository;
import com.example.astrology.service.UserService;

class UserServiceTests {

	private AppUserRepository appUserRepository;
	private PasswordEncoder passwordEncoder;
	private UserService userService;

	@BeforeEach
	void setUp() {
		appUserRepository = mock(AppUserRepository.class);
		passwordEncoder = mock(PasswordEncoder.class);
		userService = new UserService(appUserRepository, passwordEncoder);
	}

	@Test
	void changePasswordSuccessfullyUpdatesPasswordHash() {
		AppUser user = new AppUser();
		user.setEmail("testuser@gmail.com");
		user.setPasswordHash("hashed_old_password");

		when(appUserRepository.findByEmail("testuser@gmail.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("oldPassword123", "hashed_old_password")).thenReturn(true);
		when(passwordEncoder.matches("newPassword456", "hashed_old_password")).thenReturn(false);
		when(passwordEncoder.encode("newPassword456")).thenReturn("hashed_new_password");

		userService.changePassword("testuser@gmail.com", "oldPassword123", "newPassword456", "newPassword456");

		assertEquals("hashed_new_password", user.getPasswordHash());
		verify(appUserRepository).save(user);
	}

	@Test
	void changePasswordFailsWhenCurrentPasswordIncorrect() {
		AppUser user = new AppUser();
		user.setEmail("testuser@gmail.com");
		user.setPasswordHash("hashed_old_password");

		when(appUserRepository.findByEmail("testuser@gmail.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("wrongOldPassword", "hashed_old_password")).thenReturn(false);

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
				userService.changePassword("testuser@gmail.com", "wrongOldPassword", "newPassword456", "newPassword456"));

		assertEquals("Mật khẩu hiện tại không chính xác.", ex.getMessage());
	}

	@Test
	void changePasswordFailsWhenNewPasswordTooShort() {
		AppUser user = new AppUser();
		user.setEmail("testuser@gmail.com");
		user.setPasswordHash("hashed_old_password");

		when(appUserRepository.findByEmail("testuser@gmail.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("oldPassword123", "hashed_old_password")).thenReturn(true);

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
				userService.changePassword("testuser@gmail.com", "oldPassword123", "123", "123"));

		assertEquals("Mật khẩu mới phải có tối thiểu 6 ký tự.", ex.getMessage());
	}

	@Test
	void changePasswordFailsWhenConfirmPasswordDoesNotMatch() {
		AppUser user = new AppUser();
		user.setEmail("testuser@gmail.com");
		user.setPasswordHash("hashed_old_password");

		when(appUserRepository.findByEmail("testuser@gmail.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("oldPassword123", "hashed_old_password")).thenReturn(true);

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
				userService.changePassword("testuser@gmail.com", "oldPassword123", "newPassword456", "differentPassword789"));

		assertEquals("Mật khẩu xác nhận không khớp với mật khẩu mới.", ex.getMessage());
	}

	@Test
	void changePasswordFailsWhenNewPasswordSameAsOld() {
		AppUser user = new AppUser();
		user.setEmail("testuser@gmail.com");
		user.setPasswordHash("hashed_old_password");

		when(appUserRepository.findByEmail("testuser@gmail.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("samePassword123", "hashed_old_password")).thenReturn(true);

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
				userService.changePassword("testuser@gmail.com", "samePassword123", "samePassword123", "samePassword123"));

		assertEquals("Mật khẩu mới không được trùng với mật khẩu cũ.", ex.getMessage());
	}
}

