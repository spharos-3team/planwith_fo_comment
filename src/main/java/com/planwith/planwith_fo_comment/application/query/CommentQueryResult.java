package com.planwith.planwith_fo_comment.application.query;

import java.time.Instant;
import java.util.UUID;

public record CommentQueryResult(
		UUID commentUuid,
		UUID storyUuid,
		UUID memberUuid,
		UUID parentCommentUuid,
		String nickname,
		String profileImage,
		String memberStatus,
		String commentContent,
		long likeCount,
		long reportCount,
		UUID storyOwnerMemberUuid,
		Boolean commentEnabled,
		String storyStatus,
		Instant createdAt,
		Instant updatedAt
) {
}
