package com.planwith.planwith_fo_comment.application.port.in;

import com.planwith.planwith_fo_comment.application.command.DeleteCommentCommand;

public interface DeleteCommentUseCase {

	void delete(DeleteCommentCommand command);
}
