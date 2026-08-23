package com.planwith.planwith_fo_comment.adapter.in.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record CommentLikedEvent(
		UUID likeUuid,
		UUID commentUuid,
		UUID memberUuid,
		UUID eventUuid,
		String eventType,
		UUID targetUuid,
		Instant occurredAt
) {

	public CommentLikedEvent(UUID likeUuid, UUID commentUuid, UUID memberUuid) {
		this(likeUuid, commentUuid, memberUuid, null, null, null, null);
	}
}
