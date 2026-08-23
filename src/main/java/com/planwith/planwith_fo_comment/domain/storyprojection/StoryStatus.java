package com.planwith.planwith_fo_comment.domain.storyprojection;

public enum StoryStatus {
	ACTIVE,
	DELETED;

	public static StoryStatus from(String value) {
		if (value == null || value.isBlank()) {
			return ACTIVE;
		}
		return StoryStatus.valueOf(value);
	}
}
