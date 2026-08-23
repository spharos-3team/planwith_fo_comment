package com.planwith.planwith_fo_comment.application.query;

import java.util.UUID;

import com.planwith.planwith_fo_comment.domain.comment.CommentSort;
import com.planwith.planwith_fo_comment.domain.memberprojection.MemberRole;

public record GetCommentsByStoryQuery(
		UUID storyUuid,
		CommentSort sort,
		UUID viewerMemberUuid,
		MemberRole viewerRole
) {

	public GetCommentsByStoryQuery {
		if (sort == null) {
			sort = CommentSort.LATEST;
		}
		if (viewerRole == null) {
			viewerRole = MemberRole.USER;
		}
	}

	public GetCommentsByStoryQuery(UUID storyUuid) {
		this(storyUuid, CommentSort.LATEST, null, MemberRole.USER);
	}

	public GetCommentsByStoryQuery(UUID storyUuid, CommentSort sort, UUID viewerMemberUuid) {
		this(storyUuid, sort, viewerMemberUuid, MemberRole.USER);
	}
}
