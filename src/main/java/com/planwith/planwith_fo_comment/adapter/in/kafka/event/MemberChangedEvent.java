package com.planwith.planwith_fo_comment.adapter.in.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record MemberChangedEvent(
		String eventType,
		UUID memberUuid,
		String nickname,
		String profileImage,
		String memberStatus,
		Long sourceVersion,
		UUID eventUuid,
		UUID targetUuid,
		Instant occurredAt
) {

	public MemberChangedEvent(
			String eventType,
			UUID memberUuid,
			String nickname,
			String profileImage,
			String memberStatus,
			Long sourceVersion
	) {
		this(eventType, memberUuid, nickname, profileImage, memberStatus, sourceVersion, null, null, null);
	}
}
