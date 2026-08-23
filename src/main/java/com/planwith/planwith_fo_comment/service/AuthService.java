package com.planwith.planwith_fo_comment.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.stereotype.Service;

import com.planwith.planwith_fo_comment.config.AuthProperties;
import com.planwith.planwith_fo_comment.dto.LoginRequest;
import com.planwith.planwith_fo_comment.dto.LoginResponse;
import com.planwith.planwith_fo_comment.exception.InvalidCredentialsException;

@Service
public class AuthService {

	private final AuthProperties authProperties;

	public AuthService(AuthProperties authProperties) {
		this.authProperties = authProperties;
	}

	public LoginResponse login(LoginRequest request) {
		if (!isValidCredentials(request)) {
			throw new InvalidCredentialsException();
		}

		return new LoginResponse(request.id(), "로그인에 성공했습니다.");
	}

	private boolean isValidCredentials(LoginRequest request) {
		return authProperties.id().equals(request.id())
				&& MessageDigest.isEqual(
						authProperties.pw().getBytes(StandardCharsets.UTF_8),
						request.pw().getBytes(StandardCharsets.UTF_8)
				);
	}
}
