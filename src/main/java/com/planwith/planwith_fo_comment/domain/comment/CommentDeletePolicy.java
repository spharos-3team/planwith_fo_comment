package com.planwith.planwith_fo_comment.domain.comment;

import java.util.UUID;

import com.planwith.planwith_fo_comment.domain.memberprojection.MemberRole;

public final class CommentDeletePolicy {

	private CommentDeletePolicy() {
	}

	public static boolean canDelete(
			UUID commentAuthorUuid,
			UUID storyOwnerUuid,
			UUID requesterUuid,
			MemberRole role
	) {
		if (requesterUuid == null) {
			return false;
		}
		if (requesterUuid.equals(commentAuthorUuid)) {
			return true;
		}
		if (storyOwnerUuid != null && requesterUuid.equals(storyOwnerUuid)) {
			return true;
		}
		return role != null && role.isAdmin();
	}
}
