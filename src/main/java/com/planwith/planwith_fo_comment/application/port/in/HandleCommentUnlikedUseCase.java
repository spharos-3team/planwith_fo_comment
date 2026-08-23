package com.planwith.planwith_fo_comment.application.port.in;

import com.planwith.planwith_fo_comment.application.command.HandleLikeCommand;

public interface HandleCommentUnlikedUseCase {

	void handleUnliked(HandleLikeCommand command);
}
