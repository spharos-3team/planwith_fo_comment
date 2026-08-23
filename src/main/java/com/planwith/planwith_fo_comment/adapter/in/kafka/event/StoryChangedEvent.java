package com.planwith.planwith_fo_comment.adapter.in.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record StoryChangedEvent(
		String eventType,
		UUID storyUuid,
		UUID ownerMemberUuid,
		Boolean commentEnabled,
		String storyStatus,
		Long sourceVersion,
		UUID eventUuid,
		UUID targetUuid,
		Instant occurredAt
) {

	public StoryChangedEvent(
			String eventType,
			UUID storyUuid,
			UUID ownerMemberUuid,
			Boolean commentEnabled,
			String storyStatus,
			Long sourceVersion
	) {
		this(eventType, storyUuid, ownerMemberUuid, commentEnabled, storyStatus, sourceVersion, null, null, null);
	}
}
