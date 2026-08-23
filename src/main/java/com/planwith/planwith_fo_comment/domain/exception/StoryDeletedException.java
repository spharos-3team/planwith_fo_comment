package com.planwith.planwith_fo_comment.domain.exception;

import java.util.UUID;

public class StoryDeletedException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public StoryDeletedException(UUID storyUuid) {
		super("삭제된 Story에는 댓글을 작성할 수 없습니다. storyUuid=" + storyUuid);
	}
}
