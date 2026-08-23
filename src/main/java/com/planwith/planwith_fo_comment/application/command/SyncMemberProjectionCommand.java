package com.planwith.planwith_fo_comment.application.command;

import java.util.UUID;

public record SyncMemberProjectionCommand(
		UUID memberUuid,
		String nickname,
		String profileImage,
		String memberStatus,
		Long sourceVersion
) {

	public SyncMemberProjectionCommand(
			UUID memberUuid,
			String nickname,
			String profileImage,
			String memberStatus
	) {
		this(memberUuid, nickname, profileImage, memberStatus, null);
	}

	public long incomingVersion() {
		return sourceVersion == null ? 0L : sourceVersion;
	}
}
