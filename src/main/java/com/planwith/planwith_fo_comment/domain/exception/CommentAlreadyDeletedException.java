package com.planwith.planwith_fo_comment.domain.exception;

import java.util.UUID;

public class CommentAlreadyDeletedException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public CommentAlreadyDeletedException(UUID commentUuid) {
		super("이미 삭제된 댓글입니다. commentUuid=" + commentUuid);
	}
}
