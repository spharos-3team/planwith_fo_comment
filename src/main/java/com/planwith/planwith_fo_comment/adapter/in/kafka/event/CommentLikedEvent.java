package com.planwith.planwith_fo_comment.adapter.in.kafka.event;

import java.util.UUID;

public record CommentLikedEvent(
		UUID likeUuid,
		UUID commentUuid,
		UUID memberUuid
) {
}
