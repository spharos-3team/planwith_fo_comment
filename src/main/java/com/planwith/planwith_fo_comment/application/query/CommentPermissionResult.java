package com.planwith.planwith_fo_comment.application.query;

public record CommentPermissionResult(
		boolean canEdit,
		boolean canDelete
) {
}
