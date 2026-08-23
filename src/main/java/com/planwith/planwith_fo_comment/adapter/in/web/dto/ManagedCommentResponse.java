package com.planwith.planwith_fo_comment.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_comment.application.query.ManagedCommentResult;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "숨김 댓글 관리 응답")
public record ManagedCommentResponse(
		UUID commentUuid,
		String profileImage,
		String nickname,
		String commentContent,
		long reportCount,
		Instant createdAt,
		Instant hiddenAt
) {

	public static ManagedCommentResponse from(ManagedCommentResult result) {
		return new ManagedCommentResponse(
				result.commentUuid(),
				result.profileImage(),
				result.nickname(),
				result.commentContent(),
				result.reportCount(),
				result.createdAt(),
				result.hiddenAt()
		);
	}
}
