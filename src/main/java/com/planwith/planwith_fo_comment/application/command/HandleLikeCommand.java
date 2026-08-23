package com.planwith.planwith_fo_comment.application.command;

import java.util.UUID;

public record HandleLikeCommand(
		UUID likeUuid,
		UUID commentUuid,
		UUID memberUuid,
		EventMetadata eventMetadata
) {

	public HandleLikeCommand(UUID likeUuid, UUID commentUuid, UUID memberUuid) {
		this(likeUuid, commentUuid, memberUuid, null);
	}
}
