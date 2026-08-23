package com.planwith.planwith_fo_comment.application.port.in;

import com.planwith.planwith_fo_comment.application.command.SyncStoryProjectionCommand;

public interface SyncStoryProjectionUseCase {

	void sync(SyncStoryProjectionCommand command);
}
