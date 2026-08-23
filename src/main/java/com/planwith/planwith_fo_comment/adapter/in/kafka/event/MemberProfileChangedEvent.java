package com.planwith.planwith_fo_comment.adapter.in.kafka.event;

import java.util.UUID;

public record MemberProfileChangedEvent(
		UUID memberUuid,
		String nickname,
		String profileImage,
		String memberStatus
) {
}
