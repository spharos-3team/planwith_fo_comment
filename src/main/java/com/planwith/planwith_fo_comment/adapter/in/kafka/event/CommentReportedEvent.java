package com.planwith.planwith_fo_comment.adapter.in.kafka.event;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CommentReportedEvent(
		UUID reportUuid,
		UUID commentUuid,
		UUID memberUuid,
		UUID eventUuid,
		String eventType,
		UUID targetUuid,
		Instant occurredAt
) {

	public CommentReportedEvent(UUID reportUuid, UUID commentUuid, UUID memberUuid) {
		this(reportUuid, commentUuid, memberUuid, null, null, null, null);
	}
}
