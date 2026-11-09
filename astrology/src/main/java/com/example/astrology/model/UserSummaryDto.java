package com.example.astrology.model;

import com.example.astrology.entity.AppUser;

public record UserSummaryDto(
		Long id,
		String username,
		String email,
		String fullName,
		String role
) {
	public static UserSummaryDto fromEntity(AppUser user) {
		if (user == null) {
			return null;
		}
		return new UserSummaryDto(
				user.getId(),
				user.getUsername(),
				user.getEmail(),
				user.getFullName(),
				user.getRole()
		);
	}
}
