package com.planwith.planwith_fo_comment.application.command;

import java.util.UUID;

public record DeleteCommentCommand(
		UUID commentUuid,
		UUID memberUuid
) {
}
