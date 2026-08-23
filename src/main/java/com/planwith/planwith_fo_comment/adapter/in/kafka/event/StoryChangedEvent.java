package com.planwith.planwith_fo_comment.adapter.in.kafka.event;

import java.util.UUID;

public record StoryChangedEvent(
		String eventType,
		UUID storyUuid,
		UUID ownerMemberUuid,
		Boolean commentEnabled,
		String storyStatus,
		Long sourceVersion
) {
}
