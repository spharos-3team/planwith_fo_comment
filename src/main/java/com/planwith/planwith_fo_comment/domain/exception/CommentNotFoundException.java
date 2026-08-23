package com.planwith.planwith_fo_comment.domain.exception;

import java.util.UUID;

public class CommentNotFoundException extends RuntimeException {

	public CommentNotFoundException(UUID commentUuid) {
		super("댓글을 찾을 수 없습니다. commentUuid=" + commentUuid);
	}
}
