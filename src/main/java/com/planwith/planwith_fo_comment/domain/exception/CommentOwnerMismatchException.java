package com.planwith.planwith_fo_comment.domain.exception;

import java.util.UUID;

public class CommentOwnerMismatchException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public CommentOwnerMismatchException(UUID commentUuid) {
		super("댓글 작성자만 변경할 수 있습니다. commentUuid=" + commentUuid);
	}
}
