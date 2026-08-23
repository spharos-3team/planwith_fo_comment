package com.planwith.planwith_fo_comment.domain.memberprojection;

public enum MemberRole {
	USER,
	ADMIN;

	public static MemberRole from(String value) {
		if (value == null || value.isBlank()) {
			return USER;
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
