package com.planwith.planwith_fo_comment.application.service;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_comment.application.query.CommentPermissionResult;
import com.planwith.planwith_fo_comment.application.query.CommentQueryResult;
import com.planwith.planwith_fo_comment.domain.comment.CommentDeletePolicy;
import com.planwith.planwith_fo_comment.domain.memberprojection.MemberRole;

@Component
public class CommentPermissionResolver {

	public CommentPermissionResult resolve(
			CommentQueryResult comment,
			UUID viewerMemberUuid,
			MemberRole viewerRole
	) {
		MemberRole resolvedRole = viewerRole == null ? MemberRole.USER : viewerRole;
		boolean canEdit = !comment.deleted()
				&& viewerMemberUuid != null
				&& viewerMemberUuid.equals(comment.memberUuid());
		boolean canDelete = !comment.deleted() && CommentDeletePolicy.canDelete(
				comment.memberUuid(),
				comment.storyOwnerMemberUuid(),
				viewerMemberUuid,
				resolvedRole
		);
		return new CommentPermissionResult(canEdit, canDelete);
	}
}
