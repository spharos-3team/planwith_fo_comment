package com.planwith.planwith_fo_comment.adapter.in.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.planwith.planwith_fo_comment.application.query.CommentThreadResult;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Story 댓글 목록 항목. 대댓글은 replies에 중첩한다.")
public record CommentThreadResponse(
		UUID commentUuid,
		UUID parentCommentUuid,
		CommentMemberResponse member,
		String commentContent,
		long commentLikeCount,
		Instant createdAt,
		Instant updatedAt,
		@JsonProperty("isUpdated")
		boolean isUpdated,
		boolean canEdit,
		boolean canDelete,
		List<CommentThreadResponse> replies
) {

	public static CommentThreadResponse from(CommentThreadResult result) {
		return new CommentThreadResponse(
				result.commentUuid(),
				result.parentCommentUuid(),
				CommentMemberResponse.from(result.member()),
				result.commentContent(),
				result.commentLikeCount(),
				result.createdAt(),
				result.updatedAt(),
				result.isUpdated(),
				result.canEdit(),
				result.canDelete(),
				result.replies() == null
						? List.of()
						: result.replies().stream().map(CommentThreadResponse::from).toList()
		);
	}
}
