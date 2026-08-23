package com.planwith.planwith_fo_comment.adapter.in.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record CommentUnlikedEvent(
		UUID likeUuid,
		UUID commentUuid,
		UUID memberUuid,
		UUID eventUuid,
		String eventType,
		UUID targetUuid,
		Instant occurredAt
) {

	public CommentUnlikedEvent(UUID likeUuid, UUID commentUuid, UUID memberUuid) {
		this(likeUuid, commentUuid, memberUuid, null, null, null, null);
	}
}
