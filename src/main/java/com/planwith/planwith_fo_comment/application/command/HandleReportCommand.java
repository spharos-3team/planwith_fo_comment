package com.planwith.planwith_fo_comment.application.command;

import java.util.UUID;

public record HandleReportCommand(
		UUID reportUuid,
		UUID commentUuid,
		UUID memberUuid,
		EventMetadata eventMetadata
) {

	public HandleReportCommand(UUID reportUuid, UUID commentUuid, UUID memberUuid) {
		this(reportUuid, commentUuid, memberUuid, null);
	}
}
