package com.planwith.planwith_fo_comment.application.query;

import java.time.Instant;
import java.util.UUID;

public record ManagedCommentResult(
		UUID commentUuid,
		String profileImage,
		String nickname,
		String commentContent,
		long reportCount,
		Instant createdAt,
		Instant hiddenAt
) {
}
