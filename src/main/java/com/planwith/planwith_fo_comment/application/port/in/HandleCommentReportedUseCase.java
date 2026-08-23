package com.planwith.planwith_fo_comment.application.port.in;

import com.planwith.planwith_fo_comment.application.command.HandleReportCommand;

public interface HandleCommentReportedUseCase {

	void handleReported(HandleReportCommand command);
}
