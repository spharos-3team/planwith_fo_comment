package com.planwith.planwith_fo_comment.application.query;

import java.util.UUID;

public record CommentMemberResult(
		UUID memberUuid,
		String nickname,
		String profileImage
) {
}
