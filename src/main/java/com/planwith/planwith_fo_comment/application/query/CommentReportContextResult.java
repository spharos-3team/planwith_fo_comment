package com.planwith.planwith_fo_comment.application.query;

import java.util.UUID;

public record CommentReportContextResult(
		UUID commentUuid,
		UUID authorMemberUuid,
		boolean reportable
) {
}
