package com.planwith.planwith_fo_comment.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_comment.application.query.CommentQueryResult;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "댓글 응답")
public record CommentResponse(
		UUID commentUuid,
		UUID storyUuid,
		UUID memberUuid,
		UUID parentCommentUuid,
		String nickname,
		String profileImage,
		String memberStatus,
		String content,
		long likeCount,
		Instant createdAt,
		Instant updatedAt
) {

	public static CommentResponse from(CommentQueryResult result) {
		return new CommentResponse(
				result.commentUuid(),
				result.storyUuid(),
				result.memberUuid(),
				result.parentCommentUuid(),
				result.nickname(),
				result.profileImage(),
				result.memberStatus(),
				result.content(),
				result.likeCount(),
				result.createdAt(),
				result.updatedAt()
		);
	}
}
