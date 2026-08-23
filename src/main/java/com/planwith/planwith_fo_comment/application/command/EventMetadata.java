package com.planwith.planwith_fo_comment.application.command;

import java.time.Instant;
import java.util.UUID;

public record EventMetadata(
		UUID eventUuid,
		String eventType,
		UUID targetUuid,
		Instant occurredAt
) {

	public static EventMetadata validated(
			UUID eventUuid,
			String eventType,
			UUID targetUuid,
			Instant occurredAt,
			UUID expectedTargetUuid
	) {
		if (eventUuid == null) {
			throw new IllegalArgumentException("eventUuid is required");
		}
		if (eventType == null || eventType.isBlank() || targetUuid == null || occurredAt == null) {
			throw new IllegalArgumentException("eventUuid, eventType, targetUuid, occurredAt are required");
		}
		if (!targetUuid.equals(expectedTargetUuid)) {
			throw new IllegalArgumentException("targetUuid does not match event target");
		}
		return new EventMetadata(eventUuid, eventType, targetUuid, occurredAt);
	}

	public static EventMetadata validatedVersioned(
			UUID eventUuid,
			String eventType,
			UUID targetUuid,
			Instant occurredAt,
			UUID expectedTargetUuid,
			Long sourceVersion
	) {
		EventMetadata metadata = validated(eventUuid, eventType, targetUuid, occurredAt, expectedTargetUuid);
		if (sourceVersion == null || sourceVersion <= 0) {
			throw new IllegalArgumentException("sourceVersion must be positive");
		}
		return metadata;
	}
}
