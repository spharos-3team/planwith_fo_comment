package com.planwith.planwith_fo_comment.application.command;

import java.util.UUID;

public record MarkStoryDeletedCommand(
		UUID storyUuid,
		Long sourceVersion,
		EventMetadata eventMetadata
) {

	public MarkStoryDeletedCommand(UUID storyUuid, Long sourceVersion) {
		this(storyUuid, sourceVersion, null);
	}

	public long incomingVersion() {
		return sourceVersion == null ? 0L : sourceVersion;
	}
}
