package com.planwith.planwith_fo_comment.adapter.in.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record StoryDeletedEvent(
		String eventType,
		UUID storyUuid,
		Long sourceVersion,
		UUID eventUuid,
		UUID targetUuid,
		Instant occurredAt
) {

	public StoryDeletedEvent(String eventType, UUID storyUuid, Long sourceVersion) {
		this(eventType, storyUuid, sourceVersion, null, null, null);
	}
}
