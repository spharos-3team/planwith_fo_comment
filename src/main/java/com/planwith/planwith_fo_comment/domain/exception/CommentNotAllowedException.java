package com.planwith.planwith_fo_comment.domain.exception;

import java.util.UUID;

public class CommentNotAllowedException extends RuntimeException {

	public CommentNotAllowedException(UUID storyUuid) {
		super("해당 Story는 댓글 작성이 허용되지 않습니다. storyUuid=" + storyUuid);
	}
}
