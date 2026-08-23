package com.planwith.planwith_fo_comment.application.query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CommentThreadResult(
		UUID commentUuid,
		UUID parentCommentUuid,
		CommentMemberResult member,
		String commentContent,
		long commentLikeCount,
		Instant createdAt,
		Instant updatedAt,
		boolean isUpdated,
		boolean canEdit,
		boolean canDelete,
		List<CommentThreadResult> replies
) {
}
