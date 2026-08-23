package com.planwith.planwith_fo_comment.application.query;

import java.util.UUID;

import com.planwith.planwith_fo_comment.domain.comment.CommentSort;

public record GetCommentsByStoryQuery(
		UUID storyUuid,
		CommentSort sort,
		UUID viewerMemberUuid
) {

	public GetCommentsByStoryQuery {
		if (sort == null) {
			sort = CommentSort.LATEST;
		}
	}

	public GetCommentsByStoryQuery(UUID storyUuid) {
		this(storyUuid, CommentSort.LATEST, null);
	}
}
