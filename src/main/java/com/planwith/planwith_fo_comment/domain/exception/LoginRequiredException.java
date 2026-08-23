package com.planwith.planwith_fo_comment.domain.exception;

public class LoginRequiredException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public LoginRequiredException() {
		super("로그인 후 댓글을 작성할 수 있습니다.");
	}
}
