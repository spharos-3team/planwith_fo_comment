package com.planwith.planwith_fo_comment.application.query;

import java.util.UUID;

import com.planwith.planwith_fo_comment.domain.memberprojection.MemberRole;

public record GetManagedCommentsQuery(
		UUID storyUuid,
		UUID requesterUuid,
		MemberRole requesterRole
) {

	public GetManagedCommentsQuery {
		if (requesterRole == null) {
			requesterRole = MemberRole.USER;
		}
	}
}
