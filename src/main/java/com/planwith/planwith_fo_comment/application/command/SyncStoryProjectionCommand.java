package com.planwith.planwith_fo_comment.application.command;

import java.util.UUID;

public record SyncStoryProjectionCommand(
		UUID storyUuid,
		UUID ownerMemberUuid,
		boolean commentEnabled,
		String storyStatus,
		Long sourceVersion,
		EventMetadata eventMetadata
) {

	public SyncStoryProjectionCommand(
			UUID storyUuid,
			UUID ownerMemberUuid,
			boolean commentEnabled,
			String storyStatus,
			Long sourceVersion
	) {
		this(storyUuid, ownerMemberUuid, commentEnabled, storyStatus, sourceVersion, null);
	}

	public long incomingVersion() {
		return sourceVersion == null ? 0L : sourceVersion;
	}
}
