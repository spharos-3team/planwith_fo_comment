package com.planwith.planwith_fo_comment.application.command;

import java.util.UUID;

public record HandleReportCommand(
		UUID reportUuid,
		UUID commentUuid,
		UUID memberUuid
) {
}
