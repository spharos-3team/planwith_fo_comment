package com.planwith.planwith_fo_comment.exception;

import java.time.Instant;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.planwith.planwith_fo_comment.domain.exception.CommentAlreadyDeletedException;
import com.planwith.planwith_fo_comment.domain.exception.CommentDeleteForbiddenException;
import com.planwith.planwith_fo_comment.domain.exception.CommentNotAllowedException;
import com.planwith.planwith_fo_comment.domain.exception.CommentNotFoundException;
import com.planwith.planwith_fo_comment.domain.exception.CommentOwnerMismatchException;
import com.planwith.planwith_fo_comment.domain.exception.InvalidCommentContentException;
import com.planwith.planwith_fo_comment.domain.exception.InvalidReplyException;
import com.planwith.planwith_fo_comment.domain.exception.LoginRequiredException;
import com.planwith.planwith_fo_comment.domain.exception.StoryDeletedException;
import com.planwith.planwith_fo_comment.domain.exception.StoryNotFoundException;
import com.planwith.planwith_fo_comment.dto.ApiErrorResponse;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException exception) {
		return createErrorResponse(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", exception.getMessage());
	}

	@ExceptionHandler(LoginRequiredException.class)
	public ResponseEntity<ApiErrorResponse> handleLoginRequired(LoginRequiredException exception) {
		return createErrorResponse(HttpStatus.UNAUTHORIZED, "LOGIN_REQUIRED", exception.getMessage());
	}

	@ExceptionHandler(StoryNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleStoryNotFound(StoryNotFoundException exception) {
		return createErrorResponse(HttpStatus.NOT_FOUND, "STORY_NOT_FOUND", exception.getMessage());
	}

	@ExceptionHandler(StoryDeletedException.class)
	public ResponseEntity<ApiErrorResponse> handleStoryDeleted(StoryDeletedException exception) {
		return createErrorResponse(HttpStatus.CONFLICT, "STORY_DELETED", exception.getMessage());
	}

	@ExceptionHandler(CommentNotAllowedException.class)
	public ResponseEntity<ApiErrorResponse> handleCommentNotAllowed(CommentNotAllowedException exception) {
		return createErrorResponse(HttpStatus.FORBIDDEN, "COMMENT_NOT_ALLOWED", exception.getMessage());
	}

	@ExceptionHandler(CommentNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleCommentNotFound(CommentNotFoundException exception) {
		return createErrorResponse(HttpStatus.NOT_FOUND, "COMMENT_NOT_FOUND", exception.getMessage());
	}

	@ExceptionHandler(CommentOwnerMismatchException.class)
	public ResponseEntity<ApiErrorResponse> handleCommentOwnerMismatch(CommentOwnerMismatchException exception) {
		return createErrorResponse(HttpStatus.FORBIDDEN, "COMMENT_OWNER_MISMATCH", exception.getMessage());
	}

	@ExceptionHandler(CommentAlreadyDeletedException.class)
	public ResponseEntity<ApiErrorResponse> handleCommentAlreadyDeleted(CommentAlreadyDeletedException exception) {
		return createErrorResponse(HttpStatus.CONFLICT, "COMMENT_ALREADY_DELETED", exception.getMessage());
	}

	@ExceptionHandler(CommentDeleteForbiddenException.class)
	public ResponseEntity<ApiErrorResponse> handleCommentDeleteForbidden(CommentDeleteForbiddenException exception) {
		return createErrorResponse(HttpStatus.FORBIDDEN, "COMMENT_DELETE_FORBIDDEN", exception.getMessage());
	}

	@ExceptionHandler(InvalidReplyException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidReply(InvalidReplyException exception) {
		return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_REPLY", exception.getMessage());
	}

	@ExceptionHandler(InvalidCommentContentException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidCommentContent(InvalidCommentContentException exception) {
		return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", exception.getMessage());
	}

	@ExceptionHandler(MissingRequestHeaderException.class)
	public ResponseEntity<ApiErrorResponse> handleMissingHeader(MissingRequestHeaderException exception) {
		return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", exception.getHeaderName() + " 헤더가 필요합니다.");
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
		return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청값 형식이 올바르지 않습니다.");
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
		String message = exception.getConstraintViolations().stream()
				.findFirst()
				.map(violation -> violation.getMessage())
				.orElse("요청값이 올바르지 않습니다.");
		return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
		String message = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.findFirst()
				.map(DefaultMessageSourceResolvable::getDefaultMessage)
				.orElse("요청값이 올바르지 않습니다.");

		return createErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", message);
	}

	private ResponseEntity<ApiErrorResponse> createErrorResponse(
			HttpStatus status,
			String code,
			String message
	) {
		ApiErrorResponse response = new ApiErrorResponse(
				Instant.now(),
				status.value(),
				code,
				message
		);
		return ResponseEntity.status(status).body(response);
	}
}
