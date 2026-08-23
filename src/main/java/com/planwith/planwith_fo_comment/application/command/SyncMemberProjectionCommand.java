package com.planwith.planwith_fo_comment.application.command;

import java.util.UUID;

public record SyncMemberProjectionCommand(
		UUID memberUuid,
		String nickname,
		String profileImage,
		String memberStatus
) {
}
