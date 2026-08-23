package com.planwith.planwith_fo_comment.application.command;

import java.util.UUID;

public record CreateCommentCommand(
		UUID storyUuid,
		UUID memberUuid,
		String content
) {
}
