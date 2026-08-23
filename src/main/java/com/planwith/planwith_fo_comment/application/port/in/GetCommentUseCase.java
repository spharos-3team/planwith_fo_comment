package com.planwith.planwith_fo_comment.application.port.in;

import com.planwith.planwith_fo_comment.application.query.CommentQueryResult;
import com.planwith.planwith_fo_comment.application.query.GetCommentQuery;

public interface GetCommentUseCase {

	CommentQueryResult get(GetCommentQuery query);
}
