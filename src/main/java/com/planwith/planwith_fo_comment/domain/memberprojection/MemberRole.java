package com.planwith.planwith_fo_comment.domain.memberprojection;

public enum MemberRole {
	USER,
	ADMIN;

	public static MemberRole from(String value) {
		if (value == null || value.isBlank()) {
			return USER;
		}
		for (String token : value.split("[\\s,]+")) {
			String normalized = token.trim();
			if (normalized.isEmpty()) {
				continue;
			}
			if ("ADMIN".equalsIgnoreCase(normalized) || "ROLE_ADMIN".equalsIgnoreCase(normalized)) {
				return ADMIN;
			}
		}
		try {
			return MemberRole.valueOf(value.trim().toUpperCase());
		}
		catch (IllegalArgumentException exception) {
			return USER;
		}
	}

	public boolean isAdmin() {
		return this == ADMIN;
	}
}
