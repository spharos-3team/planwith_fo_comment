package com.planwith.planwith_fo_comment.application.port.in;

import com.planwith.planwith_fo_comment.application.command.SyncMemberProjectionCommand;

public interface SyncMemberProjectionUseCase {

	void sync(SyncMemberProjectionCommand command);
}
