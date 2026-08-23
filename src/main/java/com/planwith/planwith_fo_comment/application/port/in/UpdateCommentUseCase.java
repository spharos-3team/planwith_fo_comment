package com.planwith.planwith_fo_comment.application.port.in;

import com.planwith.planwith_fo_comment.application.command.UpdateCommentCommand;
import com.planwith.planwith_fo_comment.application.query.CommentQueryResult;

public interface UpdateCommentUseCase {

	CommentQueryResult update(UpdateCommentCommand command);
}
