package com.planwith.planwith_fo_comment.domain.memberprojection;

public enum MemberStatus {
	ACTIVE,
	SUSPENDED,
	DELETED;

	public static MemberStatus from(String value) {
		if (value == null || value.isBlank()) {
			return ACTIVE;
		}
		return MemberStatus.valueOf(value);
	}
}
