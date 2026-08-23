package com.planwith.planwith_fo_comment.domain.exception;

import java.util.UUID;

public class StoryNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public StoryNotFoundException(UUID storyUuid) {
		super("Story를 찾을 수 없습니다. storyUuid=" + storyUuid);
	}
}
