package com.planwith.planwith_fo_comment.domain.exception;

import java.util.UUID;

public class CommentManagementForbiddenException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public CommentManagementForbiddenException(UUID storyUuid) {
		super("댓글 관리 권한이 없습니다. storyUuid=" + storyUuid);
	}
}
