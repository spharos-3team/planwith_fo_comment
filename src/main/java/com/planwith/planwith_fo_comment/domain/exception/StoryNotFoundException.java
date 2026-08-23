package com.planwith.planwith_fo_comment.domain.exception;

import java.util.UUID;

public class StoryNotFoundException extends RuntimeException {

	public StoryNotFoundException(UUID storyUuid) {
		super("Story를 찾을 수 없습니다. storyUuid=" + storyUuid);
	}
}
