package com.example.astrology.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.astrology.entity.AppUser;
import com.example.astrology.repository.AppUserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Service
public class UserService {

	private static final Logger log = LoggerFactory.getLogger(UserService.class);

	private final AppUserRepository appUserRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(
			AppUserRepository appUserRepository,
			PasswordEncoder passwordEncoder) {
		this.appUserRepository = appUserRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public Optional<AppUser> findByEmail(String email) {
		if (email == null) {
			return Optional.empty();
		}
		return appUserRepository.findByEmail(email.trim().toLowerCase());
	}

	public Optional<AppUser> findByUsername(String username) {
		if (username == null || username.isBlank()) {
			return Optional.empty();
		}
		return appUserRepository.findByUsername(username.trim().toLowerCase());
	}

	public boolean existsByEmail(String email) {
		if (email == null) {
			return false;
		}
		return appUserRepository.existsByEmail(email.trim().toLowerCase());
	}

	@Transactional
	public AppUser registerUser(String email, String rawPassword, String fullName) {
		return registerUser(email, rawPassword, fullName, null);
	}

	@Transactional
	public AppUser registerUser(String email, String rawPassword, String fullName, String username) {
		String normalizedEmail = email.trim().toLowerCase();
		if (appUserRepository.existsByEmail(normalizedEmail)) {
			throw new IllegalArgumentException("Email này đã được đăng ký trên hệ thống.");
		}

		AppUser user = new AppUser();
		user.setEmail(normalizedEmail);
		user.setPasswordHash(passwordEncoder.encode(rawPassword));
		String name = (fullName != null && !fullName.isBlank()) ? fullName.trim() : normalizedEmail.split("@")[0];
		user.setFullName(name);

		String finalUsername;
		if (username != null && !username.isBlank()) {
			String clean = username.trim().toLowerCase().replaceAll("[^a-z0-9_]", "_");
			if (!clean.isBlank() && !appUserRepository.existsByUsername(clean)) {
				finalUsername = clean;
			} else {
				finalUsername = generateUsernameFromNameOrEmail(fullName, normalizedEmail);
			}
		} else {
			finalUsername = generateUsernameFromNameOrEmail(fullName, normalizedEmail);
		}

		user.setUsername(finalUsername);
		user.setRole("USER");
		user.setEnabled(true); // Tài khoản được kích hoạt ngay, đăng nhập trực tiếp bằng mật khẩu

		AppUser saved = appUserRepository.save(user);
		log.info(">>> Đã lưu tài khoản người dùng mới vào MySQL: id={}, email={}, username={}",
				saved.getId(), saved.getEmail(), saved.getUsername());
		return saved;
	}

	@Transactional
	public void changePassword(String email, String currentPassword, String newPassword, String confirmPassword) {
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("Không xác định được phiên làm việc của người dùng.");
		}

		AppUser user = findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin tài khoản người dùng."));

		if (currentPassword == null || currentPassword.isBlank()) {
			throw new IllegalArgumentException("Vui lòng nhập mật khẩu hiện tại.");
		}

		if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
			throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác.");
		}

		if (newPassword == null || newPassword.trim().length() < 6) {
			throw new IllegalArgumentException("Mật khẩu mới phải có tối thiểu 6 ký tự.");
		}

		if (!newPassword.equals(confirmPassword)) {
			throw new IllegalArgumentException("Mật khẩu xác nhận không khớp với mật khẩu mới.");
		}

		if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
			throw new IllegalArgumentException("Mật khẩu mới không được trùng với mật khẩu cũ.");
		}

		user.setPasswordHash(passwordEncoder.encode(newPassword));
		appUserRepository.save(user);
		log.info(">>> Đổi mật khẩu thành công cho tài khoản: {}", user.getEmail());
	}

	@Transactional
	public void updateProfile(String email, String fullName, String username) {
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("Không xác định được phiên làm việc của người dùng.");
		}

		AppUser user = findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin tài khoản."));

		if (fullName != null && !fullName.isBlank()) {
			user.setFullName(fullName.trim());
			if (username == null || username.isBlank()) {
				user.setUsername(generateUsernameFromNameOrEmail(fullName.trim(), user.getEmail()));
			}
		}

		if (username != null && !username.isBlank()) {
			String cleanUsername = username.trim();
			if (!cleanUsername.equalsIgnoreCase(user.getUsername())) {
				if (appUserRepository.existsByUsername(cleanUsername)) {
					throw new IllegalArgumentException("Tên đăng nhập này đã có người sử dụng. Vui lòng chọn tên khác.");
				}
				user.setUsername(cleanUsername);
			}
		}

		appUserRepository.save(user);
		log.info(">>> Đã cập nhật thông tin hồ sơ cho tài khoản: email={}, fullName={}, username={}",
				user.getEmail(), user.getFullName(), user.getUsername());
	}

	@Transactional
	public AppUser findOrCreateGoogleUser(String email, String fullName) {
		String normalizedEmail = email.trim().toLowerCase();
		return appUserRepository.findByEmail(normalizedEmail).orElseGet(() -> {
			AppUser newUser = new AppUser();
			newUser.setEmail(normalizedEmail);
			newUser.setUsername(generateUsernameFromNameOrEmail(fullName, normalizedEmail));
			newUser.setFullName(fullName != null && !fullName.isBlank() ? fullName.trim() : normalizedEmail.split("@")[0]);
			newUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
			newUser.setRole("USER");
			newUser.setEnabled(true);
			return appUserRepository.save(newUser);
		});
	}

	public void authenticateInSession(AppUser user, HttpServletRequest request) {
		var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
		var auth = new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);
		SecurityContext sc = SecurityContextHolder.getContext();
		sc.setAuthentication(auth);
		HttpSession session = request.getSession(true);
		session.setAttribute("SPRING_SECURITY_CONTEXT", sc);
	}

	private String generateUsernameFromNameOrEmail(String fullName, String email) {
		String base;
		if (fullName != null && !fullName.isBlank()) {
			String unaccented = java.text.Normalizer.normalize(fullName.trim(), java.text.Normalizer.Form.NFD)
					.replaceAll("\\p{M}", "")
					.replaceAll("[đĐ]", "d")
					.toLowerCase()
					.replaceAll("[^a-z0-9_]", "_")
					.replaceAll("_+", "_")
					.replaceAll("^_|_$", "");
			base = unaccented.isBlank() ? "user" : unaccented;
		} else {
			base = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "_");
		}

		if (base.length() > 30) {
			base = base.substring(0, 30);
		}
		if (!appUserRepository.existsByUsername(base)) {
			return base;
		}
		int counter = 1;
		while (appUserRepository.existsByUsername(base + counter)) {
			counter++;
		}
		return base + counter;
	}
}
