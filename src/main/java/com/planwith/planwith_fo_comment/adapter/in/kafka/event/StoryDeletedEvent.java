package com.planwith.planwith_fo_comment.adapter.in.kafka.event;

import java.util.UUID;

public record StoryDeletedEvent(
		String eventType,
		UUID storyUuid,
		Long sourceVersion
) {
}
