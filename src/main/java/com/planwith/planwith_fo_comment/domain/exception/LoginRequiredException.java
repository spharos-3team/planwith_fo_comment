package com.planwith.planwith_fo_comment.domain.exception;

public class LoginRequiredException extends RuntimeException {

	public LoginRequiredException() {
		super("로그인 후 댓글을 작성할 수 있습니다.");
	}
}
