package com.planwith.planwith_fo_comment.application.command;

import java.util.UUID;

public record HandleLikeCommand(
		UUID likeUuid,
		UUID commentUuid,
		UUID memberUuid
) {
}
