package com.planwith.planwith_fo_comment.application.port.in;

import java.util.List;

import com.planwith.planwith_fo_comment.application.query.CommentThreadResult;
import com.planwith.planwith_fo_comment.application.query.GetCommentsByStoryQuery;

public interface GetCommentsByStoryUseCase {

	List<CommentThreadResult> getByStory(GetCommentsByStoryQuery query);
}
