package com.planwith.planwith_fo_comment.application.command;

import java.util.UUID;

import com.planwith.planwith_fo_comment.domain.memberprojection.MemberRole;

public record DeleteCommentCommand(
		UUID commentUuid,
		UUID memberUuid,
		MemberRole requesterRole
) {

	public DeleteCommentCommand(UUID commentUuid, UUID memberUuid) {
		this(commentUuid, memberUuid, MemberRole.USER);
	}

	public DeleteCommentCommand {
		if (requesterRole == null) {
			requesterRole = MemberRole.USER;
		}
	}
}
