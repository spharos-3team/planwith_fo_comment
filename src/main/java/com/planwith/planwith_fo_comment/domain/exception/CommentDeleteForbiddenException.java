package com.planwith.planwith_fo_comment.domain.exception;

import java.util.UUID;

public class CommentDeleteForbiddenException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public CommentDeleteForbiddenException(UUID commentUuid) {
		super("댓글을 삭제할 권한이 없습니다. commentUuid=" + commentUuid);
	}
}
