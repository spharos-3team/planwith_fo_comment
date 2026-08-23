package com.planwith.planwith_fo_comment.application.port.in;

import com.planwith.planwith_fo_comment.application.command.HandleLikeCommand;

public interface HandleLikeCreatedUseCase {

	void handleCreated(HandleLikeCommand command);
}
