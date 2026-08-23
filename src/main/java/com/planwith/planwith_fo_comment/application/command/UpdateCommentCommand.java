package com.planwith.planwith_fo_comment.application.command;

import java.util.UUID;

public record UpdateCommentCommand(
		UUID commentUuid,
		UUID memberUuid,
		String commentContent
) {
}
