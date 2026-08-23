package com.planwith.planwith_fo_comment.application.port.in;

import com.planwith.planwith_fo_comment.application.command.MarkStoryDeletedCommand;

public interface MarkStoryDeletedUseCase {

	void markDeleted(MarkStoryDeletedCommand command);
}
