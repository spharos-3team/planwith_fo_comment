package com.planwith.planwith_fo_comment.application.command;

import java.util.UUID;

public record CreateCommentCommand(
		UUID storyUuid,
		UUID memberUuid,
		String commentContent,
		UUID parentCommentUuid
) {

	public CreateCommentCommand(UUID storyUuid, UUID memberUuid, String commentContent) {
		this(storyUuid, memberUuid, commentContent, null);
	}
}
