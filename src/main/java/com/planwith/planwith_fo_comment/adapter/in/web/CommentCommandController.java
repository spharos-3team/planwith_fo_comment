package com.planwith.planwith_fo_comment.adapter.in.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_comment.adapter.in.web.dto.CommentResponse;
import com.planwith.planwith_fo_comment.adapter.in.web.dto.CreateCommentRequest;
import com.planwith.planwith_fo_comment.adapter.in.web.dto.UpdateCommentRequest;
import com.planwith.planwith_fo_comment.application.command.CreateCommentCommand;
import com.planwith.planwith_fo_comment.application.command.DeleteCommentCommand;
import com.planwith.planwith_fo_comment.application.command.UpdateCommentCommand;
import com.planwith.planwith_fo_comment.application.port.in.CreateCommentUseCase;
import com.planwith.planwith_fo_comment.application.port.in.DeleteCommentUseCase;
import com.planwith.planwith_fo_comment.application.port.in.UpdateCommentUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/planwith-fo-comment/comments")
@Tag(name = "comment-command", description = "댓글 Command API")
public class CommentCommandController {

	private final CreateCommentUseCase createCommentUseCase;
	private final UpdateCommentUseCase updateCommentUseCase;
	private final DeleteCommentUseCase deleteCommentUseCase;

	// 댓글 생성
	@PostMapping
	@Operation(summary = "댓글 생성")
	public ResponseEntity<CommentResponse> createComment(
			@RequestHeader("X-Member-Uuid") UUID memberUuid,
			@Valid @RequestBody CreateCommentRequest request
	) {
		log.info("CommentCommandController : POST createComment : 댓글 생성 요청");
		CommentResponse response = CommentResponse.from(
				createCommentUseCase.create(new CreateCommentCommand(
						request.storyUuid(),
						memberUuid,
						request.content(),
						request.parentCommentUuid()
				))
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// 댓글 수정
	@PatchMapping("/{commentUuid}")
	@Operation(summary = "댓글 수정")
	public ResponseEntity<CommentResponse> updateComment(
			@RequestHeader("X-Member-Uuid") UUID memberUuid,
			@PathVariable UUID commentUuid,
			@Valid @RequestBody UpdateCommentRequest request
	) {
		log.info("CommentCommandController : PATCH updateComment : 댓글 수정 요청");
		CommentResponse response = CommentResponse.from(
				updateCommentUseCase.update(new UpdateCommentCommand(
						commentUuid,
						memberUuid,
						request.content()
				))
		);
		return ResponseEntity.ok(response);
	}

	// 댓글 삭제
	@DeleteMapping("/{commentUuid}")
	@Operation(summary = "댓글 삭제")
	public ResponseEntity<Void> deleteComment(
			@RequestHeader("X-Member-Uuid") UUID memberUuid,
			@PathVariable UUID commentUuid
	) {
		log.info("CommentCommandController : DELETE deleteComment : 댓글 삭제 요청");
		deleteCommentUseCase.delete(new DeleteCommentCommand(commentUuid, memberUuid));
		return ResponseEntity.noContent().build();
	}
}
