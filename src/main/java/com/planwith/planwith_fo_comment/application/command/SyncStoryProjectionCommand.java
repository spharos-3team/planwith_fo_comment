package com.planwith.planwith_fo_comment.application.command;

import java.util.UUID;

public record SyncStoryProjectionCommand(
		UUID storyUuid,
		UUID ownerMemberUuid,
		boolean commentEnabled,
		String storyStatus,
		Long sourceVersion
) {

	public long incomingVersion() {
		return sourceVersion == null ? 0L : sourceVersion;
	}
}
