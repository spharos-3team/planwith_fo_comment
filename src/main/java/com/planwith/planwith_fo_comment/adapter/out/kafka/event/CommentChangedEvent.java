package com.planwith.planwith_fo_comment.adapter.out.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record CommentChangedEvent(
		String eventType,
		UUID commentUuid,
		UUID storyUuid,
		UUID memberUuid,
		UUID parentCommentUuid,
		Instant occurredAt
) {
}
