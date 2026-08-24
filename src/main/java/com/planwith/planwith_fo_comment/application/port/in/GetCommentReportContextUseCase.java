package com.planwith.planwith_fo_comment.application.port.in;

import java.util.UUID;

import com.planwith.planwith_fo_comment.application.query.CommentReportContextResult;

public interface GetCommentReportContextUseCase {

	CommentReportContextResult getReportContext(UUID commentUuid);
}
