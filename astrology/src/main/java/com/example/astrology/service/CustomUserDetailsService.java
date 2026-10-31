package com.example.astrology.service;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.astrology.entity.AppUser;
import com.example.astrology.repository.AppUserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final AppUserRepository appUserRepository;

	public CustomUserDetailsService(AppUserRepository appUserRepository) {
		this.appUserRepository = appUserRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String emailOrUsername) throws UsernameNotFoundException {
		if (emailOrUsername == null || emailOrUsername.isBlank()) {
			throw new UsernameNotFoundException("Tên đăng nhập hoặc email không được để trống.");
		}
		String query = emailOrUsername.trim().toLowerCase();
		AppUser user = appUserRepository.findByEmail(query)
				.or(() -> appUserRepository.findByUsername(query))
				.orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản với: " + query));

		return org.springframework.security.core.userdetails.User.builder()
				.username(user.getEmail())
				.password(user.getPasswordHash())
				.disabled(!user.isEnabled())
				.authorities(List.of(new SimpleGrantedAuthority("ROLE_" + (user.getRole() != null ? user.getRole() : "USER"))))
				.build();
	}
}
