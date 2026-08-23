package com.planwith.planwith_fo_comment.application.query;

import java.time.Instant;
import java.util.UUID;

public record CommentQueryResult(
		UUID commentUuid,
		UUID storyUuid,
		UUID memberUuid,
		String nickname,
		String profileImage,
		String memberStatus,
		String content,
		long likeCount,
		Instant createdAt,
		Instant updatedAt
) {
}
