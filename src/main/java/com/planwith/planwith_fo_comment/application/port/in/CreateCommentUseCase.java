package com.planwith.planwith_fo_comment.application.port.in;

import com.planwith.planwith_fo_comment.application.command.CreateCommentCommand;
import com.planwith.planwith_fo_comment.application.query.CommentQueryResult;

public interface CreateCommentUseCase {

	CommentQueryResult create(CreateCommentCommand command);
}
