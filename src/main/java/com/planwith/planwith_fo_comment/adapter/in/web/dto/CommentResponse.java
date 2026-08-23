package com.planwith.planwith_fo_comment.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.planwith.planwith_fo_comment.application.query.CommentPermissionResult;
import com.planwith.planwith_fo_comment.application.query.CommentQueryResult;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "댓글 응답. 작성자 정보는 comment_member_projection에서 채운다.")
public record CommentResponse(
		UUID commentUuid,
		UUID storyUuid,
		UUID memberUuid,
		UUID parentCommentUuid,
		String profileImage,
		String nickname,
		String memberStatus,
		String commentContent,
		long likeCount,
		long reportCount,
		UUID storyOwnerMemberUuid,
		Boolean commentEnabled,
		String storyStatus,
		Instant createdAt,
		Instant updatedAt,
		@JsonProperty("isUpdated")
		boolean isUpdated,
		boolean canEdit,
		boolean canDelete
) {

	public static CommentResponse from(CommentQueryResult result, CommentPermissionResult permission) {
		return new CommentResponse(
				result.commentUuid(),
				result.storyUuid(),
				result.memberUuid(),
				result.parentCommentUuid(),
				result.profileImage(),
				result.nickname(),
				result.memberStatus(),
				result.commentContent(),
				result.likeCount(),
				result.reportCount(),
				result.storyOwnerMemberUuid(),
				result.commentEnabled(),
				result.storyStatus(),
				result.createdAt(),
				result.updatedAt(),
				isUpdated(result.createdAt(), result.updatedAt()),
				permission.canEdit(),
				permission.canDelete()
		);
	}

	private static boolean isUpdated(Instant createdAt, Instant updatedAt) {
		return createdAt != null && updatedAt != null && updatedAt.isAfter(createdAt);
	}
}
