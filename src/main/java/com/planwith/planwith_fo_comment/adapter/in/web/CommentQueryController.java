package com.planwith.planwith_fo_comment.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_comment.adapter.in.web.dto.CommentResponse;
import com.planwith.planwith_fo_comment.adapter.in.web.dto.CommentThreadResponse;
import com.planwith.planwith_fo_comment.application.port.in.GetCommentUseCase;
import com.planwith.planwith_fo_comment.application.port.in.GetCommentsByStoryUseCase;
import com.planwith.planwith_fo_comment.application.query.GetCommentQuery;
import com.planwith.planwith_fo_comment.application.query.GetCommentsByStoryQuery;
import com.planwith.planwith_fo_comment.domain.comment.CommentSort;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/planwith-fo-comment")
@Tag(name = "comment-query", description = "댓글 Query API")
public class CommentQueryController {

	private final GetCommentUseCase getCommentUseCase;
	private final GetCommentsByStoryUseCase getCommentsByStoryUseCase;

	// Story별 댓글 목록 조회
	@GetMapping("/stories/{storyUuid}/comments")
	@Operation(summary = "Story별 댓글 목록 조회", description = "VISIBLE 댓글만 조회한다. sort 기본값은 LATEST이다.")
	public ResponseEntity<List<CommentThreadResponse>> getCommentsByStory(
			@PathVariable UUID storyUuid,
			@RequestParam(defaultValue = "LATEST") CommentSort sort,
			@RequestHeader(value = "X-Member-Uuid", required = false) UUID memberUuid
	) {
		log.info("CommentQueryController : GET getCommentsByStory : Story별 댓글 목록 조회 요청");
		log.debug(
				"CommentQueryController : GET getCommentsByStory : 조회 조건 확인 - storyUuid={}, sort={}",
				storyUuid,
				sort
		);
		List<CommentThreadResponse> responses = getCommentsByStoryUseCase
				.getByStory(new GetCommentsByStoryQuery(storyUuid, sort, memberUuid))
				.stream()
				.map(CommentThreadResponse::from)
				.toList();
		return ResponseEntity.ok(responses);
	}

	// 댓글 상세 조회
	@GetMapping("/comments/{commentUuid}")
	@Operation(summary = "댓글 상세 조회")
	public ResponseEntity<CommentResponse> getComment(@PathVariable UUID commentUuid) {
		log.info("CommentQueryController : GET getComment : 댓글 상세 조회 요청");
		return ResponseEntity.ok(CommentResponse.from(getCommentUseCase.get(new GetCommentQuery(commentUuid))));
	}
}
