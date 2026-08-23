package com.planwith.planwith_fo_comment.domain.exception;

public class InvalidReplyException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public InvalidReplyException(String message) {
		super(message);
	}
}
