package com.planwith.planwith_fo_comment.application.command;

import java.util.UUID;

public record MarkStoryDeletedCommand(
		UUID storyUuid,
		Long sourceVersion
) {

	public long incomingVersion() {
		return sourceVersion == null ? 0L : sourceVersion;
	}
}
