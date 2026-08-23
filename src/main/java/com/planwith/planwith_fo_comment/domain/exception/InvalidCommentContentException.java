package com.planwith.planwith_fo_comment.domain.exception;

public class InvalidCommentContentException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InvalidCommentContentException(String message) {
		super(message);
	}
}
