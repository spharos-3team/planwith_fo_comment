package com.planwith.planwith_fo_comment.application.command;

import java.util.UUID;

public record SyncMemberProjectionCommand(
		UUID memberUuid,
		String nickname,
		String profileImage,
		String memberStatus,
		Long sourceVersion,
		EventMetadata eventMetadata
) {

	public SyncMemberProjectionCommand(
			UUID memberUuid,
			String nickname,
			String profileImage,
			String memberStatus
	) {
		this(memberUuid, nickname, profileImage, memberStatus, null, null);
	}

	public SyncMemberProjectionCommand(
			UUID memberUuid,
			String nickname,
			String profileImage,
			String memberStatus,
			Long sourceVersion
	) {
		this(memberUuid, nickname, profileImage, memberStatus, sourceVersion, null);
	}

	public long incomingVersion() {
		return sourceVersion == null ? 0L : sourceVersion;
	}
}
