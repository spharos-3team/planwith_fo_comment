package com.planwith.planwith_fo_comment.application.port.in;

import java.util.List;

import com.planwith.planwith_fo_comment.application.query.CommentQueryResult;
import com.planwith.planwith_fo_comment.application.query.GetCommentsByStoryQuery;

public interface GetCommentsByStoryUseCase {

	List<CommentQueryResult> getByStory(GetCommentsByStoryQuery query);
}
